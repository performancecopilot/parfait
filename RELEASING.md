Release Process
===============

To release parfait out to the wider community, you will need the following:

   * checked out the Parfait git repo locally
   * Maven
   * gpg & a published GPG public key
   * An account on [OSS Sonatype Repo](https://oss.sonatype.org/)

If you're releasing from a Mac/OSX, then you'll also need:
   * Docker Desktop (due to SSH_AGENT needs, Docker Desktop is the only one that works...)


OSS Sonatype
------------

You need to have access to OSS Sonatype repo to perform some manual actions during release.

The best place to start is to [read this Overview guide](http://central.sonatype.org/pages/ossrh-guide.html).

It appears the account to be created _may_ need to be linked with access to the `io.pcp` project.  Create a new Sonatype JIRA Issue and add myself (`tallpsmith@gmail.com`) as a watcher.

gpg
---

Part of the Maven release process uses `gpg` to digitally sign the releases using a signature.  Please refer to the OSSRH Overview guide above in the OSS Sonatype section as most of the links stem from there.

As outlined in the docs, to streamline the release process I recommend encoding your `gpg` details (but not your password) into ``~/.m2/settings.xml`:

    ...
    <profiles>
    ...
      <profile>
         <id>gpg</id>
         <properties>
            <gpg.executable>gpg</gpg.executable>
            <gpg.keyname>tallpsmith@gmail.com</gpg.keyname>
          </properties>
      </profile>
    ...
    </profiles>
    ...
    <activeProfiles>
      <activeProfile>gpg</activeProfile>
    ...
    </activeProfiles>

You can configure your GPG passphrase via an environment variable before running the release process:

```markdown

export MAVEN_GPG_PASSPHRASE=....
```

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

There are some complications releasing from a computer with OSX.  As of November 2024, PCP doesn't have a supported OSX distribution,
and Parfait test harness require interaction with PCP locally to validate.  As the Maven release process involves running the
tests locally to validate, this is problematic.

To support the release process on OSX, there is a release script that leverages a `Dockerfile` used _purely_ as a mechanism for releasing.  It is
a quick'n'dirty mechanism, ugly and less than ideal, but works.

Here's the steps:
```
# Prerequisites:
#   * ensure your current working directory is in the root of the Parfait repository
#   * EXPORT your gpg PRIVATE key in armor format to directory ~/gpgkeyexport (used during the build)
#       -  gpg --armor --export-secret-key 21FFA5EB0E068E51 > ~/gpgkeyexport/gpgkey.prvt.asc

# Make sure your ssh key needed for Github is added to a running `ssh-agent` on your local host.
$ ssh-add

# Create a `.releasing.env` file (not part of SCM) that contains the following environment variables needed
#GIT_USERNAME=<your Github username>
#GIT_EMAIL=<your Github email address>
#GPG_PASSPHRASE=<passphrase for your PRIVATE GPG key exported earlier>
#These next 2 drive the Maven Release plugin
#RELEASE_VERSION=1.2.1
#DEVELOPMENT_VERSION=1.2.2-SNAPSHOT

# Run the Release script
./releasing.sh
```
