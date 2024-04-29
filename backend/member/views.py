from django.shortcuts import render
from django.contrib.auth import login, authenticate
from rest_framework import status
from rest_framework.response import Response
from rest_framework.decorators import api_view
from django.contrib.auth import get_user_model
from .serializers import UserSerializer
from .utils import kakao_user_info


User = get_user_model()


@api_view(['POST'])
def signin(request):
    if request.method == 'POST':
        serializer = UserSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        else:
            try:
                email = request.data.get('email')
                user = User.objects.get(email=email)
                login(request, user)
                return Response({'message': '로그인 성공'}, status=status.HTTP_200_OK)
            except User.DoesNotExist:
                return Response({'error': '해당 이메일을 가진 사용자가 없습니다.'}, status=status.HTTP_404_NOT_FOUND)


@api_view(['POST'])
def info(request):
    if request.method == 'POST':
        access_token = request.META.get('HTTP_AUTHORIZATION')
        user_info = kakao_user_info(access_token)
        if user_info:
            email = user_info.kakao_account.email
            user = User.objects.get(email=email)  # 현재 로그인한 사용자
            address = request.data.get('address')  # 전송된 주소 데이터
            if user and address:
                user.address = address
                user.save()
                return Response({'message': '주소가 성공적으로 저장되었습니다.'}, status=status.HTTP_200_OK)
            else:
                return Response({'error': '사용자 또는 주소 데이터가 올바르지 않습니다.'}, status=status.HTTP_400_BAD_REQUEST)
        else:
            return Response({'error': '사용자가 카카오 서버에 존재하지 않습니다.'}, status=status.HTTP_404_NOT_FOUND)