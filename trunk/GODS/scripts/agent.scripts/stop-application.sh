#!/bin/bash

PID="";
STOPSIG=10
OUTFILE="stops.log"

if [ ${#*} -ne 1 ]; then
    echo "usage:stop-application.sh <processId>";
    exit 1;
else
    PID=$1;    
fi

kill -$STOPSIG $PID > gods/logs/$(basename $0).log 2>&1

exit $?;