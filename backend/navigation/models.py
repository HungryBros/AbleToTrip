# Create your models here.
from django.db import models


class Station(models.Model):
    station_fullname = models.CharField(primary_key=True, max_length=30)
    station_name = models.CharField(max_length=20)
    line_number = models.SmallIntegerField()
    line_name = models.CharField(max_length=10)
    direction1 = models.CharField(max_length=50)
    direction2 = models.CharField(max_length=50)
    elevator_subtitle = models.TextField(null=True)
    direction1_first_train = models.CharField(max_length=50, null=True)
    direction1_last_train = models.CharField(max_length=50, null=True)
    direction2_first_train = models.CharField(max_length=50, null=True)
    direction2_last_train = models.CharField(max_length=50, null=True)


class Elevator(models.Model):
    line_number = models.SmallIntegerField()
    station_name = models.CharField(max_length=20)
    elevator_number = models.CharField(unique=True, max_length=10)
    elevator_location = models.CharField(max_length=100, null=True)
    operation_section = models.CharField(max_length=10, null=True)
    station_fullname = models.ForeignKey(Station, on_delete=models.PROTECT)


class Ramp(models.Model):
    line_number = models.SmallIntegerField()
    station_name = models.CharField(max_length=20)
    ramp_location = models.CharField(max_length=255, null=True)
    station_fullname = models.ForeignKey(Station, on_delete=models.PROTECT)


class Lift(models.Model):
    line_number = models.SmallIntegerField()
    station_name = models.CharField(max_length=20)
    lift_location = models.CharField(max_length=255, null=True)
    operation_section = models.CharField(max_length=10, null=True)
    station_fullname = models.ForeignKey(Station, on_delete=models.PROTECT)


class Restroom(models.Model):
    line_number = models.SmallIntegerField()
    station_name = models.CharField(max_length=20)
    is_outside = models.BooleanField()
    restroom_location = models.CharField(max_length=100, null=True)
    floor = models.CharField(max_length=10, null=True)
    station_fullname = models.ForeignKey(Station, on_delete=models.PROTECT)
