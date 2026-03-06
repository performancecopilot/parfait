# Contributing to Parfait

## Development Environment Setup

### Prerequisites

- Java 11 (see below for setup — your system default is likely much newer)
- Maven 3.1.0+
- Performance Co-Pilot (PCP)

### Installing and Configuring Java 11

Parfait targets Java 8 source/target and is tested in CI against Java 11, 17, and 21. Your system likely ships with a much newer Java by default, which will cause build failures. You need Java 11 available and Maven configured to use it.

**macOS (via Homebrew):**

```bash
brew install openjdk@11
```

Homebrew installs Java but doesn't make it the default. To point Maven at it, set `JAVA_HOME` in your shell profile:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
```

Or if you use multiple Java versions, set it per-project in the parfait directory:

```bash
# Create a .env or use direnv, sdkman, jenv, etc.
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
```

**Linux (Debian/Ubuntu):**

```bash
sudo apt-get install -y openjdk-11-jdk
```

If you have multiple JDKs installed, select Java 11:

```bash
sudo update-alternatives --set java /usr/lib/jvm/java-11-openjdk-amd64/bin/java
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

**Verify:**

```bash
java -version   # Should show 11.x.x
mvn -version    # "Java version" line should show 11.x.x
```

If `mvn -version` shows a different Java than `java -version`, Maven is picking up `JAVA_HOME` from somewhere else. Check your shell profile and ensure `JAVA_HOME` is set before Maven runs.

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
sudo apt-get install -y pcp pcp-zeroconf pcp-gui
```

**RHEL/Fedora:**

```bash
sudo dnf install pcp pcp-zeroconf pcp-gui
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

## License Headers

All `.java` and `.xml` files require Apache 2.0 license headers (enforced by `license-maven-plugin`). The header template is at `license/header.txt`. The `parfait-agent` module has its own header definition at `parfait-agent/license/parfait-agent-java.xml`.
