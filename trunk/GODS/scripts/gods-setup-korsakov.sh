#!/bin/bash

GODS_HOME="/var/www/gods"

CLASSPATH="$GODS_HOME/classes/:$GODS_HOME/lib/log4j-1.2.14.jar:$GODS_HOME/lib/junit-4.1.jar"

HOSTNAME="korsakov.sics.se"

CODEBASE="http://korsakov/gods/classes/"

POLICY="$GODS_HOME/javapolicies/java.policy4"

LOG_CONFIG="$GODS_HOME/config/log4j.config"

STARTUP="$GODS_HOME/config/gods.config.xml"