from django.shortcuts import render, get_object_or_404
from rest_framework.decorators import api_view
from rest_framework.response import Response
from decouple import config
from PyKakao import Local
import requests
import re

# Import Models and Serializers
from .models import Station, Convenient, Ramp, Lift, Restroom
from .serializers import ConvenientSerializer

GOOGLE_MAPS_API_KEY = config("GOOGLE_MAPS_API_KEY")
KAKAO_MAPS_API_KEY = config("KAKAO_MAPS_API_KEY")
TMAP_API_KEY = config("TMAP_API_KEY")


# 구글 경로 API 요청하는 함수
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


# 카카오 지도 API 요청하는 함수
def coordinate_request_func(keyword):
    api = Local(service_key=KAKAO_MAPS_API_KEY)
    result = api.search_keyword(keyword, dataframe=False)
    first_result = result.get("documents")[0]
    lon = round(float(first_result.get("x")), 6)
    lat = round(float(first_result.get("y")), 6)

    return (lon, lat)


# 티맵 도보 경로 API 요청 함수
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
        route_data = route_response.json()
        print("TMAP ROUTE RESPONSE SUCCESS")
        return route_data
    else:
        print(f"TMAP Route Search Error: {route_response.status_code}")
        return None


# Response Value 만들어주는 함수
def navigation_response_func(
    is_bus_exist,
    is_subway_exist,
    polyline_info=list(),
    detail_route_info=list(),
):

    response_value = {
        "is_bus_exist": is_bus_exist,
        "is_subway_exist": is_subway_exist,
        "polyline_info": polyline_info,
        "detail_route_info": detail_route_info,
    }

    return response_value


# 구글 지도 API의 response에서 steps를 추출하는 함수
def get_steps_func(data):
    routes = data.get("routes")
    legs = routes[0].get("legs")
    steps = legs[0].get("steps")

    return steps


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

    return (lon, lat)


# 엘레베이터가 있는 출구를 찾는 함수
def find_exit_func(station_name):
    try:
        station_info = get_object_or_404(Convenient, station_fullname=station_name)
        info_serializer = ConvenientSerializer(station_info)
        info = info_serializer.data.get("elevator_location")

        # 엘리베이터가 없는 경우 => 컷
        if info[0] != "승":
            return None

        station_elevator_exit = re.findall(r"\d+번\s?출구", info)
        station_elevator_exit = station_name + "호선 " + station_elevator_exit[0]

        return station_elevator_exit

    except:
        return None


# 티맵의 경로 좌표들 얻는 함수
def get_tmap_coordinate(data):
    coordinate_list = list()

    features = data.get("features")
    for feature in features:
        geometry = feature.get("geometry")

        coordinates = geometry.get("coordinates")  # 1/2차원 좌표 리스트
        geometry_type = geometry.get("type")  # Point/LineString
        if geometry_type == "Point":
            lon = round(float(coordinates[0]), 6)
            lat = round(float(coordinates[1]), 6)
            coordinate_list.append((lon, lat))
        else:
            for coordinate in coordinates:
                lon = round(float(coordinate[0]), 6)
                lat = round(float(coordinate[1]), 6)
                coordinate_list.append((lon, lat))

    return coordinate_list


@api_view(["POST"])
def navigation(request):
    origin = request.data.get("departure")
    destination = request.data.get("arrival")

    # Parameters
    mode = "transit"  # Options : walking, driving, bicycling, transit
    transit_mode = "subway"

    response_json = direction_request_func(origin, destination, mode, transit_mode)

    # Routes & Steps 데이터
    steps = get_steps_func(response_json)

    # Step, travel_mode 리스트 Init
    step_list = list()
    step_travel_mode_list = list()

    # Flags
    is_bus_exist = False  # "BUS" 유무 확인용
    is_subway_exist = False  # "SUBWAY" 유무 확인용

    # 각 Step 확인
    for step in steps:
        travel_mode = step.get("travel_mode")

        # travel_mode가 "TRANSIT"인 경우에는
        # 각 step 안에 "transit_details"이 있음
        # 그 안에 "line"안에 "vehicle"안에 "type"을 보면 "BUS"인지 "SUBWAY"인지 알 수 있음

        if travel_mode == "TRANSIT":
            transit_type = (
                step.get("transit_details").get("line").get("vehicle").get("type")
            )

            # 경로 상에 지하철이 포함된 경우, 표시
            if transit_type == "SUBWAY":
                is_subway_exist = True

            # 경로 상에 버스가 포함된 경우, 종료
            elif transit_type == "BUS":
                is_bus_exist = True
                response_value = navigation_response_func(is_bus_exist, is_subway_exist)

                return Response(response_value)

        # "travel_mode"가 버스가 아닌 경우
        step_list.append(step)  # step을 각각의 원소로 저장
        step_travel_mode_list.append(travel_mode)  # 각 스텝의 travel_mode를 저장

    # Response_value Init
    polyline_info = list()
    detail_route_info = list()

    # 도보만 이용한 경우
    if not is_subway_exist:

        """
        도보 경로 안내하는 코드
        작성 예정
        """

        response_value = navigation_response_func(
            is_bus_exist,
            is_subway_exist,
            polyline_info,
            detail_route_info,
        )

        return Response(response_value)

    # 경로에 지하철과 도보가 섞인 경우
    else:
        step_length = len(step_travel_mode_list)

        # 1) step_list의 길이가 3보다 크면 환승 구간 존재
        # 2) step_list의 [0], [-1]번째 인덱스는 도보
        #    그 사이는 홀수 인덱스는 지하철, 짝수 인덱스는 환승 구간

        # 지하철 구간의 polyline을 얻기 위해
        # 지하철 구간만 추출
        subway_polyline_list = list()

        for subway_idx in range(1, step_length, 2):
            subway_polyline_list.append(
                step_list[subway_idx]
                .get("polyline")
                .get("points")  # 각 지하철 구간의 polyline
            )

        # 출발역, 호선 번호(n호선)
        departure_station_name = (
            step_list[1].get("transit_details").get("departure_stop").get("name")
        )
        departure_line_number = (
            step_list[1].get("transit_details").get("line").get("short_name")
        ).rstrip("호선")

        # 도착역, 호선 번호(n호선)
        arrival_station_name = (
            step_list[-2].get("transit_details").get("arrival_stop").get("name")
        )
        arrival_line_number = (
            step_list[-2].get("transit_details").get("line").get("short_name")
        ).rstrip("호선")

        departure_station_fullname = (
            departure_station_name + " " + departure_line_number
        )
        arrival_station_fullname = arrival_station_name + " " + arrival_line_number

        departure_station_elevator_exit = find_exit_func(departure_station_fullname)
        arrival_station_elevator_exit = find_exit_func(arrival_station_fullname)

        # print(departure_station_elevator_exit)  # "역삼 2호선 4번출구"
        # print(arrival_station_elevator_exit)  # "경복궁 3호선 4번 출구"

        # 두 출구 하나라도 엘리베이터가 없는 경우 => 컷
        if not (departure_station_elevator_exit and arrival_station_elevator_exit):
            response_value = navigation_response_func(is_bus_exist, is_subway_exist)

            return Response(response_value)

        # 두 엘리베이터 출구의 좌표값(tuple: lon, lat)
        departure_exit_lon, departure_exit_lat = coordinate_request_func(
            departure_station_elevator_exit
        )
        # print("출발엘베출구", departure_exit_lon, departure_exit_lat)
        arrival_exit_lon, arrival_exit_lat = coordinate_request_func(
            arrival_station_elevator_exit
        )

        # 출발, 도착지 좌표
        start_lon, start_lat = get_point_coordinate_func(steps, 1)
        # print("출발지 좌표", start_lon, start_lat)
        end_lon, end_lat = get_point_coordinate_func(steps, 0)

        # 출발지 - 승차역 엘레베이터 출구: 도보 경로 요청
        start_pedestrian_route = pedestrian_request_func(
            start_lon,
            start_lat,
            departure_exit_lon,
            departure_exit_lat,
        )

        # 하차역 엘레베이터 출구 - 도착지: 도보 경로 요청
        end_pedestrian_route = pedestrian_request_func(
            arrival_exit_lon,
            arrival_exit_lat,
            end_lon,
            end_lat,
        )

        departure_pedestrian_coordinate_list = get_tmap_coordinate(
            start_pedestrian_route
        )
        arrival_pedestrian_coordinate_list = get_tmap_coordinate(end_pedestrian_route)

        # 전체 polyline, coordinate 데이터 완성
        polyline_info.append(departure_pedestrian_coordinate_list)
        polyline_info.append(subway_polyline_list)
        polyline_info.append(arrival_pedestrian_coordinate_list)

        response_value = navigation_response_func(
            is_bus_exist,
            is_subway_exist,
            polyline_info,
            detail_route_info,
        )

        return Response(response_value)
