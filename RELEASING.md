Release Process
===============

Parfait releases are performed via GitHub Actions and published to Maven Central via the [Central Portal](https://central.sonatype.com).

Prerequisites
-------------

The following GitHub repository secrets must be configured (Settings > Secrets > Actions):

| Secret | Purpose |
|---|---|
| `GPG_PRIVATE_KEY` | Armored GPG private key for signing artifacts |
| `GPG_PASSPHRASE` | Passphrase for the GPG key |
| `CENTRAL_USERNAME` | Central Portal user token username |
| `CENTRAL_PASSWORD` | Central Portal user token password |

A `release` environment with required reviewers must be configured (Settings > Environments) to prevent accidental releases.

You also need access to the [Central Portal](https://central.sonatype.com) to perform the final publish step. To generate user tokens: log in > Account > Generate User Token.

Performing a Release
--------------------

### 1. Dry Run

Always do a dry run first:

1. Go to Actions > "release" workflow > "Run workflow"
2. Enter the release version (e.g. `1.2.2`) and next development version (e.g. `1.2.3-SNAPSHOT`)
3. Check "Dry run" (default is checked)
4. Click "Run workflow"

Verify the workflow completes successfully.

### 2. Real Release

1. Go to Actions > "release" workflow > "Run workflow"
2. Enter the same versions as the dry run
3. **Uncheck** "Dry run"
4. Click "Run workflow"
5. Approve the deployment when prompted (required reviewers gate)

The workflow will:
- Run all tests
- Update POM versions
- Build and sign artifacts
- Upload deployment bundle to Central Portal
- Push release commits and tag to GitHub

### 3. Publish on Central Portal

1. Log into the [Central Portal](https://central.sonatype.com)
2. Go to "Deployments"
3. Find the `io.pcp` deployment
4. Review the validation status
5. Click "Publish"

Artifacts typically appear on Maven Central within 30 minutes of publishing.

> **Note:** The `autoPublish` setting in pom.xml is currently `false`, requiring this manual publish step. Once confident in the pipeline, set `autoPublish` to `true` in the `central-publishing-maven-plugin` configuration to eliminate this step.

Tag Convention
--------------

Parfait uses version-number-only tags (e.g. `1.2.2`, not `parfait-1.2.2`). This produces clean source tarballs on GitHub.

GPG Key Management
------------------

The release signing key should be:
- A dedicated key (not a personal key)
- Published to a public keyserver (`gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>`)
- Long-lived — only replace if compromised (consumers verify artifacts against this key)
