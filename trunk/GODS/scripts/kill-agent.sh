#!/bin/bash

###################################################################################################
###########################           FUNCTIONS               #####################################

#### Performs command provided as first argument ####
#and if the command exits with a non-zero value then it exits with 
#value provided as second argument
function command() {
    $1
    if [ $? -ne 0 ]; then exit $2; fi
}

AGENT_PID=`ps -ef | grep java | grep GodsAgent | tr -s ' ' | cut -d' ' -f2`

command "kill -9 $AGENT_PID"

exit 0
