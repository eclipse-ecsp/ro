FROM eclipseecsp/ecsp-base-java17:1.0.0

LABEL author="Suyash.Srivastava@Harman.com"

ARG PROJECT_JAR_VERSION

RUN \
  apk update && \
  apk upgrade --no-cache && \
  apk add --no-cache bash openssl shadow libgcc libstdc++ ncurses-libs

ADD target/ro-sp-${PROJECT_JAR_VERSION}-shaded.jar /opt/ro-sp/ro-sp.jar
ADD src/scripts/start.sh /opt/ro-sp/bin/start.sh
ADD src/main/resources/application.properties /opt/ro-sp/application.properties
ADD src/main/resources/kafka.client.keystore.jks /kafka/ssl/kafka.client.keystore.jks
ADD src/main/resources/kafka.client.truststore.jks /kafka/ssl/kafka.client.truststore.jks
ADD src/main/resources/logback.xml /opt/ro-sp/logback.xml

# Set Java CLASSPATH
ENV CLASSPATH /opt/*

RUN chmod 755 /opt/ro-sp/bin/start.sh && \
    chown -R 1000 /opt && \
    addgroup --system appgroup && \
    adduser -G appgroup -S appuser && \
    usermod -aG appgroup appuser && \
    chown -R appuser:appgroup /opt

USER appuser

ENTRYPOINT /bin/sh /opt/ro-sp/bin/start.sh