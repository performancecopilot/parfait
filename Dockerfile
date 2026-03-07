FROM quay.io/performancecopilot/pcp

# JDK 21 used here because the PCP base image (Fedora) no longer ships JDK 11 or 17.
# Parfait targets Java 8 source/target; CI (.github/workflows/ci.yml) tests the
# full matrix (11, 17, 21, 25) — that remains the gold standard for version coverage.
RUN yum update -y && \
    yum install -y java-21-openjdk-devel maven && \
    yum clean all

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk

# PCP installs /var/lib/pcp/tmp/mmv as drwxrwxr-x (root:root).
# Java process needs write access to create MMV memory-mapped files.
RUN chmod o+w /var/lib/pcp/tmp/mmv

COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

WORKDIR /parfait

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["mvn", "-B", "clean", "verify"]
