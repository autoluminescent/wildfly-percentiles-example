# Introduction

This repository is lifted and adapted from the 
[WildFly Micrometer quickstart](https://github.com/wildfly/quickstart/tree/main/micrometer).
We use it to define a simple application with Micrometer integration.

Its purpose is to demonstrate a very reduced example where several metrics are
wrongfully reported as zero when the RBAC access control provider is used.

# Description

Components:

- `wildfly-histogram-test`: a tiny application that contains only one resource,
  similar to the Micrometer quickstart (but simplified).  It registers a summary
  meter, where we record all the numbers that are requested.

- `init-and-deploy.sh`: a script that configures the Micrometer extension,
  builds the application and deploys it.

- `otel-configuration-config.yaml`: OpenTelemetry Collector config file, copied
  verbatim from the quickstart.

- `configure-micrometer.cli`: JBoss CLI file to configure Micrometer, copied
  verbatim from the quickstart.

# Requirements

- JDK 11+ installed with `JAVA_HOME` configured appropriately.

- Apache Maven

- Docker

- WildFly 36.0.0.Final with `JBOSS_HOME` configured appropriately, and with
  standard configuration.

# Steps

1.   Enable statistics globally:

     ```bash
     export JAVA_OPTS="-Dwildfly.statistics-enabled=true"
     ```

2.   Run WildFly in standalone mode with default configuration:

     ```bash
     standalone.sh
     ```

3.   Run the OpenTelemetry Collector container:

     ```bash
     docker run -d \
         -v ./otel-collector-config.yaml:/etc/otel-collector-config.yaml:Z \
         -p 1888:1888 \
         -p 13133:13133 \
         -p 4317:4317 \
         -p 4318:4318 \
         -p 55679:55679 \
         -p 1234:1234 \
         otel/opentelemetry-collector:0.89.0 --config=/etc/otel-collector-config.yaml
     ```
     
4.   Run the init and deployment script:

     ```bash
     ./init-and-deploy.sh
     ```

5.   Send it some requests:

     ```bash
     curl http://localhost:8080/wildfly-histogram-test/prime/1
     curl http://localhost:8080/wildfly-histogram-test/prime/2
     curl http://localhost:8080/wildfly-histogram-test/prime/40
     curl http://localhost:8080/wildfly-histogram-test/prime/2701
     ```

6.   Check the Undertow request count metric:

     ```bash
     curl http://localhost:1234/metrics | grep undertow_request_count_total
     ```

     If all went well, this should be 4.

7.   Switch to RBAC access control:

     ```bash
     jboss-cli.sh -c '/core-service=management/access=authorization:write-attribute(name="provider",value="rbac")','reload'
     ```

8.   Repeat 5 and 6.  Notice now that the reported metrics are 0.
