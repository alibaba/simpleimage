#!/bin/bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." ; pwd`
IMG_HOME=$APP_HOME/src/test/resources/conf.test/simpleimage

IMGS=$(ls $IMG_HOME/testimg)
rm -rf $IMG_HOME/pressuretest/*

for i in $IMGS
do
        mv $IMG_HOME/testimg/$i $IMG_HOME/pressuretest/$i
        nohup sh $APP_HOME/bin/pressure.sh 16 99999999999 &
        echo "$i begin test"
        for (( j=1; j<=5; j=j+1 ))
        do
                sleep 1m
                USEDMEM=$(free -m |grep Mem|awk '{print $3}')
                PIDEXIST=$(ps axu|grep java|grep PressureTester|wc -l)
                if [ $PIDEXIST -eq 0 ]; then
                    echo "$i maybe seg error" >> $IMG_HOME/leakmem.log
                elif [ $USEDMEM -gt 7000 ]; then
                    echo "$i maybe leak mem" >> $IMG_HOME/leakmem.log
                else
                    echo "$i is ok" >> $IMG_HOME/leakmem.log
                fi
        done 
        killall -9 java
        killall -9 java
        echo "$i end test"
        mv $IMG_HOME/pressuretest/$i $IMG_HOME/testcomplete/$i
        rm -rf $IMG_HOME/pressuretest/*
done
echo "mem test finished" >> $IMG_HOME/leakmem.log