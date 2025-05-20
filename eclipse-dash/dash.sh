#!/bin/bash

echo 'Cleaning previously created files in parent module'
rm -f ./NOTICE.md
rm -f ./maven.deps

dependencies="$PWD/maven.deps"

echo 'Checking dependencies'
chmod +x ./settings.xml
mvn clean verify dependency:list -U -s ./settings.xml -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -DappendOutput=true -DoutputFile="$dependencies"
rc=$?
if [ $rc -ne 0 ] ; then
  echo "Could not collect all dependencies, exit code [$rc]"
  exit $rc
fi
echo "Maven dependency extraction and file creation complete. $dependencies created."

echo 'Running dash tool for all dependencies license compliance'
chmod +x $PWD/eclipse-dash/dash.jar $PWD/maven.deps
java -jar $PWD/eclipse-dash/dash.jar $PWD/maven.deps -summary $PWD/NOTICE.md
chmod +x $PWD/NOTICE.md
echo "NOTICE.md file created."

echo 'Checking for failed dependencies'
if  grep -q "restricted" "$PWD/NOTICE.md"; then
    echo "Failed dependencies found. Exiting with status 1."
    exit 1
else
    echo "All dependencies are compliant."
fi
