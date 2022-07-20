PROFILE=test
GRPC_PORT=9510
HTTP_PORT=9500
# Used so we know what URL to give Portfolio when serving HTTP content
HTTP_ENDPOINT='https://csse-s302g8.canterbury.ac.nz/test/identity/'

fuser -k $HTTP_PORT/tcp || true
fuser -k $GRPC_PORT/tcp || true

set -o allexport
source staging-identityprovider/env
set +o allexport

java -jar staging-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar \
    --server.port=9502 \
    --spring.application.name=identity-provider \
    --server.port=$HTTP_PORT \
    --grpc.server.port=$GRPC_PORT \
    --spring.profiles.active=$PROFILE \
    --http-endpoint=$HTTP_ENDPOINT
