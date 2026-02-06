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
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.io.IOUtils.buffer;

public class IncludeUserAgents {

    private final List<IncludeRecord> records = new ArrayList<>();

    public IncludeUserAgents(InputStream stream) throws IOException {
        try {
            parseRecords(stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private void parseRecords(InputStream stream) throws IOException {
        CSVParser parser = IabFile.createParser(buffer(stream));
        for (CSVRecord record : parser) {
            records.add(new IncludeRecord(record));
        }
    }

    boolean present(String userAgent) {
        return present(userAgent, DateUtils.now());
    }

    public boolean present(String userAgent, Date accurateAt) {
        return presentLowerCase(IabFile.toLowerCase(userAgent), accurateAt);
    }

    public boolean presentLowerCase(String userAgentLowerCase, Date accurateAt) {
        for (IncludeRecord record : records) {
            boolean present = record.isPresent(userAgentLowerCase);
            if (present && (record.isActive() || record.isBeforeInactiveDate(accurateAt))) {
                return true;
            }
        }

        return false;
    }

}
