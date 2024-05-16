import os
import torch

DEVICE = 'cpu'

# FILE_PATH is now removed because we will handle the uploaded file dynamically
# FILE_PATH = os.environ.get(
#     'file_path',
#     os.path.normpath("test/videos/IMG_9891.mp4")
# )

YOLO_MODEL_PATH = os.environ.get(
    'yolo_model',
    os.path.normpath("object_detection/YOLOS_cars.pt")
)
LPR_MODEL_PATH = os.environ.get(
    'lpr_model',
    os.path.normpath("lpr_net/model/weights/LPRNet__iteration_2000_28.09.pth")
)

YOLO_CONF = 0.5
YOLO_IOU = 0.4
LPR_MAX_LEN = 9
LPR_DROPOUT = 0

FINAL_FRAME_RES = (640, 480)
DETECTION_AREA = [(100, 200), (2920, 500)]
