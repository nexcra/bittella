#!/bin/bash

GODS_HOME="/home/ozair/workspace/gods-www"

CLASSPATH="$GODS_HOME/gods.jar:$GODS_HOME/lib/log4j-1.2.14.jar"
LOG_CONFIG="$GODS_HOME/config/log4j.config"
ARG_GENS_FILE="$GODS_HOME/config/arggens.config/gods.churn.arggens.xml"

function usage() {
	echo "usage: $(basename $0) <exp.gen.params> [arggens.xml]"
	echo
	echo "For example expgens.config/dummy.exp.xml"
}

if [ ${#*} -eq 0 ]; then
	usage
	exit 1;
fi

if [ ${#*} -eq 1 ]; then
	PARAMS_FILE=$1
fi

if [ ${#*} -eq 2 ]; then
	PARAMS_FILE=$1
	ARG_GENS_FILE=$2
else
	usage
fi

echo $PARAMS_FILE
echo $ARG_GENS_FILE

java -Dorg.apache.log4j.config.file=$LOG_CONFIG -cp $CLASSPATH gods.experiment.ExperimentGenerator $PARAMS_FILE $ARG_GENS_FILE