from rest_framework import serializers
from django.contrib.auth import get_user_model

User = get_user_model()

class UserSerializer(serializers.ModelSerializer):

    class Meta:
        model = User
        fields = ['email', 'address']  # 주소 필드 추가

    def create(self, validated_data):
        user = User.objects.create_user(
            email=validated_data['email'],
        )
        return user
