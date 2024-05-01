from math import radians, sin, cos, sqrt, atan2
from decouple import config

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

    distance = round(R * c * 1000)  # 거리 (단위: m)
    return distance


def get_image_url(filename):
    image_base_url = config('S3_BASE_URL')
    image_url = f"{image_base_url}images/{filename}.png"
    return image_url

