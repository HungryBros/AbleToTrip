from rest_framework.decorators import api_view
from rest_framework.response import Response

# Import Util Functions
from .utils import (
    direction_request_func,
    get_steps_func,
    get_point_coordinate_func,
    find_exit_func,
    coordinate_request_func,
    pedestrian_request_func,
    get_tmap_info_func,
    navigation_response_func,
)


@api_view(["POST"])
def navigation(request):
    origin = request.data.get("departure")
    destination = request.data.get("arrival")

    # Google Maps 경로 API Request Parameters
    mode = "transit"  # Options : walking, driving, bicycling, transit
    transit_mode = "subway"

    response_json = direction_request_func(origin, destination, mode, transit_mode)

    # Routes & Steps 데이터
    steps = get_steps_func(response_json)

    # Step, travel_mode 리스트 Init
    step_list = list()
    step_travel_mode_list = list()

    # Transit Flags
    is_bus_exist = False  # "BUS" 유무
    is_subway_exist = False  # "SUBWAY" 유무

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

    # "도보"
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

    # "지하철 + 도보"
    else:
        step_length = len(step_travel_mode_list)

        # 1) step_length > 3 == True -> 환승 구간 o
        # 2) step_list의 [0], [-1]번째 인덱스는 도보,
        #    나머지 홀수 인덱스는 지하철, 짝수 인덱스는 환승 구간

        # 지하철 구간 각 polyline 및 경로 상세 정보 추출
        subway_stops = list()
        subway_polyline_list = list()
        subway_description_list = list()

        for subway_idx in range(1, step_length, 2):
            transit_details = step_list[subway_idx].get("transit_details")

            departure_station_name = transit_details.get("departure_stop").get("name")
            arrival_station_name = transit_details.get("arrival_stop").get("name")
            line_name = transit_details.get("line").get("short_name")

            subway_description_list.append(
                f"{line_name}: {departure_station_name} - {arrival_station_name}"
            )

            line_number = line_name.rstrip("호선")

            departure_station_fullname = f"{departure_station_name} {line_number}"
            subway_stops.append(departure_station_fullname)
            # 지하철 환승이 존재하는 경우
            # idx가 1일 때 앞 역만 저장하고, 나머지는 앞뒤역 다 저장

            subway_polyline_list.append(
                {
                    "line": line_name,
                    "polyline": step_list[subway_idx].get("polyline").get("points"),
                }
            )

            if step_length > 3 and subway_idx == 1:
                continue
            arrival_station_fullname = f"{arrival_station_name} {line_number}"
            subway_stops.append(arrival_station_fullname)

        ####################################################
        ####################################################
        ## 위의 코드 부분에서 환승 API로 받아온 정보를
        ## 사이사이에 넣어줘야 하는데...
        ## "역삼(2)-교대(2,3)-경복궁(3)"의 경우
        ## 환승역이 있는지 판단해서, 있다면
        ## "교대역"을 찾고 -> "가능"
        ## "2호선", "3호선"을 찾고 -> "가능"
        ## "교대역 2"를 DB에서 조회해서 역 코드 알아내고 -> "가능"
        ## "교대역 3"의 앞 뒤 역들의 번호를 알아내야 함 -> 어케함?!
        ####################################################
        ####################################################

        # 엘레베이터 출구 찾기
        departure_station_elevator_exit = find_exit_func(subway_stops[0])
        arrival_station_elevator_exit = find_exit_func(subway_stops[-1])

        # 두 출구 하나라도 엘레베이터가 없는 경우 => 컷
        if not (departure_station_elevator_exit and arrival_station_elevator_exit):
            response_value = navigation_response_func(is_bus_exist, is_subway_exist)

            return Response(response_value)

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

        departure_pedestrian_coordinate_list, departure_pedestrian_description_list = (
            get_tmap_info_func(start_pedestrian_route)
        )
        arrival_pedestrian_coordinate_list, arrival_pedestrian_description_list = (
            get_tmap_info_func(end_pedestrian_route)
        )

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

        response_value = navigation_response_func(
            is_bus_exist,
            is_subway_exist,
            polyline_info,
            detail_route_info,
        )

        return Response(response_value)
