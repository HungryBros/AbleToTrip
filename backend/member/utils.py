import requests


def verify_kakao_access_token(access_token):
    # 카카오 API 엔드포인트 및 액세스 토큰
    url = "https://kapi.kakao.com/v1/user/access_token_info"
    headers = {"Authorization": f"{access_token}"}

    # 카카오 API 호출
    response = requests.get(url, headers=headers)
    return response.status_code == 200


def kakao_user_info(access_token):
    # 카카오 API 엑세스 토큰

    # GET 요청을 보낼 URL
    url = "https://kapi.kakao.com/v2/user/me"

    # Authorization 헤더 설정
    headers = {
        "Authorization": access_token,
        "Content-Type": "application/x-www-form-urlencoded;charset=utf-8",
    }

    # GET 요청 보내기
    response = requests.get(url, headers=headers)

    # 응답 확인
    if response.status_code == 200:
        # 요청 성공
        user_info = response.json()
        return user_info
    else:
        # 요청 실패
        return False


def is_logged_in(request):
    # 요청 헤더의 인증 토큰
    access_token = request.META.get("HTTP_AUTHORIZATION")
    if access_token is None:
        return False
    # 카카오에 인증 토큰의 유효성 검사
    logged_in = verify_kakao_access_token(access_token)
    if not logged_in:
        return False
    return True
