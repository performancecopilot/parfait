FROM quay.io/performancecopilot/pcp

RUN yum update -y
RUN yum install -y pcp-gui java-11-openjdk maven maven-openjdk11

WORKDIR /parfait