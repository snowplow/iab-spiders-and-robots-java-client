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

import com.snowplowanalytics.iab.spidersandrobotsclient.test.TestResources;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;

public class IpRangesTest {

    @Test
    public void belongFromFile() throws IOException {
        IpRanges ranges = new IpRanges(TestResources.ipExcludeCurrent());

        assertThat(belong(ranges, "132.52.0.0")).isFalse();
        assertThat(belong(ranges, "215.151.101.210")).isTrue();
        assertThat(belong(ranges, "198.152.89.200")).isFalse();
        assertThat(belong(ranges, "131.52.0.5")).isTrue();
        assertThat(belong(ranges, "215.152.201.210")).isFalse();
        assertThat(belong(ranges, "192.127.245.128")).isTrue();
        assertThat(belong(ranges, "192.127.245.129")).isFalse();
        assertThat(belong(ranges, "12.0.0.0")).isTrue();
        assertThat(belong(ranges, "12.0.0.1")).isTrue();
        assertThat(belong(ranges, "12.255.255.255")).isTrue();
    }

    private static boolean belong(IpRanges ranges, String ipAddress) throws IOException {
        return ranges.belong(InetAddress.getByName(ipAddress));
    }

    @Test
    public void belong() throws IOException {
        assertThat(belong("132.52.0.0", "132.52.0.0")).isTrue();
        assertThat(belong("132.52.0.0", "132.52.120.0")).isFalse();

        assertThat(belong("132.52.0.0/15", "132.51.0.0")).isFalse();
        assertThat(belong("132.52.0.0/15", "132.52.0.0")).isTrue();
        assertThat(belong("132.52.0.0/15", "132.53.255.255")).isTrue();
        assertThat(belong("132.52.0.0/15", "132.54.0.0")).isFalse();

        assertThat(belong("217.152.89.223/27", "217.152.89.224")).isFalse();
        assertThat(belong("217.152.89.223/27", "217.152.89.223")).isTrue();
        assertThat(belong("217.152.89.223/27", "217.152.89.198")).isTrue();
        assertThat(belong("217.152.89.223/27", "217.152.89.192")).isTrue();
        assertThat(belong("217.152.89.223/27", "217.152.89.191")).isFalse();

        assertThat(belong("127.0.0.233/1", "0.0.0.0")).isTrue();
        assertThat(belong("127.0.0.223/1", "127.162.162.162")).isTrue();
        assertThat(belong("127.0.0.223/1", "127.255.255.255")).isTrue();
        assertThat(belong("127.0.0.223/1", "128.0.0.0")).isFalse();

        assertThat(belong("255.0.0.255/0", "210.162.5.0")).isTrue();

        assertThat(belong("147.1.2.223", "147.1.2.223")).isTrue();

        assertThat(belong("89.205.228.130/32", "89.205.228.129")).isFalse();
        assertThat(belong("89.205.228.130/32", "89.205.228.130")).isTrue();
        assertThat(belong("89.205.228.130/32", "89.205.228.131")).isFalse();

        assertThat(belong("89.205.228.130/31", "89.205.228.129")).isFalse();
        assertThat(belong("89.205.228.130/31", "89.205.228.130")).isTrue();
        assertThat(belong("89.205.228.130/31", "89.205.228.131")).isTrue();
        assertThat(belong("89.205.228.130/31", "89.205.228.132")).isFalse();

        assertThat(belong("::9", "147.1.9.93")).isFalse();
        assertThat(belong("217.152.89.67/15", "1000:abcd:0:6119::dead:beef")).isFalse();

        assertThat(belong("1000:abcd:0:6119::dead:beef/64", "1000:abcd:0:6119::0:0")).isTrue();
        assertThat(belong("1000:abcd:0:6119::0:0/128", "1000:abcd:0:6119::0:0")).isTrue();
        assertThat(belong("1000:abcd:0:6119::0:0/0", "aaaa:bbbb:0:6119::0:0")).isTrue();
    }

    private static boolean belong(String ipRecord, String ipAddress) throws IOException {
       return new IpRanges(TestResources.asInputStream(ipRecord)).belong(InetAddress.getByName(ipAddress));
    }
}
