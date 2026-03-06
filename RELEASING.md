Release Process
===============

Parfait releases are performed via GitHub Actions.

Prerequisites
-------------

The following GitHub repository secrets must be configured (Settings > Secrets > Actions):

| Secret | Purpose |
|---|---|
| `GPG_PRIVATE_KEY` | Armored GPG private key for signing artifacts |
| `GPG_PASSPHRASE` | Passphrase for the GPG key |
| `SONATYPE_USERNAME` | OSS Sonatype account username |
| `SONATYPE_PASSWORD` | OSS Sonatype account password |

A `release` environment with required reviewers must be configured (Settings > Environments) to prevent accidental releases.

You also need access to [OSS Sonatype](https://oss.sonatype.org/) to perform the final promotion step. See the [OSSRH guide](https://central.sonatype.org/pages/ossrh-guide.html) for account setup.

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
- Deploy to Sonatype staging
- Push release commits and tag to GitHub

### 3. Promote on Sonatype

1. Log into [OSS Sonatype](https://oss.sonatype.org/)
2. Go to "Staging Repositories"
3. Find the `io.pcp` staging repo
4. Click "Close" — this runs Sonatype's validation rules
5. Once closed successfully, click "Release"

You'll receive an email when promotion to Maven Central is complete.

Tag Convention
--------------

Parfait uses version-number-only tags (e.g. `1.2.2`, not `parfait-1.2.2`). This produces clean source tarballs on GitHub.

GPG Key Management
------------------

The release signing key should be:
- A dedicated key (not a personal key)
- Published to a public keyserver (`gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>`)
- Rotated periodically by updating the GitHub secret
