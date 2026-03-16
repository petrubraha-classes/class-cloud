import json
import logging
import os

import requests
from django.http import JsonResponse, HttpResponse
from django.views import View
from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator

logger = logging.getLogger("proxy")

# ---------------------------------------------------------------------------
# Service base URLs  (configurable via .env)
# ---------------------------------------------------------------------------

_FLUXO_SERVICE_URL = os.environ.get("FLUXO_SERVICE_URL", "http://localhost:8080")
_ROUTE_SERVICE_URL = os.environ.get("ROUTE_SERVICE_URL", "http://localhost:8081")
_STORE_SERVICE_URL = os.environ.get("STORE_SERVICE_URL", "http://localhost:8082")


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

_API_KEYS: set[str] | None = None


def _load_api_keys() -> set[str]:
    """Load valid client API keys from config/api_keys.json (cached)."""
    global _API_KEYS
    if _API_KEYS is None:
        keys_path = os.path.join(
            os.path.dirname(__file__), "..", "config", "api_keys.json"
        )
        with open(os.path.abspath(keys_path), "r") as f:
            _API_KEYS = set(json.load(f))
    return _API_KEYS


def _validate_request(request) -> str | None:
    """
    Return the API key from the Authorization header, or None if missing/invalid.
    Raises nothing – callers check the return value.
    """
    auth_header = request.headers.get("Authorization", "")
    # Accept bare token or "Bearer <token>"
    token = auth_header.removeprefix("Bearer ").strip()
    if not token:
        return None
    if token not in _load_api_keys():
        return None
    return token


def _forward(request, url: str, service_api_key: str | None = None) -> HttpResponse:
    """
    Forward the incoming request to *url*.
    If *service_api_key* is provided it is injected as the Authorization header;
    otherwise no Authorization header is sent to the upstream service.
    Returns an HttpResponse mirroring the upstream reply.
    """
    # Build forwarded headers – strip hop-by-hop and the incoming Authorization
    excluded = {"host", "authorization", "content-length", "transfer-encoding"}
    headers = {
        k: v
        for k, v in request.headers.items()
        if k.lower() not in excluded
    }
    if service_api_key:
        headers["Authorization"] = service_api_key

    body = request.body  # raw bytes; may be empty for GET
    body_str = body.decode("utf-8", errors="replace") if body else ""

    # ── Inbound request ────────────────────────────────────────────────────
    logger.info(
        "[INBOUND]  %s %s  body=%r",
        request.method,
        request.get_full_path(),
        body_str,
    )

    # ── Forwarded request ──────────────────────────────────────────────────
    logger.info(
        "[FORWARD]  %s %s  body=%r",
        request.method,
        url,
        body_str,
    )

    upstream = requests.request(
        method=request.method,
        url=url,
        headers=headers,
        data=body,
        allow_redirects=True,
        timeout=10,
    )

    upstream_body_str = upstream.content.decode("utf-8", errors="replace")

    # ── Upstream response ──────────────────────────────────────────────────
    logger.info(
        "[RESPONSE] %s %s → %d  body=%r",
        request.method,
        url,
        upstream.status_code,
        upstream_body_str,
    )

    response = HttpResponse(
        content=upstream.content,
        status=upstream.status_code,
        content_type=upstream.headers.get("Content-Type", "application/json"),
    )
    return response


# ---------------------------------------------------------------------------
# Views
# ---------------------------------------------------------------------------

@method_decorator(csrf_exempt, name="dispatch")
class WaiterView(View):
    """
    GET /api/waiters/<waiterId>
    → FluxoService  GET _FLUXO_SERVICE_URL/waiters/<waiterId>
    FluxoService requires no API key, so no Authorization header is forwarded.
    """

    def get(self, request, waiterId):
        if not _validate_request(request):
            logger.warning("[AUTH] 403 Forbidden  %s %s", request.method, request.get_full_path())
            return JsonResponse({"error": "Forbidden"}, status=403)

        # FluxoService does not accept requests with api/ prefix.
        url = f"{_FLUXO_SERVICE_URL}/waiters/{waiterId}"
        return _forward(request, url)  # no service API key

@method_decorator(csrf_exempt, name="dispatch")
class RouteView(View):
    """
    POST /api/routes
    → RouteService  POST _ROUTE_SERVICE_URL/api/routes
    """

    def post(self, request):
        if not _validate_request(request):
            logger.warning("[AUTH] 403 Forbidden  %s %s", request.method, request.get_full_path())
            return JsonResponse({"error": "Forbidden"}, status=403)

        api_key = os.environ.get("ROUTE_SERVICE_API_KEY", "")
        url = f"{_ROUTE_SERVICE_URL}/api/routes"
        return _forward(request, url, api_key)


@method_decorator(csrf_exempt, name="dispatch")
class StoreView(View):
    """
    POST /api/stores
    → StoreService  POST _STORE_SERVICE_URL/api/stores
    """

    def post(self, request):
        if not _validate_request(request):
            logger.warning("[AUTH] 403 Forbidden  %s %s", request.method, request.get_full_path())
            return JsonResponse({"error": "Forbidden"}, status=403)

        api_key = os.environ.get("STORE_SERVICE_API_KEY", "")
        url = f"{_STORE_SERVICE_URL}/api/stores"
        return _forward(request, url, api_key)
