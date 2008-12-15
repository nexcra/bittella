#!/bin/bash

###############################################################################
######################           VARIABLES               ######################

SOURCE_PATH="/home/athena/src"
DESTINATION_PATH="/home/isthar"
REMOTE_USER="isthar"


#Check if there is exactly one command line argument
if [ ${#*} -ne 1 ]; then
    echo "usage: $0 <hostname>";
    exit 1;
fi

HOSTNAME=$1 


###############################################################################
######################           FUNCTIONS               ######################

#### Performs command provided as first argument ####
#and if the command exits with a non-zero value then it exits with 
#value provided as second argument
function command() {
    $1
    if [ $? -ne 0 ]; then exit $2; fi
} 

function syncagentcode() {
	#Later this can be modified to just copy files relevant to deployment on host machines	
	#command "echo rsync -r $SOURCE_PATH $REMOTE_USER@$HOSTNAME:$DESTINATION_PATH/gods-bin" $1
	command "rsync -r $SOURCE_PATH/classes/gods $REMOTE_USER@$HOSTNAME:$DESTINATION_PATH/gods/classes/" $1
}

function syncjavapolicies() {
	command "rsync -r $SOURCE_PATH/javapolicies $REMOTE_USER@$HOSTNAME:$DESTINATION_PATH/gods/" $1
}

function synclib() {
	command "rsync -r $SOURCE_PATH/lib $REMOTE_USER@$HOSTNAME:$DESTINATION_PATH/gods/" $1
}

function syncconfig() {
	command "rsync -r $SOURCE_PATH/config/agent.config/ $REMOTE_USER@$HOSTNAME:$DESTINATION_PATH/gods/config" $1
}

function syncscripts() {
	command "rsync -r $SOURCE_PATH/scripts/agent.scripts/ $REMOTE_USER@$HOSTNAME:$DESTINATION_PATH/gods/scripts" $1
}

###############################################################################
#######################         THE ACTUAL SCRIPT         #####################

syncagentcode 2
synclib 3
syncjavapolicies 4
syncconfig 5
syncscripts 6

#exit 0