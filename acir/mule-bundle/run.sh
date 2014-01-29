#!/bin/bash

# set this to where to ACIr installation
ACIR_HOME=/opt/clients/rivet/acir-4.1.2-E-0/

export MULE_HOME=$ACIR_HOME

if [ "$1 " == "start " ]; then
   $ACIR_HOME/bin/mule -config mule-config.xml 

elif [ "$1 " == "stop " ]; then
   $ACIR_HOME/bin/mule stop
   
else
    echo "'start' or 'stop' required as first argument!"
fi
