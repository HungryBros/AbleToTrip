from django.shortcuts import render
from rest_framework.decorators import api_view
from rest_framework.response import Response


# Create your views here.
@api_view(['GET'])
def attraction(request):
    return Response({"response": "attraction"})


@api_view(['GET'])
def attraction_specific(request):
    return Response({"response": "attraction_specific"})


@api_view(['GET'])
def attraction_more(request, category1):
    return Response({"response": f"{category1}"})


@api_view(['GET'])
def attraction_search(request):
    return Response({"response": "attraction_search"})
