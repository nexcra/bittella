#!/bin/bash

/home/gods/gods/scripts/kill-all.sh GodsAgent
/home/gods/gods/scripts/kill-all.sh dks

rm -f /home/gods/dks/logs/*.log
rm -f /home/gods/gods/logs/*.log
rm -f /home/gods/gods/logs/agent/*.log
rm -f /home/gods/gods/logs/agent/*.log.*
