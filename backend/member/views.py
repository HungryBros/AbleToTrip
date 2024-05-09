from rest_framework import status
from rest_framework.response import Response
from rest_framework.decorators import api_view
from django.contrib.auth import get_user_model
from .serializers import UserSerializer
from .utils import kakao_user_info, get_user


User = get_user_model()


@api_view(["POST"])
def signin(request):
    if request.method == "POST":
        serializer = UserSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            print("회원가입 완료, 주소 등록이 필요합니다")
            return Response(
                {"message": "회원가입 완료, 주소 등록이 필요합니다"},
                status=status.HTTP_201_CREATED,
            )
        else:
            user_email = get_user(request)
            if user_email:
                try:
                    user = User.objects.get(email=user_email)
                    address = user.address
                    if address:
                        return Response(
                            {"message": "기존 회원, 로그인 성공"},
                            status=status.HTTP_200_OK,
                        )
                    else:
                        return Response(
                            {
                                "message": "기존 회원이지만 주소 입력을 하지 않았습니다. 주소 입력을 해주세요."
                            },
                            status=status.HTTP_202_ACCEPTED,
                        )

                except User.DoesNotExist:
                    return Response(
                        {"error": "정보가 유효하지 않습니다."},
                        status=status.HTTP_400_BAD_REQUEST,
                    )
            else:
                return Response(
                    {"error": "사용자가 존재하지 않습니다."},
                    status=status.HTTP_404_NOT_FOUND,
                )


@api_view(["POST"])
def info(request):
    if request.method == "POST":
        user_email = get_user(request)
        if user_email:
            user = User.objects.get(email=user_email)  # 현재 로그인한 사용자
            user_address = user.address
            address = request.data.get("address")  # 전송된 주소 데이터
            if not user_address:
                if user and address:
                    user.address = address
                    user.save()
                    return Response(
                        {"message": "주소가 성공적으로 저장되었습니다. 로그인 성공"},
                        status=status.HTTP_200_OK,
                    )
                else:
                    return Response(
                        {"error": "사용자 또는 주소 데이터가 올바르지 않습니다."},
                        status=status.HTTP_400_BAD_REQUEST,
                    )
            else:
                return Response(
                    {"message": "이미 주소가 저장된 회원입니다."},
                    status=status.HTTP_208_ALREADY_REPORTED,
                )
        else:
            return Response(
                {"error": "사용자가 존재하지 않습니다."},
                status=status.HTTP_404_NOT_FOUND,
            )
