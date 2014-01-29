#!/bin/bash

#mvn clean assembly:assembly

cd target/integration-0.0.*-with-dependencies.dir/integration-0.0.*/

export MULE_LIB=.
for a in `find . -type f -name '*.jar'`; do export MULE_LIB=$a:$MULE_LIB; done
export MULE_HOME=/Users/vagifjalilov/Desktop/Classroom/Mule/mule-1.4.3
mule -debug -config mule-config.xml > /tmp/integration-mule.log 2>&1 & echo $! > integration.pid
tail -f /tmp/integration-mule.log
kill `cat integration.pid`
kill `cat /Users/vagifjalilov/Desktop/Classroom/mule-1.4.3/bin/./.mule.pid`
cd ../../../
