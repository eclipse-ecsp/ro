#!/bin/bash

echo "Starting RO stream processor"

exec java ${JVM_OPTS} -DLOG_FILE_LOCATION=${LOG_FILE_LOCATION} -DLOG_LEVEL=${LOG_LEVEL} -DAPPENDER=$LOG_APPENDER -cp /opt/ro-sp/ro-sp.jar org.eclipse.ecsp.analytics.stream.base.Launcher /opt/ro-sp/application.properties