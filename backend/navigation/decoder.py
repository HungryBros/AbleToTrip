from rest_framework import status
from rest_framework.response import Response
from member.utils import is_logged_in
from rest_framework.decorators import api_view


def replace_backslashes(input_string):
    """Replaces all single backslashes with double backslashes in a given string.

    Args:
        input_string (str): The string to modify.

    Returns:
        str: Modified string where all backslashes are doubled.
    """
    return input_string.replace("\\", "\\\\")


def decode_polyline(polyline_str):
    """Decodes a polyline that was encoded using the Google Maps algorithm.

    Args:
        polyline_str (str): Encoded polyline string.

    Returns:
        List of tuples where each tuple represents latitude and longitude.
    """
    index, lat, lng, coordinates = 0, 0, 0, []
    while index < len(polyline_str):
        for is_lat in [True, False]:  # True for latitude, False for longitude
            result, shift = 0, 0
            byte = 0x20
            while byte >= 0x20:
                byte = ord(polyline_str[index]) - 63
                index += 1
                result |= (byte & 0x1F) << shift
                shift += 5
            if result & 1:
                result = ~result
            result >>= 1
            coord = result / 1e5
            if is_lat:  # This is for latitude
                lat += coord
            else:  # This is for longitude
                lng += coord
                coordinates.append([lat, lng])
    return coordinates


@api_view(["POST"])
def get_decoded_polyline(request):
    if not is_logged_in(request):
        return Response(
            {"message": "인증된 사용자가 아닙니다."},
            status=status.HTTP_401_UNAUTHORIZED,
        )
    try:
        encoded_polyline = request.data.get("input")
        non_escape_encoded_polyline = replace_backslashes(encoded_polyline)
        decoded_polyline = decode_polyline(non_escape_encoded_polyline)
        is_success = 1 if decoded_polyline else 0
        data = {
            "success": is_success,
            "data": decoded_polyline,
        }
        return Response(
            data,
            status=status.HTTP_200_OK if is_success else status.HTTP_204_NO_CONTENT,
        )
    except:
        return Response(
            {"message": "잘못된 요청입니다."},
            status=status.HTTP_400_BAD_REQUEST,
        )
