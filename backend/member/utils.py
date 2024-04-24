import requests

def verify_kakao_access_token(access_token):
    # 카카오 API 엔드포인트 및 액세스 토큰
    url = "https://kapi.kakao.com/v1/user/access_token_info"
    headers = {
        "Authorization": f"Bearer {access_token}"
    }

    # 카카오 API 호출
    response = requests.get(url, headers=headers)

    # 응답 확인
    if response.status_code == 200:
        # 액세스 토큰이 유효한 경우
        return True
    else:
        # 액세스 토큰이 유효하지 않은 경우
        return False
