#!/bin/bash

PID="";
KILLSIG=12;

if [ ${#*} -ne 1 ]; then
    echo "usage: $0 <processId>";
    exit 1;
else
    PID=$1;    
fi

kill -$KILLSIG $PID > gods/logs/$(basename $0).log 2>&1 

exit $?;