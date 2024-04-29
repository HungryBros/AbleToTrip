from django.shortcuts import get_list_or_404
from rest_framework import status
from rest_framework.response import Response
from rest_framework.decorators import api_view
from .models import Attraction
from .serializers import AttractionSerializer
import os
import base64
from math import radians, sin, cos, sqrt, atan2
from heapq import heappop, heappush

category1_map = {
    'exhibition-performance': '전시/공연',
    'leisure-park': '레저/공원',
    'culture-famous': '문화관광/명소',
}

category2_map = {
        'park': '공원',
        'tour': '관광지',
        'leisure': '대형레저시설',
        'sports': '대형체육시설',
        'beauty': '명승지',
        'perform': '영화/연극/공연',
        'exhibit': '전시/기념관',
    }


def calculate_distance(lat1, lon1, lat2, lon2):
    R = 6371.0  # 지구의 반지름 (단위: km)
    lat1_rad = radians(lat1)
    lon1_rad = radians(lon1)
    lat2_rad = radians(lat2)
    lon2_rad = radians(lon2)

    d_lat = lat2_rad - lat1_rad
    d_lon = lon2_rad - lon1_rad

    a = sin(d_lat / 2)**2 + cos(lat1_rad) * cos(lat2_rad) * sin(d_lon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))

    distance = R * c * 1000  # 거리 (단위: m)
    return distance

import time
# Create your views here.
@api_view(["GET"])
def attraction(request):
    user_latitude = float(request.META.get('HTTP_LATITUDE', 0))  # 'HTTP_LATITUDE' 헤더가 없으면 기본값으로 0 설정
    user_longitude = float(request.META.get('HTTP_LONGITUDE', 0)) # 유저 경도
    start_time = time.time()
    
    # 관광지 딕셔너리
    attraction_dict = {}


    # 거리순 20개
    attractions_all = get_list_or_404(Attraction)
    attractions = []
    exhibition_performance = []
    culture_famous = []
    leisure_park = []
    for attraction in attractions_all:
        distance = round(calculate_distance(user_latitude, user_longitude, attraction.latitude, attraction.longitude))
        image_code = attraction.attraction_image
        image = base64.b64encode(image_code).decode('utf-8')
        attraction_data = {
            "attraction_name": attraction.attraction_name,
            "si": attraction.si,
            "gu": attraction.gu,
            "distance": distance,
            "attraction_image": image,
        }
        attraction_name = attraction.attraction_name
        attraction_dict.setdefault(attraction_name, attraction_data)

        # 전시/공연
        if attraction.category1 == category1_map["exhibition-performance"]:
            heappush(exhibition_performance, [distance, attraction_name])

        # 문화관광/명소
        elif attraction.category1 == category1_map["culture-famous"]:
            heappush(culture_famous, [distance, attraction_name])

        # 레저/공원
        elif attraction.category1 == category1_map["leisure-park"]:
            heappush(leisure_park, [distance, attraction_name])
        
        # 전체
        heappush(attractions, [distance, attraction_name])

    # 전체 거리순 20개
    nearby_attractions = []
    for _ in range(20):
        distance, attraction_name = heappop(attractions)
        nearby_attractions.append(attraction_dict.get(attraction_name))

    # 각 테마별 거리순 10개
    nearby_exhibition_performance = []
    nearby_culture_famous = []
    nearby_leisure_park = []
    for _ in range(10):
        if exhibition_performance:
            distance, attraction_name = heappop(exhibition_performance)
            nearby_exhibition_performance.append(attraction_dict.get(attraction_name))

        if culture_famous:
            distance, attraction_name = heappop(culture_famous)
            nearby_culture_famous.append(attraction_dict.get(attraction_name))

        if leisure_park:
            distance, attraction_name = heappop(leisure_park)
            nearby_leisure_park.append(attraction_dict.get(attraction_name))

    data = {
        'attractions': {
            'nearby': nearby_attractions,
            'exhibition-performance': nearby_exhibition_performance,
            'leisure-park': nearby_leisure_park,
            'culture-famous': nearby_culture_famous,
        }
    }
    print(data)
    end_time = time.time()
    print('소요시간', end_time-start_time)
    return Response(data, status=status.HTTP_200_OK)


@api_view(["GET"])
def attraction_by_category(request):
    scroll = request.GET.get('scroll', 0) # 스크롤 횟수

    user_latitude = float(request.META.get('HTTP_LATITUDE', 0))  # 'HTTP_LATITUDE' 헤더가 없으면 기본값으로 0 설정
    user_longitude = float(request.META.get('HTTP_LONGITUDE', 0)) # 유저 경도

    category2 = request.GET.get('category2', '').strip('/') # 카테고리2

    if request.method == 'GET':
        attractions = Attraction.objects.filter(category2=category2_map.get(category2, ''))
        nearby_attractions = []
        for attraction in attractions:
            distance = round(calculate_distance(user_latitude, user_longitude, attraction.latitude, attraction.longitude))
            nearby_attractions.append({
                "attraction_name": attraction.attraction_name,
                "operation_hours": attraction.operation_hours,
                "closed_days": attraction.closed_days,
                "is_entrance_fee": attraction.is_entrance_fee,
                "si": attraction.si,
                "gu": attraction.gu,
                "dong": attraction.dong,
                "attraction_image": attraction.attraction_image,
                'distance': distance,
            })
        nearby_attractions = sorted(nearby_attractions, key=lambda x: x['distance'])[20*scroll:20*(scroll+1)]
        data = {
            'attractions': nearby_attractions,
        }
        return Response(data, status=status.HTTP_200_OK)


@api_view(["GET"])
def attraction_more(request):
    scroll = request.GET.get('scroll', 0) # 스크롤 횟수

    user_latitude = float(request.META.get('HTTP_LATITUDE', 0))  # 'HTTP_LATITUDE' 헤더가 없으면 기본값으로 0 설정
    user_longitude = float(request.META.get('HTTP_LONGITUDE', 0)) # 유저 경도

    category1 = request.GET.get('category1', '').strip('/')

    if request.method == 'GET':
        attractions = Attraction.objects.filter(category1=category1_map.get(category1, ''))
        nearby_attractions = []
        for attraction in attractions:
            distance = round(calculate_distance(user_latitude, user_longitude, attraction.latitude, attraction.longitude))
            nearby_attractions.append({
                "attraction_name": attraction.attraction_name,
                "operation_hours": attraction.operation_hours,
                "closed_days": attraction.closed_days,
                "is_entrance_fee": attraction.is_entrance_fee,
                "si": attraction.si,
                "gu": attraction.gu,
                "dong": attraction.dong,
                "attraction_image": attraction.attraction_image,
                'distance': distance,
            })
        nearby_attractions = sorted(nearby_attractions, key=lambda x: x['distance'])[20*scroll:20*(scroll+1)]
        data = {
            'attractions': nearby_attractions,
        }
        return Response(data, status=status.HTTP_200_OK)


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
