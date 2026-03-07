# Maintainer Experience Uplift — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Modernize parfait's CI, automate releases via GitHub Actions, and provide a simple local clean-room container for testing.

**Architecture:** Three sequential phases — DX uplift (CI + docs), release automation (GHA workflow + secrets), clean-room container (Podman-based test runner). Each phase is a parent GitHub issue with checklist items. Each checklist item gets its own PR.

**Tech Stack:** GitHub Actions, Maven, PCP, GPG, Sonatype Nexus, Podman

**Note:** This plan is infrastructure/CI/docs work, not application code. Steps are "make the change, validate, commit" rather than red-green-refactor. Tasks marked `[MANUAL]` require human action outside the codebase (e.g. creating GitHub secrets).

---

## Task 0: Create GitHub Issues

Create three parent issues on `performancecopilot/parfait` to track the work.

**Step 1: Create Phase 1 parent issue**

```bash
gh issue create --repo performancecopilot/parfait \
  --title "Uplift developer experience: CI, Java matrix, local dev docs" \
  --body "$(cat <<'EOF'
Modernize CI infrastructure and developer documentation.

## Checklist

- [ ] Update CI to Ubuntu 24.04, fix PCP install, update actions
- [ ] Add Java version matrix (11, 17, 21) to CI
- [ ] Document local development setup (macOS Homebrew PCP, Linux, CONTRIBUTING.md)
- [ ] Update CLAUDE.md to reflect macOS PCP availability

See design doc: `docs/plans/2026-03-06-maintainer-experience-uplift-design.md`
EOF
)"
```

**Step 2: Create Phase 2 parent issue**

```bash
gh issue create --repo performancecopilot/parfait \
  --title "Automate release process via GitHub Actions" \
  --body "$(cat <<'EOF'
Replace the Docker-based local release process with a GitHub Actions workflow.

## Checklist

- [ ] Set up release credentials as GitHub secrets (GPG key, Sonatype creds)
- [ ] Create release workflow with dry-run validation
- [ ] Enable real releases via GitHub Actions (with environment protection)
- [ ] Delete old Docker release infrastructure (releasing.sh, releasing-scripts/, Dockerfile)

## Key decisions
- Release builds pinned to Java 11
- Sonatype "Close and Release" stays manual for now
- New dedicated GPG key for releases

See design doc: `docs/plans/2026-03-06-maintainer-experience-uplift-design.md`
EOF
)"
```

**Step 3: Create Phase 3 parent issue**

```bash
gh issue create --repo performancecopilot/parfait \
  --title "Podman-based local clean-room for testing" \
  --body "$(cat <<'EOF'
Provide a simple container-based testing environment for maintainers.

## Checklist

- [ ] Replace Dockerfile with test-only version
- [ ] Add test-in-container.sh convenience script (uses podman)
- [ ] Update documentation (CONTRIBUTING.md/README)
- [ ] Final cleanup of any remaining old release container remnants

## Key decisions
- Dockerfile name kept (PCP ecosystem consistency)
- Scripts use podman, public docs use docker
- Container only runs tests — no GPG, no SSH, no deploy credentials

See design doc: `docs/plans/2026-03-06-maintainer-experience-uplift-design.md`
EOF
)"
```

**Step 4: Note the issue numbers**

Record the three issue numbers for use in commit messages and PR references.

---

## Task 1: Update CI to Ubuntu 24.04

**Files:**
- Modify: `.github/workflows/ci.yml`

**Step 1: Update the runner and actions versions**

Replace the full contents of `.github/workflows/ci.yml`:

```yaml
name: ci

on: [ push, pull_request ]

jobs:
  java:
    runs-on: ubuntu-24.04
    steps:
      - uses: actions/checkout@v4
      - name: install pcp
        run: |
          curl -s https://packagecloud.io/install/repositories/performancecopilot/pcp/script.deb.sh | sudo bash
          sudo apt-get update -q
          sudo apt-get install -y pcp-zeroconf
      - name: verify pcp install
        run: pcp
      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '11'
      - name: open up access
        run: sudo chmod o+w /var/lib/pcp/tmp/mmv
      - name: verify parfait
        run: mvn -B -V clean install verify
```

Changes from current:
- `ubuntu-20.04` → `ubuntu-24.04`
- `actions/checkout@v2` → `actions/checkout@v4`
- Removed deprecated `apt-key add` step (packagecloud script handles GPG key management)
- Removed `pcp-gui` from install (not needed for CI, reduces install footprint)

**Step 2: Commit**

```bash
git add .github/workflows/ci.yml
git commit -m "Update CI to Ubuntu 24.04, fix deprecated PCP install

Ubuntu 20.04 is EOL. Remove deprecated apt-key usage, let
packagecloud script handle GPG keys. Drop unused pcp-gui.
Update actions/checkout to v4.

Refs #PHASE1_ISSUE"
```

**Step 3: Push and validate**

Push the branch and verify the CI workflow runs green on GitHub Actions. If the packagecloud PCP install fails on noble, fall back to the PCP project's own apt repository.

---

## Task 2: Add Java version matrix to CI

**Files:**
- Modify: `.github/workflows/ci.yml`
- Modify: `README.md:11-13`

**Step 1: Add matrix strategy to ci.yml**

Update `.github/workflows/ci.yml` to add a Java version matrix:

```yaml
name: ci

on: [ push, pull_request ]

jobs:
  java:
    runs-on: ubuntu-24.04
    strategy:
      matrix:
        java-version: ['11', '17', '21']
      fail-fast: false
    steps:
      - uses: actions/checkout@v4
      - name: install pcp
        run: |
          curl -s https://packagecloud.io/install/repositories/performancecopilot/pcp/script.deb.sh | sudo bash
          sudo apt-get update -q
          sudo apt-get install -y pcp-zeroconf
      - name: verify pcp install
        run: pcp
      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: ${{ matrix.java-version }}
      - name: open up access
        run: sudo chmod o+w /var/lib/pcp/tmp/mmv
      - name: verify parfait
        run: mvn -B -V clean install verify
```

Key points:
- `fail-fast: false` ensures all Java versions are tested even if one fails
- No `continue-on-error` on Java 21 initially — let's see if it passes; we can add it if needed

**Step 2: Update README.md requirements section**

Change line 13 of `README.md` from:

```
Parfait requires Java 11-17 (as of Parfait 1.2.x). While Parfait (the published library) should _run_ on newer JVMs, the current test code only successfully runs on Java versions 11-17.
```

To:

```
Parfait requires Java 11+ (as of Parfait 1.2.x). CI tests against Java 11, 17, and 21.
```

**Step 3: Commit**

```bash
git add .github/workflows/ci.yml README.md
git commit -m "Add Java 11/17/21 matrix to CI

Validate against multiple Java versions instead of just 11.
Update README requirements to reflect tested versions.

Refs #PHASE1_ISSUE"
```

---

## Task 3: Document local development setup

**Files:**
- Create: `CONTRIBUTING.md`
- Modify: `README.md:11-14`
- Modify: `RELEASING.md:96-128`

**Step 1: Create CONTRIBUTING.md**

```markdown
# Contributing to Parfait

## Development Environment Setup

### Prerequisites

- Java 11 or later (11, 17, and 21 are tested in CI)
- Maven 3.1.0+
- Performance Co-Pilot (PCP)

### Installing PCP

PCP is required to run the full test suite, including integration tests that write MMV (Memory-Mapped Values) files.

**macOS (via Homebrew):**

```bash
brew tap performancecopilot/pcp
brew install --cask pcp
```

See [performancecopilot/homebrew-pcp](https://github.com/performancecopilot/homebrew-pcp) for details and troubleshooting.

**Debian/Ubuntu:**

```bash
curl -s https://packagecloud.io/install/repositories/performancecopilot/pcp/script.deb.sh | sudo bash
sudo apt-get install -y pcp-zeroconf
```

**RHEL/Fedora:**

```bash
sudo dnf install pcp-zeroconf
```

### MMV Directory Permissions

The PCP MMV agent needs a writable directory for memory-mapped files:

```bash
sudo chmod o+w /var/lib/pcp/tmp/mmv
```

### Building and Testing

```bash
# Full build with all tests
mvn clean install verify

# Unit tests only (integration tests excluded by surefire config)
mvn test

# Single test class
mvn test -pl parfait-core -Dtest=MonitoredCounterTest

# Single test method
mvn test -pl parfait-core -Dtest=MonitoredCounterTest#testMethodName

# Skip license header check (useful during development)
mvn install -Dlicense.skip=true
```

### Building the Agent

```bash
mvn clean package install
cd parfait-agent && mvn assembly:single
```

The fat JAR is produced at `parfait-agent/target/parfait-agent-jar-with-dependencies.jar`.

## License Headers

All `.java` and `.xml` files require Apache 2.0 license headers (enforced by `license-maven-plugin`). The header template is at `license/header.txt`. The `parfait-agent` module has its own header definition at `parfait-agent/license/parfait-agent-java.xml`.
```

**Step 2: Update README.md requirements section**

After line 13 (the Java requirements line updated in Task 2), add a reference to the new contributing guide:

```
See [CONTRIBUTING.md](CONTRIBUTING.md) for development environment setup, including PCP installation on macOS and Linux.
```

**Step 3: Update RELEASING.md — remove outdated macOS section**

Replace lines 96-128 of `RELEASING.md` (the "Releasing from OSX" section) with:

```markdown
Releasing from macOS
====================

PCP is now available on macOS via Homebrew. See [CONTRIBUTING.md](CONTRIBUTING.md) for installation instructions.

With PCP installed locally, the release process works the same on macOS as on Linux — no Docker container required.
```

Note: This section will be substantially rewritten again in Phase 2 when the release process moves to GitHub Actions. This is an interim fix to remove the incorrect "PCP doesn't have a supported OSX distribution" statement.

**Step 4: Commit**

```bash
git add CONTRIBUTING.md README.md RELEASING.md
git commit -m "Document local dev setup, add CONTRIBUTING.md

Add macOS Homebrew PCP install instructions, Linux setup,
MMV permissions, and build commands. Remove outdated claim
that PCP isn't available on macOS.

Refs #PHASE1_ISSUE"
```

---

## Task 4: Update CLAUDE.md

**Files:**
- Modify: `CLAUDE.md`

**Step 1: Update CLAUDE.md**

In the "Testing Notes" section, replace:

```
- On macOS, PCP is not natively available. CI runs on Ubuntu with PCP installed. For local macOS development, use `-DskipTests` or the Docker-based release workflow.
```

With:

```
- PCP is available on macOS via Homebrew (`brew tap performancecopilot/pcp && brew install --cask pcp`). See CONTRIBUTING.md for full setup.
```

In the "Platform Requirements" section, replace:

```
- PCP must be installed for integration tests. CI installs `pcp-zeroconf` and opens `/var/lib/pcp/tmp/mmv`.
```

With:

```
- PCP must be installed for integration tests. See CONTRIBUTING.md for install instructions (macOS, Debian/Ubuntu, RHEL/Fedora). Requires writable `/var/lib/pcp/tmp/mmv`.
```

**Step 2: Commit**

```bash
git add CLAUDE.md
git commit -m "Update CLAUDE.md to reflect macOS PCP availability

Refs #PHASE1_ISSUE"
```

---

## Task 5: Set up release credentials [MANUAL]

This task is performed by a human in the GitHub UI and locally.

**Step 1: Generate a new GPG key for releases**

```bash
gpg --full-generate-key
# Choose: RSA and RSA, 4096 bits, no expiry (or long expiry)
# Name: "Parfait Release Signing Key"
# Email: use a project-appropriate email
```

**Step 2: Export the private key**

```bash
gpg --armor --export-secret-keys <KEY_ID> | pbcopy
```

**Step 3: Create GitHub repository secrets**

Go to `github.com/performancecopilot/parfait/settings/secrets/actions` and create:

| Secret Name | Value |
|---|---|
| `GPG_PRIVATE_KEY` | The armored GPG private key |
| `GPG_PASSPHRASE` | The passphrase for the GPG key |
| `SONATYPE_USERNAME` | Your OSS Sonatype username |
| `SONATYPE_PASSWORD` | Your (freshly rotated) OSS Sonatype password |

**Step 4: Create a `release` environment with protection rules**

Go to `github.com/performancecopilot/parfait/settings/environments` and create a `release` environment with:
- Required reviewers: add yourself (and any other maintainers who should approve releases)

**Step 5: Publish the new GPG public key**

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
```

---

## Task 6: Create release workflow (dry-run first)

**Files:**
- Create: `.github/workflows/release.yml`
- Modify: `RELEASING.md`

**Step 1: Create the release workflow**

Create `.github/workflows/release.yml`:

```yaml
name: release

on:
  workflow_dispatch:
    inputs:
      release_version:
        description: 'Release version (e.g. 1.2.2)'
        required: true
      next_dev_version:
        description: 'Next development version (e.g. 1.2.3-SNAPSHOT)'
        required: true
      dry_run:
        description: 'Dry run (no actual release)'
        type: boolean
        default: true

jobs:
  release:
    runs-on: ubuntu-24.04
    environment: ${{ inputs.dry_run == false && 'release' || '' }}
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: install pcp
        run: |
          curl -s https://packagecloud.io/install/repositories/performancecopilot/pcp/script.deb.sh | sudo bash
          sudo apt-get update -q
          sudo apt-get install -y pcp-zeroconf

      - name: start pmcd
        run: sudo systemctl start pmcd

      - name: open up mmv access
        run: sudo chmod o+w /var/lib/pcp/tmp/mmv

      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '11'

      - name: import gpg key
        run: |
          echo "${{ secrets.GPG_PRIVATE_KEY }}" | gpg --batch --import
        env:
          GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}

      - name: configure maven settings
        run: |
          mkdir -p ~/.m2
          cat > ~/.m2/settings.xml << 'SETTINGS'
          <settings>
            <servers>
              <server>
                <id>sonatype-nexus-snapshots</id>
                <username>${env.SONATYPE_USERNAME}</username>
                <password>${env.SONATYPE_PASSWORD}</password>
              </server>
              <server>
                <id>sonatype-nexus-staging</id>
                <username>${env.SONATYPE_USERNAME}</username>
                <password>${env.SONATYPE_PASSWORD}</password>
              </server>
            </servers>
            <profiles>
              <profile>
                <id>gpg</id>
                <properties>
                  <gpg.executable>gpg</gpg.executable>
                  <gpg.passphrase>${env.MAVEN_GPG_PASSPHRASE}</gpg.passphrase>
                </properties>
              </profile>
            </profiles>
            <activeProfiles>
              <activeProfile>gpg</activeProfile>
            </activeProfiles>
          </settings>
          SETTINGS

      - name: configure git
        run: |
          git config user.email "parfait-release@pcp.io"
          git config user.name "Parfait Release Bot"

      - name: maven release
        env:
          SONATYPE_USERNAME: ${{ secrets.SONATYPE_USERNAME }}
          SONATYPE_PASSWORD: ${{ secrets.SONATYPE_PASSWORD }}
          MAVEN_GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
        run: |
          mvn --batch-mode \
            -DdryRun=${{ inputs.dry_run }} \
            -DreleaseVersion=${{ inputs.release_version }} \
            -DdevelopmentVersion=${{ inputs.next_dev_version }} \
            release:prepare release:perform
```

Key design points:
- `environment: release` is only applied when `dry_run` is false — dry runs skip the approval gate
- `fetch-depth: 0` gives full git history needed by `release:prepare`
- `contents: write` permission lets the workflow push release commits and tags
- GPG passphrase flows via environment variable, same as the old Docker script
- Maven `settings.xml` uses `${env.VAR}` syntax to read credentials from environment at runtime
- Git user is a bot identity, not a personal account

**Step 2: Update RELEASING.md with the new process**

Replace the entire content of `RELEASING.md` with:

```markdown
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
```

**Step 3: Commit**

```bash
git add .github/workflows/release.yml RELEASING.md
git commit -m "Add GitHub Actions release workflow

Replace Docker-based release process with workflow_dispatch
triggered workflow. Supports dry-run validation. Uses GitHub
environment protection rules to gate real releases.

Refs #PHASE2_ISSUE"
```

**Step 4: Validate with a dry run**

Push the branch, merge to main, then trigger the release workflow with `dry_run: true` from the Actions tab. Verify it completes successfully.

---

## Task 7: Validate real release

**Step 1: Trigger a real release**

Once the dry run passes, trigger the workflow again with `dry_run: false` for the next actual parfait release. Approve the environment gate.

**Step 2: Complete Sonatype promotion**

Log into OSS Sonatype, Close and Release the staging repository.

**Step 3: Verify**

Confirm the release artifacts appear on Maven Central and the git tag/commits are pushed to GitHub.

---

## Task 8: Delete old Docker release infrastructure

**Files:**
- Delete: `releasing.sh`
- Delete: `releasing-scripts/docker-release-build.sh`
- Delete: `releasing-scripts/` (directory)
- Modify: `.gitignore:13` (remove `.releasing.env` line)

**Step 1: Remove old files**

```bash
rm releasing.sh
rm -r releasing-scripts/
```

**Step 2: Clean up .gitignore**

Remove line 13 (`.releasing.env`) from `.gitignore` — it's no longer relevant.

**Step 3: Commit**

```bash
git add -A releasing.sh releasing-scripts/
git add .gitignore
git commit -m "Remove old Docker-based release infrastructure

Release process now runs in GitHub Actions. The Docker
container, SSH agent forwarding, and GPG key export
gymnastics are no longer needed.

Refs #PHASE2_ISSUE"
```

---

## Task 9: Replace Dockerfile with test-only version

**Files:**
- Modify: `Dockerfile`

**Step 1: Replace Dockerfile contents**

Replace the entire content of `Dockerfile`:

```dockerfile
FROM quay.io/performancecopilot/pcp

RUN yum update -y && \
    yum install -y java-11-openjdk maven maven-openjdk11 && \
    yum clean all

RUN chmod o+w /var/lib/pcp/tmp/mmv

COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

WORKDIR /parfait

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["mvn", "-B", "clean", "verify"]
```

**Step 2: Create the entrypoint script**

Create `docker-entrypoint.sh`:

```bash
#!/bin/sh
set -e

# Start PCP's metric collector daemon
/usr/libexec/pcp/lib/pmcd start

exec "$@"
```

**Step 3: Commit**

```bash
git add Dockerfile docker-entrypoint.sh
git commit -m "Simplify Dockerfile to test-only container

No more GPG, SSH, or deploy credentials. Just starts PMCD
and runs the test suite. Used for local clean-room testing.

Refs #PHASE3_ISSUE"
```

---

## Task 10: Add test-in-container.sh convenience script

**Files:**
- Create: `test-in-container.sh`

**Step 1: Create the script**

Create `test-in-container.sh`:

```bash
#!/bin/sh
set -e

IMAGE_NAME="parfait-test"

echo "Building test container..."
podman build -t "$IMAGE_NAME" .

if [ $# -eq 0 ]; then
    echo "Running: mvn -B clean verify"
    podman run --rm -v "$(pwd):/parfait:Z" "$IMAGE_NAME"
else
    echo "Running: $*"
    podman run --rm -v "$(pwd):/parfait:Z" "$IMAGE_NAME" "$@"
fi
```

Notes:
- `:Z` suffix on the volume mount handles SELinux labeling (harmless on non-SELinux systems)
- Default command comes from the Dockerfile CMD (`mvn -B clean verify`)
- Override example: `./test-in-container.sh mvn test -pl parfait-core`

**Step 2: Make it executable**

```bash
chmod +x test-in-container.sh
```

**Step 3: Commit**

```bash
git add test-in-container.sh
git commit -m "Add test-in-container.sh for local clean-room testing

Uses podman to build and run tests in a PCP-equipped container.
Accepts optional Maven command override.

Refs #PHASE3_ISSUE"
```

---

## Task 11: Update documentation for container testing

**Files:**
- Modify: `CONTRIBUTING.md`
- Modify: `CLAUDE.md`

**Step 1: Add container testing section to CONTRIBUTING.md**

Append to `CONTRIBUTING.md` before the "License Headers" section:

```markdown
## Container-Based Testing

If you don't want to install PCP locally, or want to validate in a clean environment:

```bash
# Run full test suite in a container (requires docker or podman)
./test-in-container.sh

# Run specific module tests
./test-in-container.sh mvn test -pl parfait-core

# Run with any Maven command
./test-in-container.sh mvn clean install -DskipTests
```

The script uses `podman` by default. If you use Docker, you can run the equivalent commands directly:

```bash
docker build -t parfait-test .
docker run --rm -v "$(pwd):/parfait" parfait-test
```
```

**Step 2: Update CLAUDE.md**

Add to the "Build & Test Commands" section:

```bash
# Run tests in a clean container (requires podman)
./test-in-container.sh

# Run specific module in container
./test-in-container.sh mvn test -pl parfait-core
```

**Step 3: Commit**

```bash
git add CONTRIBUTING.md CLAUDE.md
git commit -m "Document container-based testing option

Refs #PHASE3_ISSUE"
```

---

## Task 12: Final cleanup

**Step 1: Review for remnants**

Search for any remaining references to the old release process:

```bash
grep -r "docker-release" .
grep -r "releasing.env" . --include="*.md"
grep -r "Docker Desktop" . --include="*.md"
grep -r "SSH_AUTH_SOCK" .
grep -r "gpgkeyexport" .
```

**Step 2: Fix any remaining references**

If any references are found, update them. If none, this task is done.

**Step 3: Commit (if needed)**

```bash
git add -A
git commit -m "Clean up remaining references to old release process

Refs #PHASE3_ISSUE"
```

**Step 4: Close all three parent issues**

Verify all checklist items are complete, then close the three parent GitHub issues.
