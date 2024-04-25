# Create your models here.
from django.db import models


class Attraction(models.Model):
    attraction_name = models.CharField(max_length=255)
    attraction_sub_name = models.CharField(max_length=255, blank=True, null=True)
    category1 = models.CharField(max_length=100)
    category2 = models.CharField(max_length=100, blank=True, null=True)
    si = models.CharField(max_length=100)
    gu = models.CharField(max_length=100)
    dong = models.CharField(max_length=100)
    street_number = models.CharField(max_length=100, blank=True, null=True)
    road_name = models.CharField(max_length=255, blank=True, null=True)
    latitude = models.FloatField()
    longitude = models.FloatField()
    postal_code = models.CharField(max_length=20, blank=True, null=True)
    road_name_address = models.CharField(max_length=255, blank=True, null=True)
    lot_number_address = models.CharField(max_length=255, blank=True, null=True)
    contact_number = models.CharField(max_length=20, blank=True, null=True)
    homepage_url = models.URLField(blank=True, null=True)
    closed_days = models.CharField(max_length=255, blank=True, null=True)
    operation_hours = models.CharField(max_length=255, blank=True, null=True)
    is_free_parking = models.BooleanField(default=False)
    is_paid_parking = models.BooleanField(default=False)
    is_entrance_fee = models.BooleanField(default=False)
    is_disabled_restroom = models.BooleanField(default=False)
    is_disabled_parking = models.BooleanField(default=False)
    is_large_parking = models.BooleanField(default=False)
    is_audio_guide = models.BooleanField(default=False)
    attraction_image = models.BinaryField(default=None, blank=True, null=True)

    def __str__(self):
        return self.attraction_name
