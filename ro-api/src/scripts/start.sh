#!/bin/bash

sed -i  's/protocol=\"HTTP\/1.1\"/protocol=\"HTTP\/1.1\" compression=\"on\" compressionMinSize=\"1024\" compressableMimeType=\"application\/json\" maxThreads=\"17000\" maxConnections=\"5000\" acceptorThreadCount=\"12\" acceptCount=\"100\" keepAliveTimeout=\"10000\" maxKeepAliveRequests=\"500\" processorCache=\"17000\" /g' /usr/local/tomcat/conf/server.xml
sed -i 's/connectionTimeout=\"20000\"/connectionTimeout=\"60000\" /g' /usr/local/tomcat/conf/server.xml
/bin/sh /usr/local/tomcat/bin/catalina.sh run