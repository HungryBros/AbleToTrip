from django.urls import path
from . import views

urlpatterns = [
    path('signup/', views.signup, name='signup'),
    path('post_address/', views.post_address, name='post_address'),
    path('signin/', views.signin, name='signin'),
]
