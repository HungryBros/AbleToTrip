from rest_framework import serializers
from django.contrib.auth import get_user_model

User = get_user_model()

class UserSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ['email', 'address']  # 주소 필드 추가

    def create(self, validated_data):
        user = User.objects.create_user(
            email=validated_data['email'],
            address=validated_data.get('address')  # 주소 정보 저장
        )
        return user
