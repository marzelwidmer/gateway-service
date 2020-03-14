

 
# OKD cheat sheet
[oc cli tricks](https://gist.github.com/tuxfight3r/79bddbf4af9b6d13d590670c40fec3e0#file-openshift_cli_tricks-md)

Show not Running POD's
```bash
$ oc get pods --field-selector=status.phase!=Running
```

Show only Running POD's
```bash
$ oc get pods --field-selector=status.phase=Running
```

Get Secret
```bash
$ oc get bc/catalog-service-pipeline -n jenkins -o json | jq '.spec.triggers[].github.secret'
```

Get Route Host
```bash
$ oc get route/jaeger-collector -o json | jq '.spec.host'e
```

Tail log of POD
```bash
$ oc logs -f order-service-22-cqqn4 --tail=50
```

# Git cheatsheet
- [git-log-format](https://devhints.io/git-log-format)
- [git-log](https://devhints.io/git-log)
- [git-tricks](https://devhints.io/git-tricks)
- [git-branch](https://devhints.io/git-branch)
- [git-revisions](https://devhints.io/git-revisions)
- [tig](https://devhints.io/tig)
- [git-extras](https://devhints.io/git-extras)



# Stern
see: https://github.com/wercker/stern
```bash
 stern gateway-service
```


# Jaeger Docker Setup Local Machine

## Initial run
```
docker run -d --name jaeger -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 -p 5775:5775/udp -p 6831:6831/udp -p 6832:6832/udp -p 5778:5778 -p 16686:16686 -p 14268:14268 -p 9411:9411 jaegertracing/all-in-one:1.16
```

## Stop Jaeger
```
docker stop jaeger 
```
## Start Jaeger
```
docker start jaeger 
```

## URL
```
http://localhost:16686/
```



# Skaffold Run Pipeline on OKD
## Login Internal Docker Registry
```bash
docker login -u developer -p `oc whoami -t` registry.apps.c3smonkey.ch

WARNING! Using --password via the CLI is insecure. Use --password-stdin.
Login Succeeded
```

# Skaffold and Kustomize
## Build
```bash
skaffold build -p monkey
```
## Build
```bash
skaffold build -p monkey
```




## Separate Steps
# Kustomize 
```bash
kustomize build k8s/overlays/dev > deployment.yaml
```

## Run Skaffold Pipeline
```bash
skaffold run -p monkey
```

## RateLimite Test
```bash
for i in {1..10}; do http "http://localhost:8080/test-kotlin" ; done
```




## Redis 
### Remote Shell in Redis POD
```bash
oc rsh redis-1-j8d67
```
### Open Redis CLI
```bash
sh-4.2$ redis-cli
```

### Authenticate 
```bash
127.0.0.1:6379> auth <password>
```

### Retrieving All Existing Keys 
The Keys are only temporaries when the rate limit has been recognized
https://chartio.com/resources/tutorials/how-to-get-all-keys-in-redis/
```bash
127.0.0.1:6379> KEYS *
1) "request_rate_limiter.{1}.timestamp"
2) "request_rate_limiter.{1}.tokens"
127.0.0.1:6379>
```







# Kustomize myhelsana-dev-ez
```bash
kustomize build k8s/overlays/myhelsana-dev-ez > deployment-myhelsana-dev-ez.yaml
```

## Run Skaffold Pipeline
```bash
skaffold run -p myhelsana-dev-ez
```











```bash
http -vv GET http://spring-cloud-gateway-dev.apps.c3smonkey.ch/coolapp/services/foo \
 forwarded:for='portal.azure.com;host=portal.azure.com;proto=https' 
```



```bash
http -vv GET http://spring-cloud-gateway-dev.apps.c3smonkey.ch/services/foo \
 x-forwarded-proto:https \
 x-forwarded-host:example.com \
 x-forwarded-port:9090 \
 forwarded:for='portal.azure.com;host=portal.azure.com;proto=https' "Authorization: Bearer ${TOKEN}" 
```

```bash
http -vv GET http://foo-service-dev.apps.c3smonkey.ch \
 x-forwarded-proto:https \
 x-forwarded-host:example.com \
 x-forwarded-port:9090 \
 forwarded:for='portal.azure.com;host=portal.azure.com;proto=https'
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
    "predicate": "(Paths: [/foo/services/bar/**], match trailing slash: true && Between: 2019-08-12T23:33:47.789+02:00[CET] and 2019-09-12T23:33:47.789+02:00[CET])",
    "route_id": "openshift_route",
    "uri": "http://foo-service-development.apps.c3smonkey.ch:80"
}
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
oc set triggers dc/spring-cloud-gateway --from-image=build/spring-cloud-gateway:dev -c spring-cloud-gateway -ndev
```




```bash
oc create route edge --service=spring-cloud-gateway --hostname=http://spring-cloud-gateway-build.apps.c3smonkey.ch/ --path=/
oc create dc spring-cloud-gateway --image=docker-registry.default.svc:5000/build/spring-cloud-gateway:latest -n dev
oc patch dc/spring-cloud-gateway  -p '{"spec":{"template":{"spec":{"containers":[{"name":"default-container","imagePullPolicy":"Always"}]}}}}' -n dev

oc expose dc spring-cloud-gateway -n dev --port=8080

```