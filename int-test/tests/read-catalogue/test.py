from datetime import datetime
from requests import get
start = datetime.now()
get('http://thredds:8080/thredds/catalog.html')
end = datetime.now()
print('TEST_EVENT:thredds load in %ss'% (end - start))


