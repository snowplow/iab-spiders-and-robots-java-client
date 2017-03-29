# IAB Spiders And Robots Java Client

[![Build Status](https://api.travis-ci.org/snowplow/iab-spiders-and-robots-java-client.svg?branch=master)][travis]
[![Release](https://img.shields.io/github/release/snowplow/iab-spiders-and-robots-java-client.svg?style=flat)][releases]
[![License](http://img.shields.io/badge/license-Apache--2-blue.svg?style=flat)][license]

This is a Java 7+ client library for the IAB and ABC International Spiders and Robots lists.

It uses Gradle as its build tool and contains a comprehensive set of JUnit tests.

It has CI/CD enabled and is available from Maven Central.

It must be released under the Apache 2.0 License.

## Installation

Add into your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.snowplowanalytics.iab</groupId>
    <artifactId>spiders-and-robots-client</artifactId>
    <version>0.1.0</version>
</dependency>
```

## A Simple Example

Assume we have a HTTP request from the IP address: `128.101.101.101` with a user agent string: 
`Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:50.0) Gecko/20100101 Firefox/50.0`.
To perform a robot or spider check using **[this algorithm][wiki-algorithm]**:

```java
// A File object pointing to your ip_exclude_current_cidr.txt file
File ipFile = new File("/path/to/ip_exclude_current_cidr.txt");

// File objects pointing to your include and exclude lists
File excludeUaFile = new File("/path/to/exclude_current.txt");
File includeUaFile = new File("/path/to/include_current.txt");

// This creates the IabClient object, which should be reused across lookups.
IabClient client = new IabClient(ipFile, excludeUaFile, includeUaFile);

String useragent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:50.0) Gecko/20100101 Firefox/50.0";
InetAddress ipAddress = InetAddress.getByName("128.101.101.101");
IabResponse iabResponse = client.check(useragent, ipAddress);
```

For more complex examples and step by step description, please, refer the library Wiki: **[Usage Of The Library][wiki-usage]**

## Quickstart

Assuming git, **[Vagrant][vagrant-install]** and **[VirtualBox][virtualbox-install]** installed:

```bash
host$ git clone https://github.com/snowplow/iab-spiders-and-robots-java-client.git
host$ cd iab-spiders-and-robots-java-client
host$ vagrant up && vagrant ssh
guest$ cd /vagrant
guest$ ./gradlew clean build
guest$ ./gradlew test
```

## Find out more

* **[Usage Of The Library][wiki-usage]**

* **[The Library Algorithm][wiki-algorithm]**

## Copyright and License

IAB Spiders And Robots Java Client is copyright 2017 Snowplow Analytics Ltd.

Licensed under the **[Apache License, Version 2.0][license]** (the "License"); you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[travis]: https://travis-ci.org/snowplow/iab-spiders-and-robots-java-client
[releases]: https://github.com/snowplow/iab-spiders-and-robots-java-client/releases

[vagrant-install]: http://docs.vagrantup.com/v2/installation/index.html
[virtualbox-install]: https://www.virtualbox.org/wiki/Downloads

[wiki-usage]: https://github.com/snowplow/iab-spiders-and-robots-java-client/wiki/Usage-Of-The-Library
[wiki-algorithm]: https://github.com/snowplow/iab-spiders-and-robots-java-client/wiki/The-Library-Algorithm
[license]: http://www.apache.org/licenses/LICENSE-2.0

