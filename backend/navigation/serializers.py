from rest_framework import serializers
from .models import Convenient


class ConvenientSerializer(serializers.ModelSerializer):
    class Meta:
        model = Convenient
        fields = "__all__"
