#!/bin/bash

###############################################################################
######################           VARIABLES               ######################

SOURCE="."

#Check if there is exactly one command line argument
if [ ${#*} -ne 1 ]; then
    echo "usage: $0 <destination>";
    echo "<destination> can be \'localfolder\' OR";
    echo "<destination> can be \'user@host:folder\' ";
    exit 1;
fi

DESTINATION=$1 


###############################################################################
######################           FUNCTIONS               ######################

#### Performs command provided as first argument ####
#and if the command exits with a non-zero value then it exits with 
#value provided as second argument
function command() {
    echo $1
    $1
    if [ $? -ne 0 ]; then exit $2; fi
} 

function syncagentcode() {	
	command "scp -r $SOURCE/classes/gods $DESTINATION/gods/classes" $1
}

function syncjavapolicies() {
	command "scp -r $SOURCE/javapolicies $DESTINATION/gods/" $1
}

function synclib() {
	command "scp -r $SOURCE/lib $DESTINATION/gods/" $1
}

function syncconfig() {
	command "scp -r $SOURCE/config/ $DESTINATION/gods/config" $1
}

function syncscripts() {
	command "scp -r $SOURCE/scripts/ $DESTINATION/gods/scripts" $1
}

###############################################################################
#######################         THE ACTUAL SCRIPT         #####################

ant genstubs
syncagentcode 2
synclib 3
syncjavapolicies 4
syncconfig 5
syncscripts 6

exit 0;
