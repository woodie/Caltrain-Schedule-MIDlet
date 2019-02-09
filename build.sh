#!/bin/bash

JDK=/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
JME=/Applications/Java_ME_SDK_3.0.app/Contents/Resources

CLDC_1_1=$JME/lib/cldc_1.1.jar
MIDP_2_0=$JME/lib/midp_2.0.jar
WMA_CLASSES=$JME/lib/jsr205_2.0.jar
LWUIT_CLASSES=$JME/javamesdk/cldc/modules/libs/LWUIT.jar

APP=NextCaltrain
rm -rf tmpclasses classes dist
mkdir -p tmpclasses classes dist

$JDK/bin/javac -target 1.3 -source 1.3 \
    -bootclasspath $CLDC_1_1:$MIDP_2_0:$LWUIT_CLASSES \
    -d tmpclasses src/*.java

$JME/bin/preverify \
    -classpath $CLDC_1_1:$MIDP_2_0:$LWUIT_CLASSES \
    -d classes tmpclasses

$JDK/bin/jar cvmf MANIFEST.MF dist/$APP.jar -C classes . -C res .

cat MANIFEST.MF > dist/$APP.jad
echo "MIDlet-Jar-URL: $APP.jar
MIDlet-Jar-Size: $(stat -f %z dist/$APP.jar)" >> dist/$APP.jad
 
if [ -f dist/$APP.jad ]; then
  $JME/bin/emulator -Xdevice:DefaultCldcPhone1 \
      -Xdescriptor:dist/$APP.jad -Xdomain:maximum
fi

# java -jar $JME/javamesdk/cldc/modules/antcp/ResourceBuilder.jar
