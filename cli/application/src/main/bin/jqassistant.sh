#!/bin/sh
BIN_DIR=`dirname "$0"`

if [ "$JAVA_HOME" == "" ]; then
  JAVA_CMD="java"
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

export JQASSISTANT_HOME=`cd "$BIN_DIR/.." && pwd -P`
exec "$JAVA_CMD" $JQASSISTANT_OPTS -jar "$JQASSISTANT_HOME/lib/${project.groupId}-${project.artifactId}-${project.version}.${project.packaging}" "$@"
