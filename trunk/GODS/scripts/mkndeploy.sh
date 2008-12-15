#!/bin/bash

REMOTE_USER="gods"
DESTINATION_PATH="/home/$REMOTE_USER/models"

if [ ${#*} -lt 3 ]; then
	echo
    echo "usage: $0 -[h|f] <parameter> <model_dir>";
    echo
    echo "-h 	specifies that the following parameter is THE HOST on which the" 
    echo "	network is to be deployed";
    echo
    echo "-f 	specifies that the following parameter is a FILE containing LIST"
    echo "	OF HOSTS on which the network is to be deployed";
    echo
    exit 1;
fi


if getopts ":h:f:" Option
then
  case $Option in
    h ) HOST_NAME=$OPTARG; echo "Deploy on $HOST_NAME";;
    f ) HOSTS_FILE=$OPTARG; echo "Deploy on all hosts in $HOSTS_FILE";;
  esac
fi

MODEL_DIR=$3

function command() {
    $1
    if [ "$?" -ne "0" ]; then
    	echo "ERROR WHILE EXECUTING: $1" 
    	exit $2; 
    fi
} 

### THE SCRIPT ###
./mkemunet.sh $MODEL_DIR
./deployemunet.sh $*

exit 0