#!/bin/bash

REMOTE_USER="gods"

for arg in $*
do
	echo $arg
done

if [ ${#*} -ne 1 ]; then
    echo "usage: $(basename $0) <hostsfile>";
    exit 1;
fi

DATE="01/01/1970"
HOSTS_FILE=$1

#Check if it exists
if [ ! -e "$HOSTS_FILE" ]; then
    echo "$HOSTS_FILE does not exist."; 
    exit 2;
fi

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

#deployhost on hostmachine
function setclock() {
	rcommand "sudo date -s \"$DATE\"" $1 $2
}

#Deployhost on multiple machines
function syncclocks() {
    for ((i=0; i<NOOFHOSTS; i++))
    do
		setclock ${HOSTS[i]} $1
    done
}

### THE SCRIPT ###

#NOOFHOSTS=0 
#echo "Deploying on all host in $HOSTS_FILE"
#readhosts 3;

HOSTSCNT=`wc -l $HOSTS_FILE | cut -f1 -d ' '`
pssh -h $HOSTS_FILE -p $HOSTSCNT -l $REMOTE_USER "sudo date -u -s \"$DATE\""
#syncclocks $HOSTS 4;

exit 0