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
package com.snowplowanalytics.iab.spidersandrobotsclient;

import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.ExcludeCheckResult;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.ExcludeUserAgents;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.IncludeUserAgents;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.IpRanges;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.DateUtils;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.util.IabFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.UserAgentCategory.ACTIVE_SPIDER_OR_ROBOT;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.UserAgentCategory.INACTIVE_SPIDER_OR_ROBOT;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.IabClientErrors.accurateAtIsNullError;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.internal.IabClientErrors.userAgentAndIpAddressBothAreNullError;

/**
 * This class can be reused across lookups.
 */
public class IabClient {

    private IpRanges ipRanges;

    private IncludeUserAgents includeUserAgents;

    private ExcludeUserAgents excludeUserAgents;

    private final List<String> customIncludeUserAgents;

    private final List<String> customExcludeUserAgents;

    public IabClient(File ipFile,
                     File excludeUserAgentFile,
                     File includeUserAgentFile) throws IOException {
        this(ipFile, excludeUserAgentFile, includeUserAgentFile,
             Collections.emptyList(), Collections.emptyList());
    }

    public IabClient(File ipFile,
                     File excludeUserAgentFile,
                     File includeUserAgentFile,
                     List<String> excludeUserAgents,
                     List<String> includeUserAgents) throws IOException {
        try (InputStream ip = FileUtils.openInputStream(ipFile);
             InputStream excludeUserAgent = FileUtils.openInputStream(excludeUserAgentFile);
             InputStream includeUserAgent = FileUtils.openInputStream(includeUserAgentFile)) {
            init(ip, excludeUserAgent, includeUserAgent);
        }
        this.customExcludeUserAgents = toLowerCaseList(excludeUserAgents);
        this.customIncludeUserAgents = toLowerCaseList(includeUserAgents);
    }

    IabClient(InputStream ip,
              InputStream excludeUserAgent,
              InputStream includeUserAgent) throws IOException {
        this(ip, excludeUserAgent, includeUserAgent,
             Collections.emptyList(), Collections.emptyList());
    }

    IabClient(InputStream ip,
              InputStream excludeUserAgent,
              InputStream includeUserAgent,
              List<String> excludeUserAgents,
              List<String> includeUserAgents) throws IOException {
        init(ip, excludeUserAgent, includeUserAgent);
        this.customExcludeUserAgents = toLowerCaseList(excludeUserAgents);
        this.customIncludeUserAgents = toLowerCaseList(includeUserAgents);
    }

    private void init(InputStream ip,
                      InputStream excludeUserAgent,
                      InputStream includeUserAgent) throws IOException {
        try {
            ipRanges = new IpRanges(ip);
            includeUserAgents = new IncludeUserAgents(includeUserAgent);
            excludeUserAgents = new ExcludeUserAgents(excludeUserAgent);
        } finally {
            IOUtils.closeQuietly(ip, excludeUserAgent, includeUserAgent);
        }
    }

    private static List<String> toLowerCaseList(List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(patterns.size());
        for (String pattern : patterns) {
            if (pattern != null) {
                result.add(IabFile.toLowerCase(pattern));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public IabResponse check(String userAgent, InetAddress ipAddress) {
        return checkAt(userAgent, ipAddress, DateUtils.now());
    }

    public IabResponse checkAt(String userAgent, InetAddress ipAddress, Date accurateAt) {
        assertCheckAtArguments(userAgent, ipAddress, accurateAt);

        String userAgentLower = IabFile.toLowerCase(userAgent);

        if (matchesAny(userAgentLower, customIncludeUserAgents)) {
            return IabResponse.identifiedAsBrowser();
        }

        if (matchesAny(userAgentLower, customExcludeUserAgents)) {
            return IabResponse.customExcludeCheckFailed();
        }

        if (ipAddress != null && ipRanges.belong(ipAddress)) {
            return IabResponse.ipCheckFailed();
        }

        if (userAgent == null) {
            return IabResponse.identifiedAsBrowser();
        }

        if (!includeUserAgents.presentLowerCase(userAgentLower, accurateAt)) {
            return IabResponse.includeCheckFailed();
        }

        IabResponse excludeResponse = toIabResponse(excludeUserAgents.checkLowerCase(userAgentLower), accurateAt);
        return excludeResponse == null ? IabResponse.identifiedAsBrowser() : excludeResponse;
    }

    private static boolean matchesAny(String userAgentLower, List<String> patterns) {
        for (String pattern : patterns) {
            if (StringUtils.contains(userAgentLower, pattern)) {
                return true;
            }
        }
        return false;
    }

    private static void assertCheckAtArguments(String userAgent, InetAddress ipAddress, Date accurateAt) {
        if (userAgent == null && ipAddress == null) {
            throw userAgentAndIpAddressBothAreNullError();
        }

        if (accurateAt == null) {
            throw accurateAtIsNullError();
        }
    }

    private static IabResponse toIabResponse(ExcludeCheckResult result, Date accurateAt) {
        if (!result.isPresent()) {
            return null;
        }

        if (result.inactiveDateIsNotSet() || result.isBeforeInactiveDate(accurateAt)) {
            return IabResponse.excludeCheckFailed(ACTIVE_SPIDER_OR_ROBOT, result.getPrimaryImpact());
        }

        return IabResponse.excludeCheckFailed(INACTIVE_SPIDER_OR_ROBOT, result.getPrimaryImpact());
    }

}
