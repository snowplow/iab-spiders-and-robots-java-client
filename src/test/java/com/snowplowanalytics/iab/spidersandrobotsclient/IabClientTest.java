/*
 * Copyright (c) 2017-2026 Snowplow Analytics Ltd. All rights reserved.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
    public void customIncludeUseragentsTakesPrecedence() throws IOException {
        List<String> includeUseragents = Arrays.asList("TrustedBot", "InternalMonitor");
        List<String> excludeUseragents = Collections.emptyList();

        IabClient client = clientWithCustomLists(EMPTY, EMPTY, EMPTY, excludeUseragents, includeUseragents);

        assertBrowserResponse(client.check("TrustedBot/1.0", localHost));
        assertBrowserResponse(client.check("My InternalMonitor Agent", localHost));
    }

    @Test
    public void customExcludeUseragentsClassifiesAsBot() throws IOException {
        List<String> includeUseragents = Collections.emptyList();
        List<String> excludeUseragents = Arrays.asList("BadBot", "Scraper");

        IabClient client = clientWithCustomLists(EMPTY, EMPTY, EMPTY, excludeUseragents, includeUseragents);

        assertCustomExcludeResponse(client.check("BadBot/1.0", localHost));
        assertCustomExcludeResponse(client.check("My Scraper Agent", localHost));
    }

    @Test
    public void customIncludeOverridesCustomExclude() throws IOException {
        List<String> includeUseragents = Arrays.asList("TrustedBot");
        List<String> excludeUseragents = Arrays.asList("Bot");

        IabClient client = clientWithCustomLists(EMPTY, EMPTY, EMPTY, excludeUseragents, includeUseragents);

        assertBrowserResponse(client.check("TrustedBot/1.0", localHost));
        assertCustomExcludeResponse(client.check("OtherBot/1.0", localHost));
    }

    @Test
    public void customListsAreCaseInsensitive() throws IOException {
        List<String> includeUseragents = Arrays.asList("trustedbot");
        List<String> excludeUseragents = Arrays.asList("badbot");

        IabClient client = clientWithCustomLists(EMPTY, EMPTY, EMPTY, excludeUseragents, includeUseragents);

        assertBrowserResponse(client.check("TRUSTEDBOT/1.0", localHost));
        assertBrowserResponse(client.check("TrustedBot/1.0", localHost));
        assertCustomExcludeResponse(client.check("BADBOT/1.0", localHost));
        assertCustomExcludeResponse(client.check("BadBot/1.0", localHost));
    }

    @Test
    public void customListsUseSubstringMatching() throws IOException {
        List<String> includeUseragents = Arrays.asList("trusted");
        List<String> excludeUseragents = Arrays.asList("bad");

        IabClient client = clientWithCustomLists(EMPTY, EMPTY, EMPTY, excludeUseragents, includeUseragents);

        assertBrowserResponse(client.check("MyTrustedAgent/1.0", localHost));
        assertCustomExcludeResponse(client.check("MyBadAgent/1.0", localHost));
    }

    @Test
    public void customListsTakePrecedenceOverIabFiles() throws IOException {
        List<String> includeUseragents = Arrays.asList("robot");
        List<String> excludeUseragents = Collections.emptyList();

        IabClient client = clientWithCustomLists(EMPTY, EMPTY, "Browser|1|0", excludeUseragents, includeUseragents);

        assertBrowserResponse(client.check("robot", localHost));
    }

    @Test
    public void emptyCustomListsBehaveAsOriginal() throws IOException {
        List<String> emptyList = Collections.emptyList();

        IabClient client = clientWithCustomLists(EMPTY, EMPTY, "Browser|1|0", emptyList, emptyList);

        assertBrowserResponse(client.check("browser", localHost));
        assertIncludeUaResponse(client.check("robot", localHost));
    }

    @Test
    public void customExcludeTakesPrecedenceOverIabIncludeFile() throws IOException {
        List<String> includeUseragents = Collections.emptyList();
        List<String> excludeUseragents = Arrays.asList("browser");

        IabClient client = clientWithCustomLists(EMPTY, EMPTY, "browser|1|0", excludeUseragents, includeUseragents);

        assertCustomExcludeResponse(client.check("browser", localHost));
    }

    @Test
    public void nullCustomListsBehaveAsEmpty() throws IOException {
        IabClient client = clientWithCustomLists(EMPTY, EMPTY, "Browser|1|0", null, null);

        assertBrowserResponse(client.check("browser", localHost));
        assertIncludeUaResponse(client.check("robot", localHost));
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

    private static void assertCustomExcludeResponse(IabResponse response) {
        assertResponse(
                response,
                true,
                ACTIVE_SPIDER_OR_ROBOT,
                FAILED_UA_EXCLUDE,
                PAGE_AND_AD_IMPRESSIONS
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

    private static IabClient clientWithCustomLists(String ipFile,
                                                   String excludeUserAgentFile,
                                                   String includeUserAgentFile,
                                                   List<String> excludeUseragents,
                                                   List<String> includeUseragents) throws IOException {
        return new IabClient(
                asInputStream(ipFilePrefix(ipFile)),
                asInputStream(dummyRecordPrefix(excludeUserAgentFile)),
                asInputStream(dummyRecordPrefix(includeUserAgentFile)),
                excludeUseragents,
                includeUseragents);
    }

}
