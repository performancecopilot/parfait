#!/bin/sh

set -e

[ ! -f .releasing.env ] && echo ".releasing.env file not found" && exit 1
source .releasing.env

[ ! $GIT_USERNAME ] && echo "GIT_USERNAME is not set" && exit 1
[ ! $GIT_EMAIL ] && echo "GIT_EMAIL is not set" && exit 1
[ ! $GPG_PASSPHRASE ] && echo "GPG_PASSPHRASE is not set" && exit 1

docker build . -t parfait-build
docker run --rm --env GIT_USERNAME="${GIT_USERNAME}" --env GIT_EMAIL="${GIT_EMAIL}" --env MAVEN_GPG_PASSPHRASE="${GPG_PASSPHRASE}" -v `pwd`:/parfait -v ~/.m2:/root/.m2 -v ~/gpgkeyexport:/root/gpgkeyexport parfait-builder sh -c 'releasing-scripts/docker-release-build.sh'



