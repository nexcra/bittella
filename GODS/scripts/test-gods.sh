#!/bin/bash
JAVA_PATH="jre1.5.0_08/bin"

export PATH=/var/www/jre1.5.0_08/bin/:$PATH
 
#Check if there are any command line argument
if [ ${#*} -lt 1 ]; then
	echo "usage: $0 <gods-setup.sh> [TRUE/false]";
	exit 1;
fi

if [ ${#*} -lt 2 ]; then	
	AUTO_SETUP="true";
else
	AUTO_SETUP=$1;
fi

source $1

#Check if rmiregistry is already running, else start rmiregistry
#ps -C rmiregistry
#if [ $? -ne 0 ]; then
#	echo "Starting rmiregistry..." 
#	rmiregistry & 
#else
#	echo "rmiregistry already started"
#fi

echo "java -Djava.rmi.server.codebase=$CODEBASE -Djava.rmi.server.hostname=$HOSTNAME -Djava.security.policy=$POLICY -Dorg.apache.log4j.config.file=$LOG_CONFIG -Dgods.home=$GODS_HOME -classpath $CLASSPATH gods.tests.GodsTestSuite -gods $CONFIG_FILE $AUTO_SETUP"
java -Djava.rmi.server.codebase=$CODEBASE -Djava.rmi.server.hostname=$HOSTNAME -Djava.security.policy=$POLICY -Dorg.apache.log4j.config.file=$LOG_CONFIG -Dgods.home=$GODS_HOME -classpath $CLASSPATH gods.tests.GodsTestSuite -gods $CONFIG_FILE $AUTO_SETUP

exit 0