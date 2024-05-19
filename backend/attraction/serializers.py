from rest_framework import serializers
from .models import Attraction
from .utils import get_image_url


class AttractionSerializer(serializers.ModelSerializer):
    image_url = serializers.SerializerMethodField()
    
    class Meta:
        model = Attraction
        fields = "__all__"
    
    def get_image_url(self, obj):
        return get_image_url(obj.attraction_name)
