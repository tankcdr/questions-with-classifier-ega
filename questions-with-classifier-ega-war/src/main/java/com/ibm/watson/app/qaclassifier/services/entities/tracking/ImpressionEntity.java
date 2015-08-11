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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.ibm.watson.app.qaclassifier.services.jpa.EntityConstants;
import com.ibm.watson.app.qaclassifier.services.rest.UserTrackingLogger.Location;
import com.ibm.watson.app.qaclassifier.services.rest.UserTrackingLogger.Provenance;

@NamedQueries({
        @NamedQuery(
                /*
                 * All impressions have a Query referrer except top questions.
                 * We return the impressions in descending ID order because the top questions may have been
                 * displayed multiple times in the same conversation, and we're only interested in the most
                 * recent impressions.
                 */
                name = EntityConstants.QUERY_TOP_QUESTION_IMPRESSIONS,
                query = "SELECT im from ImpressionEntity im WHERE im.user = :user and im.referral IS NULL ORDER BY im.id DESC")
})
@Entity
@DiscriminatorValue(value = "IMPRESSION")
public class ImpressionEntity extends TrackingEventEntity {

    public static final int MAX_PROMPT_LENGTH = 1024;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private QueryEntity referral;

    @OneToOne(mappedBy = "referral", targetEntity = ClickEntity.class, fetch = FetchType.LAZY)
    private ClickEntity click;

    @Column(name = "PROMPT", length = MAX_PROMPT_LENGTH)
    private String prompt;

    @Column(name = "CLASS")
    private String classifiedClass;

    @Column(name = "OFFSET")
    private Integer offset;

    @Column(name = "ACTION")
    private String action;

    @Column(name = "LOCATION")
    @Enumerated(EnumType.STRING)
    private Location location;

    @Column(name = "PROVENANCE")
    @Enumerated(EnumType.STRING)
    private Provenance provenance;

    @Column(name = "CONFIDENCE")
    private Double confidence;

    public ImpressionEntity() {
    }

    public ImpressionEntity(Date timestamp, String user, String host, QueryEntity referral, String prompt,
            String classifiedClass, Integer offset, String action, Location location, Provenance provenance,
            Double confidence) {
        super(timestamp, user, host);
        this.referral = referral;
        this.prompt = prompt;
        this.classifiedClass = classifiedClass;
        this.offset = offset;
        this.action = action;
        this.location = location;
        this.provenance = provenance;
        this.confidence = confidence;
    }

    @Override
    protected void checkRequiredFields() {
        checkRequiredField("prompt", prompt);
        checkRequiredField("offset", offset);
        checkRequiredField("location", location);
        checkRequiredField("provenance", provenance);
        checkRequiredField("confidence", confidence);
    }

    @PrePersist
    private void truncatePrompt() {
        if (prompt.length() > 1024) {
            prompt = prompt.substring(0, 1021) + "...";
        }
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getClassifiedClass() {
        return classifiedClass;
    }

    public void setClassifiedClass(String classifiedClass) {
        this.classifiedClass = classifiedClass;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Provenance getProvenance() {
        return provenance;
    }

    public void setProvenance(Provenance provenance) {
        this.provenance = provenance;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public QueryEntity getReferral() {
        return referral;
    }

    public void setReferral(QueryEntity referral) {
        this.referral = referral;
    }

    public ClickEntity getClick() {
        return click;
    }

    public void setClick(ClickEntity click) {
        this.click = click;
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("referral", this.referral == null ? null : this.referral.getId())
                .append("prompt", this.prompt)
                .append("class", this.classifiedClass)
                .append("offset", this.offset)
                .append("action", this.action)
                .append("location", this.location)
                .append("provenance", this.provenance)
                .append("confidence", this.confidence)
                .toString();
    }
}
