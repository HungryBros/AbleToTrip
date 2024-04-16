from split_settings.tools import include, optional
from decouple import config

# include base.py

include(
	'base.py',
	'logging.py',
	optional('local_setting.py')
)

if 'dev' == config('DJANGO_ENV'):
	include('dev.py')
elif 'prod' == config('DJANGO_ENV'):
	include('prod.py')