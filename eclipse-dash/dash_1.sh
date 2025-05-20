#!/bin/bash

echo 'Cleaning previously created files in parent module'
rm -f ./MAVEN_DEPENDENCIES
rm -f ./HARMAN_DEPENDENCIES
rm -f ./temp.deps
rm -f ./github.deps
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

input_file=$PWD/maven.deps
output_file=$PWD/github.deps
temp_file=$PWD/temp.deps

# Ensure input file exists
if [ ! -f "$input_file" ]; then
    echo "Input file $input_file not found."
    exit 1
fi

# Remove output file if it already exists
if [ -f "$output_file" ]; then
    rm "$output_file"
fi

# Remove temp file if it already exists
if [ -f "$temp_file" ]; then
    rm "$temp_file"
fi

# Loop through each line in the input file
while IFS= read -r line; do
    # Check if the line contains "com.harman"
    if echo "$line" | grep -q "com.harman"; then
        # Append the line to the output file
        echo "$line" >> "$output_file"
    else
        # Append the line to a temporary file
        echo "$line" >> "$temp_file"
    fi
done < "$input_file"

# Replace the original input file with the temporary file
chmod +x "$temp_file"
chmod +x "$input_file"
chmod +x "$output_file"
mv "$temp_file" "$input_file"
echo "Lines containing HARMAN Github package dependencies have been copied to $output_file and removed from $input_file."

# Remove temp file if it already exists
if [ -f "$temp_file" ]; then
    rm "$temp_file"
fi

echo 'Running dash tool for all third-party maven dependencies license compliance'
chmod +x $PWD/eclipse-dash/dash.jar $PWD/maven.deps
java -jar $PWD/eclipse-dash/dash.jar $PWD/maven.deps -summary $PWD/MAVEN_DEPENDENCIES
chmod +x $PWD/MAVEN_DEPENDENCIES
echo "MAVEN_DEPENDENCIES file created."

echo 'Running dash tool for all HARMAN dependencies license compliance'
chmod +x $PWD/eclipse-dash/dash.jar $PWD/github.deps
java -jar $PWD/eclipse-dash/dash.jar $PWD/github.deps -summary $PWD/HARMAN_DEPENDENCIES
chmod +x $PWD/HARMAN_DEPENDENCIES
echo "HARMAN_DEPENDENCIES file created."

fail=false

echo 'Checking for failed maven dependencies'
if  grep -q "restricted" "$PWD/MAVEN_DEPENDENCIES"; then
    echo "Failed third-party dependencies found."
    fail=true
else
    echo "All third-party dependencies are compliant."
fi

echo 'Checking for failed github dependencies'
if  grep -q "restricted" "$PWD/HARMAN_DEPENDENCIES"; then
    echo "Failed HARMAN dependencies found."
    fail=true
else
    echo "All HARMAN dependencies are compliant."
fi

if [ "$fail" == true ]; then
    echo "Failed dependencies found. Exiting with status 1."
    exit 1
fi
