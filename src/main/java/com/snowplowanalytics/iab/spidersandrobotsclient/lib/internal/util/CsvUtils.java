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
package com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public final class CsvUtils {

    private CsvUtils() {

    }

    public static String getLowCaseString(CSVRecord record, int index) {
        return IabFile.toLowerCase(getString(record, index));
    }

    public static String getString(CSVRecord record, int index) {
        return index < record.size() ? StringUtils.trim(record.get(index)) : null;
    }

    public static boolean getBoolean(CSVRecord record, int index, boolean defaultValue) {
        Boolean result = getBoolean(record, index);
        return result == null ? defaultValue : result;
    }

    public static Boolean getBoolean(CSVRecord record, int index) {
        return IabFile.toBoolean(getString(record, index));
    }

    public static Integer getInteger(CSVRecord record, int index) {
        return toInteger(getString(record, index));
    }

    public static Integer toInteger(String str) {
        if (str == null) {
            return null;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException nfe) {
            return null;
        }
    }

}
