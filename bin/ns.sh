#!/bin/bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." ; pwd`

JAVA_MEM_OPTS=" -server -Xmx4g -Xms4g -Xmn1024m -XX:MaxPermSize=128m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+AggressiveOpts -XX:+UseBiasedLocking "

JAVA_CP=$APP_HOME/target/simpleimage-1.1.1.jar:$APP_HOME/lib/*:$APP_HOME/target/test-classes

taskset -c 0-6   java $JAVA_MEM_OPTS -classpath $JAVA_CP com.alibaba.simpleimage.codec.jpeg.NumberSpeedTest $1 $2