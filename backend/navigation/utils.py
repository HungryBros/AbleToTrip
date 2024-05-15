from django.shortcuts import get_object_or_404
from rest_framework import status
from decouple import config
from PyKakao import Local
import pandas as pd
import requests
import joblib
import time
import re
from pprint import pprint

# Import Models and Serializers
from .models import Convenient
from .serializers import ConvenientSerializer


# Load Trained Model
model_path = "navigation/trained_models/trained_ETA_model.pkl"
trained_ETA_model = joblib.load(model_path)

# API Keys
GOOGLE_MAPS_API_KEY = config("GOOGLE_MAPS_API_KEY")
KAKAO_MAPS_API_KEY = config("KAKAO_MAPS_API_KEY")
TMAP_API_KEY = config("TMAP_API_KEY")


# 현재시간 반환하는 함수
def log_time_func():
    now = time.strftime("[%Y-%m-%d %H:%M:%S]", time.gmtime(time.time() + (9 * 3600)))

    return now


# Google Maps 경로 API 요청 함수
def direction_request_func(origin, destination, mode, transit_mode=None):
    prefix_url = "https://maps.googleapis.com/maps/api/directions/json?"
    origin_parameter_url = (
        f"&origin={origin}"  # 좌표로 넣는 법: origin=41.43206,-81.38992
    )
    destination_parameter_url = f"&destination={destination}"
    mode_parameter_url = f"&mode={mode}"
    transit_mode_parameter_url = (
        f"&transit_mode={transit_mode}"
        if transit_mode != None
        else ""  # 삼항연산자: 값이 없으면 빈 문자열로
    )
    suffix_url = f"&language=ko&key={GOOGLE_MAPS_API_KEY}"

    url = (
        prefix_url
        + origin_parameter_url
        + destination_parameter_url
        + mode_parameter_url
        + transit_mode_parameter_url
        + suffix_url
    )

    response = requests.get(url)

    return response.json()


# Google Maps 경로 API의 response의 steps 추출 함수
def get_steps_func(data):
    routes = data.get("routes")
    legs = routes[0].get("legs")
    steps = legs[0].get("steps")

    # x시간 y분으로 나올 때도 생각해야 함
    duration_text = legs[0].get("duration").get("text")
    if " " in duration_text:
        hour_text, minute_text = duration_text.split()
        duration = int(hour_text.rstrip("시간")) * 60 + int(minute_text.rstrip("분"))

    else:
        duration = int(duration_text.rstrip("분"))

    return steps, duration


# Google Maps 경로 API response의 steps 출발, 도착지 좌표 반환 함수
def get_point_coordinate_func(steps, is_start):
    # is_start가 1이면 start, 0이면 end
    location = "start_location"
    step_idx = 0

    if not is_start:
        step_idx = -1
        location = "end_location"

    point_location = steps[step_idx].get(location)
    lat = round(float(point_location.get("lat")), 6)
    lon = round(float(point_location.get("lng")), 6)
    print(point_location)

    return (lon, lat)


# 엘레베이터 출구번호 반환 함수
def find_exit_func(station_name):
    try:
        station_info = get_object_or_404(Convenient, station_fullname=station_name)

        info_serializer = ConvenientSerializer(station_info)
        info = info_serializer.data.get("elevator_location")

        # 엘리베이터가 없는 경우 => 컷
        if not info.strip() or info[0] != "승":
            return None

        s, l = station_name.split()

        if s[-1] != "역":
            s += "역"

        station_elevator_exit = (
            s + " " + l + "호선 " + re.findall(r"\d+번\s?출구", info)[0]
        )

        print("엘출", station_elevator_exit)

        return station_elevator_exit

    except:
        return None


# Kakao Maps 좌표반환 API 요청 함수(키워드)
def search_keyword_func(keyword):
    if keyword.startswith("대한민국 서울"):
        keyword = keyword.replace("대한민국 서울", "서울", 1)

    api = Local(service_key=KAKAO_MAPS_API_KEY)
    result = api.search_keyword(keyword, dataframe=False)
    result_documents = result.get("documents")

    if len(result_documents):
        first_result = result_documents[0]
        lon = round(float(first_result.get("x")), 6)
        lat = round(float(first_result.get("y")), 6)

        return (lon, lat)

    else:
        print(
            f"{log_time_func()} - Navigation: 카카오 지도에서 {keyword}의 검색 결과 없음"
        )
        return (0, 0)


# Kakao Maps 좌표반환 API 요청 함수(주소)
def coordinate_request_func(keyword):
    if keyword.startswith("대한민국 서울"):
        keyword = keyword.replace("대한민국 서울", "서울", 1)

    api = Local(service_key=KAKAO_MAPS_API_KEY)
    # result = api.search_address(keyword, dataframe=False)
    result = api.search_keyword(keyword, dataframe=False)
    result_documents = result.get("documents")

    if len(result_documents):
        first_result = result_documents[0]
        lon = round(float(first_result.get("x")), 6)
        lat = round(float(first_result.get("y")), 6)

        return (lon, lat)

    else:
        print(
            f"{log_time_func()} - Navigation: {keyword}의 카카오 지도 주소로 검색 실패, 키워드로 검색 START"
        )

        # 주소로 검색에 실패하는 경우, 키워드로 검색하게 함
        return search_keyword_func(keyword)


# T Map 도보 경로 API 요청 함수
def pedestrian_request_func(start_lon, start_lat, end_lon, end_lat):
    route_url = "https://apis.openapi.sk.com/tmap/routes/pedestrian"

    route_params = {
        "version": 1,
        "startX": start_lon,  # 출발지 경도(lon)
        "startY": start_lat,  # 출발지 위도(lat)
        "endX": end_lon,  # 목적지 경도(lon)
        "endY": end_lat,  # 목적지 위도(lat)
        "startName": "%EC%B6%9C%EB%B0%9C",
        "endName": "%EB%8F%84%EC%B0%A9",
    }

    headers = {
        "appKey": TMAP_API_KEY,  # API 키를 헤더에 포함,
        "Content-Type": "application/json",
        "callback": "function",
    }

    route_response = requests.post(route_url, headers=headers, params=route_params)

    if route_response.status_code == 200:
        # print("안될 땐 얘가 안됨")
        route_data = route_response.json()
        # pprint(route_data)
        return route_data
    else:
        print(
            f"{log_time_func()} - Navigation: TMAP에서 받아온 데이터 JSON으로 변경 불가"
        )
        return None


# T Map의 경로 좌표, 상세 안내 정보 얻는 함수
def get_tmap_info_func(data):
    coordinate_list = list()
    description_list = list()
    duration_seconds = 0
    distance_meters = 0

    features = data.get("features")
    features_length = len(features)
    for i, feature in enumerate(features):
        geometry = feature.get("geometry")

        # type: Point/LineString
        if geometry.get("type") == "LineString":
            coordinates = geometry.get("coordinates")  # 1/2차원 좌표 리스트
            properties = feature.get("properties")
            distance_meters += properties.get("distance")
            duration_seconds += properties.get("time")

            if i != 1:
                coordinates = coordinates[1:]

            for coordinate in coordinates:
                lon = round(float(coordinate[0]), 6)
                lat = round(float(coordinate[1]), 6)
                coordinate_list.append(
                    {
                        "longitude": lon,
                        "latitude": lat,
                    }
                )

        else:
            if i == (features_length - 1):
                break
            description = feature.get("properties").get("description").strip()
            description = description.replace(" 을 ", "을(를) ")
            description_list.append(description)

    duration = (((duration_seconds // 60) + 1) // 7 * 4) + 1  # 시속 7km로 계산

    return coordinate_list, description_list, duration, distance_meters


# ETA 계산 함수
def get_additional_ETA_func(
    departure_pedestrian_distance,
    departure_pedestrian_duration,
    arrival_pedestrian_distance,
    arrival_pedestrian_duration,
    subway_stops,
):
    global trained_ETA_model

    eta_model_input_data = {
        "distance_to_station": [departure_pedestrian_distance],
        "distance_from_station": [departure_pedestrian_duration // 7 * 4],
        "duration_to_station": [arrival_pedestrian_distance],  # 하차역 - 도착지 거리(m)
        "duration_from_station": [arrival_pedestrian_duration // 7 * 4],
        "transfer_count": [len(subway_stops) - 1],
    }

    X_ETA_test = pd.DataFrame(eta_model_input_data)
    additional_ETA = int(trained_ETA_model.predict(X_ETA_test))

    return additional_ETA


# Response Value 만들어주는 함수
def navigation_response_func(
    message,
    duration,
    is_subway_exist,
    polyline_info=list(),
    detail_route_info=list(),
):

    response_value = {
        "message": message,
        "duration": duration,
        "is_subway_exist": is_subway_exist,
        "polyline_info": polyline_info,
        "detail_route_info": detail_route_info,
    }

    return response_value
