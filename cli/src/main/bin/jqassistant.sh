#!/bin/sh
if [ -z "$JQASSISTANT_HOME" ] ; then
  BIN_DIR=`dirname "$0"`
  export JQASSISTANT_HOME=`cd "$BIN_DIR/.." && pwd -P`
fi
LIB_DIR=$JQASSISTANT_HOME/lib
java $JQASSISTANT_OPTS -jar "$LIB_DIR/${project.groupId}-${project.artifactId}-${project.version}.${project.packaging}" $*

