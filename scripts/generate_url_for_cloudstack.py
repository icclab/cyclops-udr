import urllib2
import urllib
import hashlib 
import hmac
import base64

# Basic settings as endpoint, api and secret keys
baseurl='put_your_url_here'
request={}
request['apikey']='put_your_api_key_here'
secretkey='put_your_secret_key_here'

# Request command, settings and parameters
request['command']='listUsers'
request['response']='json'

# Join it all together
request_str='&'.join(['='.join([k,urllib.quote_plus(request[k])]) for k in request.keys()])
sig_str='&'.join(['='.join([k.lower(),urllib.quote_plus(request[k].lower().replace('+','%20'))])for k in sorted(request.iterkeys())])
sig=urllib.quote_plus(base64.encodestring(hmac.new(secretkey,sig_str,hashlib.sha1).digest()).strip())
req=baseurl+'?'+request_str+'&signature='+sig

# Here is your URL
print req

# Or you can directly make the Call
#res=urllib2.urlopen(req)
#print res.read()
