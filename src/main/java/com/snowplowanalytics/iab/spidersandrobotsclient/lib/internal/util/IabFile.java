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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;

public final class IabFile {

    private static final char PIPE = '|';

    public static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    private static final Locale LOCALE = Locale.ENGLISH;

    private static final String FALSE_STRING = "0";

    private static final String TRUE_STRING = "1";

    private static final String INACTIVE_DATE_FORMAT = "MM/dd/yyyy";

    private static final char COMMENT = '#';

    private static final CSVFormat DEFAULT_FORMAT = CSVFormat.DEFAULT.withDelimiter(PIPE)
            .withCommentMarker(COMMENT);

    private IabFile() {

    }

    public static CSVParser createParser(InputStream stream) throws IOException {
        return createParser(stream, DEFAULT_FORMAT);
    }

    private static CSVParser createParser(InputStream stream, CSVFormat format) throws IOException {
        return new CSVParser(new InputStreamReader(stream, CHARSET), format);
    }

    public static Boolean toBoolean(String booleanString) {
        if (IabFile.FALSE_STRING.equals(booleanString)) {
            return false;
        }

        if (IabFile.TRUE_STRING.equals(booleanString)) {
            return true;
        }

        return null;

    }

    public static String toBooleanStr(boolean value) {
        return value ? TRUE_STRING : FALSE_STRING;
    }

    public static Date parseDate(String date) {
        return DateUtils.parseQuietly(date, INACTIVE_DATE_FORMAT);
    }

    public static String toLowerCase(String value) {
        return StringUtils.lowerCase(value, LOCALE);
    }

}
