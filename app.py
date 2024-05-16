import os
import shutil
import tempfile
import logging
import re
from typing import List

import cv2
import numpy as np
import torch
import uvicorn
from fastapi import FastAPI, File, UploadFile, Form, HTTPException,Query
from fastapi.responses import JSONResponse

from db import create_connection, insert_user_data
from object_detection.detect_car_YOLO import ObjectDetection
from lpr_net.model.lpr_net import build_lprnet
from lpr_net.rec_plate import rec_plate, CHARS
from track_logic import check_numbers_overlaps, check_roi
import settings

app = FastAPI()

# Set up basic logging
logging.basicConfig(level=logging.INFO)

# Load the YOLO model
detector = ObjectDetection(
    settings.YOLO_MODEL_PATH,
    conf=settings.YOLO_CONF,
    iou=settings.YOLO_IOU,
    device=settings.DEVICE
)

# Load the LPRNet model
LPRnet = build_lprnet(
    lpr_max_len=settings.LPR_MAX_LEN,
    phase=False,
    class_num=len(CHARS),
    dropout_rate=settings.LPR_DROPOUT
)
LPRnet.to(torch.device(settings.DEVICE))
LPRnet.load_state_dict(
    torch.load(settings.LPR_MODEL_PATH, map_location=torch.device(settings.DEVICE))
)


def preprocess(image: np.ndarray, size: tuple) -> np.ndarray:
    image = cv2.resize(image, size, fx=0, fy=0, interpolation=cv2.INTER_CUBIC)
    return image


def get_boxes(results, frame):
    labels, cord = results
    n = len(labels)
    x_shape, y_shape = frame.shape[1], frame.shape[0]

    labls_cords = {'numbers': [], 'cars': [], 'trucks': [], 'buses': []}

    for i in range(n):
        row = cord[i]
        x1, y1, x2, y2 = (
            int(row[0] * x_shape),
            int(row[1] * y_shape),
            int(row[2] * x_shape),
            int(row[3] * y_shape)
        )

        if labels[i] == 0:
            labls_cords['numbers'].append((x1, y1, x2, y2))
        elif labels[i] == 1:
            labls_cords['cars'].append((x1, y1, x2, y2))
        elif labels[i] == 2:
            labls_cords['trucks'].append((x1, y1, x2, y2))
        elif labels[i] == 3:
            labls_cords['buses'].append((x1, y1, x2, y2))

    return labls_cords


def insert_user_data(conn, user_login, user_phone, video, plates):
    sql = ''' INSERT INTO users(login, phone, video, plates)
              VALUES(?,?,?,?) '''
    cur = conn.cursor()
    cur.execute(sql, (user_login, user_phone, video, plates))
    conn.commit()
    return cur.lastrowid


async def process_video(file: UploadFile, user_login: str, user_phone: str) -> str:
    conn = create_connection("user_video.db")

    # Save uploaded video to a temporary file
    with tempfile.NamedTemporaryFile(delete=False, suffix=".mp4") as temp_file:
        shutil.copyfileobj(file.file, temp_file)
        temp_file_path = temp_file.name

    cap = cv2.VideoCapture(temp_file_path)

    plates_detected = set()  # Use a set to avoid duplicates

    while True:
        ret, frame = cap.read()
        if not ret:
            break
        proc_frame = preprocess(frame, settings.FINAL_FRAME_RES)
        results = detector.score_frame(proc_frame)
        labls_cords = get_boxes(results, frame)
        new_cars = check_numbers_overlaps(labls_cords)

        for car in new_cars:
            plate_coords = car[0]
            if check_roi(plate_coords):
                plate_box_image = frame[plate_coords[1]:plate_coords[3], plate_coords[0]:plate_coords[2]]
                plate_text = rec_plate(LPRnet, plate_box_image)
                if re.match("[A-Z]{1}[0-9]{3}[A-Z]{2}[0-9]{2,3}", plate_text):
                    plates_detected.add(plate_text)
                    logging.info(f"Detected License Plate: {plate_text}")
                    # We break after the first detection to return only the first plate
                    break

        if plates_detected:  # If we have detected any plates, break after the first one
            break

    cap.release()
    conn.close()

    # Clean up the temporary file
    os.unlink(temp_file_path)

    # Return the first detected plate or an empty string if none were detected
    return next(iter(plates_detected), "")


@app.get("/")
async def root():
    return {"message": "Hello, this is your license plate detection API!"}

def get_user_history(conn, user_login):
    sql = ''' SELECT * FROM users WHERE login=? '''
    cur = conn.cursor()
    cur.execute(sql, (user_login,))
    rows = cur.fetchall()
    return rows


@app.post("/upload-video/")
async def upload_video(file: UploadFile, user_login: str = Form(...), user_phone: str = Form(...)):
    try:
        if not file.filename.lower().endswith(('.mp4', '.avi', '.mov')):
            raise HTTPException(status_code=400, detail="Unsupported file format")

        first_plate = await process_video(file, user_login, user_phone)
        return JSONResponse(content={"filename": file.filename, "first_plate": first_plate}, status_code=200)
    except Exception as e:
        logging.error(f"An error occurred: {str(e)}")
        return JSONResponse(content={"error": str(e)}, status_code=500)

@app.get("/user-history/")
async def user_history(user_login: str = Query(...)):
    try:
        conn = create_connection("user_video.db")
        history = get_user_history(conn, user_login)
        conn.close()
        return JSONResponse(content={"history": history}, status_code=200)
    except Exception as e:
        logging.error(f"An error occurred: {str(e)}")
        return JSONResponse(content={"error": str(e)}, status_code=500)




if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
