#!/bin/bash

ME2=/Library/Java/microemulator-2.0.4
JDK=/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
JME=/Applications/Java_ME_SDK_3.0.app/Contents/Resources

CLDC_1_1=$JME/lib/cldc_1.1.jar
MIDP_2_0=$JME/lib/midp_2.0.jar

DEVICE=$ME2/devices/microemu-device-large.jar
APP_NAME=NextCaltrain

VER=$(grep MIDlet-Version MANIFEST.MF | cut -d' ' -f 2)
APP=$APP_NAME-$VER

rm -rf tmpclasses classes dist
mkdir -p tmpclasses classes dist

$JDK/bin/javac -target 1.3 -source 1.3 \
    -bootclasspath $CLDC_1_1:$MIDP_2_0 \
    -d tmpclasses src/*.java

$JME/bin/preverify \
    -classpath $CLDC_1_1:$MIDP_2_0 \
    -d classes tmpclasses

$JDK/bin/jar cvmf MANIFEST.MF dist/$APP.jar -C classes . -C res .

cat MANIFEST.MF > dist/$APP.jad
echo "MIDlet-Jar-URL: $APP.jar
MIDlet-Jar-Size: $(stat -f %z dist/$APP.jar)" >> dist/$APP.jad

cd dist
zip -r $APP.zip $APP.jad $APP.jar
cd ..

# https://code.google.com/archive/p/microemu/
if [ -f dist/$APP.jad ]; then
  $JDK/bin/java -cp $ME2/microemulator.jar:$DEVICE:dist/$APP.jar \
      org.microemu.app.Main $APP_NAME --propertiesjad dist/$APP.jad \
      --device org/microemu/device/large/device.xml
fi
