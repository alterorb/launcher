#!/bin/bash

PACKR_VER=4.0.0

echo 'Jlinking the jvm...'

jlink.exe --no-header-files \
  --no-man-pages \
  --compress=2 \
  --strip-debug \
  --add-modules java.base,java.desktop,java.net.http,java.naming,jdk.crypto.ec \
  --output jdk-win64

if ! [ -f packr.jar ] ; then
    echo 'Downloading packr...'
    curl -o packr.jar https://github.com/runelite/packr/releases/download/${PACKR_VER}/packr-all-${PACKR_VER}.jar
fi

echo 'Packing...'
java -jar packr.jar \
  --platform windows64\
  --jdk jdk-win64 \
  --executable AlterOrb \
  --classpath $(find target/modules/ -path '*.jar' | tr '\n' ' ') \
  --mainclass net.alterorb.launcher.Bootstrap \
  --vmargs Xmx256m \
  --output native-win64

