#!/bin/bash
#
# Copyright (c) 2016 Red Hat
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# Simple shell script to start a java program with the parfait-agent
# jar injected via the -javaagent command line option.
#

usage()
{
cat <<EOF
usage: parfait [-n name] [-i interval] [-c cluster] [--] javaargs

  -n use the string which follows as the program name exported
     via PCP memory mapped value (MMV) metrics.  mmv.<name> in
     the performance metric name space (PMNS).

  -c use the numeric cluster identifier for PCP/MMV metric IDs
     (default is to build a hash identifier using progam name)

  -i use numeric interval as the delta upon which JMX values will
     be reevaluated for exporting as memory mapped values (1 sec,
     by default)

  -- optional separator to distinguish trailing arguments

  javaargs trailing arguments to be supplied to the java command

The script employs the java command found in the current execution
PATH.  If PARFAIT_JAVA_ARGS is set then this is inserted into the
java command line before the -javaagent argument and before any
arguments in javaargs.
EOF
exit
}

# use PARFAIT_HOME to locate installed parfait release
if [ -z "$PARFAIT_HOME" ]; then
    # use root of the path to this script to locate the parfait-agent jar
    PARFAIT_HOME=${0%*/bin/parfait.sh}
    # also allow for rename to plain parfait
    if [ "$PARFAIT_HOME" == "$0" ]; then
        PARFAIT_HOME=${0%*/bin/parfait}
    fi
    if [ "$PARFAIT_HOME" == "$0" ]; then
        echo "Unable to find parfait home"
        exit
    fi
fi

# the parfait agent jar should be in the lib directory
if [ -f ${PARFAIT_HOME}/lib/parfait.jar ]; then
    PARFAIT_JAR=${PARFAIT_HOME}/lib/parfait.jar
else
    echo "Cannot locate parfait agent jar"
    exit
fi
AGENT_PREFIX="-javaagent:${PARFAIT_JAR}"
AGENT_OPTS=""

PARFAIT_NAME=
PARFAIT_INTERVAL=
PARFAIT_CLUSTER=

while [ $# -ge 1 -a "${1#-*}" != "$1" ]
do
    if [ "$1" == "-n" -a $# -ge 2 ]; then
        PARFAIT_NAME=$2
        shift;
        shift;
    elif [ "$1" == "-i" -a $# -ge 2 ]; then
        PARFAIT_INTERVAL=$2
        shift;
        shift;
    elif [ "$1" == "-c" -a $# -ge 2 ]; then
        PARFAIT_CLUSTER=$2
        shift;
        shift;
    elif [ "$1" == "--" ]; then
        shift;
        break;
    else
        # unrecognised option -- must be start of javaargs
        break
    fi
done

if [ "$PARFAIT_NAME" != "" ]; then
    AGENT_OPTS="${AGENT_OPTS},name:${PARFAIT_NAME}"
fi
if [ "$PARFAIT_CLUSTER" != "" ]; then
    AGENT_OPTS="${AGENT_OPTS},cluster:${PARFAIT_CLUSTER}"
fi
if [ "$PARFAIT_INTERVAL" != "" ]; then
    AGENT_OPTS="${AGENT_OPTS},interval:${PARFAIT_INTERVAL}"
fi

AGENT_ARGUMENT=${AGENT_PREFIX}=${AGENT_OPTS}

# allow for extra java opts via setting PARFAIT_JAVA_OPTS
exec java ${PARFAIT_JAVA_OPTS} ${AGENT_ARGUMENT} ${INJECT_JAVA_LANG_OPTS} $*
