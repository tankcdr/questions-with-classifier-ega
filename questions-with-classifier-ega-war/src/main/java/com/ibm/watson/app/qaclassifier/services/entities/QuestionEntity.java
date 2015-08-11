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

package com.ibm.watson.app.qaclassifier.services.entities;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "QUESTIONS")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ID")
    private long id;
   
    @Column(name = "TEXT", unique = true, nullable = false)
    private String text;
    
    @Column(name = "SUBMITTER", unique = false, nullable = false)
    private String submitter;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(
        name = "QA_MAPPING",
        joinColumns={@JoinColumn(name="QUESTION_ID", referencedColumnName="ID")},
        inverseJoinColumns={@JoinColumn(name="ANSWER_CLASS", referencedColumnName="CLASS")})
    private Set<AnswerEntity> classes; 

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public Set<AnswerEntity> getClasses() {
        return classes;
    }

    public void setClasses(Set<AnswerEntity> classes) {
        this.classes = classes;
    }
}
