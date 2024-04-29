from django.urls import path
from . import views

app_name = ""
urlpatterns = [
    # 홈 - 기본
    path("", views.attraction, name="attraction"),
    # 홈 - 카테고리2 직접 선택
    path(
        "specific/", views.attraction_specific, name="attraction_specific"
    ),  # 특정 관광지 정보 요청
    # 홈 - 카테고리1 더보기
    path("more/", views.attraction_more, name="attraction_more"),
    # 관광지 검색
    path("search/", views.attraction_search, name="attraction_search"),
    # 이미지 저장 - admin용
    path(
        "update-attraction-images/",
        views.update_attraction_images,
        name="update_attraction_images",
    ),
]
