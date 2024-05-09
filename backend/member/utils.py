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
    # response.status_code가 200 OK라면 response.json 리턴, 아니라면 False 리턴
    return False if response.status_code != 200 else response.json()


def is_logged_in(request):
    # 요청 헤더의 인증 토큰
    access_token = request.META.get("HTTP_AUTHORIZATION")
    if access_token is None:
        return False
    # 카카오에 인증 토큰의 유효성 검사 True/False 리턴
    return verify_kakao_access_token(access_token)


def get_user(request):
    # 요청 헤더의 인증 토큰
    access_token = request.META.get("HTTP_AUTHORIZATION")
    if access_token is None:
        return False
    # 카카오에 인증 토큰의 유효성 검사
    if verify_kakao_access_token(access_token):
        user_info = kakao_user_info(access_token)
        # user_info가 False면 False 리턴/아니면 유저 이메일 리턴
        return (
            False if user_info == False else user_info.get("kakao_account").get("email")
        )
    else:
        return False
