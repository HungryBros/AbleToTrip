from rest_framework import serializers
from .models import Attraction
from .utils import calculate_distance, make_image_url


class AttractionSerializer(serializers.ModelSerializer):
    class Meta:
        model = Attraction
        fields = "__all__"
