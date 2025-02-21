import asyncio
import websockets
import board
import neopixel_spi as neopixel
import colorsys

# 설정
LED_COUNT = 26  # LED 개수
SPI_BUS = board.SPI()  # SPI 버스 사용
ORDER = neopixel.GRB  # 색상 순서 (GRB가 일반적)

# SPI 네오픽셀 객체 생성
pixels = neopixel.NeoPixel_SPI(SPI_BUS, LED_COUNT, pixel_order=ORDER, brightness=0.2)  # 밝기 50%

# 무지개 색상 계산 함수
def generate_rainbow_colors(led_count):
    colors = []
    for i in range(led_count):
        hue = i / led_count  # 0부터 1까지의 값을 분할
        rgb = colorsys.hsv_to_rgb(hue, 1.0, 1.0)  # HSV -> RGB 변환
        colors.append((int(rgb[0] * 255), int(rgb[1] * 255), int(rgb[2] * 255)))
    return colors

# 무지개 색상 배열 생성
rainbow_colors = generate_rainbow_colors(LED_COUNT)

# 특정 LED를 무지개 색으로 점등
def light_up_rainbow(index):
    # 모든 LED 끄기
    pixels.fill((0, 0, 0))
    if 0 <= index < LED_COUNT:  # 유효한 인덱스인지 확인
        pixels[index] = rainbow_colors[index]  # 해당 LED에 무지개 색상 적용
    pixels.show()

# 클라이언트 핸들러
async def client_handler():
    uri = "ws://localhost:8765"  # 서버 주소와 포트
    try:
        async with websockets.connect(uri) as websocket:
            print("서버에 연결되었습니다.")

            while True:
                # 서버로부터 메시지 수신
                message = await websocket.recv()
                print(f"서버로부터 받은 메시지: {message}")

                # 메시지가 숫자인지 확인
                if message.isdigit():
                    led_index = int(message) - 1  # 입력값(1~15)을 0부터 시작하는 인덱스로 변환
                    if 0 <= led_index < LED_COUNT:
                        light_up_rainbow(led_index)  # 해당 LED를 무지개 색으로 점등
                        print(f"{led_index + 1}번 LED에 무지개 색상 적용")
                    else:
                        print("유효하지 않은 LED 번호입니다. 1~15 사이로 입력해주세요.")
                else:
                    print("숫자가 아닙니다. 유효한 번호(1~15)를 보내주세요.")
    except websockets.exceptions.ConnectionClosed as e:
        pixels.fill((0, 0, 0))  # 끄기
        pixels.show()
        print(f"서버 연결 종료: {e}")
    except Exception as e:
        pixels.fill((0, 0, 0))  # 끄기
        pixels.show()
        print(f"예외 발생: {e}")

# 메인 실행
if __name__ == "__main__":
    asyncio.run(client_handler())

