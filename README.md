# iab-spiders-and-robots-java-client

This is a Java 7+ client library for the IAB and ABC International Spiders and Robots lists.

It uses Gradle as its build tool and contains a comprehensive set of JUnit tests.

It has CI/CD enabled and is available from Maven Central.

It must be released under the Apache 2.0 License.

### Installation

Add into your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.snowplowanalytics.iab</groupId>
    <artifactId>spiders-and-robots-client</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Usage

#### Initialization

You must initialize your IAP checker with:

1. The IAB file of exclusion IP address CIDR ranges, and:
2. The IAB files of the exclusion and inclusion useragents

Here's a maximalist approach:

```java
// A File object pointing to your ip_exclude_current_cidr.txt file
File ipFile = new File("/path/to/ip_exclude_current_cidr.txt");

// File objects pointing to your include and exclude lists
File excludeUaFile = new File("/path/to/exclude_current.txt");
File includeUaFile = new File("/path/to/include_current.txt");

// This creates the IabClient object, which should be reused across
// lookups.
IabClient client = new IabClient(ipFile, excludeUaFile, includeUaFile);
```

For example files see the 
[test/resources](https://github.com/snowplow/iab-spiders-and-robots-java-client/tree/master/src/test/resources) 
sub-folder in this repo.

#### Performing a check

Let's assume that we have the following inputs:

```java
String useragent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:50.0) Gecko/20100101 Firefox/50.0";
InetAddress ipAddress = InetAddress.getByName("128.101.101.101");
Date accurateAt = new Date(2017, 2, 28);
```

where `accurateAt` is the datetime as of when the `useragent` and `ipAddress` are accurate.

There are two ways of doing a check:

```java
IabResponse iabResponse = client.check(useragent, ipAddress);
IabResponse iabResponse = client.checkAt(useragent, ipAddress, accurateAt);
```

It is allowed for either `useragent` or `ipAddress` to be null, but not both (throws an `IllegalArgumentException`).

In `check()`, the `accurateAt` is taken to be `now()` for the purposes of checking active versus inactive spider and bot useragents.

In `checkAt()`, the `accurateAt` must not be null.

#### The IabResponse

The `IabResponse` is a POJO with the following fields:

```
iabResponse.spiderOrRobot = true | false
iabResponse.category = SPIDER_OR_ROBOT | ACTIVE_SPIDER_OR_ROBOT | INACTIVE_SPIDER_OR_ROBOT | BROWSER
iabResponse.reason = FAILED_IP_EXCLUDE | FAILED_UA_INCLUDE | FAILED_UA_EXCLUDE | PASSED_ALL
iabResponse.primaryImpact = PAGE_IMPRESSIONS | AD_IMPRESSIONS | PAGE_AND_AD_IMPRESSIONS | UNKNOWN | NONE
```

#### Algorithm

##### 1. IP checks

First we check the IP address.

If the IP is listed or belongs to one of the CIDR ranges, then:

```java
iabResponse.spiderOrRobot = true
iabResponse.category = SPIDER_OR_ROBOT
iabResponse.reason = FAILED_IP_EXCLUDE
iabResponse.primaryImpact = UNKNOWN
```

Else continue...

##### 2. Include UA checks

Run a check for the useragent in the include file, making sure to take into account whether the pattern must match at the beginning of the useragent or not. 

If the useragent is **not** found in the include useragents file, then:

```java
iabResponse.spiderOrRobot = true
iabResponse.category = SPIDER_OR_ROBOT
iabResponse.reason = FAILED_UA_INCLUDE
iabResponse.primaryImpact = UNKNOWN
```

If we have found UA in the record with `active flag =  0 (false)`, we should check the `inactive date`:

  1. if `inactive date` is not present, then we entirely ignore current record and go to the next one
  2. if `inactive date` is present and `inactive date <= accurateAt` then  ignore current record and go to the next one
  3. if `inactive date` is present and `accurateAt < inactive date` then return above result (with `iabResponse.spiderOrRobot = true`)


Else continue...

##### 3. Exclude UA checks

Run a check for the useragent in the exclude file, making sure to take into account any exceptions to the pattern and whether the pattern must match at the beginning of the useragent or not.

If the useragent is found in the exclude file, then check if the `accurateAt` date is before or after the Inactive Date.

If our `accurateAt` date is before the Inactive Date (or that date is not set), then:

```java
iabResponse.spiderOrRobot = true
iabResponse.category = ACTIVE_SPIDER_OR_ROBOT
iabResponse.reason = FAILED_UA_EXCLUDE
```

Use column 5) the primary impact flag to determine:

```java
uaResponse.primaryImpact = PAGE_IMPRESSIONS | AD_IMPRESSIONS | PAGE_AND_AD_IMPRESSIONS
```

If our `accurateAt` date is after or equal the Inactive Date, then:

```java
iabResponse.spiderOrRobot = true
iabResponse.category = INACTIVE_SPIDER_OR_ROBOT
iabResponse.reason = FAILED_UA_EXCLUDE
```

Again, use column 5) the primary impact flag to determine:

```java
iabResponse.primaryImpact = PAGE_IMPRESSIONS | AD_IMPRESSIONS | PAGE_AND_AD_IMPRESSIONS
```

#### 4. Identifying a browser

Otherwise we have a browser:

```java
iabResponse.spiderOrRobot = false
iabResponse.category = BROWSER
iabResponse.reason = PASSED_ALL
iabResponse.primaryImpact = NONE
```

## Copyright and License

IAB Spiders And Robots Java Client is copyright 2017 Snowplow Analytics Ltd.

Licensed under the **[Apache License, Version 2.0] [license]** (the "License"); you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[license]: http://www.apache.org/licenses/LICENSE-2.0
