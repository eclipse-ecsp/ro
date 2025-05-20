FROM ghcr.io/harman-automotive/csp-base-java17-tomcat9:3.0.0-abba886

LABEL author="HCPDEV <Hemant.Bhedasgaonkar@harman.com>"

ARG API_WAR_NAME
ARG PROJECT_JAR_VERSION

RUN \
  apk update && \
  apk upgrade --no-cache && \
  apk add --no-cache bash shadow openssl libgcc libstdc++ ncurses-libs

# Set Java CLASSPATH
ENV CLASSPATH /opt/*:/usr/local/tomcat/bin

ADD target/ro-api-${PROJECT_JAR_VERSION}.war /tmp/${API_WAR_NAME}.war
COPY src/scripts/* /opt/ro-api/bin/

RUN mkdir /opt/logback && \
    chmod 755 /opt/logback && \
    chmod 755 /opt/ro-api/bin/start.sh && \
    rm -rf /usr/local/tomcat/webapps/* && \
    mv /tmp/${API_WAR_NAME}.war /usr/local/tomcat/webapps && \
    mkdir /logs && \
    addgroup --system appgroup && \
    adduser -G appgroup -S appuser  && \
    usermod -aG appgroup appuser

ADD src/main/resources/logback.xml /opt/logback/logback.xml

ENTRYPOINT /bin/sh /opt/ro-api/bin/start.sh