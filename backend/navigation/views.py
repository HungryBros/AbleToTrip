from django.shortcuts import get_object_or_404
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from django.core.cache import cache
from member.utils import is_logged_in
from pprint import pprint

# Import Models and Serializers
from .models import Restroom

# Import Util Functions
from .utils import (
    log_time_func,
    direction_request_func,
    get_steps_func,
    get_point_coordinate_func,
    find_exit_func,
    coordinate_request_func,
    pedestrian_request_func,
    get_tmap_info_func,
    navigation_response_func,
    get_additional_ETA_func,
)


# Find Route
@api_view(["POST"])
def navigation(request):
    # 로그인 인증
    if not is_logged_in(request):
        return Response(
            {"message": "인증된 사용자가 아닙니다."},
            status=status.HTTP_401_UNAUTHORIZED,
        )

    print(f"{log_time_func()} - Navigation: Navigation 함수 START")

    print(f"{log_time_func()} - Navigation: Load trained ETA model")
    # global trained_ETA_model

    print(f"{log_time_func()} - Navigation: REQUEST USER - {request.user}")
    origin = request.data.get("departure")
    destination = request.data.get("arrival")

    # Google Maps 경로 API Request Parameters
    mode = "transit"  # Options : walking, driving, bicycling, transit
    transit_mode = "subway"

    response_json = direction_request_func(origin, destination, mode, transit_mode)

    # Routes & Steps 데이터
    steps, duration = get_steps_func(response_json)

    print(f"{log_time_func()} - Navigation: DURATION - {duration}")

    # Step, travel_mode 리스트 Init
    step_list = list()
    step_travel_mode_list = list()

    # Transit Flags
    is_bus_exist = False  # "BUS" 유무
    is_subway_exist = False  # "SUBWAY" 유무
    is_pedestrian_route = False  # 도보 경로 안내 유무

    # 각 Step 확인
    for step in steps:
        travel_mode = step.get("travel_mode")
        print(f"{log_time_func()} - Navigation: TRAVEL MODE - {travel_mode}")

        # Transit의 Type 확인
        if travel_mode == "TRANSIT":
            transit_type = (
                step.get("transit_details").get("line").get("vehicle").get("type")
            )
            print(f"{log_time_func()} - Navigation:     TRANSIT TYPE - {transit_type}")

            # 경로 상에 지하철이 포함된 경우, 표시
            if transit_type == "SUBWAY":
                is_subway_exist = True

            # # 경로 상에 버스가 포함된 경우, 종료
            elif transit_type == "BUS":
                print(f"{log_time_func()} - Navigation: 경로에 버스 포함")

                is_bus_exist = True
                break

        # "travel_mode"가 버스가 아닌 경우
        step_list.append(step)  # step을 각각의 원소로 저장
        step_travel_mode_list.append(travel_mode)  # 각 스텝의 travel_mode를 저장

    google_route_pedestrian_departure_duration = int(
        steps[0].get("duration").get("text").rstrip("분")
    )
    google_route_pedestrian_arrival_duration = int(
        steps[-1].get("duration").get("text").rstrip("분")
    )

    # Response_value Init
    polyline_info = list()
    detail_route_info = list()

    # 1) "지하철 x" -> 도보 경로 안내
    if not is_subway_exist:
        is_pedestrian_route = True

        try:
            print(f"{log_time_func()} - Navigation: 경로 상에 지하철 없음")
            print(f"{log_time_func()} - Navigation: 도보 경로 탐색 START")

            print(f"{log_time_func()} - Navigation: T Map 출발지 좌표 요청 START")

            departure_lon, departure_lat = coordinate_request_func(origin)
            arrival_lon, arrival_lat = coordinate_request_func(destination)

            print(f"{log_time_func()} - Navigation: T Map 출발지 좌표 요청 SUCCESS")

            # T Map: "출발지 - 승차역 엘레베이터 출구" 도보 경로 요청
            print(f"{log_time_func()} - Navigation: T Map ONLY 도보 경로 요청 START")

            pedestrian_route = pedestrian_request_func(
                departure_lon,
                departure_lat,
                arrival_lon,
                arrival_lat,
            )

            (
                pedestrian_coordinate_list,
                pedestrian_description_list,
                duration,
                distance,
            ) = get_tmap_info_func(pedestrian_route)

            print(f"{log_time_func()} - Navigation: T Map ONLY 도보 경로 요청 SUCCESS")

            polyline_info.append(
                {
                    "type": "walk",
                    "info": pedestrian_coordinate_list,
                }
            )

            detail_route_info.append(
                {
                    "type": "walk",
                    "info": pedestrian_description_list,
                }
            )

            response_value = navigation_response_func(
                duration,
                is_bus_exist,
                is_subway_exist,
                is_pedestrian_route,
                polyline_info,
                detail_route_info,
            )

            print(f"{log_time_func()} - Navigation: 도보 경로 탐색 SUCCESS")

            return Response(response_value)

        except Exception as err:

            print(f"{log_time_func()} - Navigation: 도보 경로 탐색 FAIL")
            print(f"{log_time_func()} - Navigation: EXCEPT ERROR: {err}")

            response_value = navigation_response_func(
                0,
                is_bus_exist,
                is_subway_exist,
                is_pedestrian_route,
                polyline_info,
                detail_route_info,
            )

            return Response(response_value)

    # 2) "지하철 o" -> 지하철 경로 안내
    else:
        try:
            print(f"{log_time_func()} - Navigation: 지하철 경로 탐색 START")
            print(f"{log_time_func()} - Navigation: BUS - {is_bus_exist}")
            print(f"{log_time_func()} - Navigation: SUBWAY - {is_subway_exist}")

            step_length = len(step_travel_mode_list)

            # 지하철역에서 시작하는 경우 steps[0]이 지하철 경로임
            if step_travel_mode_list[0] == "TRANSIT":
                step_start_idx = 0
            else:
                step_start_idx = 1

            print(f"{log_time_func()} - Navigation: STEP LENGTH {step_length}")

            # 지하철 구간 각 polyline 및 경로 상세 정보 추출
            subway_stops = list()
            cached_subway_stops = list()
            subway_polyline_list = list()
            subway_description_list = list()

            print(f"{log_time_func()} - Navigation: POLYLINE, 상세 경로 추출 START")

            for step_idx in range(0, step_length):
                print(
                    f"{log_time_func()} - Navigation: POLYLINE 추출 for 문 step_idx: {step_idx}"
                )
                if step_travel_mode_list[step_idx] != "TRANSIT":
                    continue

                print(
                    f"{log_time_func()} - Navigation: POLYLINE 추출 if 통과 step_idx: {step_idx}"
                )
                transit_details = step_list[step_idx].get("transit_details")

                departure_station_name = transit_details.get("departure_stop").get(
                    "name"
                )
                arrival_station_name = transit_details.get("arrival_stop").get("name")
                line_name = transit_details.get("line").get("short_name")

                subway_description_list.append(
                    f"{line_name}: {departure_station_name} - {arrival_station_name}"
                )

                line_number = line_name.rstrip("호선")

                departure_station_fullname = f"{departure_station_name} {line_number}"
                cached_subway_stops.append(f"{departure_station_name} {line_number}")
                subway_stops.append(departure_station_fullname)

                # 지하철 환승이 존재하는 경우
                # idx가 처음일 때 앞 역만 저장하고, 나머지는 앞뒤역 다 저장

                subway_polyline_list.append(
                    {
                        "line": line_name,
                        "polyline": step_list[step_idx].get("polyline").get("points"),
                    }
                )

                if step_length >= 3 and step_idx == step_start_idx:
                    continue

                arrival_station_fullname = f"{arrival_station_name} {line_number}"
                cached_subway_stops.append(f"{arrival_station_name} {line_number}")
                subway_stops.append(arrival_station_fullname)

            print(f"{log_time_func()} - Navigation: POLYLINE 추출 for문 완료")
            print(f"{log_time_func()} - Navigation: POLYLINE, 상세 경로 추출 SUCCESS")

            print(f"{log_time_func()} - Navigation: REDIS 저장 START")
            print(f"{log_time_func()} - Navigation: REQUEST USER: {request.user}")
            print(
                f"{log_time_func()} - Navigation: CACHED STATIONS: {cached_subway_stops}"
            )

            cache.set(request.user, cached_subway_stops, 3600 * 2)

            print(f"{log_time_func()} - Navigation: REDIS 저장 SUCCESS")

            print(f"{log_time_func()} - Navigation: 엘레베이터 출구 탐색 START")

            # 엘레베이터 출구 찾기
            departure_station_elevator_exit = find_exit_func(subway_stops[0])
            arrival_station_elevator_exit = find_exit_func(subway_stops[-1])

            # 두 출구 하나라도 엘레베이터가 없는 경우 => 탐색 종료
            if not (departure_station_elevator_exit and arrival_station_elevator_exit):
                print(f"{log_time_func()} - Navigation: 엘레베이터 출구 탐색 FAIL")
                print(
                    f"{log_time_func()} - Navigation: {subway_stops[0]} 엘레베이터 출구 {departure_station_elevator_exit}"
                )
                print(
                    f"{log_time_func()} - Navigation: {subway_stops[-1]} 엘레베이터 출구 {arrival_station_elevator_exit}"
                )

                response_value = navigation_response_func(
                    0,
                    is_bus_exist,
                    is_subway_exist,
                    is_pedestrian_route,
                )

                return Response(response_value)

            print(f"{log_time_func()} - Navigation: 엘레베이터 출구 탐색 SUCCESS")

            # 두 엘레베이터 출구의 좌표값, tuple: (lon, lat)
            departure_exit_lon, departure_exit_lat = coordinate_request_func(
                departure_station_elevator_exit
            )
            arrival_exit_lon, arrival_exit_lat = coordinate_request_func(
                arrival_station_elevator_exit
            )

            # Google Maps 경로 API에서 출발, 도착지 좌표 반환
            start_lon, start_lat = get_point_coordinate_func(steps, 1)
            end_lon, end_lat = get_point_coordinate_func(steps, 0)

            print(f"{log_time_func()} - Navigation: 양 출구 T Map 도보 경로 요청 START")
            # T Map: "출발지 - 승차역 엘레베이터 출구" 도보 경로 요청
            start_pedestrian_route = pedestrian_request_func(
                start_lon,
                start_lat,
                departure_exit_lon,
                departure_exit_lat,
            )

            # T Map: "하차역 엘레베이터 출구 - 도착지" 도보 경로 요청
            end_pedestrian_route = pedestrian_request_func(
                arrival_exit_lon,
                arrival_exit_lat,
                end_lon,
                end_lat,
            )

            print(
                f"{log_time_func()} - Navigation: 양 출구 T Map 도보 경로 요청 SUCCESS"
            )

            (
                departure_pedestrian_coordinate_list,
                departure_pedestrian_description_list,
                departure_pedestrian_duration,
                departure_pedestrian_distance,
            ) = get_tmap_info_func(start_pedestrian_route)
            (
                arrival_pedestrian_coordinate_list,
                arrival_pedestrian_description_list,
                arrival_pedestrian_duration,
                arrival_pedestrian_distance,
            ) = get_tmap_info_func(end_pedestrian_route)

            # 전체 polyline, coordinate 데이터 완성
            polyline_info.append(
                {
                    "type": "walk",
                    "info": departure_pedestrian_coordinate_list,
                }
            )
            polyline_info.append(
                {
                    "type": "subway",
                    "info": subway_polyline_list,
                }
            )
            polyline_info.append(
                {
                    "type": "walk",
                    "info": arrival_pedestrian_coordinate_list,
                }
            )

            # 전체 detail_route_info 완성
            detail_route_info.append(
                {
                    "type": "walk",
                    "info": departure_pedestrian_description_list,
                }
            )
            detail_route_info.append(
                {
                    "type": "subway",
                    "info": subway_description_list,
                }
            )
            detail_route_info.append(
                {
                    "type": "walk",
                    "info": arrival_pedestrian_description_list,
                }
            )

            print(f"{log_time_func()} - Navigation: 지하철 경로 탐색 SUCCESS")

            # Calculate ETA
            print(f"{log_time_func()} - Navigation: ETA 계산 START")

            additional_ETA = get_additional_ETA_func(
                departure_pedestrian_distance,
                departure_pedestrian_duration,
                arrival_pedestrian_distance,
                arrival_pedestrian_duration,
                subway_stops,
            )

            print(
                f"{log_time_func()} - Navigation: additional ETA - {additional_ETA} minutes"
            )

            # 총 시간 계산
            duration = (
                duration
                + departure_pedestrian_duration
                + arrival_pedestrian_duration
                - google_route_pedestrian_departure_duration
                - google_route_pedestrian_arrival_duration
                + additional_ETA
            )

            print(f"{log_time_func()} - Navigation: Total ETA - {duration} minutes")

            print(f"{log_time_func()} - Navigation: ETA 계산 SUCCESS")

        except Exception as err:
            print(f"{log_time_func()} - Navigation: 지하철 경로 탐색 FAIL")
            print(f"{log_time_func()} - Navigation: EXCEPT ERROR: {err}")
            polyline_info = list()
            detail_route_info = list()
            duration = 0

        response_value = navigation_response_func(
            duration,
            is_bus_exist,
            is_subway_exist,
            is_pedestrian_route,
            polyline_info,
            detail_route_info,
        )

        return Response(response_value)


# Find Restrooms on Current Route
@api_view(["GET"])
def restroom(request):
    # 로그인 인증
    if not is_logged_in(request):
        return Response(
            {"message": "인증된 사용자가 아닙니다."},
            status=status.HTTP_401_UNAUTHORIZED,
        )

    try:
        cached_subway_stops = cache.get(request.user)

        filtered_restrooms = Restroom.objects.filter(
            station_fullname__in=cached_subway_stops
        )

        # 특정 필드만 출력
        restrooms = filtered_restrooms.values(
            "station_fullname",
            "is_outside",
            "floor",
            "restroom_location",
        )

    except:
        restrooms = []

    if restrooms:
        for restroom in restrooms:
            restroom["station_fullname"] = (
                str(restroom.get("station_fullname")) + "호선"
            )
            longitude, latitude = coordinate_request_func(
                restroom.get("station_fullname")
            )

            restroom["coordinate"] = {
                "longitude": longitude,
                "latitude": latitude,
            }

        return Response({"restrooms": restrooms})

    else:
        return Response({"restrooms": ""})
