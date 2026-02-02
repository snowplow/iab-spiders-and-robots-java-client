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
package com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal;

import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.DateUtils;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.IabFile;
import com.snowplowanalytics.iab.spidersandrobotsclient.test.TestResources;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class IncludeUserAgentsTest {

    @Test
    public void checkPatternStartsAtBeginning() throws IOException {
        final boolean startOfString = true;
        assertThat(agent("Age", startOfString).present("agent")).isTrue();
        assertThat(agent("AGE", startOfString).present("agent")).isTrue();
        assertThat(agent("age", startOfString).present("AGEnt")).isTrue();

        assertThat(agent("Age", startOfString).present("Some")).isFalse();
        assertThat(agent("gent", startOfString).present("agent")).isFalse();
    }

    @Test
    public void checkPatternStartsAnywhere() throws IOException {
        final boolean startOfString = false;
        assertThat(agent("Age", startOfString).present("some agent")).isTrue();
        assertThat(agent("AGE", startOfString).present("some agent")).isTrue();
        assertThat(agent("age", startOfString).present("some AGEnt")).isTrue();
        assertThat(agent("gent", startOfString).present("agent")).isTrue();
        assertThat(agent("Age", startOfString).present("Some")).isFalse();
    }

    @Test
    public void checkCommentedRecord() throws IOException {
        final boolean startOfString = false;
        assertThat(agent("#agent", startOfString).present("agent")).isFalse();
    }

    @Test
    public void checkActiveRecord() throws IOException {
        final boolean activeRecord = true;
        final boolean inactiveRecord = false;
        final String nullInactiveDate = null;

        assertThat(active("browser", activeRecord, nullInactiveDate).present("some browser")).isTrue();
        assertThat(active("browser", inactiveRecord, nullInactiveDate).present("some browser")).isFalse();

        final String date = "03/23/2017";

        Date accurateAtBefore = DateUtils.date(2017, 3, 22);
        assertThat(
                active("browser", inactiveRecord, date).present("some browser", accurateAtBefore)
        ).isTrue();

        Date accurateAtEqual = DateUtils.date(2017, 3, 23);
        assertThat(
                active("browser", inactiveRecord, date).present("some browser", accurateAtEqual)
        ).isFalse();

        Date accurateAtAfter = DateUtils.date(2017, 3, 24);
        assertThat(
                active("browser", inactiveRecord, date).present("some browser", accurateAtAfter)
        ).isFalse();
    }

    @Test
    public void checkRecordsFromFile() throws IOException {
        IncludeUserAgents agents = new IncludeUserAgents(TestResources.includeCurrent());

        assertThat(agents.present("xdroid some")).isTrue();

        assertThat(agents.present("user agent at the start only")).isTrue();
        assertThat(agents.present("xxx user agent at the start only")).isFalse();

        assertThat(agents.present("Commented browser")).isFalse();
    }

    private static IncludeUserAgents active(String userAgentPattern,
                                            boolean active, String inactiveDate) throws IOException {
        final boolean startOfString = false;
        return record(
                userAgentPattern,
                IabFile.toBooleanStr(active),
                IabFile.toBooleanStr(startOfString),
                inactiveDate
        );
    }

    private static IncludeUserAgents agent(String userAgentPattern,
                                           boolean startOfString) throws IOException {
        final boolean active = true;
        return record(userAgentPattern, IabFile.toBooleanStr(active), IabFile.toBooleanStr(startOfString));
    }

    private static IncludeUserAgents record(String... values) throws IOException {
        return new IncludeUserAgents(TestResources.firstDummyRecordAndRecord(values));
    }

}
