fuser -k 9501/tcp || true

java -jar staging-portfolio/libs/portfolio-0.0.1-SNAPSHOT.jar \
    --server.port=9501 \
    --spring.application.name=portfolio \
    --grpc.client.identity-provider-grpc-server.address=static://127.0.0.1:9500/test/identity/ \
    --grpc.client.identity-provider-grpc-server.enableKeepAlive=true \
    --grpc.client.identity-provider-grpc-server.keepAliveWithoutCalls=true \
    --grpc.client.identity-provider-grpc-server.negotiationType=plaintext \
    --spring.mvc.servlet.path=/ \
    --spring.datasource.url=jdbc:mariadb://db2.csse.canterbury.ac.nz/seng302-2022-team800-portfolio-test \
    --spring.datasource.username=seng302-team800 \
    --spring.datasource.password=LocallyBlanket3943 \
    --spring.datasource.driverClassName=org.mariadb.jdbc.Driver \
    --spring.jpa.hibernate.ddl-auto=update \
    --spring.jpa.hibernate.dialect=org.hibernate.dialect.MariaDBDialect \
    --base-url=/test/portfolio/

