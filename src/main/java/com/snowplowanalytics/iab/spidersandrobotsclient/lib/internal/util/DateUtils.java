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


import org.apache.commons.lang3.StringUtils;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class DateUtils {

    private static final Locale LOCALE = Locale.getDefault();

    private DateUtils() {

    }

    public static Date now() {
        return new Date();
    }

    public static Date date(int year, int month, int dayOfMonth) {
        // return new Date(year, month - 1, dayOfMonth);
        return new GregorianCalendar(year, month - 1, dayOfMonth).getTime();
    }

    public static Date parseQuietly(final String date, final String pattern) {
        try {
            return StringUtils.isBlank(date) ? null : parse(date, pattern);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date parse(String date, String pattern) throws ParseException {
        return parse(date, DateFormatHolder.getFormat(pattern));
    }

    private static Date parse(final String date, final DateFormat format) throws ParseException {
        return format.parse(date);
    }

    private static final class DateFormatHolder {

        private static final ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>> FORMATS =
                new ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>>() {
                    @Override
                    protected SoftReference<Map<String, SimpleDateFormat>> initialValue() {
                        return new SoftReference<Map<String, SimpleDateFormat>>(
                                new HashMap<String, SimpleDateFormat>());
                    }
                };

        private DateFormatHolder() {

        }

        private static DateFormat getFormat(final String pattern) {
            final SoftReference<Map<String, SimpleDateFormat>> ref = FORMATS.get();
            Map<String, SimpleDateFormat> formats = ref.get();
            if (formats == null) {
                formats = new HashMap<>();
                FORMATS.set(new SoftReference<>(formats));
            }
            SimpleDateFormat format = formats.get(pattern);
            if (format == null) {
                format = new SimpleDateFormat(pattern, LOCALE);
                formats.put(pattern, format);
            }
            return format;
        }
    }

}
