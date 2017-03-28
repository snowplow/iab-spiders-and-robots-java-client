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
package com.snowplowanalytics.iab.spidersandrobotsclient.test;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class TestResources {

    private static final char PIPE = '|';

    private static final String DUMMY_RECORD = StringUtils.join(
            Arrays.asList("1", "2", "3", "4", "5", "6", "7"),
            PIPE
    );

    private TestResources() {

    }

    public static File ipExcludeCurrentFile() throws IOException {
        try (InputStream stream = ipExcludeCurrent()) {
            return copyToTmpFile(stream, "ip_exclude_current_cidr");
        }
    }

    public static File includeCurrentFile() throws IOException {
        try (InputStream stream = includeCurrent()) {
            return copyToTmpFile(stream, "include_current");
        }
    }

    public static File excludeCurrentFile() throws IOException {
        try (InputStream stream = excludeCurrent()) {
            return copyToTmpFile(stream, "exclude_current");
        }
    }

    private static File copyToTmpFile(InputStream stream, String fileName) throws IOException {
        File result = File.createTempFile(fileName, ".txt");
        FileUtils.copyInputStreamToFile(stream, result);
        return result;
    }

    public static InputStream ipExcludeCurrent() {
        return resourceByName("ip_exclude_current_cidr.txt");
    }

    public static InputStream includeCurrent() {
        return resourceByName("include_current.txt");
    }

    public static InputStream excludeCurrent() {
        return resourceByName("exclude_current.txt");
    }

    private static InputStream resourceByName(String name) {
        return TestResources.class.getClassLoader().getResourceAsStream(name);
    }

    public static InputStream firstDummyRecordAndRecord(String... values) {
        return asInputStream(dummyRecordPrefix(StringUtils.join(values, PIPE)));
    }

    public static String ipFilePrefix(String value) {
        return "0.0.0.0" + StringUtils.LF + value;
    }

    public static String dummyRecordPrefix(String value) {
        return DUMMY_RECORD + StringUtils.LF + value;
    }

    public static InputStream asInputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

}
