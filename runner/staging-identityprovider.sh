fuser -k 9500/tcp || true
set -o allexport
source staging-identityprovider/env
set +o allexport
java -jar staging-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar \
    --server.port=9502 \
    --spring.application.name=identity-provider \
    --grpc.server.port=9500 \
    --spring.profiles.active=test
