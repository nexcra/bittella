#!/bin/bash 

if [ ${#*} -lt 2 ]; then
    echo "usage:launch-application.sh <launch-app> [app-args...] <sourceIp>";
    exit 0;    
fi

SOURCE_IP=""
for ARG in $@
do
	SOURCE_IP=$ARG
done

export LD_PRELOAD=/usr/local/lib/libipaddr.so
export SRCIP=$SOURCE_IP
source $*

exit $?