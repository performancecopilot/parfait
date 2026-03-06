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
