# IAB Spiders And Robots Java Client

[![Build Status](https://api.travis-ci.org/snowplow/iab-spiders-and-robots-java-client.svg?branch=master)][travis]
[![Release](http://img.shields.io/badge/release-0.2.0-blue.svg?style=flat)][releases]
[![License](http://img.shields.io/badge/license-Apache--2-blue.svg?style=flat)][license]

This is a Java 8 client library for the **[IAB/ABC International Spiders and Bots List][list]** (available separately).

The library is available from Maven Central, and is published under the Apache 2.0 License.

It uses Gradle as its build tool and contains a comprehensive set of JUnit tests.

## Installation

Add into your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.snowplowanalytics</groupId>
    <artifactId>iab-spiders-and-robots-client</artifactId>
    <version>0.2.0</version>
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

IAB Spiders And Robots Java Client is copyright 2017-2020 Snowplow Analytics Ltd.

Licensed under the **[Apache License, Version 2.0][license]** (the "License"); you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[travis]: https://travis-ci.org/snowplow/iab-spiders-and-robots-java-client
[releases]: https://github.com/snowplow/iab-spiders-and-robots-java-client/releases

[vagrant-install]: http://docs.vagrantup.com/v2/installation/index.html
[virtualbox-install]: https://www.virtualbox.org/wiki/Downloads

[wiki-usage]: https://github.com/snowplow/iab-spiders-and-robots-java-client/wiki/Usage-Of-The-Library
[wiki-algorithm]: https://github.com/snowplow/iab-spiders-and-robots-java-client/wiki/The-Library-Algorithm
[license]: http://www.apache.org/licenses/LICENSE-2.0

[list]: https://www.iab.com/guidelines/iab-abc-international-spiders-bots-list/
