import requests
import uuid
import time
import json
import base64
import cv2
import numpy as np

# Base64 인코딩된 이미지 데이터를 가져올 API URL
IMAGE_API_URL = "http://i12a206.p.ssafy.io:8088/api/iot-storage"

# Clova OCR API 설정
CLOVA_OCR_URL = "https://gl1rglva15.apigw.ntruss.com/custom/v1/37978/1c8e0ea95fc2b6b214e0c323d1e5ebce70067e862d0ae604081d378698cf9919/general"
SECRET_KEY = "R0RhR3lpY3JEbklsb2dJWkJtWUNxaFB4a1R1a0RNREo="

def fetch_base64_image():
    """
    API에서 Base64 인코딩된 이미지 문자열을 가져와 디코딩 후 OpenCV 이미지로 변환
    :return: OpenCV 이미지 (numpy 배열), Base64 문자열
    """
    response = requests.get(IMAGE_API_URL)
    if response.status_code != 200:
        print("❌ Error: 이미지 데이터를 가져올 수 없습니다.", response.status_code)
        exit()

    # Base64 문자열 정리
    base64_string = response.text.strip().replace("\n", "").replace("\r", "")

    # Base64 문자열을 디코딩 후 다시 인코딩 (정확한 포맷 유지)
    try:
        image_bytes = base64.b64decode(base64_string)
        base64_string = base64.b64encode(image_bytes).decode("utf-8")  # Clova OCR은 UTF-8 문자열 요구
    except Exception as e:
        print("❌ Base64 디코딩 오류:", e)
        exit()

    # OpenCV 이미지로 변환
    image_np = np.frombuffer(image_bytes, dtype=np.uint8)
    img = cv2.imdecode(image_np, cv2.IMREAD_COLOR)
    
    if img is None:
        print("❌ Error: 이미지를 디코딩할 수 없습니다.")
        exit()

    print("✅ 이미지 디코딩 성공!")
    return img, base64_string

def clova_ocr(base64_string, image_format="jpg"):
    """
    Clova OCR API 호출
    :param base64_string: Base64 인코딩된 이미지 문자열
    :param image_format: 이미지 확장자 (jpg, png 등)
    :return: OCR 결과(JSON)
    """
    request_body = {
        "images": [
            {
                "format": image_format,
                "name": "sample_image",
                "data": base64_string
            }
        ],
        "requestId": str(uuid.uuid4()),
        "version": "V2",
        "timestamp": int(round(time.time() * 1000))
    }

    headers = {
        "Content-Type": "application/json",
        "X-OCR-SECRET": SECRET_KEY
    }

    response = requests.post(CLOVA_OCR_URL, headers=headers, data=json.dumps(request_body))
    if response.status_code == 200:
        return response.json()
    else:
        print("❌ OCR API 오류:", response.status_code, response.text)
        return None

if __name__ == "__main__":
    # Base64 이미지 가져오기
    img, base64_string = fetch_base64_image()

    # OCR 실행
    ocr_result = clova_ocr(base64_string)
    if ocr_result is None:
        exit()

    # OCR 필드 추출 및 처리 (이전 코드 유지 가능)
    try:
        fields = ocr_result["images"][0]["fields"]
    except KeyError:
        print("❌ OCR API에서 텍스트를 감지하지 못함.")
        exit()

    # OCR 결과를 이미지에 표시
    for field in fields:
        text = field["inferText"]
        bbox = field["boundingPoly"]["vertices"]

        x1, y1 = int(bbox[0]["x"]), int(bbox[0]["y"])
        x2, y2 = int(bbox[2]["x"]), int(bbox[2]["y"])

        cv2.rectangle(img, (x1, y1), (x2, y2), (0, 255, 0), 2)

    # 결과 이미지 저장
    cv2.imwrite("result_with_bbox.jpg", img)
    print("✅ 결과 이미지 저장 완료: result_with_bbox.jpg")
