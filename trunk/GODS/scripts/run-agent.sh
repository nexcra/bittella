#!/bin/bash
#GODS: Script for running up agent on a host
#This script will be executed from the ControlCenter 
#for each host separately, 

#The script will run rmiregistry and agent on each host

#The script name should be mentioned in GODS startup Properties file
#Error Codes should be documented in a separate Java XML Properties file, which
#is required by the GODS Bootstrapper to inform problems in script execution

###############################################################################
######################           VARIABLES               ######################

#Check if there is exactly one command line argument
if [ ${#*} -ne 2 ]; then
	echo
    echo "usage: $0 <gods-agent-startup.sh> <hostname>";
    exit 1;
fi

source $1
HOSTNAME=$2

###############################################################################


###############################################################################
######################           FUNCTIONS               ######################

#### Performs command provided as first argument on the specified host ####
#and if the command exits with a non-zero value then it exits with 
#value provided as second argument. It logs the output with $scriptname.log
function rcommand() {
    ssh $REMOTE_USER@$2 "nohup $1 >gods/logs/$(basename $0).log 2>&1 &"
    if [ $? -ne 0 ]; then
    	echo "ERROR WHILE EXECUTING: ssh $REMOTE_USER@$2 \"nohup $1 >gods/logs/$(basename $0).log 2>&1 & \"" 
    	exit $3; 
    fi
}

#### Function to run agents on host ####
#It starts Agent on each host using the variables specified for remote execution.
function execagent() {
    rcommand "$JAVA_PATH/java -Djava.rmi.server.codebase=$CODEBASE -Djava.security.policy=$JAVA_POLICY -Dorg.apache.log4j.config.file=$AGENT_LOG -Dgods.agent.home=$AGENT_HOME -classpath $CLASSPATH gods.agent.GodsAgent $CCHOSTNAME $AGENT_CONFIG" $HOSTNAME $1
}
###############################################################################



###############################################################################
#######################         THE ACTUAL SCRIPT         #####################

execagent 3

exit 0
