from django.shortcuts import render
from rest_framework.decorators import api_view
from rest_framework.response import Response
from decouple import config
import requests
from pprint import pprint

GOOGLE_MAPS_API_KEY = config("GOOGLE_MAPS_API_KEY")


# 구글 지도 API 요청하는 함수
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


@api_view(["POST"])
def navigation(request):
    origin = request.data.get("departure")
    destination = request.data.get("arrival")

    # Parameters
    mode = "transit"  # Options : walking, driving, bicycling, transit
    transit_mode = "subway"

    response_json = direction_request_func(origin, destination, mode, transit_mode)

    # Routes & Steps 데이터
    routes = response_json.get("routes")
    legs = routes[0].get("legs")
    steps = legs[0].get("steps")

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

        ########################
        # 도보 경로 안내하는 코드 #
        ########################

        response_value = navigation_response_func(
            is_bus_exist,
            is_subway_exist,
            polyline_info,
            detail_route_info,
        )

        return Response(response_value)

    # 경로에 지하철과 도보가 섞인 경우
    else:

        ###################
        # 경로 안내하는 코드 #
        ###################

        response_value = navigation_response_func(
            is_bus_exist,
            is_subway_exist,
            polyline_info,
            detail_route_info,
        )

        return Response(response_value)
