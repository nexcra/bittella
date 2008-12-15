#!/bin/bash

if [ ${#*} -ne 1 ]; then
    CLASS_PATH="/home/ozair/workspace/Programs/JavaRmiTool/classes"
else
	CLASS_PATH=$1
fi

echo $CLASS_PATH

java -cp $CLASS_PATH org.kth.java.rmi.tools.JavaRmiTool