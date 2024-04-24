from django.shortcuts import render
from django.contrib.auth import login
from rest_framework import status
from rest_framework.response import Response
from rest_framework.decorators import api_view
from django.contrib.auth import get_user_model
from .serializers import UserSerializer
from .utils import verify_kakao_access_token


User = get_user_model()


@api_view(['POST'])
def signup(request):
    if request.method == 'POST':
        serializer = UserSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


@api_view(['POST'])
def post_address(request):
    if request.method == 'POST':
        user = request.user  # 현재 로그인한 사용자
        address = request.data.get('address')  # 전송된 주소 데이터
        if user and address:
            user.address = address
            user.save()
            return Response({'message': '주소가 성공적으로 저장되었습니다.'}, status=status.HTTP_200_OK)
        else:
            return Response({'error': '사용자 또는 주소 데이터가 올바르지 않습니다.'}, status=status.HTTP_400_BAD_REQUEST)
        

@api_view(['POST'])
def signin(request):
    if request.method == 'POST':
        email = request.data.get('email')
        access_token = request.data.get('access_token')
        
        # 프론트에서 받은 이메일을 기반으로 사용자를 가져옵니다.
        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            return Response({'error': '해당 이메일을 가진 사용자가 없습니다.'}, status=status.HTTP_404_NOT_FOUND)
        
        # 사용자의 access_token을 확인하여 인증합니다.
        if verify_kakao_access_token(access_token):
            # 인증에 성공하면 Django의 login 함수를 사용하여 사용자를 로그인시킵니다.
            login(request, user)
            return Response({'message': '로그인 성공'}, status=status.HTTP_200_OK)
        else:
            return Response({'error': '액세스 토큰이 올바르지 않습니다.'}, status=status.HTTP_401_UNAUTHORIZED)