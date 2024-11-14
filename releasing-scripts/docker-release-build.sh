#!/bin/sh

set -e

echo "Starting PMCD"
/usr/libexec/pcp/lib/pmcd start

echo "Importing GPGKEY"
# this trick allows the GPG secret key to be imported via the command line
# thank goodness for Google
echo $MAVEN_GPG_PASSPHRASE | gpg --batch --yes --passphrase-fd 0  --import  /root/gpgkeyexport/gpgkey.prvt.asc

echo "Building Parfait"
mvn clean verify gpg:sign
