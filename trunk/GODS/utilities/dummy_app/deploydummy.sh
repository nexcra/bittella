#!/bin/bash

SOURCE_PATH=/home/ozair/workspace/gods-www/utilities/dummy_app
DEST_PATH="/home/gods/dummy_app/"
REMOTE_USER="gods"

if [ ${#*} -ne 1 ]; then
    echo "usage: $0 <hostname>";
    exit 1;
fi

HOSTNAME=$1

function command() {
    $1
    if [ $? -ne 0 ]; then
    	echo "ERROR WHILE EXECUTING: $1" 
    	exit $2; 
    fi
}

function deploy(){
	command "rsync $SOURCE_PATH/$1 $REMOTE_USER@$HOSTNAME:$DEST_PATH/" $2
}

#The script
deploy dummy_app 2
deploy launchdummy.sh 3
deploy cleanup.sh 4