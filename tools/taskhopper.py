import sys, urllib, urllib2

prefix = "http://localhost:8080"

if "new" in sys.argv:
    new_url = "http://linkedgov.org/data/ministers-interests/1/issue/1"
    args = urllib.urlencode(['url', new_url])
    req = urllib2.Request(prefix + "/report")
    handler = urllib2.urlopen(req, args)
    print(''.join(handler.readlines()))

if "get" in sys.argv:
    pass

if "fixed" in sys.argv:
    pass
