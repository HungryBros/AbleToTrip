from rest_framework import serializers
from .models import Station, Convenient, Ramp, Restroom, Lift


class ConvenientSerializer(serializers.ModelSerializer):
    class Meta:
        model = Convenient
        fields = "__all__"
