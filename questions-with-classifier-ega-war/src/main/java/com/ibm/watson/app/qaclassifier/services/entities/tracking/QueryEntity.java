/*
 * Copyright IBM Corp. 2015
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.ibm.watson.app.qaclassifier.services.rest.UserTracking.InputMode;

@Entity
@DiscriminatorValue(value = "QUERY")
public class QueryEntity extends TrackingEventEntity {

    @OneToMany(mappedBy = "referral", targetEntity = ImpressionEntity.class, fetch = FetchType.LAZY)
    private Set<ImpressionEntity> impressions;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private ClickEntity referral;

    @Column(name = "STRING", length = 1024)
    private String string;

    @Column(name = "MODE")
    @Enumerated(EnumType.STRING)
    private InputMode mode;

    public QueryEntity() {
    }

    public QueryEntity(Date timestamp, String user, String host, ClickEntity referral, String string, InputMode mode) {
        super(timestamp, user, host);
        this.referral = referral;
        this.string = string;
        this.mode = mode;
    }

    @Override
    protected void checkRequiredFields() {
        checkRequiredField("string", string);
        checkRequiredField("mode", mode);
    }

    public Set<ImpressionEntity> getImpressions() {
        return impressions;
    }

    public ClickEntity getReferral() {
        return referral;
    }

    public String getString() {
        return string;
    }

    public InputMode getMode() {
        return mode;
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("referral", this.referral == null ? null : this.referral.getId())
                .append("string", this.string)
                .append("mode", this.mode)
                .toString();
    }
}
