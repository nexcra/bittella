#!/bin/bash

AGENT_HOME="/home/gods/gods"

JAVA_PATH="jre1.5.0_08/bin"

CLASSPATH="$AGENT_HOME/classes/:$AGENT_HOME/lib/log4j-1.2.14.jar"

CODEBASE="http://small.sics.se/gods/classes/"

JAVA_POLICY="$AGENT_HOME/javapolicies/java.policy4"

CCHOSTNAME="small.sics.se"

REMOTE_USER="gods"

AGENT_CONFIG="$AGENT_HOME/config/gods.agent.config.xml"

AGENT_LOG="$AGENT_HOME/config/agent.log4j.config"