from django.urls import path
from . import views, decoder

app_name = "navigation"
urlpatterns = [
    # 전체 경로 찾기
    path("search-direction/", views.navigation, name="navigation"),
    # 화장실 위치 찾기
    path("restroom/", views.restroom, name="restroom"),
    # Polyline 디코더
    path("polyline/", decoder.get_decoded_polyline, name="polyline"),
]
