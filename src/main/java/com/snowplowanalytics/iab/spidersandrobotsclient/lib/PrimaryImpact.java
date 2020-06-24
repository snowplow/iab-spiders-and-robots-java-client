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
package com.snowplowanalytics.iab.spidersandrobotsclient.lib;

public enum PrimaryImpact {

    PAGE_IMPRESSIONS,

    AD_IMPRESSIONS,

    PAGE_AND_AD_IMPRESSIONS,

    UNKNOWN,

    NONE;

    public static PrimaryImpact fromPrimaryImpactFlag(String value) {
        switch (value) {
            case "0":
                return PAGE_IMPRESSIONS;
            case "1":
                return AD_IMPRESSIONS;
            case "2":
                return PAGE_AND_AD_IMPRESSIONS;
            default:
                throw new IllegalArgumentException(
                        String.format("Invalid value of impression flag: '%s'", value)
                );
        }
    }

}
