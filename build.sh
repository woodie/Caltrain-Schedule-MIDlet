#!/bin/bash

JDK=/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
JME=/Applications/Java_ME_SDK_3.0.app/Contents/Resources

APP=CaltrainSchedule
mkdir tmpclasses classes

$JDK/bin/javac -target 1.3 -source 1.3 \
    -bootclasspath $JME/lib/cldc_1.1.jar:$JME/lib/midp_2.0.jar \
    -d tmpclasses src/*.java

$JME/bin/preverify \
    -classpath $JME/lib/cldc_1.1.jar:$JME/lib/midp_2.0.jar \
    -d classes tmpclasses

$JDK/bin/jar cvmf MANIFEST.MF dist/$APP.jar -C classes . -C images .

cat MANIFEST.MF > dist/$APP.jad
echo "MIDlet-Jar-URL: $APP.jar
MIDlet-Jar-Size: $(stat -f %z dist/$APP.jar)" >> dist/$APP.jad

$JDK/bin/java -jar /Library/Java/mpp-sdk/player.jar dist/$APP.jad
