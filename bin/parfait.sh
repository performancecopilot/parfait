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
# jar injected either via the -javaagent command line option or via
# its main() routine in JMX-proxying mode.
#

usage()
{
    cat <<EOF
Usage: parfait [options] [--] [javaargs]
       parfait [options] --jmxserver host:port

Options:
  -n/--name NAME
     Use the string which follows as the program name exported
     via PCP memory mapped value (MMV) metrics.  mmv.<name> in
     the performance metric name space (PMNS).  Overrides the
     parfait.name Java system property.  If not set, a generated
     name will be used.

  -c/--cluster N
     Use the numeric cluster identifier for PCP/MMV metric IDs
     (default is to build a hash identifier using progam name).
     Overrides the parfait.cluster Java system property.

  -i/--interval N
     Use numeric interval as the delta upon which JMX values will
     be reevaluated for exporting as memory mapped values (1 sec,
     by default).  Overrides the parfait.interval Java system
     property.  Specified in milliseconds.

  -j/--jmxserver HOSTNAME:PORT
     Connect to the JMX server within a Java process setup with
     JMX mbean exporting enabled.  This runs parfait-agent in a
     "proxying" mode where values for managed JMX beans from an
     external process are monitored.  This setting overrides the
     parfait.connect Java system property which has a default
     value of localhost:9875.

  -s/--startup N
     Allow a max startup time in which JMX values are still being
     created, before exporting as memory mapped values (5 seconds
     by default).  Overrides the parfait.startup Java system
     property.  Specified in milliseconds.

  -h/--help
     Show this usage message and exit.

  --
     Optional separator to distinguish trailing arguments.

  javaargs
     Optional trailing arguments to be supplied to the java command.

The script employs the java command found in the current execution
PATH.  If the environment variable PARFAIT_JAVA_OPTS is set then
this is inserted into the java command line before the -javaagent
argument and before any arguments in javaargs.
EOF
    exit $1
}

# use PARFAIT_HOME to locate installed parfait release
if [ -z "$PARFAIT_HOME" ]; then
    # use root of the path to this script to locate the parfait-agent jar
    PARFAIT_HOME="${0%*/bin/parfait.sh}"
    # also allow for rename to plain parfait
    if [ "$PARFAIT_HOME" = "$0" ]; then
        PARFAIT_HOME="${0%*/bin/parfait}"
    fi
    if [ "$PARFAIT_HOME" = "$0" ]; then
        echo "Unable to find parfait home"
        exit 1
    fi
fi

# the parfait agent jar should be in the lib directory
if [ -f "${PARFAIT_HOME}/lib/parfait.jar" ]; then
    PARFAIT_JAR="${PARFAIT_HOME}/lib/parfait.jar"
elif [ -f "${PARFAIT_HOME}/share/java/parfait/parfait.jar" ]; then
    PARFAIT_JAR="${PARFAIT_HOME}/share/java/parfait/parfait.jar"
else
    echo "Cannot locate parfait agent jar"
    exit 1
fi

JVMMODE="agent"	# running in -javaagent mode or as JMX server proxy?
OPTIONS=""

PARFAIT_NAME=
PARFAIT_INTERVAL=
PARFAIT_CLUSTER=
PARFAIT_STARTUP=
PARFAIT_CONNECT=

while [ $# -ge 1 -a "${1#-*}" != "$1" ]
do
    if [ "$1" = "-n" -o "$1" = "--name" ]; then
        [ $# -ge 2 ] || usage 1
        PARFAIT_NAME=$2
        shift;
        shift;
    elif [ "$1" = "-i" -o "$1" = "--interval" ]; then
        [ $# -ge 2 ] || usage 1
        PARFAIT_INTERVAL=$2
        shift;
        shift;
    elif [ "$1" = "-c" -o "$1" = "--cluster" ]; then
        [ $# -ge 2 ] || usage 1
        PARFAIT_CLUSTER=$2
        shift;
        shift;
    elif [ "$1" = "-s" -o "$1" = "--startup" ]; then
        [ $# -ge 2 ] || usage 1
        PARFAIT_STARTUP=$2
        shift;
        shift;
    elif [ "$1" = "-j" -o "$1" = "--jmxserver" -o "$1" = "--connect" ]; then
        [ $# -ge 2 ] || usage 1
        PARFAIT_CONNECT=$2
        JVMMODE="proxy"
        shift;
        shift;
    elif [ "$1" = "-h" -o "$1" = "--help" ]; then
        usage 0
    elif [ "$1" = "--" ]; then
        shift;
        break;
    else
        # unrecognised option -- must be start of javaargs
        break
    fi
done

if [ "$JVMMODE" = "proxy" ]; then
    OPTIONS="-connect $PARFAIT_CONNECT"
    [ -n "$PARFAIT_NAME" ] && OPTIONS="$OPTIONS -name $PARFAIT_NAME"
    [ -n "$PARFAIT_CLUSTER" ] && OPTIONS="$OPTIONS -cluster $PARFAIT_CLUSTER"
    [ -n "$PARFAIT_INTERVAL" ] && OPTIONS="$OPTIONS -interval $PARFAIT_INTERVAL"
    ARGUMENTS="-jar ${PARFAIT_JAR} ParfaitAgent ${OPTIONS}"
else
    [ -z "$*" ] && usage 1
    [ -n "$PARFAIT_NAME" ] && OPTIONS="$OPTIONS,name:$PARFAIT_NAME"
    [ -n "$PARFAIT_CLUSTER" ] && OPTIONS="$OPTIONS,cluster:$PARFAIT_CLUSTER"
    [ -n "$PARFAIT_INTERVAL" ] && OPTIONS="$OPTIONS,interval:$PARFAIT_INTERVAL"
    [ -n "$PARFAIT_STARTUP" ] && OPTIONS="$OPTIONS,startup:$PARFAIT_STARTUP"
    ARGUMENTS="-javaagent:${PARFAIT_JAR}=${OPTIONS}"
fi

# allow for extra java opts via setting PARFAIT_JAVA_OPTS
exec java ${PARFAIT_JAVA_OPTS} ${ARGUMENTS} "$@"
