#!/bin/sh
BIN_DIR=`dirname "$0"`
export JQASSISTANT_HOME=`cd "$BIN_DIR/.." && pwd -P`
java $JQASSISTANT_OPTS -jar "$JQASSISTANT_HOME/lib/${project.groupId}-${project.artifactId}-${project.version}.${project.packaging}" "$@"
