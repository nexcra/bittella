#!/bin/bash

if [ ${#*} -ne 1 ]; then
	echo "usage: $0 <app-name>"
	exit 1;
fi

PIDS=`ps -ef | grep $1 | tr -s ' ' | cut -d' ' -f2`
COUNT=0

for PID in $PIDS; 
	do 
		echo $PID; 
		kill -15  $PID; 
		let COUNT=$COUNT+1; 
	done

echo "Killed $COUNT apps"