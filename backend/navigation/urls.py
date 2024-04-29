from django.urls import path
from . import views

app_name = "navigation"
urlpatterns = [
    # 전체 경로 찾기
    path("search-direction/", views.navigation, name="navigation"),
]
