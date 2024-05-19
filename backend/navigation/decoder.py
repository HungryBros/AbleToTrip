from rest_framework import status
from rest_framework.response import Response
from member.utils import is_logged_in
from rest_framework.decorators import api_view
import polyline


def decode_polyline(encoded_polyline):
    decoded = polyline.decode(encoded_polyline)
    coordinates = []
    for d in decoded:
        coordinates.append([d[0], d[1]])
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
        decoded_polyline = decode_polyline(encoded_polyline)
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
        data = {
            "success": 0,
            "data": [[37.501286, 127.0396029]],
        }
        return Response(data, status=status.HTTP_200_OK)
