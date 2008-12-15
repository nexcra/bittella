#!/bin/bash

# NOT BEING USED CURRENTLY 

if [ ${#*} -ne 2 ]; then
    echo "usage: $(basename $0) <remotelogfile> <hostname>";
    exit 1;
fi