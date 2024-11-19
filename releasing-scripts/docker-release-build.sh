#!/bin/sh

set -e

echo "Starting PMCD"
/usr/libexec/pcp/lib/pmcd start

# Setup GPG agent in this container to allow preset passphrases
mkdir -p /root/.gnupg
chmod -R 500 /root/.gnupg
echo allow-preset-passphrase  >> /root/.gnupg/gpg-agent.conf

echo "Importing GPGKEY"
# this trick allows the GPG secret key to be imported via the command line
# thank goodness for Google
echo $MAVEN_GPG_PASSPHRASE | gpg --batch --yes --passphrase-fd 0  --import  /root/gpgkeyexport/gpgkey.prvt.asc

# now iterate over each KEYGRIP you can see and preset the passphrase (one of them will be the right one)
echo "Dumping keygrips"
gpg --list-secret-keys --with-keygrip
for KEYGRIP in `gpg --list-secret-keys --with-keygrip | grep Keygrip | awk -F = '{print $2}'`; do /usr/libexec/gpg-preset-passphrase --preset --passphrase $MAVEN_GPG_PASSPHRASE $KEYGRIP; done

# now do a simple GPG sign to 'prime' the gpg to ensure when Maven ends up running this GPG cache thing is ready
echo "Doing a fake GPG signing now to prime the GPG agent password cache"
echo "test" | gpg --clearsign

echo "Adding Github to known_hosts files"
# see https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/githubs-ssh-key-fingerprints
cat << EOF >> ~/.ssh/known_hosts
github.com ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIOMqqnkVzrm0SdG6UOoqKLsabgH5C9okWi0dh2l9GKJl
github.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBEmKSENjQEezOmxkZMy7opKgwFB9nkt5YRrYMjNuG5N87uRgg6CLrbo5wAdT/y6v0mKV0U2w0WZ2YB/++Tpockg=
github.com ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCj7ndNxQowgcQnjshcLrqPEiiphnt+VTTvDP6mHBL9j1aNUkY4Ue1gvwnGLVlOhGeYrnZaMgRK6+PKCUXaDbC7qtbW8gIkhL7aGCsOr/C56SJMy/BCZfxd1nWzAOxSDPgVsmerOBYfNqltV9/hWCqBywINIR+5dIg6JTJ72pcEpEjcYgXkE2YEFXV1JHnsKgbLWNlhScqb2UmyRkQyytRLtL+38TGxkxCflmO+5Z8CSSNY7GidjMIZ7Q4zMjA2n1nGrlTDkzwDCsw+wqFPGQA179cnfGWOWRVruj16z6XyvxvjJwbz0wQZ75XK5tKSb7FNyeIEs4TT4jk+S4dhPeAUC5y+bDYirYgM4GC7uEnztnZyaVWQ7B381AK4Qdrwt51ZqExKbQpTUNn+EjqoTwvqNj4kqx5QUCI0ThS/YkOxJCXmPUWZbhjpCg56i+2aB6CmK2JGhn57K5mj0MNdBXA4/WnwH6XoPWJzK5Nyu2zB3nAZp+S5hpQs+p1vN1/wsjk=
EOF

# echo "configuring Git"
git config --global user.email "$GIT_EMAIL"
git config --global user.name "$GIT_USERNAME"
git config --global --list

echo "Building Parfait"
MAVEN_GPG_PASSPHRASE=$MAVEN_GPG_PASSPHRASE mvn --batch-mode release:prepare release:perform -DreleaseVersion="${RELEASE_VERSION}" -DdevelopmentVersion="${DEVELOPMENT_VERSION}"
