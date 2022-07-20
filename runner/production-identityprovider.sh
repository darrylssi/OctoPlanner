fuser -k 10500/tcp || true
set -o allexport
source production-identityprovider/env
set +o allexport
java -jar production-identityprovider/libs/identityprovider-0.0.1-SNAPSHOT.jar \
    --server.port=10502 \
    --spring.application.name=identity-provider \
    --grpc.server.port=10500 \
    --spring.profiles.active=prod
