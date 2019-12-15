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


# Fabric8 


Add `Pull` policy to pull image from `Build` namespace
```bash
oc policy add-role-to-group system:image-puller system:serviceaccounts:dev -n build
```


Create Resource for `Dev` namespace.
```bash
mvn fabric8:resource -Dfabric8.namespace=dev
```

Apply Configuration
```bash
mvn fabric8:resource-apply -Dfabric8.namespace=dev
```

Build Application in `Build` namespace.
```bash
mvn package fabric8:build  -Dfabric8.namespace=build
```






# Create Tag
````bash
oc tag spring-cloud-gateway:latest spring-cloud-gateway:dev -nbuild
````
# Delete Tag
```bash
oc delete istag/spring-cloud-gateway:dev -nbuild
```
# Check Tags
```bash
oc get is -nbuild
```






# Set Trigger - Redeploy App in Dev 
```bash
oc set triggers dc/spring-cloud-gateway --from-image=build/spring-cloud-gateway:dev -c spring-boot -ndev


oc set triggers dc/spring-cloud-gateway --from-image=build/spring-cloud-gateway:dev -c spring-cloud-gateway -ndev
```




```bash
oc create route edge --service=spring-cloud-gateway --hostname=http://spring-cloud-gateway-build.apps.c3smonkey.ch/ --path=/
oc create dc spring-cloud-gateway --image=docker-registry.default.svc:5000/build/spring-cloud-gateway:latest -n dev
oc patch dc/spring-cloud-gateway  -p '{"spec":{"template":{"spec":{"containers":[{"name":"default-container","imagePullPolicy":"Always"}]}}}}' -n dev

oc expose dc spring-cloud-gateway -n dev --port=8080

```