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
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.io.IOUtils.buffer;


public class IpRanges {

    private final List<SubnetUtils.SubnetInfo> cidrAddresses = new ArrayList<>();

    private final Set<String> plainAddresses = new HashSet<>();

    public IpRanges(InputStream stream) throws IOException {
        try {
            parseRecords(stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void parseRecords(InputStream stream) throws IOException {
        LineIterator it = IOUtils.lineIterator(buffer(stream), IabFile.CHARSET);
        while (it.hasNext()) {
            String record = StringUtils.trimToNull(it.nextLine());
            if (record != null) {
                if (isCidrNotation(record)) {
                    cidrAddresses.add(subnetInfo(record));
                } else {
                    plainAddresses.add(record);
                }
            }
        }
    }

    private static boolean isCidrNotation(String address) {
        return address.contains("/");
    }

    private static SubnetUtils.SubnetInfo subnetInfo(String address) {
        SubnetUtils result = new SubnetUtils(address);
        result.setInclusiveHostCount(true);
        return result.getInfo();
    }

    public boolean belong(InetAddress ipAddress) {
        String checkedIp = ipAddress.getHostAddress();

        if (plainAddresses.contains(checkedIp)) {
            return true;
        }

        for (SubnetUtils.SubnetInfo subnetInfo : cidrAddresses) {
            if (subnetInfo.isInRange(checkedIp)) {
                    return true;
                }
        }

        return false;
    }

}

