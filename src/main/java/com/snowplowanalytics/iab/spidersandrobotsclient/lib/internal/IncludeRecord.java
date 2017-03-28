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

import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.CsvUtils;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.IabFile;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class IncludeRecord {

    private final String userAgentPattern;

    private final boolean active;

    private final boolean startOfStringFlag;

    private final String inactiveDate;

    public IncludeRecord(CSVRecord record) {
        userAgentPattern = CsvUtils.getLowCaseString(record, 0);
        active = CsvUtils.getBoolean(record, 1, false);
        startOfStringFlag = CsvUtils.getBoolean(record, 2, false);
        inactiveDate = CsvUtils.getString(record, 3);
    }

    public boolean isPresent(String userAgentLowCase) {
        return startOfStringFlag ? StringUtils.startsWith(userAgentLowCase, userAgentPattern) :
                StringUtils.contains(userAgentLowCase, userAgentPattern);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isBeforeInactiveDate(Date accurateAt) {
        Date inactiveDate = getInactiveDateAsDate();
        return inactiveDate != null && accurateAt.compareTo(inactiveDate) < 0;
    }

    private Date getInactiveDateAsDate() {
        return IabFile.parseDate(inactiveDate);
    }

}
