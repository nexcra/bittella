#!/bin/bash

LOGFILE=/home/gods/gods/logs/$(basename $0).log
DATE="01/01/1970"

sudo date -u -s "$DATE"
#sudo date -u -s \"$DATE\" > $LOGFILE 2>&1
#date +%s.%N
	
exit 0