# Maintainer Experience Uplift — Design Document

Date: 2026-03-06

## Problem Statement

The parfait development and release experience has accumulated significant friction:

- CI runs on EOL Ubuntu 20.04 with stale PCP install methods
- The release process requires a Docker container that mounts SSH agents, exports GPG private keys, and pre-seeds gpg-agent keygrips — fragile and painful
- macOS developers couldn't run integration tests locally because PCP wasn't available on macOS. It now is, via Homebrew.
- Documentation references outdated platform constraints and doesn't cover macOS setup

## Approach

Sequential phases, each tracked as a parent GitHub issue with a checklist. Each checklist item gets its own PR. Documentation is freshened inline with each change, not as a separate pass.

Ordering: Phase 1 (DX uplift) -> Phase 2 (GHA release) -> Phase 3 (clean-room container). This ordering ensures the clean-room container only needs to support testing (not releasing), keeping it simple.

## Key Decisions

- **Release builds** pinned to Java 11; CI matrix tests 11, 17, 21
- **Sonatype "Close and Release"** stays as a manual gate in the Nexus UI. Automating via `autoReleaseAfterClose` is a future follow-up once confidence is established.
- **Container tooling:** `podman` in scripts, `docker` in public-facing documentation, `Dockerfile` as the container file name (consistent with broader PCP ecosystem)
- **Credentials:** GitHub Actions secrets for GPG private key and Sonatype credentials. A new dedicated GPG key for releases. Sonatype password rotated.
- **Docs updated per-issue**, not as a standalone docs sweep

---

## Phase 1: Developer Experience Uplift

Parent issue: "Uplift developer experience: CI, Java matrix, local dev docs"

### 1.1 Update CI to Ubuntu 24.04

The `ci.yml` workflow runs on `ubuntu-20.04` (EOL April 2025). Update to `ubuntu-24.04`.

Changes:
- Update runner to `ubuntu-24.04`
- Update `actions/checkout` from v2 to v4
- Fix PCP install method — `apt-key add` is deprecated; verify packagecloud or alternative PCP package source works on noble
- Verify PCP packages available and PMCD starts correctly
- Update docs referencing the CI platform where relevant

### 1.2 Add Java version matrix to CI

The README states Java 11-17 support but CI only tests Java 11.

Changes:
- Add matrix strategy: Java 11, 17, 21
- Java 21 can be marked `continue-on-error: true` initially if needed
- Update README.md requirements section to reflect tested versions

### 1.3 Document local development setup

macOS developers can now install PCP via Homebrew: https://github.com/performancecopilot/homebrew-pcp

Changes:
- Add CONTRIBUTING.md (or "Development" section in README) covering:
  - macOS: `brew install --cask performancecopilot/pcp/pcp` (verify exact command from homebrew-pcp repo)
  - Linux: apt/yum install instructions
  - MMV directory permissions (`/var/lib/pcp/tmp/mmv`)
  - Java 11+ and Maven 3.1.0+ requirements
- Update README.md requirements section to mention macOS support
- Update RELEASING.md to remove "PCP doesn't have a supported OSX distribution" (line 99)
- Reference the homebrew-pcp repository

### 1.4 Update CLAUDE.md

Reflect that PCP is available on macOS. Remove "skip tests on macOS" guidance.

---

## Phase 2: GitHub Actions Release Automation

Parent issue: "Automate release process via GitHub Actions"

### 2.1 Set up release credentials as GitHub secrets

Changes:
- Generate a new GPG key dedicated to parfait releases
- Store as GitHub secrets: `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`
- Store Sonatype credentials: `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`
- Rotate Sonatype password
- Document in RELEASING.md which secrets exist and their purpose (not values)

### 2.2 Create release workflow with dry-run validation

A new `.github/workflows/release.yml` triggered via `workflow_dispatch`.

Inputs:
- `release_version` (e.g. `1.2.2`)
- `next_development_version` (e.g. `1.2.3-SNAPSHOT`)

Steps:
- Checkout with full history
- Install PCP (same as CI)
- Setup Java 11 (pinned — release builds use known-good Java)
- Import GPG key from secrets
- Generate `~/.m2/settings.xml` from secrets (Sonatype credentials)
- Run `mvn --batch-mode -DdryRun=true -DreleaseVersion=$INPUT -DdevelopmentVersion=$INPUT release:prepare release:perform`

Update RELEASING.md to document the dry-run process.

### 2.3 Enable real releases via GitHub Actions

Changes:
- Add `dry_run` as a workflow input (boolean, default false)
- Add GitHub environment protection rule (e.g. `release` environment requiring approval) to prevent accidental releases
- Workflow pushes release commits and tags back to the repo (requires `contents: write` permission)
- Deploys to Sonatype staging — manual "Close and Release" in Nexus UI remains as human gate
- Update RELEASING.md with the full new release process

### 2.4 Delete old Docker release infrastructure

Changes:
- Delete `releasing.sh`
- Delete `releasing-scripts/` directory
- Update RELEASING.md: remove Docker Desktop, SSH agent, GPG export sections entirely
- Remove references to `.releasing.env`

---

## Phase 3: Local Clean-Room Container

Parent issue: "Podman-based local clean-room for testing"

### 3.1 Replace Dockerfile with test-only version

The current `Dockerfile` is a release build container. Replace it with a test-only container.

Changes:
- Keep the filename as `Dockerfile` (consistent with PCP ecosystem)
- Base on a PCP image or Ubuntu + PCP packages (whichever is more maintainable)
- Install Java 11, Maven
- Entrypoint starts PMCD and sets up MMV directory permissions
- No GPG, no git config, no SSH, no Maven deploy credentials

### 3.2 Add test-in-container.sh convenience script

A simple script to run the test suite in the container via podman.

Changes:
- Uses `podman build` and `podman run`
- Mounts the source tree, runs `mvn clean verify` by default
- Accepts optional Maven command override (e.g. `./test-in-container.sh mvn test -pl parfait-core`)
- No env files required

### 3.3 Update documentation

Changes:
- Document container-based testing in CONTRIBUTING.md/README
- Position as optional: "if you don't want to install PCP locally, or want to validate in a clean environment"
- Public-facing docs use `docker` commands (per project convention), scripts use `podman`

### 3.4 Final cleanup

Cross-reference with 2.4. If any remnants of the old release container infrastructure remain, clean them up here.

---

## Future Work (Out of Scope)

- Automate Sonatype "Close and Release" via `autoReleaseAfterClose` — separate issue once GHA release process has proven reliable
- CodeQL workflow updates (exists at `.github/workflows/codeql.yml`, not addressed here)
- Broader CI improvements (caching Maven dependencies, parallel module builds)
