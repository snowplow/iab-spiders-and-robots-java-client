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

import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.IabFile;
import inet.ipaddr.IPAddressString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.io.IOUtils.buffer;


public class IpRanges {

    private final Set<String> cidrAddresses = new HashSet<>();

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
                    cidrAddresses.add(record);
                } else {
                    plainAddresses.add(record);
                }
            }
        }
    }

    private static boolean isCidrNotation(String address) {
        return address.contains("/");
    }

    public boolean belong(InetAddress ipAddress) {
        String hostAddress = ipAddress.getHostAddress();
        IPAddressString ipAddressString = new IPAddressString(hostAddress);

        if (plainAddresses.contains(hostAddress)) {
            return true;
        }

        IPAddressString subnet;
        for (String address : cidrAddresses) {
            subnet = new IPAddressString(address);
            if (subnet.getAddress().toPrefixBlock().contains(ipAddressString.getAddress())) {
                return true;
            }
        }
        return false;
    }
}
