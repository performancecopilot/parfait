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
