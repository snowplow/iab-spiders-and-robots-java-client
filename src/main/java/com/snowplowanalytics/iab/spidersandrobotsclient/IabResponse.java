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
package com.snowplowanalytics.iab.spidersandrobotsclient;

import com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact;
import com.snowplowanalytics.iab.spidersandrobotsclient.lib.UserAgentCategory;

import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason.FAILED_IP_EXCLUDE;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason.FAILED_UA_EXCLUDE;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason.FAILED_UA_INCLUDE;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.CheckReason.PASSED_ALL;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact.NONE;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact.UNKNOWN;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.UserAgentCategory.BROWSER;
import static com.snowplowanalytics.iab.spidersandrobotsclient.lib.UserAgentCategory.SPIDER_OR_ROBOT;

public class IabResponse {

    private final boolean spiderOrRobot;

    private final UserAgentCategory category;

    private final CheckReason reason;

    private final PrimaryImpact primaryImpact;

    private IabResponse(boolean spiderOrRobot, UserAgentCategory category, CheckReason reason,
                        PrimaryImpact primaryImpact) {
        this.spiderOrRobot = spiderOrRobot;
        this.category = category;
        this.reason = reason;
        this.primaryImpact = primaryImpact;
    }

    public boolean isSpiderOrRobot() {
        return spiderOrRobot;
    }

    public UserAgentCategory getCategory() {
        return category;
    }

    public CheckReason getReason() {
        return reason;
    }

    public PrimaryImpact getPrimaryImpact() {
        return primaryImpact;
    }

    static IabResponse identifiedAsBrowser() {
        final boolean spiderOrRobot = false;
        return new IabResponse(spiderOrRobot, BROWSER, PASSED_ALL, NONE);
    }

    static IabResponse ipCheckFailed() {
        return createForSpiderOrRobot(SPIDER_OR_ROBOT, FAILED_IP_EXCLUDE, UNKNOWN);
    }

    static IabResponse includeCheckFailed() {
        return createForSpiderOrRobot(SPIDER_OR_ROBOT, FAILED_UA_INCLUDE, UNKNOWN);
    }

    public static IabResponse excludeCheckFailed(UserAgentCategory category, PrimaryImpact primaryImpact) {
        return createForSpiderOrRobot(category, FAILED_UA_EXCLUDE, primaryImpact);
    }

    private static IabResponse createForSpiderOrRobot(UserAgentCategory category, CheckReason reason,
                                                      PrimaryImpact primaryImpact) {
        final boolean spiderOrRobot = true;
        return new IabResponse(spiderOrRobot, category, reason, primaryImpact);
    }

}
