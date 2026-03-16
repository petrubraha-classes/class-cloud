from django.urls import path
from .views import WaiterView, RouteView, StoreView

urlpatterns = [
    path("api/waiters/<str:waiterId>", WaiterView.as_view(), name="waiter-detail"),
    path("api/routes", RouteView.as_view(), name="route-create"),
    path("api/stores", StoreView.as_view(), name="store-create"),
]
