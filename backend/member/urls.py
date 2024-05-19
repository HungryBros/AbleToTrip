from django.urls import path
from . import views

app_name = "member"
urlpatterns = [
    path("signin/", views.signin, name="signin"),
    path("info/", views.info, name="info"),
]
