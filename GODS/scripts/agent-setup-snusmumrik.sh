#!/bin/bash

AGENT_HOME="/home/isthar/gods"

JAVA_PATH="/root/jre1.6.0_11/bin"

CLASSPATH="$AGENT_HOME/classes/:$AGENT_HOME/lib/log4j-1.2.14.jar"

CODEBASE="http://192.168.1.34/~jmcamacho/classes/"

JAVA_POLICY="$AGENT_HOME/javapolicies/java.policy4"

CCHOSTNAME="athena"

REMOTE_USER="isthar"

AGENT_CONFIG="$AGENT_HOME/config/gods.agent.config.xml"

AGENT_LOG="$AGENT_HOME/config/agent.log4j.config"