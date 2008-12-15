#!/bin/bash

REMOTE_USER="gods"

if [ ${#*} -ne 3 ]; then
    echo "usage: $(basename $0) <remotelogfile> <centralizedfile> <hostsfile>";
    exit 1;
fi

LOGFILE=$1
CENTRALIZEDFILE=$2
HOSTS_FILE=$3

#Executes a command, and if it exits from the script with the 
#exit value of the failed command
function command() {
    $1
    if [ $? -ne 0 ]; then
    	echo "ERROR WHILE EXECUTING: $1" 
    	exit $2; 
    fi
}

#Executes a remote command
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

#Checks if the specified file exits on each host
#function checkFile() {
#	for ((i=0; i<NOOFHOSTS; i++))
#    do
#		rcommand "if [ -e \"$LOGFILE\" ]; then; fi" ${HOSTS[i]} $1
#    done
#}

function gatherlogs() {
	for ((i=0; i<NOOFHOSTS; i++))
    do
		command "scp $REMOTE_USER@${HOSTS[i]}:$LOGFILE tempexplog${HOSTS[i]}" $1
    done
}

function echologs() {
	for ((i=0; i<NOOFHOSTS; i++))
    do
		command "cat tempexplog${HOSTS[i]}" $1
		#command "echo =========================================="
    done
}

function combinelogs() {
	for ((i=0; i<NOOFHOSTS; i++))
    do
		#command "cat templog${HOSTS[i]} >> templogCENTRALIZED" $1
		cat tempexplog${HOSTS[i]} >> tempexplogCENTRALIZED
    done
}

NOOFHOSTS=0
readhosts 2

#checkFile 3

gatherlogs 4

#echologs 5

combinelogs 6

#command "cat tempexplogCENTRALIZED"
#command "echo =========================================="

sort -n tempexplogCENTRALIZED > $CENTRALIZEDFILE

#command "cat $CENTRALIZEDFILE"

command "rm tempexplog*"

exit 0