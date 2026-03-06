# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What is Parfait

Parfait is a Java performance monitoring library that extracts metrics and exports them via JMX and Performance Co-Pilot (PCP) memory-mapped values (MMV). Group ID: `io.pcp`, current version: `1.2.2-SNAPSHOT`.

## Build & Test Commands

```bash
# Full build with tests (requires PCP installed and /var/lib/pcp/tmp/mmv writable)
mvn clean install verify

# Build without tests
mvn clean install -DskipTests

# Run unit tests only (integration tests excluded by default via surefire)
mvn test

# Run a single test class
mvn test -pl parfait-core -Dtest=MonitoredCounterTest

# Run a single test method
mvn test -pl parfait-core -Dtest=MonitoredCounterTest#testMethodName

# Run integration tests (named *IntegrationTest.java, run via failsafe plugin)
mvn verify

# Build the standalone agent fat JAR
mvn clean package install && cd parfait-agent && mvn assembly:single

# License header check (runs automatically during initialize phase)
mvn license:check

# Skip license check
mvn install -Dlicense.skip=true

# Run tests in a clean container (requires podman)
./test-in-container.sh

# Run specific module in container
./test-in-container.sh mvn test -pl parfait-core
```

## Architecture

Multi-module Maven project. Dependency flow is roughly: `parfait-core` -> `dxm` -> `parfait-pcp` -> `parfait-agent`.

### Module Responsibilities

- **parfait-core** (`io.pcp.parfait`) - Core types: `Monitorable<T>`, `MonitorableRegistry`, `MonitoringView`, `MonitoredCounter`, `MonitoredValue`, `ValueSemantics`. All other modules depend on this.
- **dxm** (`io.pcp.parfait.dxm`) - PCP MMV (Memory-Mapped Values) binary format writer. `PcpMmvWriter` is the main class. Supports MMV format v1 and v2. Writes directly to memory-mapped files that PCP reads.
- **parfait-pcp** (`io.pcp.parfait.pcp`) - Bridge between parfait core `Monitorable` objects and the DXM writer. `PcpMonitorBridge` implements `MonitoringView`.
- **parfait-jmx** - Exports parfait metrics as JMX MBeans via `JmxView`.
- **parfait-jdbc** - JDBC driver wrapper that automatically collects SQL metrics.
- **parfait-spring** - Spring Framework integration and XML configuration support.
- **parfait-cxf** - Apache CXF web service metrics integration.
- **parfait-dropwizard** - Bridge from Dropwizard Metrics to parfait.
- **parfait-agent** - Standalone Java agent (`-javaagent`). Entry point: `io.pcp.parfait.ParfaitAgent`. Bundles everything into a fat JAR.
- **parfait-io** - I/O related monitoring utilities.
- **parfait-benchmark** - Performance benchmarks.

### Key Design Patterns

- **MonitorableRegistry** is the central collection of metrics. A default singleton exists, but multiple registries are supported for multi-app JVMs.
- **MonitoringView** is the output interface — implementations push metric values to external systems (PCP, JMX, etc).
- **ValueSemantics** enum distinguishes counters (monotonically increasing) from free-running point-in-time values and constants.
- Units use JSR-385 (`javax.measure`) with the Indriya reference implementation.

## Testing Notes

- Unit tests: standard JUnit 4 with Mockito and Hamcrest.
- Integration tests: files named `*IntegrationTest.java`, separated via maven-failsafe-plugin. These typically require a running PCP installation.
- The DXM module's `InMemoryByteBufferFactory` is the key test utility — allows testing MMV writing without filesystem/PCP dependencies.
- PCP is available on macOS via Homebrew (`brew tap performancecopilot/pcp && brew install --cask pcp`). See CONTRIBUTING.md for full setup.

## Platform Requirements

- Java 8 source/target (enforced by maven-enforcer-plugin), tested on Java 11-17 at runtime.
- Maven 3.1.0+ required.
- PCP must be installed for integration tests. See CONTRIBUTING.md for install instructions (macOS, Debian/Ubuntu, RHEL/Fedora). Requires writable `/var/lib/pcp/tmp/mmv`.

## License

Apache 2.0. License headers are enforced on all `.java` and `.xml` files via `license-maven-plugin` (except `parfait-agent` which has its own header definition). New source files need the header from `license/header.txt`.
