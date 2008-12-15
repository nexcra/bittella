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
MODEL_DIR=${MODEL_DIR%/} #Remove trailing / from path if it exists
WORKING_DIR=$(pwd)

if [ ! -e "$MODEL_DIR" ]; then
    echo "$MODEL_DIR does not exist."; 
    exit 2;
fi

#Check if it is a directory
if [ ! -d "$MODEL_DIR" ]; then
  	echo "$MODEL_DIR is not a directory.";
  	exit 3;
fi

function command() {
    $1
    if [ $? -ne 0 ]; then
    	echo "ERROR WHILE EXECUTING: $1" 
    	exit $2; 
    fi
} 

function rcommand() {
    ssh $REMOTE_USER@$2 "nohup $1 >gods/logs/$(basename $0).log 2>&1 &"
    if [ $? -ne 0 ]; then
    	echo "ERROR WHILE EXECUTING: ssh $REMOTE_USER@$2 \"nohup $1 >gods/logs/$(basename $0).log 2>&1 & \"" 
    	exit $3; 
    fi
}

#Reads hosts from specified file
function readhosts() {

	#Check if specified host file exists
	if [ ! -e "$HOSTS_FILE" ]; then
	    echo "$HOSTS_FILE does not exist."; 
	    exit $1;
	fi
    
    n=0
    #Read hosts from HOSTSFILE one by one separated by \n
    for host in $(awk 'BEGIN{FS="\n"}{print $1}' < "$HOSTS_FILE" )
    do
		HOSTS[$n]=$host
		let "n += 1"
    done  
    
    #NOOFHOSTS is equal to value of n at this point
    let NOOFHOSTS=n
    echo "No of hosts are: " $NOOFHOSTS

    #Display output
    for((i=0; i<$NOOFHOSTS; i++))
    do
		echo ${HOSTS[$i]}
    done
}

#Extracts model name from given model directory path
#This function is not being used since this functionality is provided by bash 
#utility 'basename'
function getmodelname() {

	MODEL_NAME=$MODEL_DIR
	
	#Remove / from back
	MODEL_NAME=${MODEL_NAME%/} #echo $MODEL_NAME
	#Remove / from front
	MODEL_NAME=${MODEL_NAME#/} #echo $MODEL_NAME
	
	#Check for any /'s in path now
	index=$(expr index $MODEL_NAME /) #echo $index
	#If found, append the path to a slash again
	if [ $index -ne "0" ]; then
		MODEL_NAME="/$MODEL_NAME" #echo $MODEL_NAME
	fi
	
	#Now remove everything between first and last /, so that we get the model name
	MODEL_NAME=${MODEL_NAME##/*/} #echo $DEST_MODEL_DIR
}

#Checks if model file exists in the specified folder
function getmodelfile() {
	#echo "Checking 'model' file"
	cd $MODEL_DIR
	MODEL_FILES=$(ls *.model)
	COUNTMF=0
		
	for MODEL_FILE in $MODEL_FILES;
	do
		let COUNTMF=$COUNTMF+1;
		
		if [ "$COUNTMF" -gt 1 ]; then
			echo "There should be only 1 'model' file in the specified folder.";
			echo "Remove $MODEL_FILE or move it to another folder"
			cd $WORKING_DIR
			exit $1
		fi 
	done
	cd $WORKING_DIR
	echo "$MODEL_FILE"
}

#Checks if route file exists in the specified folder
function getroutefile() {
	#echo "Checking 'route' file"
	cd $MODEL_DIR
	ROUTE_FILES=$(ls *.route)
	COUNTRF=0
	
	for ROUTE_FILE in $ROUTE_FILES;
	do
		let COUNTRF=$COUNTRF+1;
		
		if [ "$COUNTRF" -gt 1 ]; then
			echo "There should be only 1 'route' file in the specified folder.";
			echo "Remove $ROUTE_FILE or move it to another folder"
			cd $WORKING_DIR
			exit $1
		fi
	done
	cd $WORKING_DIR
	echo "$ROUTE_FILE"
}

#Copy files to hostmachine
function copymodel() {
	command "rsync $MODEL_DIR/$MODEL_FILE $REMOTE_USER@$1:$DEST_MODEL_DIR/" $2
	command "rsync $MODEL_DIR/$ROUTE_FILE $REMOTE_USER@$1:$DEST_MODEL_DIR/" $2
}

#deployhost on hostmachine
function deploymodel() {
	echo "Deploying Model on Host $1..."
	rcommand "deployhost $DEST_MODEL_DIR/$MODEL_FILE $DEST_MODEL_DIR/$ROUTE_FILE" $1 $2
}

#Deployhost on multiple machines
function deploymodelonhosts() {
    for ((i=0; i<NOOFHOSTS; i++))
    do
		copymodel ${HOSTS[i]} $1
		deploymodel ${HOSTS[i]} $1
    done
}

### THE SCRIPT ###
MODEL_NAME=$(basename $MODEL_DIR)
echo "$MODEL_NAME is being deployed"
DEST_MODEL_DIR="$DESTINATION_PATH/$MODEL_NAME"
#echo $DEST_MODEL_DIR

getmodelfile 4
#echo "Model file is $MODEL_FILE"

getroutefile 5
#echo "Route file is $ROUTE_FILE"

NOOFHOSTS=0

if [ "$HOST_NAME" != "" ]; then
	copymodel $HOST_NAME 6;
	deploymodel $HOST_NAME 7;
else
	readhosts 8;
	deploymodelonhosts $HOSTS 9;
fi

exit 0