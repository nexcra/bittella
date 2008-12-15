#!/bin/sh

DUMMY_HOME=/home/gods/dummy_app
LOGFILE="$DUMMY_HOME/dummy.log"
OUTFILE="/dev/null"
STOPSIG=10
KILLSIG=12

if [ ${#*} -lt 1 ]; then
    echo "usage: $0 <nodeid>";
    exit 0;    
fi

NODEID=$1

function command() { 
    $1
    if [ $? -ne 0 ]; then
    	echo "ERROR WHILE EXECUTING: $1" 
    	exit $2; 
    fi
}

ARGS="$NODEID $LOGFILE $STOPSIG $KILLSIG"

$DUMMY_HOME/dummy_app $ARGS > $OUTFILE 2>&1 &

EXIT_VALUE=$?

PID=$!

if [ $EXIT_VALUE -eq 0 ]; then
	echo -n $PID
	exit 0;
else
	cat $LOGFILE
#	echo "EXIT_VALUE="$EXIT_VALUE
	exit $EXIT_VALUE;
fi