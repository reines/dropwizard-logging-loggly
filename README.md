Dropwizard Logging Loggly
========

A log appender factory for Dropwizard adding support for appending logs to Loggly

[![Build Status](https://api.travis-ci.org/reines/dropwizard-logging-loggly.png)](https://travis-ci.org/reines/dropwizard-logging-loggly)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jamierf.dropwizard/dropwizard-logging-loggly/badge.png)](https://maven-badges.herokuapp.com/maven-central/com.jamierf.dropwizard/dropwizard-logging-loggly)

dropwizard-logging-loggly can be found in maven central.

## Installation

```xml
<dependency>
    <groupId>com.jamierf.dropwizard</groupId>
    <artifactId>dropwizard-logging-loggly</artifactId>
    <version>...</version>
</dependency>
```

## Configuration

```yaml
logging:
  appenders:
    - type: loggly
      token: "..."
```
