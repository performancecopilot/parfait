#!/bin/sh -x
[ -f AgentDemo.java ] || javac AgentDemo.java

agent=../target/parfait-agent-jar-with-dependencies.jar
java -javaagent:$agent AgentDemo
