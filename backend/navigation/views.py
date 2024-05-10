from django.shortcuts import get_object_or_404
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from django.core.cache import cache
from member.utils import is_logged_in, get_user

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

    origin = request.data.get("departure")
    destination = request.data.get("arrival")
    print(f"{log_time_func()} - Navigation: 출발 / 도착 - {origin} / {destination}")

    # Google Maps 경로 API Request Parameters
    mode = "transit"  # Options : walking, driving, bicycling, transit
    transit_mode = "subway"

    response_json = direction_request_func(origin, destination, mode, transit_mode)

    # Routes & Steps 데이터
    steps, duration = get_steps_func(response_json)

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

        # Transit의 Type 확인
        if travel_mode == "TRANSIT":
            transit_type = (
                step.get("transit_details").get("line").get("vehicle").get("type")
            )

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
            print(f"{log_time_func()} - Navigation: 지하철 없음, 도보 경로 탐색")

            departure_lon, departure_lat = coordinate_request_func(origin)
            arrival_lon, arrival_lat = coordinate_request_func(destination)

            if not (departure_lon and departure_lat and arrival_lon and arrival_lat):

                return Response(
                    navigation_response_func(0, False, False, False),
                    status=status.HTTP_204_NO_CONTENT,
                )

            # T Map: "출발지 - 승차역 엘레베이터 출구" 도보 경로 요청
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

            return Response(
                response_value,
                status=status.HTTP_200_OK,
            )

        except Exception as err:
            print(f"{log_time_func()} - Navigation: 도보 경로 탐색 ERROR: {err}")

            return Response(
                navigation_response_func(0, False, False, False),
                status=status.HTTP_204_NO_CONTENT,
            )

    # 2) "지하철 o" -> 지하철 경로 안내
    else:
        try:
            print(f"{log_time_func()} - Navigation: 지하철 경로 탐색")

            step_length = len(step_travel_mode_list)

            # 지하철 구간 각 polyline 및 경로 상세 정보 추출
            subway_stops = list()
            cached_subway_stops = list()
            subway_polyline_list = list()
            subway_description_list = list()

            for step_idx in range(0, step_length):

                if step_travel_mode_list[step_idx] != "TRANSIT":
                    continue

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
                subway_stops.append(departure_station_fullname)
                cached_subway_stops.append(f"{departure_station_name} {line_number}")

                subway_polyline_list.append(
                    {
                        "line": line_name,
                        "polyline": step_list[step_idx].get("polyline").get("points"),
                    }
                )

                arrival_station_fullname = f"{arrival_station_name} {line_number}"
                cached_subway_stops.append(f"{arrival_station_name} {line_number}")
                subway_stops.append(arrival_station_fullname)

            print(
                f"{log_time_func()} - Navigation: CACHED STATIONS for REDIS: {cached_subway_stops}"
            )

            # 유저 이메일 가져오기
            user = get_user(request)
            print(f"{log_time_func()} - Navigation: REQUEST USER: {user}")

            cache.set(user, cached_subway_stops, 3600 * 2)
            print(f"{log_time_func()} - Navigation: REDIS 저장 SUCCESS")

            # 엘레베이터 출구 찾기
            departure_station_elevator_exit = find_exit_func(subway_stops[0])
            arrival_station_elevator_exit = find_exit_func(subway_stops[-1])

            # 두 출구 하나라도 엘레베이터가 없는 경우 => 탐색 종료
            if not (departure_station_elevator_exit and arrival_station_elevator_exit):
                print(
                    f"{log_time_func()} - Navigation: {subway_stops[0]} 엘레베이터 출구 {departure_station_elevator_exit}"
                )
                print(
                    f"{log_time_func()} - Navigation: {subway_stops[-1]} 엘레베이터 출구 {arrival_station_elevator_exit}"
                )
                print(f"{log_time_func()} - Navigation: 길찾기 불가능")

                return Response(
                    navigation_response_func(0, False, False, False),
                    status=status.HTTP_204_NO_CONTENT,
                )

            print(f"{log_time_func()} - Navigation: 엘레베이터 출구 탐색 SUCCESS")

            # 두 엘레베이터 출구의 좌표값, tuple: (lon, lat)
            departure_exit_lon, departure_exit_lat = coordinate_request_func(
                departure_station_elevator_exit
            )
            arrival_exit_lon, arrival_exit_lat = coordinate_request_func(
                arrival_station_elevator_exit
            )

            # Kakao Maps API 결과 없을 때
            if not (
                departure_exit_lon
                and departure_exit_lat
                and arrival_exit_lon
                and arrival_exit_lat
            ):

                return Response(
                    navigation_response_func(0, False, False, False),
                    status=status.HTTP_204_NO_CONTENT,
                )

            # Google Maps 경로 API에서 출발, 도착지 좌표 반환
            start_lon, start_lat = get_point_coordinate_func(steps, 1)
            end_lon, end_lat = get_point_coordinate_func(steps, 0)

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
            additional_ETA = get_additional_ETA_func(
                departure_pedestrian_distance,
                departure_pedestrian_duration,
                arrival_pedestrian_distance,
                arrival_pedestrian_duration,
                subway_stops,
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

            print(f"{log_time_func()} - Navigation: ETA 계산 SUCCESS")
            print(
                f"{log_time_func()} - Navigation: Additional / Total ETA (min) - {additional_ETA} / {duration}"
            )

            response_value = navigation_response_func(
                duration,
                is_bus_exist,
                is_subway_exist,
                is_pedestrian_route,
                polyline_info,
                detail_route_info,
            )

            return Response(
                response_value,
                status=status.HTTP_200_OK,
            )

        except Exception as err:
            print(f"{log_time_func()} - Navigation: 지하철 경로 탐색 FAILED")
            print(f"{log_time_func()} - Navigation: EXCEPT ERROR: {err}")

            return Response(
                navigation_response_func(0, False, False, False),
                status=status.HTTP_204_NO_CONTENT,
            )


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
        user = get_user(request)
        cached_subway_stops = cache.get(user)

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
        print(f"{log_time_func()} - Restroom: 화장실 없음")
        restrooms = []

    if restrooms:
        for restroom in restrooms:
            station_fullname = restroom.get("station_fullname")

            name, line = station_fullname.split()
            name = name if name[-1] == "역" else name + "역 "
            line += "호선"
            station_fullname = name + line

            restroom["station_fullname"] = station_fullname

            longitude, latitude = coordinate_request_func(station_fullname)

            restroom["coordinate"] = {
                "longitude": longitude,
                "latitude": latitude,
            }

        print(f"{log_time_func()} - Restroom: 화장실 탐색 SUCCESS")

        return Response({"restrooms": restrooms})

    else:
        print(f"{log_time_func()} - Restroom: 화장실 탐색 종료")
        return Response({"restrooms": ""})