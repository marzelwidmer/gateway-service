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

# Check Routes
```bash
http :8080/actuator/gateway/routes/openshift_route
HTTP/1.1 200 OK
Content-Length: 491
Content-Type: application/json

{
    "filters": [
        "[[StripPrefix parts = 3], order = 1]",
        "[ch.keepcalm.demo.gateway.filters.LoggingGatewayFilterFactory$apply$1@1edf183d, order = 1]",
        "[[RewritePath /service(?<segment>/?.*) = '${segment}'], order = 2]"
    ],
    "order": 0,
    "predicate": "(Paths: [/myhelsana/services/medicheck/**], match trailing slash: true && Between: 2019-08-12T23:33:47.789+02:00[CET] and 2019-09-12T23:33:47.789+02:00[CET])",
    "route_id": "openshift_route",
    "uri": "http://medicheck-service-development.apps.c3smonkey.ch:80"
}
```