/* Copyright IBM Corp. 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.watson.app.qaclassifier.services.entities.tracking;

import java.util.Date;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@DiscriminatorValue(value = "CLICK")
public class ClickEntity extends TrackingEventEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private ImpressionEntity referral;

    @OneToMany(mappedBy = "referral", targetEntity = QueryEntity.class, fetch = FetchType.LAZY)
    private Set<QueryEntity> queries;

    public ClickEntity() {
    }

    public ClickEntity(Date timestamp, String user, String host, ImpressionEntity referral) {
        super(timestamp, user, host);
        this.referral = referral;
    }

    @Override
    protected void checkRequiredFields() {
        // No required fields
    }

    public ImpressionEntity getReferral() {
        return referral;
    }

    public void setReferral(ImpressionEntity referral) {
        this.referral = referral;
    }

    public Set<QueryEntity> getQueries() {
        return queries;
    }

    public void setQueries(Set<QueryEntity> queries) {
        this.queries = queries;
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("referral", referral.getId())
                .toString();
    }
}
