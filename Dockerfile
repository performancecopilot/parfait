FROM quay.io/performancecopilot/pcp

RUN yum update -y && \
    yum install -y java-11-openjdk maven maven-openjdk11 && \
    yum clean all

# Check if MMV directory permissions need fixing
# TODO: Remove chmod if 'other' already has write access in base image
RUN echo "MMV dir permissions BEFORE chmod:" && ls -la /var/lib/pcp/tmp/ | grep mmv && \
    chmod o+w /var/lib/pcp/tmp/mmv && \
    echo "MMV dir permissions AFTER chmod:" && ls -la /var/lib/pcp/tmp/ | grep mmv

COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

WORKDIR /parfait

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["mvn", "-B", "clean", "verify"]
