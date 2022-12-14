PROFILE=prod
GRPC_PORT=10510
HTTP_PORT=10500
# Used so we know what URL to give Portfolio when serving HTTP content
HTTP_ENDPOINT='https://csse-s302g8.canterbury.ac.nz/prod/identity/'

fuser -k $HTTP_PORT/tcp || true
fuser -k $GRPC_PORT/tcp || true
set -o allexport
source production-identityprovider/env
set +o allexport

java -jar staging-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar \
    --spring.application.name=identity-provider \
    --server.port=$HTTP_PORT \
    --grpc.server.port=$GRPC_PORT \
    --spring.profiles.active=$PROFILE \
    --http-endpoint=$HTTP_ENDPOINT
