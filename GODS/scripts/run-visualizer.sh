#!/bin/bash

#Check if there are any command line argument
if [ ${#*} -lt 1 ]; then
	echo "usage: $0 <gods-setup.sh>";
	exit 1;
fi

source $1

echo "java -Djava.rmi.server.codebase=$CODEBASE -Djava.rmi.server.hostname=$HOSTNAME -Djava.security.policy=$POLICY -Dorg.apache.log4j.config.file=$LOG_CONFIG -Dgods.home=$GODS_HOME -classpath $CLASSPATH gods.visualizer.Visualizer $HOSTNAME"
java -Djava.rmi.server.codebase=$CODEBASE -Djava.rmi.server.hostname=$HOSTNAME -Djava.security.policy=$POLICY -Dorg.apache.log4j.config.file=$LOG_CONFIG -Dgods.home=$GODS_HOME -classpath $CLASSPATH gods.visualizer.Visualizer $HOSTNAME

exit 0