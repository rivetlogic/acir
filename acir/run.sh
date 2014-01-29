#!/bin/bash

MVN='mvn'

if [ "$1 " == "-c " ]; then
    MVN_PARAMS="clean assembly:assembly"    
    
    if [ "$2 " == "-o " ]; then
        MVN_PARAMS="--offline "$MVN_PARAMS
    fi
    
    echo "Running $MVN $MVN_PARAMS"
    
    $MVN $MVN_PARAMS
fi

MULE_VER=mule-standalone-2.2.1
export MULE_HOME=/Users/sokeeffe/Documents/Projects/Mule/$MULE_VER

if [ `echo $PATH | grep -F '$MULE_VER' | wc -l` == 0 ];then
    PATH=$PATH:$MULE_HOME/bin
fi

cd target/integration-*-with-dependencies.dir/integration-*/

#for a in `find . -type f -name '*.jar'`; do export MULE_LIB=$a:$MULE_LIB; done

MULE_LIB=integration-0.0.8.jar:$MULE_LIB;
MULE_LIB=log4j.properties:$MULE_LIB;
MULE_LIB=lib/cma-api-3.1.0-E-1.jar:$MULE_LIB;
MULE_LIB=lib/cma-impl-3.1.0-E-1.jar:$MULE_LIB;
MULE_LIB=lib/alfresco-core-3.1.0.jar:$MULE_LIB;
MULE_LIB=lib/alfresco-repository-3.1.0.jar:$MULE_LIB;
MULE_LIB=lib/acegi-security-0.8.3.jar:$MULE_LIB;
MULE_LIB=lib/activation.osgi-1.1.jar:$MULE_LIB;
MULE_LIB=lib/base64-1.0.0.jar:$MULE_LIB;
MULE_LIB=lib/castor-1.2.jar:$MULE_LIB;
MULE_LIB=lib/commons-httpclient-3.1.jar:$MULE_LIB;
MULE_LIB=lib/commons-beanutils.osgi-1.7.0.jar:$MULE_LIB;
MULE_LIB=lib/commons-cli.osgi-1.0.jar:$MULE_LIB;
MULE_LIB=lib/commons-io.osgi-1.3.1.jar:$MULE_LIB;
MULE_LIB=lib/commons-codec-1.2.jar:$MULE_LIB;
MULE_LIB=lib/commons-lang.osgi-2.4.jar:$MULE_LIB;
MULE_LIB=lib/commons-logging-1.0.4.jar:$MULE_LIB;
MULE_LIB=lib/commons-pool-1.4.jar:$MULE_LIB;
MULE_LIB=lib/dom4j-1.6.1.jar:$MULE_LIB;
#MULE_LIB=:$MULE_LIB;

export MULE_LIB=.:$MULE_LIB

echo $MULE_LIB

$MULE_HOME/bin/mule -debug -config mule-config.xml 

cd ../../../
