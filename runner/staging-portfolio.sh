PROFILE=test

fuser -k 9501/tcp || true

java -jar staging-portfolio/libs/portfolio-0.0.1-SNAPSHOT.jar \
    --server.port=9501 \
    --spring.application.name=portfolio \
    --grpc.client.identity-provider-grpc-server.address=static://127.0.0.1:9510 \
    --grpc.client.identity-provider-grpc-server.enableKeepAlive=true \
    --grpc.client.identity-provider-grpc-server.keepAliveWithoutCalls=true \
    --grpc.client.identity-provider-grpc-server.negotiationType=plaintext \
    --spring.profiles.active=$PROFILE \
    --base-url=/test/portfolio/
