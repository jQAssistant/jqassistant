#!/bin/sh
BIN_DIR=`dirname "$0"`
if [ -z "$JQASSISTANT_HOME" ] ; then
  JQASSISTANT_HOME=`readlink -f "$BIN_DIR/.."`
fi
LIB_DIR=$JQASSISTANT_HOME/lib
java -jar $LIB_DIR/${project.artifactId}-${project.version}.${project.packaging} $*
