#/bin/bash

jlink \
  --no-header-files \
  --no-man-pages \
  --compress=2 \
  --strip-debug \
  --add-modules java.base,java.desktop,java.net.http,java.naming,jdk.crypto.ec \
  --output jdk-linux64

jpackage \
  --input target/modules \
  --name alterorb \
  --type deb \
  --runtime-image jdk-linux64 \
  --java-options -Xmx256m \
  --main-jar alterorb-launcher-3.0.0.jar \
  --main-class net.alterorb.launcher.Bootstrap \
  --linux-shortcut
