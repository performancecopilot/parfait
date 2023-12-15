FROM ubuntu:latest

RUN apt-get update && apt-get install -y git pcp pcp-gui gpg
RUN apt-get install -y openjdk-17-jdk maven