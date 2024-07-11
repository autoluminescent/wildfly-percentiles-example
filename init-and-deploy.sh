#!/bin/bash

set -e

# configure micrometer
jboss-cli.sh --connect --file=configure-micrometer.cli

# reload the server
jboss-cli.sh --connect --commands=reload || true

cd wildfly-histogram-test

mvn clean package

mvn wildfly:deploy
