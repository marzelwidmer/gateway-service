
# Skaffold Run Pipeline on OKD
## Login Internal Docker Registry
```bash
docker login -u developer -p `oc whoami -t` registry.apps.c3smonkey.ch

WARNING! Using --password via the CLI is insecure. Use --password-stdin.
Login Succeeded
```

# Kustomize 
```bash
kustomize build k8s/overlays/dev > k8s-deployment.yaml
```

## Run Skaffold Pipeline
```bash
skaffold run -p monkey
```

## RateLimite Test
```bash
for i in {1..10}; do http "http://localhost:8080/test-kotlin" ; done
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


# Fabric8 

Create Projects
```bash
oc new-project build --display-name="Build Environment"
oc new-project dev --display-name="Development Stage"
```

Add `Pull` policy to pull image from `Build` namespace
```bash
oc policy add-role-to-group system:image-puller system:serviceaccounts:dev -n build
```


Create Resource for `Dev` namespace.
```bash
mvn clean fabric8:resource -Dfabric8.namespace=dev
```

Apply Configuration
```bash
mvn fabric8:resource-apply -Dfabric8.namespace=dev
```
Set Trigger for `Build` project image change.
```bash
oc set triggers dc/spring-cloud-gateway --from-image=build/spring-cloud-gateway:latest -c spring-cloud-gateway -ndev

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
oc set triggers dc/spring-cloud-gateway --from-image=build/spring-cloud-gateway:dev -c spring-cloud-gateway -ndev
```




```bash
oc create route edge --service=spring-cloud-gateway --hostname=http://spring-cloud-gateway-build.apps.c3smonkey.ch/ --path=/
oc create dc spring-cloud-gateway --image=docker-registry.default.svc:5000/build/spring-cloud-gateway:latest -n dev
oc patch dc/spring-cloud-gateway  -p '{"spec":{"template":{"spec":{"containers":[{"name":"default-container","imagePullPolicy":"Always"}]}}}}' -n dev

oc expose dc spring-cloud-gateway -n dev --port=8080

```