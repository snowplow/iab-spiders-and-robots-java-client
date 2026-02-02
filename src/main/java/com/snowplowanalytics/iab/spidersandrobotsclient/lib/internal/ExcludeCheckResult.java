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

import com.snowplowanalytics.iab.spidersandrobotsclient.lib.PrimaryImpact;

import java.util.Date;

public class ExcludeCheckResult {

    private final boolean present;

    private final Date inactiveDate;

    private final PrimaryImpact primaryImpact;

    private ExcludeCheckResult(boolean present, Date inactiveDate, PrimaryImpact primaryImpact) {
        this.present = present;
        this.inactiveDate = inactiveDate;
        this.primaryImpact = primaryImpact;
    }

    public boolean isPresent() {
        return present;
    }

    public boolean inactiveDateIsNotSet() {
        return inactiveDate == null;
    }

    public boolean isBeforeInactiveDate(Date accurateAt) {
        return accurateAt.compareTo(inactiveDate) < 0;
    }

    public PrimaryImpact getPrimaryImpact() {
        return primaryImpact;
    }

    static ExcludeCheckResult present(Date inactiveDate, PrimaryImpact primaryImpact) {
        final boolean present = true;
        return new ExcludeCheckResult(present, inactiveDate, primaryImpact);
    }

    static ExcludeCheckResult notPresent() {
        final boolean present = false;
        final Date inactiveDate = null;
        final PrimaryImpact primaryImpact = null;
        return new ExcludeCheckResult(present, inactiveDate, primaryImpact);
    }

}
