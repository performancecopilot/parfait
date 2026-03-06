#!/bin/sh
set -e

# Start PCP's metric collector daemon
/usr/libexec/pcp/lib/pmcd start

exec "$@"
