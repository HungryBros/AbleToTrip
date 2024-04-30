from django.urls import path
from . import views

app_name = ""
urlpatterns = [
    # 홈 - 기본
    path("", views.attraction, name="attraction"),
   
    # 특정 관광지 정보 요청
    path( "detail/<int:id>", views.attraction_detail),
    # 홈 - 카테고리2 직접 선택
    path("by_category/", views.attraction_by_category, name="attraction_by_category"
    ),
    # 홈 - 카테고리1 더보기
    path("more/", views.attraction_more, name="attraction_more"),
    # 관광지 검색
    path("search/", views.attraction_search, name="attraction_search"),
]
