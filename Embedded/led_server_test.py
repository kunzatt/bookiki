import asyncio
import websockets

# 웹소켓 서버 핸들러
async def handle_client(websocket):
    print("클라이언트가 연결되었습니다.")
    try:
        while True:
            # 서버에서 숫자 입력받기
            message = input("클라이언트로 보낼 숫자 입력 (1~15): ").strip()
            
            # 메시지가 유효한 숫자인지 확인
            if message.isdigit() and 1 <= int(message) <= 26:
                await websocket.send(message)  # 클라이언트로 메시지 전송
                print(f"클라이언트로 '{message}' 전송 완료")
            else:
                print("유효하지 않은 입력입니다. 1~15 사이의 숫자를 입력하세요.")
    except websockets.exceptions.ConnectionClosed as e:
        print(f"클라이언트 연결 종료: {e}")
    except Exception as e:
        print(f"예외 발생: {e}")

# 메인 실행
async def main():
    # 웹소켓 서버 시작
    async with websockets.serve(handle_client, "localhost", 8765):
        print("웹소켓 서버가 localhost:8765에서 시작되었습니다.")
        await asyncio.Future()  # 서버를 계속 실행 상태로 유지

if __name__ == "__main__":
    asyncio.run(main())

