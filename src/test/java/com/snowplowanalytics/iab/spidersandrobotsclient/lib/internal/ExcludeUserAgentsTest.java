/*
 * Copyright (c) 2017 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal;

import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.IabFile;
import com.snowplowanalytics.iab.spidersandrobotsclient.test.TestResources;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact.AD_IMPRESSIONS;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact.PAGE_AND_AD_IMPRESSIONS;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact.PAGE_IMPRESSIONS;
import static org.assertj.core.api.Assertions.assertThat;

public class ExcludeUserAgentsTest {

    @Test
    public void checkPatternStartsAtBeginning() throws IOException {
        final boolean startOfString = true;
        assertThat(agent("Age", startOfString).check("agent").isPresent()).isTrue();
        assertThat(agent("AGE", startOfString).check("agent").isPresent()).isTrue();
        assertThat(agent("age", startOfString).check("AGEnt").isPresent()).isTrue();

        assertThat(agent("Age", startOfString).check("Some").isPresent()).isFalse();
        assertThat(agent("gent", startOfString).check("agent").isPresent()).isFalse();
    }

    @Test
    public void checkPatternStartsAnywhere() throws IOException {
        final boolean startOfString = false;
        assertThat(agent("Age", startOfString).check("some agent").isPresent()).isTrue();
        assertThat(agent("AGE", startOfString).check("some agent").isPresent()).isTrue();
        assertThat(agent("age", startOfString).check("some AGEnt").isPresent()).isTrue();
        assertThat(agent("gent", startOfString).check("agent").isPresent()).isTrue();
        assertThat(agent("Age", startOfString).check("Some").isPresent()).isFalse();
    }

    @Test
    public void checkExceptionPatterns() throws IOException {
        assertThat(exception("Age", null).check("agent").isPresent()).isTrue();
        assertThat(exception("AGE", "").check("agent").isPresent()).isTrue();

        assertThat(exception("age", "age").check("age").isPresent()).isFalse();
        assertThat(exception("age", "some, age").check("age").isPresent()).isFalse();
        assertThat(exception("age", "some age").check("age").isPresent()).isTrue();

        assertThat(
                exception("Xeznam", "Rdition Xeznam, Rdition+Xeznam").check("Some Xeznam").isPresent()
        ).isTrue();
        assertThat(
                exception("Xeznam", "Rdition Xeznam, Rdition+Xeznam").check("Some Rdition Xeznam Yes").isPresent()
        ).isFalse();
        assertThat(
                exception("Xeznam", "Rdition Xeznam, Rdition+Xeznam").check("Some Rdition+Xeznam Yes").isPresent()
        ).isFalse();
        assertThat(
                exception("xcho", "bonxcho, XchoArena, Xchofon").check("Some Xchofon Yes").isPresent()
        ).isFalse();
    }

    @Test
    public void checkCommentedRecord() throws IOException {
        final boolean startOfString = false;
        assertThat(agent("#agent", startOfString).check("agent").isPresent()).isFalse();
    }

    @Test
    public void inactiveDate() throws IOException {
        assertThat(inactiveDate("agent", null).check("agent").inactiveDateIsNotSet()).isTrue();
        assertThat(inactiveDate("agent", "").check("agent").inactiveDateIsNotSet()).isTrue();
        assertThat(inactiveDate("agent", "incorrect date").check("agent").inactiveDateIsNotSet()).isTrue();

        final String date = "03/23/2017";

        Date accurateAtBefore = IabFile.parseDate("03/22/2017");
        assertThat(inactiveDate("agent", date).check("agent").isBeforeInactiveDate(accurateAtBefore)).isTrue();

        Date accurateAtEqual = IabFile.parseDate("03/23/2017");
        assertThat(inactiveDate("agent", date).check("agent").isBeforeInactiveDate(accurateAtEqual)).isFalse();

        Date accurateAtAfter = IabFile.parseDate("03/24/2017");
        assertThat(inactiveDate("agent", date).check("agent").isBeforeInactiveDate(accurateAtAfter)).isFalse();
    }

    @Test
    public void primaryImpact() throws IOException {
        assertThat(primaryImpact("agent", "0").check("agent").getPrimaryImpact())
                .isEqualTo(PAGE_IMPRESSIONS);
        assertThat(primaryImpact("agent", "1").check("agent").getPrimaryImpact())
                .isEqualTo(AD_IMPRESSIONS);
        assertThat(primaryImpact("agent", "2").check("agent").getPrimaryImpact())
                .isEqualTo(PAGE_AND_AD_IMPRESSIONS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void primaryImpactEmpty() throws IOException {
        primaryImpact("agent", "").check("agent");
    }

    @Test(expected = IllegalArgumentException.class)
    public void primaryImpactIllegal() throws IOException {
        primaryImpact("agent", "illegal impact").check("agent");
    }

    @Test
    public void checkRecordsFromFile() throws IOException {
        ExcludeUserAgents agents = new ExcludeUserAgents(TestResources.excludeCurrent());

        assertThat(agents.check("user agent xsoft url some").isPresent()).isTrue();
        assertThat(agents.check("user agent xsoft some").isPresent()).isFalse();

        assertThat(agents.check("user agent at the start only").isPresent()).isTrue();
        assertThat(agents.check("xxx user agent at the start only").isPresent()).isFalse();

        assertThat(agents.check("agent xcho some string").isPresent()).isTrue();
        assertThat(agents.check("exception: bonxcho agent xcho").isPresent()).isFalse();
        assertThat(agents.check("xcho exception: XchoSerena").isPresent()).isFalse();
        assertThat(agents.check("xcho exception: Xchofon").isPresent()).isFalse();

        assertThat(agents.check("Commented browser").isPresent()).isFalse();

        assertThat(agents.check("browser/5.0 (compatible; msie 5.0)").getPrimaryImpact())
                .isEqualTo(PAGE_AND_AD_IMPRESSIONS);
    }

    private static ExcludeUserAgents agent(String userAgentPattern,
                                           boolean startOfString) throws IOException {
        final String primaryImpactFlag = "0";
        return record(userAgentPattern, null, null, null, primaryImpactFlag,
                IabFile.toBooleanStr(startOfString), null);
    }

    private static ExcludeUserAgents exception(String userAgentPattern,
                                               String patterns) throws IOException {
        final String primaryImpactFlag = "0";
        final boolean startOfString = false;
        return record(userAgentPattern, null, patterns, null, primaryImpactFlag,
                IabFile.toBooleanStr(startOfString), null);
    }

    private static ExcludeUserAgents inactiveDate(String userAgentPattern,
                                                  String inactiveDate) throws IOException {
        final String primaryImpactFlag = "0";
        final boolean startOfString = false;
        return record(userAgentPattern, null, null, null, primaryImpactFlag,
                IabFile.toBooleanStr(startOfString), inactiveDate);
    }

    private static ExcludeUserAgents primaryImpact(String userAgentPattern,
                                                   String primaryImpactFlag) throws IOException {
        final boolean startOfString = false;
        return record(userAgentPattern, null, null, null, primaryImpactFlag,
                IabFile.toBooleanStr(startOfString), null);
    }

    private static ExcludeUserAgents record(String... values) throws IOException {
        return new ExcludeUserAgents(TestResources.firstDummyRecordAndRecord(values));
    }

}
