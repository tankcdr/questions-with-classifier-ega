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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.ibm.watson.app.qaclassifier.rest.model.Answer.TypeEnum;
import com.ibm.watson.app.qaclassifier.services.jpa.EntityConstants;

@NamedQueries({
@NamedQuery(name=EntityConstants.QUERY_ALL_ANSWERS,
    query="SELECT a FROM AnswerEntity a")          
})
@Entity
@Table(name = "ANSWERS")
public class AnswerEntity {    
    @Id
    @Column(name = "CLASS")
    private String answerClass;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", unique = false, nullable = false)
    private TypeEnum answerType;
    
    @Lob
    @Column(name = "TEXT", unique = false, nullable = false)
    private String answerText;
    
    @Column(name = "CANONICAL_QUESTION", unique = false, nullable = false)
    private String canonicalQuestion;

    public String getAnswerClass() {
        return answerClass;
    }

    public void setAnswerClass(String answerClass) {
        this.answerClass = answerClass;
    }

    public TypeEnum getAnswerType() {
        return answerType;
    }

    public void setAnswerType(TypeEnum answerType) {
        this.answerType = answerType;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getCanonicalQuestion() {
        return canonicalQuestion;
    }

    public void setCanonicalQuestion(String canonicalQuestion) {
        this.canonicalQuestion = canonicalQuestion;
    }

    @Override
    public String toString() {
        return "Answer [answerClass=" + answerClass + ", answerType=" + answerType + ", answerText=" + answerText + "]";
    }
}
