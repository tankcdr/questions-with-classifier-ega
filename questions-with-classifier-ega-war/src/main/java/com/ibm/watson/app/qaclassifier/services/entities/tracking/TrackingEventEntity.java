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

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.ibm.watson.app.qaclassifier.services.jpa.EntityConstants;
import com.ibm.watson.app.qaclassifier.util.rest.MessageKey;

@NamedQueries({
    @NamedQuery(
            name = EntityConstants.QUERY_CONVERSATION_EVENTS,
            query = "SELECT e from TrackingEventEntity e WHERE e.user = :user ORDER BY e.id ASC"),
    @NamedQuery(
            name = EntityConstants.QUERY_ALL_EVENTS,
            query = "SELECT e from TrackingEventEntity e ORDER BY e.id ASC")
})
@Entity
@Table(name = "TRACKING_EVENTS")
@DiscriminatorColumn(name = "EVENT_TYPE")
public abstract class TrackingEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ID")
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TIMESTAMP", nullable = false)
    private Date timestamp;

    @Column(name = "USER", nullable = false)
    private String user;

    @Column(name = "HOST", nullable = false)
    private String host;

    public TrackingEventEntity() {
    }

    public TrackingEventEntity(Date timestamp, String user, String host) {
        this.timestamp = timestamp;
        this.user = user;
        this.host = host;
    }

    protected void checkRequiredField(String fieldName, Object fieldValue) {
        if (fieldValue == null) {
            throw new IllegalArgumentException(
            		MessageKey.AQWQAC24100E_field_name_required_field_on_object_and_not_null_2.getMessage(fieldName, getClass().getName()).getFormattedMessage()
            		);
        }
    }

    public long getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * All events have an optional referrer.  Subclasses are left to implement this method
     * since each subclass limits its referral to a specific subclass type.
     * @return The referral for this event, or null if no referral exists
     */
    public abstract TrackingEventEntity getReferral();

    /**
     * Subclasses can't annotate fields as nullable=false because all subclass entities are
     * stored in the same table.  Subclasses should check required fields by calling
     * checkRequiredField(String, Object).
     * 
     * @throws IllegalArgumentException if a required field is not set
     */
    @PrePersist
    protected abstract void checkRequiredFields() throws IllegalArgumentException;

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("user", this.user)
                .append("host", this.host)
                .append("id", this.id)
                .toString();
    }
}
