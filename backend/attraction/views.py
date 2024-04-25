from django.shortcuts import render
from rest_framework import status
from rest_framework.response import Response
from rest_framework.decorators import api_view
from .models import Attraction
from .serializers import AttractionSerializer
import os


# Create your views here.
@api_view(["GET"])
def attraction(request):
    if request.method == 'GET':
        attractions = Attraction.objects.all()
        serializer = AttractionSerializer(attractions, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(["GET"])
def attraction_specific(request, category):
    if request.method == 'GET':
        # attractions = Attraction.objects.filter(attraction=request.
        serializer = AttractionSerializer(attractions, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


@api_view(["GET"])
def attraction_more(request, category1):
    return Response({"response": f"{category1}"})


@api_view(["GET"])
def attraction_search(request):
    return Response({"response": "attraction_search"})


@api_view(["PUT"])
def update_attraction_images(request):
    # 모든 Attraction 데이터 불러오기
    attractions = Attraction.objects.all()

    # 각 Attraction 레코드마다 이미지 데이터 업데이트
    for attraction in attractions:
        # 이미지 경로
        image_path = f"attraction/images/{attraction.attraction_name}.png"

        # 이미지 파일 존재 여부 확인
        if os.path.exists(image_path):
            try:
                # 이미지 로드 및 데이터 업데이트
                with open(image_path, "rb") as image_file:
                    image_data = image_file.read()
                    attraction.attraction_image = image_data
                    attraction.save()
                    print(f"{attraction.attraction_name} 이미지 업로드 성공")

            except Exception as e:
                print(f"{attraction.attraction_name} 이미지 업로드 실패: {e}")

        else:
            print(f"{attraction.attraction_name} 이미지 파일 없음")

    return Response({"message": "작업 완료"})
