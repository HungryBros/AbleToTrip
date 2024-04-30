from django.shortcuts import get_object_or_404, get_list_or_404
from rest_framework import status
from rest_framework.response import Response
from rest_framework.decorators import api_view
from .models import Attraction
from heapq import heappop, heappush
from .utils import category1_map, category2_map, calculate_distance, get_image_url


# Create your views here.
@api_view(["GET"])
def attraction(request):
    user_latitude = float(request.META.get("HTTP_LATITUDE", 0))  # "HTTP_LATITUDE" 헤더가 없으면 기본값으로 0 설정
    user_longitude = float(request.META.get("HTTP_LONGITUDE", 0)) # 유저 경도
    
    # 관광지 딕셔너리
    attraction_dict = {}


    # 거리순 20개
    attractions_all = get_list_or_404(Attraction)
    attractions = []
    exhibition_performance = []
    culture_famous = []
    leisure_park = []
    for attraction in attractions_all:
        distance = calculate_distance(user_latitude, user_longitude, attraction.latitude, attraction.longitude)
        attraction_data = {
            "attraction_name": attraction.attraction_name,
            "si": attraction.si,
            "gu": attraction.gu,
            "distance": distance,
            "image_url": get_image_url(attraction.attraction_name),
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
        if attractions:
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
        "attractions": {
            "nearby": nearby_attractions,
            "exhibition-performance": nearby_exhibition_performance,
            "leisure-park": nearby_leisure_park,
            "culture-famous": nearby_culture_famous,
        }
    }
    return Response(data, status=status.HTTP_200_OK)


@api_view(["GET"])
def attraction_by_category(request):
    scroll = int(request.GET.get("scroll", "0").rstrip("/")) # 스크롤 횟수

    user_latitude = float(request.META.get("HTTP_LATITUDE", 0))  # "HTTP_LATITUDE" 헤더가 없으면 기본값으로 0 설정
    user_longitude = float(request.META.get("HTTP_LONGITUDE", 0)) # 유저 경도

    categories = request.GET.get("category2", "").rstrip("/").split("-") # 카테고리2를 케밥으로 묶기 대문에 split("-")한 리스트로 카테고리2 추출
    category_list = []

    for category in categories:
        category_list.append(category2_map.get(category, "")) # 카테고리2 mapping시켜서 리스트에 담기

    if request.method == "GET":
        attractions = get_list_or_404(Attraction, category2__in=category_list)
        nearby_attractions = []
        for attraction in attractions:
            distance = calculate_distance(user_latitude, user_longitude, attraction.latitude, attraction.longitude)
            nearby_attractions.append({
                "attraction_name": attraction.attraction_name,
                "operation_hours": attraction.operation_hours,
                "closed_days": attraction.closed_days,
                "is_entrance_fee": attraction.is_entrance_fee,
                "si": attraction.si,
                "gu": attraction.gu,
                "dong": attraction.dong,
                "image_url": get_image_url(attraction.attraction_name),
                "distance": distance,
            })
        nearby_attractions = sorted(nearby_attractions, key=lambda x: x["distance"])[20*scroll:20*(scroll+1)]
        data = {
            "attractions": nearby_attractions,
        }
        return Response(data, status=status.HTTP_200_OK)


@api_view(["GET"])
def attraction_more(request):
    scroll = int(request.GET.get("scroll", "0").rstrip("/")) # 스크롤 횟수

    user_latitude = float(request.META.get("HTTP_LATITUDE", 0))  # "HTTP_LATITUDE" 헤더가 없으면 기본값으로 0 설정
    user_longitude = float(request.META.get("HTTP_LONGITUDE", 0)) # 유저 경도

    category1 = request.GET.get("category1", "").rstrip("/")

    if request.method == "GET":
        attractions = get_list_or_404(Attraction, category1=category1_map.get(category1, ""))
        nearby_attractions = []
        for attraction in attractions:
            distance = calculate_distance(user_latitude, user_longitude, attraction.latitude, attraction.longitude)
            nearby_attractions.append({
                "attraction_name": attraction.attraction_name,
                "operation_hours": attraction.operation_hours,
                "closed_days": attraction.closed_days,
                "is_entrance_fee": attraction.is_entrance_fee,
                "si": attraction.si,
                "gu": attraction.gu,
                "dong": attraction.dong,
                "image_url": get_image_url(attraction.attraction_name),
                "distance": distance,
            })
        nearby_attractions = sorted(nearby_attractions, key=lambda x: x["distance"])[20*scroll:20*(scroll+1)]
        data = {
            "attractions": nearby_attractions,
        }
        return Response(data, status=status.HTTP_200_OK)


@api_view(["GET"])
def attraction_search(request):
    return Response({"response": "attraction_search"})

@api_view(["GET"])
def attraction_detail(request, id):
    attraction = get_object_or_404(Attraction, pk=id)
    serializer = AttractionSerializer(attraction)
    return Response(serializer.data)