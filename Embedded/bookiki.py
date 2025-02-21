# -*- coding: utf-8 -*-
import time
import requests
import json
import base64
import uuid
import numpy as np
import cv2
from ultralytics import YOLO
from collections import defaultdict
from pyzbar.pyzbar import decode

# API URL 설정
IMAGE_API_URL = "http://i12a206.p.ssafy.io:8088/api/iot-storage"
POST_API_URL = "http://i12a206.p.ssafy.io:8088/api/books/return/scan"  # 실제 API URL로 변경

# YOLO 모델 로드
model = YOLO("yolov8m.pt")  # 로컬에 yolov8m.pt가 있다고 가정

while True:
    try:
        print("\n[Step] 새로운 이미지 가져오기...")
        response = requests.get(IMAGE_API_URL)
        if response.status_code != 200:
            print("Error: 이미지 데이터를 가져올 수 없습니다.", response.status_code)
            time.sleep(10)
            continue

        # JSON 응답에서 base64 인코딩된 이미지 추출
        base64_string = response.text.strip()
        if not base64_string:
            print("Error: Base64 이미지 데이터가 없습니다.")
            time.sleep(10)
            continue

        # Base64 디코딩
        try:
            image_bytes = base64.b64decode(base64_string)
        except Exception as e:
            print(f"Base64 디코딩 에러: {e}")
            time.sleep(10)
            continue

        # OpenCV 이미지 변환
        image_np = np.frombuffer(image_bytes, dtype=np.uint8)
        img = cv2.imdecode(image_np, cv2.IMREAD_COLOR)

        if img is None:
            print("Error: 이미지를 디코딩할 수 없습니다.")
            time.sleep(10)
            continue

        print("이미지 디코딩 성공!")

        ################################
        # 1) YOLO로 책 등 검출
        ################################
        print("[Step] YOLO를 사용하여 책 등 검출 중...")
        results = model.predict(source=img, conf=0.5, iou=0.4)
        yolo_result = results[0]

        det_boxes = []
        names = model.names

        # 책 등(bounding box)만 추출
        for box, conf, cls_id in zip(
            yolo_result.boxes.xyxy,
            yolo_result.boxes.conf,
            yolo_result.boxes.cls
        ):
            label_name = names[int(cls_id)]
            if label_name == "book":
                x1, y1, x2, y2 = box.tolist()
                det_boxes.append({
                    "id": len(det_boxes),
                    "bbox": (x1, y1, x2, y2),
                    "conf": float(conf)
                })

        print(f"  → 검출된 책 등 수: {len(det_boxes)}")

        ################################
        # 2) Clova OCR 호출
        ################################
        print("[Step] Clova OCR을 사용하여 텍스트 추출 중...")
        CLOVA_OCR_URL = "https://gl1rglva15.apigw.ntruss.com/custom/v1/37978/1c8e0ea95fc2b6b214e0c323d1e5ebce70067e862d0ae604081d378698cf9919/general"
        SECRET_KEY = "R0RhR3lpY3JEbklsb2dJWkJtWUNxaFB4a1R1a0RNREo="

        def clova_ocr(image_bytes):
            encoded_image = base64.b64encode(image_bytes).decode('utf-8')

            request_body = {
                "images": [
                    {
                        "format": "jpg",
                        "name": "sample_image",
                        "data": encoded_image
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
            resp = requests.post(CLOVA_OCR_URL, headers=headers, data=json.dumps(request_body))
            if resp.status_code == 200:
                return resp.json()
            else:
                print("Clova OCR API 호출 실패:", resp.status_code, resp.text)
                return None

        ocr_result = clova_ocr(image_bytes)
        if ocr_result is None:
            time.sleep(10)
            continue

        # OCR 결과에서 (텍스트, 바운딩 박스) 추출
        ocr_boxes = []
        if "images" in ocr_result:
            for image_info in ocr_result["images"]:
                fields = image_info.get("fields", [])
                for f in fields:
                    text = f["inferText"]
                    vertices = f["boundingPoly"]["vertices"]
                    xs = [v["x"] for v in vertices]
                    ys = [v["y"] for v in vertices]
                    x1, y1 = min(xs), min(ys)
                    x2, y2 = max(xs), max(ys)
                    ocr_boxes.append({
                        "text": text,
                        "bbox": (x1, y1, x2, y2)
                    })

        print(f"  → OCR 텍스트 박스 수: {len(ocr_boxes)}")

        ################################
        # 3) 책 등 범위 안에 속하는 OCR 텍스트를 책별로 묶기
        ################################
        spine_ocr_map = defaultdict(list)  # { spine_id: [ text1, text2, ... ], ... }

        for ocr_obj in ocr_boxes:
            ox1, oy1, ox2, oy2 = ocr_obj["bbox"]
            text_str = ocr_obj["text"]

            for spine in det_boxes:
                sx1, sy1, sx2, sy2 = spine["bbox"]
                # OCR 텍스트가 책 등 박스 안에 완전히 포함되면 매핑
                if ox1 >= sx1 and oy1 >= sy1 and ox2 <= sx2 and oy2 <= sy2:
                    spine_id = spine["id"]
                    spine_ocr_map[spine_id].append(text_str)

        ################################
        # 4) QR 코드 디코딩 및 x좌표 정렬
        ################################
        print("[Step] QR 코드 디코딩 중...")
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        decoded_qrs = decode(gray)

        # 책 등별 QR 정보 (원한다면 spine별로 연결할 수도 있음)
        spine_qr_map = defaultdict(list)

        for qr_obj in decoded_qrs:
            rect = qr_obj.rect
            qx1, qy1 = rect.left, rect.top
            qx2, qy2 = rect.left + rect.width, rect.top + rect.height
            qr_data = qr_obj.data.decode('utf-8', errors='replace')

            # URL에서 숫자 ID 추출 (예: http://.../books/21 -> "21")
            qr_id = qr_data.split("/")[-1] if "/" in qr_data else qr_data

            # 책 등 바운딩 박스 내부인지 판별
            for spine in det_boxes:
                sx1, sy1, sx2, sy2 = spine["bbox"]
                if (qx1 >= sx1 and qy1 >= sy1 and qx2 <= sx2 and qy2 <= sy2):
                    spine_qr_map[spine["id"]].append({
                        "qr_id": qr_id,
                        "bbox": (qx1, qy1, qx2, qy2)
                    })

        # QR 코드 ID를 x 좌표 기준으로 정렬
        all_qrs = sorted(
            [item for qr_list in spine_qr_map.values() for item in qr_list],
            key=lambda item: item["bbox"][0]  # x 좌표 기준 정렬
        )

        # 그룹 수 설정 (최대 3개)
        num_groups = 3
        shelfBookItemsMap = {str(i + 1): [] for i in range(num_groups)}
        shelfBookItemsMap['0'] = [int(0)]

        # 균등하게 나누기 (순서대로 그룹에 배분)
        total_qrs = len(all_qrs)
        chunk_sizes = [total_qrs // num_groups] * num_groups  # 기본 크기 설정
        for i in range(total_qrs % num_groups):  # 나머지 개수를 앞에서부터 분배
            chunk_sizes[i] += 1

        # QR을 그룹별로 나누기
        start_idx = 0
        for group_id, chunk_size in enumerate(chunk_sizes, start=1):
            shelfBookItemsMap[str(group_id)] = [
                int(qr["qr_id"]) for qr in all_qrs[start_idx:start_idx + chunk_size]
            ]
            start_idx += chunk_size  # 다음 그룹 시작 위치 업데이트

        ################################
        # (디버그) 결과 로그 출력
        ################################
        # 책 등별 OCR 결과를 리스트로 저장
        spine_ocr_list = []
        for spine_id in sorted(spine_ocr_map.keys()):  # spine ID 기준으로 정렬
            texts = spine_ocr_map[spine_id]
            combined_text = " ".join(texts)  # OCR 결과를 공백으로 묶어서 저장
            spine_ocr_list.append(combined_text)  # 한 권의 OCR 결과를 하나의 문자열로 저장
        
        # 디버깅용 출력
        print("\n===== [책 등별 OCR 결과] =====")
        for idx, ocr_text in enumerate(spine_ocr_list, start=1):
            print(f"  - 책 {idx}: {ocr_text}")

        print("\n===== [QR 코드 x좌표 정렬 결과] =====")
        for idx, qr_item in enumerate(all_qrs, start=1):
            print(f"  {idx}. QR ID={qr_item['qr_id']} | bbox={qr_item['bbox']}")

        print("\n===== [shelfBookItemsMap] =====")
        print(" ", shelfBookItemsMap)

        ################################
        # 6) 최종 JSON 생성 및 서버 전송
        ################################
        print("\n[Step] 데이터 전송 중...")

        # 최종 JSON 데이터 구성
        post_data = {
            "ocrResults": spine_ocr_list,  # OCR 결과를 string 배열로 변경
            "shelfBookItemsMap": shelfBookItemsMap  # 3개의  그룹화된 QR 코드 ID
        }

        headers = {"Content-Type": "application/json"}
        post_resp = requests.post(POST_API_URL, headers=headers, data=json.dumps(post_data))

        if post_resp.status_code == 200:
            print("데이터 전송 성공")
        else:
            print("데이터 전송 실패")

    except Exception as e:
        print("예외 발생:", str(e))

    print("10초 후 다시 실행...")
    time.sleep(10)
