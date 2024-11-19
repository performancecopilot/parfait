#!/bin/sh

set -e

[ ! -f .releasing.env ] && echo ".releasing.env file not found" && exit 1
source .releasing.env

[ ! "$GIT_USERNAME" ] && echo "GIT_USERNAME is not set" && exit 1
[ ! "$GIT_EMAIL" ] && echo "GIT_EMAIL is not set" && exit 1
[ ! "$GPG_PASSPHRASE" ] && echo "GPG_PASSPHRASE is not set" && exit 1

[ ! "$RELEASE_VERSION" ] && echo "RELEASE_VERSION is not set" && exit 1
[ ! "$DEVELOPMENT_VERSION" ] && echo "DEVELOPMENT_VERSION is not set" && exit 1

docker build . -t parfait-builder
# This runs the Docker-based build:
#   * sets up Git & PGP environment variables
#   * mounts the code base into /parfait (which becomes the WORKDIR)
#   * mounts your GPG exported key (see RELEASING.md for the requirements there)
#   * mounts the SSH_AUTH_SOCK so you can leverage your local SSH agent
#   * then launches the releasing-scripts/docker-release-build.sh to perform the build inside the container
docker run --rm --env GIT_USERNAME="${GIT_USERNAME}" --env GIT_EMAIL="${GIT_EMAIL}" --env MAVEN_GPG_PASSPHRASE="${GPG_PASSPHRASE}" --env SSH_AUTH_SOCK="/run/host-services/ssh-auth.sock" --volume `pwd`:/parfait --volume ~/.m2:/root/.m2 --volume ~/gpgkeyexport:/root/gpgkeyexport --mount type=bind,src=/run/host-services/ssh-auth.sock,target=/run/host-services/ssh-auth.sock parfait-builder sh -c 'releasing-scripts/docker-release-build.sh'



