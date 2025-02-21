import requests
import base64
import numpy as np
import cv2
from ultralytics import YOLO

# 1. API에서 Base64 인코딩된 이미지 가져오기
IMAGE_API_URL = "http://i12a206.p.ssafy.io:8088/api/iot-storage"  # 실제 API URL로 변경

response = requests.get(IMAGE_API_URL)
if response.status_code != 200:
    print("❌ Error: 이미지 데이터를 가져올 수 없습니다.", response.status_code)
    exit()

# 2. Base64 문자열 디코딩
base64_string = response.text.strip()  # 혹시 모를 공백 제거
image_bytes = base64.b64decode(base64_string)

# 3. OpenCV 이미지로 변환
image_np = np.frombuffer(image_bytes, dtype=np.uint8)
img = cv2.imdecode(image_np, cv2.IMREAD_COLOR)

if img is None:
    print("❌ Error: 이미지를 디코딩할 수 없습니다.")
    exit()

print("✅ 이미지 디코딩 성공!")

# 4. YOLO 모델 로드
model = YOLO("yolov8m.pt")  # YOLOv8 모델 불러오기

# 5. 추론 실행
results = model.predict(source=img, conf=0.5, iou=0.4)

# 6. 결과 이미지 저장 (각 객체 검출 후 표시)
for i, result in enumerate(results):
    annotated_image = result.plot()  # 결과를 이미지에 그리기
    output_path = f"output_result_{i}.jpg"  # 저장할 파일 이름 설정
    cv2.imwrite(output_path, annotated_image)  # 이미지 파일 저장
    print(f"✅ 결과 이미지 저장 완료: {output_path}")

