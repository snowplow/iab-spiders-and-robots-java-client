/*
 * Copyright (c) 2017-2020 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.iab.spidersandrobotsclient;

import com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.UserAgentCategory;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.DateUtils;
import com.snowplowanalytics.iab.spidersandrobotsclient.test.TestResources;
import org.assertj.core.api.ThrowableAssert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason.FAILED_IP_EXCLUDE;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason.FAILED_UA_EXCLUDE;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason.FAILED_UA_INCLUDE;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason.PASSED_ALL;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact.NONE;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact.PAGE_AND_AD_IMPRESSIONS;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact.PAGE_IMPRESSIONS;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact.UNKNOWN;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.UserAgentCategory.ACTIVE_SPIDER_OR_ROBOT;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.UserAgentCategory.BROWSER;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.UserAgentCategory.SPIDER_OR_ROBOT;
import static com.snowplowanalytics.iab.spidersandrobotsclient.test.TestResources.asInputStream;
import static com.snowplowanalytics.iab.spidersandrobotsclient.test.TestResources.dummyRecordPrefix;
import static com.snowplowanalytics.iab.spidersandrobotsclient.test.TestResources.ipFilePrefix;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IabClientTest {

    private static final String LOCAL_HOST_STR = "127.0.0.1";

    private static InetAddress localHost;

    @BeforeClass
    public static void init() throws UnknownHostException {
        localHost = InetAddress.getByName(LOCAL_HOST_STR);
    }

    @Test
    public void userAgentAndIpAddressBothAreNullError() throws IOException {
        final String userAgent = null;
        final InetAddress ipAddress = null;
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                assertThat(emptyClient().check(userAgent, ipAddress));
            }
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userAgent")
                .hasMessageContaining("ipAddress ");
    }

    @Test
    public void accurateAtIsNullError() throws IOException {
        final String userAgent = EMPTY;
        final InetAddress ipAddress = localHost;
        final Date accurateAt = null;
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() throws Throwable {
                assertThat(emptyClient().checkAt(userAgent, ipAddress, accurateAt));
            }
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("accurateAt");
    }

    @Test
    public void userAgentIsNull() throws IOException {
        final String nullUserAgent = null;

        assertBrowserResponse(
                emptyClient().check(nullUserAgent, localHost)
        );

        assertIpCheckResponse(
                client(LOCAL_HOST_STR, EMPTY, EMPTY).check(nullUserAgent, localHost)
        );
    }

    @Test
    public void userIpAddressIsNull() throws IOException {
        final InetAddress nullIpAddress = null;

        assertBrowserResponse(
                client(EMPTY, EMPTY, "Browser|1|0").check("browser", nullIpAddress)
        );
    }

    @Test
    public void ipRangesCheck() throws IOException {
        final String nullUserAgent = null;
        assertIpCheckResponse(
                client("127.0.0.1/16", EMPTY, EMPTY).check(nullUserAgent, localHost)
        );

        assertBrowserResponse(
                client("127.0.0.2", EMPTY, EMPTY).check(nullUserAgent, localHost)
        );
    }

    @Test
    public void includeUaCheck() throws IOException {
        assertIncludeUaResponse(
                client("127.0.0.2", EMPTY, "Browser|1|0").check("robot", localHost)
        );

        assertBrowserResponse(
                client("127.0.0.2", EMPTY, "Browser|1|0").check("browser", localHost)
        );
    }

    @Test
    public void excludeUaCheck() throws IOException {
        assertExcludeUaResponse(
                client(
                        "127.0.0.2",
                        "Tricky robot|1|exception||0|0", // exclude
                        "Tricky|1|0").check("tricky robot", localHost),
                PAGE_IMPRESSIONS
        );

        assertBrowserResponse(
                client(
                        "127.0.0.2",
                        "Tricky robot|1|exception||0|0", // exclude
                        "Tricky|1|0").check("tricky exception robot", localHost)
        );
    }

    @Test
    public void checkFromFiles() throws IOException {
        IabClient client = new IabClient(
                TestResources.ipExcludeCurrentFile(),
                TestResources.excludeCurrentFile(),
                TestResources.includeCurrentFile()
        );


        assertIpCheckResponse(
                client.check("tricky robot", InetAddress.getByName("192.127.245.128"))
        );

        assertIncludeUaResponse(
                client.check("tricky robot", InetAddress.getByName("192.127.245.129"))
        );

        final InetAddress ipAddressNull = null;
        assertIncludeUaResponse(
                client.check("xxx user agent at the start only", ipAddressNull)
        );

        assertIncludeUaResponse(
                client.checkAt("Some Inactive Browser", ipAddressNull, DateUtils.date(2017, 3, 30))
        );

        assertBrowserResponse(
                client.checkAt("Some Inactive Browser", ipAddressNull, DateUtils.date(2017, 3, 29))
        );

        assertExcludeUaResponse(
                client.check("user agent at the start only", ipAddressNull),
                PAGE_AND_AD_IMPRESSIONS
        );

        assertBrowserResponse(
                client.check("user agent at the start only User Agent Exclude", ipAddressNull)
        );
    }

    private static void assertIpCheckResponse(IabResponse response) {
        assertResponse(
                response,
                true,
                SPIDER_OR_ROBOT,
                FAILED_IP_EXCLUDE,
                UNKNOWN
        );
    }

    private static void assertBrowserResponse(IabResponse response) {
        assertResponse(
                response,
                false,
                BROWSER,
                PASSED_ALL,
                NONE
        );
    }

    private static void assertIncludeUaResponse(IabResponse response) {
        assertResponse(
                response,
                true,
                SPIDER_OR_ROBOT,
                FAILED_UA_INCLUDE,
                UNKNOWN
        );
    }

    private static void assertExcludeUaResponse(IabResponse response, PrimaryImpact impact) {
        assertResponse(
                response,
                true,
                ACTIVE_SPIDER_OR_ROBOT,
                FAILED_UA_EXCLUDE,
                impact
        );
    }

    private static void assertResponse(IabResponse response,
                                       boolean spiderOrRobot,
                                       UserAgentCategory category,
                                       CheckReason reason,
                                       PrimaryImpact primaryImpact) {
        assertThat(response.isSpiderOrRobot()).isEqualTo(spiderOrRobot);
        assertThat(response.getCategory()).isEqualTo(category);
        assertThat(response.getReason()).isEqualTo(reason);
        assertThat(response.getPrimaryImpact()).isEqualTo(primaryImpact);
    }

    private static IabClient emptyClient() throws IOException {
        return client(EMPTY, EMPTY, EMPTY);
    }

    private static IabClient client(String ipFile,
                                    String excludeUserAgentFile,
                                    String includeUserAgentFile) throws IOException {
        return new IabClient(
                asInputStream(ipFilePrefix(ipFile)),
                asInputStream(dummyRecordPrefix(excludeUserAgentFile)),
                asInputStream(dummyRecordPrefix(includeUserAgentFile)));
    }

}
