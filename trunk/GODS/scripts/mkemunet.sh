#!/bin/bash

if [ ${#*} -ne 1 ]; then
    echo "usage: $0 <model_dir>";
    exit 1;
fi

MODEL_DIR=$1

#Check if it exists
if [ ! -e "$MODEL_DIR" ]; then
    echo "$1 does not exist."; 
    exit 2;
fi

#Check if it is a directory
if [ ! -d "$MODEL_DIR" ]; then
  	echo "$1 is not a directory.";
  	exit 3;
fi	

function command() {
    $1
    if [ $? -ne 0 ]; then
    	echo "ERROR WHILE EXECUTING: $1" 
    	exit $2; 
    fi
} 

#Checks if only one 'graph' file exists in specified directory.
function getgraphfile() {
	
	echo "Checking 'graph' file"
	GRAPH_FILES=$(ls *.graph) #GRAPH_FILES=$(ls $MODEL_NAME/*.graph)
	COUNTGF=0
		
	for GRAPH_FILE in $GRAPH_FILES;
	do
		let COUNTGF=$COUNTGF+1;
		
		if [ "$COUNTGF" -gt 1 ]; then
			echo "There should be only 1 'graph' file in the specified folder.";
			echo "Remove $GRAPH_FILE or move it to another folder"
			cd $WORKING_DIR
			exit $1
		fi
	done
	echo "$GRAPH_FILE" 
}

#Checks if only one 'machines' file exists in specified directory.
function getmachinesfile() {

	echo "Checking 'machines' file"
	MACHINES_FILES=$(ls *.machines) #MACHINES_FILES=$(ls $MODEL_NAME/*.machines)
	COUNTMF=0
		
	for MACHINES_FILE in $MACHINES_FILES;
	do
		let COUNTMF=$COUNTMF+1;
		
		if [ "$COUNTMF" -gt 1 ]; then
			echo "There should be only 1 'machines' file in the specified folder.";
			echo "Remove $MACHINES_FILE or move it to another folder"
			cd $WORKING_DIR
			exit $1
		fi
	done
	echo "$MACHINES_FILE" 
}

### THE SCRIPT ###
WORKING_DIR=$(pwd)
cd $MODEL_DIR
echo $(pwd)

GRAPH_FILE=""
getgraphfile 4

MACHINES_FILE=""
getmachinesfile 5

filename=${GRAPH_FILE%".graph"}
ROUTE_FILE="$filename.route"
MODEL_FILE="$filename.model"

#echo "Generating $ROUTE_FILE ..."
allpairs $GRAPH_FILE > $ROUTE_FILE

#echo "Generating $MODEL_FILE ..."
mkmodel $GRAPH_FILE $MACHINES_FILE > $MODEL_FILE

cd $WORKING_DIR
exit 0