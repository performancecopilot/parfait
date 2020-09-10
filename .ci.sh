#!/bin/bash
set -e
#ensure no prompts for stuff like timezones
export DEBIAN_FRONTEND=noninteractive

apt-get update
apt-get install -y wget gnupg ca-certificates
wget -qO - https://bintray.com/user/downloadSubjectPublicKey?username=pcp | apt-key add -
echo "deb https://dl.bintray.com/pcp/focal focal main" >> /etc/apt/sources.list
apt-get -qq update

apt-get install -y pcp pcp-gui
touch /var/lib/pcp/pmdas/mmv/.NeedInstall
/etc/init.d/pmcd start

pcp

# Java8
apt-get install -y openjdk-8-jdk

# would pull in Java11 if Java8 wasn't installed
apt-get install -y maven

# Run maven
cd /parfait
mvn -B -V clean install verify
