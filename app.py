from flask import Flask, request, jsonify
import threading
import os
import cv2
from main import main as process_video
from werkzeug.utils import secure_filename
app = Flask(__name__)


@app.route('/upload', methods=['POST'])
def upload_file():
    # Check if a video file is in the POST request
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400
    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        filepath = os.path.join('/path/to/save/uploads', filename)
        file.save(filepath)

        # Process the video in a separate thread
        thread = threading.Thread(target=process_video, args=(filepath,))
        thread.start()

        return jsonify({"message": "File uploaded and processing started"}), 202


def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in {'mp4', 'avi'}


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
