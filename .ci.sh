#!/bin/bash
set -e

# Add extras missing in the docker image
apt-get -qq update
apt-get install -y apt-transport-https

# Setup PCP
# Due to Ubuntu/Python dependency hell, building against the latest PCP from bintray just doesn't install
# TODO need to fix this - see Issue #69
wget -qO - https://bintray.com/user/downloadSubjectPublicKey?username=pcp | apt-key add -
echo "deb https://dl.bintray.com/pcp/focal focal main" >> /etc/apt/sources.list
apt-get -qq update
#apt-get install -y python3.6
apt-get install -y pcp pcp-gui
touch /var/lib/pcp/pmdas/mmv/.NeedInstall
/etc/init.d/pmcd start

pcp

# Run maven
cd /parfait
mvn -B -V clean install verify
