#!/bin/bash

DUMMY_HOME="/home/gods/dummy_app"
EXCLUDE=$0

#Kill all instances of dks
function killapp() {
	PIDS=`ps -ef | grep $1 | grep -v grep | grep -v $EXCLUDE | tr -s ' ' | cut -d' ' -f2`
	COUNT=0

	for PID in $PIDS; 
	do 
		echo $PID; 
		kill -15  $PID;
		if [ $? -eq 0 ]; then 
			let COUNT=$COUNT+1;
		fi 
	done

	echo "Killed $COUNT $1"
}

#Remove all logs
function clearlogs() {
	echo "Clearing logs..." 
	rm -f $DUMMY_HOME/dummy.log
}

#The Script
killapp dummy_app
clearlogs

exit 0
