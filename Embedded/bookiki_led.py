import time
import asyncio
import json
import websockets
import board
import neopixel_spi as neopixel

# 설정
LED_COUNT = 26  # LED 개수
SPI_BUS = board.SPI()  # SPI 버스 사용
#SPI_BUS = board.SPI(clock=1_000_000)  # 1MHz (기본값보다 낮게 설정)

ORDER = neopixel.GRB  # 색상 순서 (GRB가 일반적)

# SPI 네오픽셀 객체 생성
pixels = neopixel.NeoPixel_SPI(SPI_BUS, LED_COUNT, pixel_order=ORDER, brightness=0.2)  # 밝기 50%


def light_up_led_range(start_index, end_index, color):
    """지정된 범위의 LED를 특정 색상으로 변경 후 10초 뒤에 끄기"""
    if start_index < 0 or end_index >= LED_COUNT:
        print(f"LED 인덱스 범위 오류: {start_index} ~ {end_index} (총 {LED_COUNT}개)")
        return

    pixels.fill((0, 0, 0))  # 모든 LED 끄기
    for i in range(start_index, end_index + 1):
        pixels[i] = color
    pixels.show()
    print(f"LED {start_index+1} ~ {end_index+1} 색상 변경됨: {color}")
    
    # 10초 후 LED 끄기
    time.sleep(10)
    turn_off_leds()

# 모든 LED 끄기 함수 (잔류 전류 방전 추가)
def turn_off_leds():
    pixels.fill((0, 0, 0))  # 모든 LED 끄기
    pixels.show()
    print("모든 LED가 꺼졌습니다.")
    time.sleep(0.5)  # LED 전류 방전 시간 추가

# 클라이언트 핸들러
async def client_handler():
    uri = "ws://i12a206.p.ssafy.io:8088/iot/ws"  # 서버 주소와 포트

    while True:
        try:
            async with websockets.connect(uri) as websocket:
                print("서버에 연결되었습니다.")

                while True:
                    try:
                        message = await websocket.recv()
                        print(f"서버로부터 받은 메시지: {message}")

                        # JSON 파싱
                        data = json.loads(message)

                        if data.get("type") == "CONNECT_SUCCESS":
                            print("WebSocket 연결 성공")
                            continue
                        
                        if "shelf" in data and "id" in data["shelf"]:
                            shelf_id = data["shelf"]["id"]
                            
                            if shelf_id == 1:
                                light_up_led_range(0, 8, (255, 0, 0))  # 1~15번 LED 빨간색
                            elif shelf_id == 2:
                                light_up_led_range(9, 16, (0, 255, 0))  # 16~30번 LED 초록색
                            elif shelf_id == 3:
                                light_up_led_range(17, 25, (0, 0, 255))  # 31~45번 LED 파란색
                            else:
                                print("유효하지 않은 shelf_id입니다. 1~3 사이로 입력해주세요.")

                        else:
                            print("유효한 JSON 데이터가 아닙니다.")

                    except json.JSONDecodeError:
                        print("JSON 파싱 오류")
                    except websockets.exceptions.ConnectionClosed:
                        print("서버 연결이 닫혔습니다. 재연결 중...")
                        break  # 연결이 끊어지면 루프 탈출 후 재연결

        except (websockets.exceptions.ConnectionClosed, OSError) as e:
            print(f"서버 연결 실패: {e}. 3초 후 재연결 중...")
            await asyncio.sleep(3)  # 재연결 전 대기
        except KeyboardInterrupt:
            print("\n[Ctrl+C 감지] 프로그램을 종료합니다.")
            break
        except Exception as e:
            print(f"예외 발생: {e}")
        finally:
            turn_off_leds()  # 종료 시 LED 끄기

# 메인 실행
if __name__ == "__main__":
    try:
        turn_off_leds()  # 프로그램 시작 시 잔류 전류 방전
        asyncio.run(client_handler())
    except KeyboardInterrupt:
        print("\n[Ctrl+C 감지] 프로그램 종료 중...")
        turn_off_leds()

