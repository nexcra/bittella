#!/bin/bash

GODS_HOME="/var/www/gods"

CLASSPATH="$GODS_HOME/classes/:$GODS_HOME/lib/log4j-1.2.14.jar:$GODS_HOME/lib/junit-4.1.jar"

HOSTNAME="small.sics.se"

CODEBASE="http://small.sics.se/gods/classes/"

POLICY="$GODS_HOME/javapolicies/java.policy4"

LOG_CONFIG="$GODS_HOME/config/log4j.properties"

STARTUP="$GODS_HOME/config/gods12.xml"
