Release Process
===============

To release parfait out to the wider community, you will need the following:

   * checked out the Parfait git repo locally
   * Maven
   * gpg
   * An account on [OSS Sonatype Repo](https://oss.sonatype.org/)

If you're releasing from a Mac/OSX, then you'll also need:
   * Docker

OSS Sonatype
------------

You need to have access to OSS Sonatype repo to perform some manual actions during release.

The best place to start is to [read this Overview guide](http://central.sonatype.org/pages/ossrh-guide.html).

It appears the account to be created _may_ need to be linked with access to the `io.pcp` project.  Create a new Sonatype JIRA Issue and add myself (`tallpsmith@gmail.com`) as a watcher.

gpg
---

Part of the Maven release process uses `gpg` to digitally sign the releases using a signature.  Please refer to the OSSRH Overview guide above in the OSS Sonatype section as most of the links stem from there.

As outlined in the docs, to streamline the release process I recommend encoding your `gpg` password into ``~/.m2/settings.xml`:

    ...
    <profiles>
    ...
      <profile>
         <id>gpg</id>
         <properties>
            <gpg.executable>gpg</gpg.executable>
            <gpg.keyname>tallpsmith@gmail.com</gpg.keyname>
            <gpg.passphrase>..................</gpg.passphrase>
          </properties>
      </profile>
    ...
    </profiles>
    ...
    <activeProfiles>
      <activeProfile>gpg</activeProfile>
    ...
    </activeProfiles>


Otherwise you will be asked for the passphrase for every single Parfait module (which is quite a few)....

Maven
-----

Along with the `gpg` key, you need to store your password for the OSS Sonatype account in the `~/.m2/settings.xml`:

    ...
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>psmith@aconex.com</username>
      <password>..................</password>
    </server>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>psmith@aconex.com</username>
      <password>..................</password>
    </server>


When Maven wants to push the artifacts to OSS Sonatype, it'll use this block.

ACTUALLY doing the release
==========================

Once you have all the above components setup, the actual release process involves the following steps:

  1. Invoke Maven's release process, a neat one-liner is this:
  ```
  mvn release:prepare release:perform
  ```
  Note: by default Parfait uses the tag convention of version number only - i.e. X.Y.Z only, without "parfait-" prefix -
  as this results in generation of source tarballs on github with the appropriate contents.
  This step publishes the artifacts to a _*temporary*_ holding area within OSS Sonatype.

  2. The next step is outlined well here: [http://central.sonatype.org/pages/releasing-the-deployment.html]


Once the `Release` action is performed you & others in the OSS Sonatype group for this project will receive an email from Nexus indicating the promotion is complete.  Once you receive this, the new version should be referencable in any POM.

Releasing from OSX
==================
There are some complications releasing from a computer with OSX.  As of December 2023, PCP doesn't have a supported OSX distribution,
and Parfait test harness require interaction with PCP locally to validate.  As the Maven release process involves running the
tests locally to validate, this is problematic.

To support the release process on OSX, there is a `Dockerfile` used _purely_ as a mechanism for releasing.  It is
a quick'n'dirty mechanism, ugly and less than ideal, but works.

Here's the steps:
```
# Prerequisites:
#   * ensure your current working directory is in the root of the Parfait repository
#   * EXPORT your gpg PRIVATE key in armor format to directory ~/gpgkeyexport (used later)
#       -  gpg --armor --export 21FFA5EB0E068E51 > ~/gpgkeyexport/tallpsmith@gmail.com.prvt.asc

# Make sure your ssh key needed for Github is added to a running `ssh-agent` on your local host.
$ ssh-add

# Build the Docker image used for running the release
$ docker build .

# Find the imageID you just built, it should be the one at the top
$ docker images | head -2
REPOSITORY                                        TAG                    IMAGE ID       CREATED         SIZE
<none>                                            <none>                 b2de17c68635   17 hours ago    851MB

# Grab that ImageID to set an environment variable
$ IMAGEID=b2de17c68635

# Run the Docker image
#   - maps the ssh-agent on your host into the container
#   - maps the Parfait codebase to /code in the container
#   - maps your exported GPG key to a path needed later
# The Docker image is a simple Ubuntu image with Java, PCP, git, and gpg installed
$ docker run -e SSH_AUTH_SOCK="/run/host-services/ssh-auth.sock"  -v.:/code -v ~/.m2:/root/.m2 -v ~/gpgkeyexport:/root/gpgkeyexport --mount type=bind,src=/run/host-services/ssh-auth.sock,target=/run/host-services/ssh-auth.sock  -it $IMAGEID /bin/sh

# Now we're in the running container, we need to import the GPG key
# Import your private GPG key into the containers enviroment
# I couldn't find a working way to reference my gpg setup from the container, so this is was a hacky way to solve it
$ gpg --import /root/gpgkeyexport/tallpsmith@gmail.com.prvt.asc

# start PCP, this is needed by the tests
$ service pmcd start

# setup git in the container to support the release process
$ git config --global user.email “tallpsmith@gmail.com”
$ git config --global user.name “Paul “Smith
$ git config --global gpg.program gpg

# change path to where the Parfait code is mapped into the container
$ cd /code

# This is needed otherwise you’ll get
# gpg: signing failed: Inappropriate ioctl for device
# the GPG signing process needs to prompt you for your passphrase
# even though the Maven GPG plugin allows you to declare the password
# this seems to still be needed...
$ export GPG_TTY=$(tty)

# The Maven JavaDoc plugin needs to set the JAVA_HOME..
$ export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-arm64

# Now we can finally start the release process itself!
$ mvn release:prepare release:perform

# You'll be prompted on screen for your GPG passphrase (if you have one)
# Maven will build, test, verify, package and sign and push to OSS Sonatype
# Follow the Standard OSS Sonatype release process from here
# you can now exit the container
$ exit

```
