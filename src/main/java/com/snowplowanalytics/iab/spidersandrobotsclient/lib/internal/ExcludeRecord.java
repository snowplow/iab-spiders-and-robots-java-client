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
package com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal;

import com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.CsvUtils;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.IabFile;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ExcludeRecord {

    private static final String EXCEPTION_PATTERNS_SEPARATOR = ",";

    private final String userAgentPattern;

    private final boolean active;

    private final String exceptionPatterns;

    private final String primaryImpactFlag;

    private final boolean startOfStringFlag;

    private final String inactiveDate;

    public ExcludeRecord(CSVRecord record) {
        userAgentPattern = CsvUtils.getLowCaseString(record, 0);
        active = CsvUtils.getBoolean(record, 1, false);
        exceptionPatterns = CsvUtils.getLowCaseString(record, 2);

        // isn't used, because of previous check for valid UA should fail for such record
        // additionalFlag = CsvUtils.getInteger(record, 3);

        primaryImpactFlag = CsvUtils.getString(record, 4);
        startOfStringFlag = CsvUtils.getBoolean(record, 5, false);
        inactiveDate = CsvUtils.getString(record, 6);
    }

    public boolean isPresent(String userAgentLowCase) {
        if (!active && inactiveDateIsNotSet()) {
            return false;
        }

        return isUserAgentPatternPresent(userAgentLowCase) && !isExceptionPatternPresent(userAgentLowCase);
    }

    private boolean isUserAgentPatternPresent(String userAgentLowCase) {
        return startOfStringFlag ? StringUtils.startsWith(userAgentLowCase, userAgentPattern) :
                StringUtils.contains(userAgentLowCase, userAgentPattern);
    }

    private boolean isExceptionPatternPresent(String userAgentLowCase) {
        List<String> patterns = toPatternsList(exceptionPatterns);

        for (String pattern : patterns) {
            if (StringUtils.contains(userAgentLowCase, pattern)) {
                return true;
            }
        }

        return false;
    }

    private List<String> toPatternsList(String patterns) {
        String[] parts = StringUtils.split(patterns, EXCEPTION_PATTERNS_SEPARATOR);
        if (ArrayUtils.isEmpty(parts)) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>(parts.length);

        for (String part : parts) {
            String pattern = StringUtils.trimToNull(part);
            if (pattern != null) {
                result.add(pattern);
            }
        }

        return result;
    }

    private boolean inactiveDateIsNotSet() {
        return getInactiveDateAsDate() == null;
    }

    public Date getInactiveDateAsDate() {
        return IabFile.parseDate(inactiveDate);
    }

    public PrimaryImpact getPrimaryImpact() {
        return PrimaryImpact.fromPrimaryImpactFlag(primaryImpactFlag);
    }

}
