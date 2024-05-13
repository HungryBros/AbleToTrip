print("RUNNING ON DEVELOPMENT ENVIRONMENT")
DEBUG = True

ALLOWED_HOSTS = ["*"]

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": "postgres",
        "USER": "postgres",
        "PASSWORD": "0000",
        "HOST": "localhost",  # 또는 데이터베이스가 호스팅되는 IP 주소
        "PORT": 5432,  # 기본 PostgreSQL 포트
    }
}
