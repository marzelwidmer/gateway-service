```bash
urlA=:8080/myhelsana/services/medicheck

http -vv GET $urlA \
 x-forwarded-proto:https \
 x-forwarded-host:example.com \
 x-forwarded-port:9090 \
 forwarded:for='portal.helsana-entwicklung.ch;host=portal.helsana-entwicklung.ch;proto=https'
```


```bash
urlM=http://medicheck-service-development.apps.c3smonkey.ch

http -vv GET $urlM \
 x-forwarded-proto:https \
 x-forwarded-host:example.com \
 x-forwarded-port:9090 \
 forwarded:for='portal.helsana-entwicklung.ch;host=portal.helsana-entwicklung.ch;proto=https'
```



```bash
http -vv GET :8080/myhelsana/services/medicheck/api/medicheck/landing-page \
 x-forwarded-proto:https \
 x-forwarded-host:example.com \
 x-forwarded-port:9090 \
 forwarded:for='localhost:8080;host=localhost:8080;proto=http' "Authorization: Bearer ${TOKEN}"
```