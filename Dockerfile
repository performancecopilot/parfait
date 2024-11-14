FROM quay.io/performancecopilot/pcp

RUN yum update -y
RUN yum install -y pcp-gui git java-11-openjdk maven maven-openjdk11

WORKDIR /parfait

ENV GIT_AUTHOR_NAME=""
ENV GIT_AUTHOR_EMAIL=""

