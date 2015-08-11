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

package com.ibm.watson.app.qaclassifier.services;

import org.junit.Test;

import com.ibm.watson.app.qaclassifier.services.rest.UserTracking.InputMode;

/**
 * Abstract base class for user tracking tests.
 * 
 * This allows us to use the same testcases and test at both the 
 * REST API level (which also tests the conversation and feedback APIs) 
 * and by calling the tracking service directly.
 *
 */
public abstract class AbstractUserTrackingIT {

    private AbstractUserTrackingIT GIVEN = this, WHEN = this, THEN = this, AND = this;

    @Test
    public void click_is_logged_when_answer_is_helpful() {
        GIVEN.a_question_is_answered_with_high_confidence();
        WHEN.user_clicks_this_is_helpful();
        THEN.the_positive_feedback_is_logged();
    }

    @Test
    public void click_is_logged_when_answer_is_unhelpful() {
        GIVEN.a_question_is_answered_with_high_confidence();
        WHEN.user_clicks_I_still_need_help();
        THEN.the_negative_feedback_is_logged();
    }

    @Test
    public void forum_visit_is_logged() {
        GIVEN.a_question_is_answered_with_low_confidence();
        AND.user_clicks_none_of_the_above();
        WHEN.user_clicks_visit_the_forum();
        THEN.the_forum_visit_is_logged();
    }

    @Test
    public void forum_visit_is_logged_when_no_answers_are_returned() {
        GIVEN.a_question_is_not_answered();
        WHEN.user_clicks_visit_the_forum();
        THEN.the_forum_visit_is_logged();
    }

    @Test
    public void impressions_are_logged_when_answer_is_high_conf() {
        WHEN.a_question_is_answered_with_high_confidence();
        THEN.the_top_answer_text_is_logged();
        AND.the_feedback_buttons_are_logged();
    }

    @Test
    public void impressions_are_logged_when_answer_is_low_conf() {
        WHEN.a_question_is_answered_with_low_confidence();
        THEN.the_canonical_questions_are_logged();
        AND.none_of_the_above_is_logged();
    }

    @Test
    public void latest_top_question_referral_is_logged_when_top_questions_are_redisplayed() {
        GIVEN.the_top_questions_are_displayed();
        AND.a_question_is_clicked();
        AND.the_top_questions_are_redisplayed();
        WHEN.a_question_is_clicked();
        THEN.the_recorded_mode_is(InputMode.CLICKED);
        AND.the_two_questions_have_different_referrers();
    }

    @Test
    public void mode_is_CLICKED_when_user_clicks_question() {
        GIVEN.the_top_questions_are_displayed();
        WHEN.a_question_is_clicked();
        THEN.the_recorded_mode_is(InputMode.CLICKED);
    }

    @Test
    public void mode_is_TYPED_when_user_types_question() {
        WHEN.a_question_is_typed();
        THEN.the_recorded_mode_is(InputMode.TYPED);
    }

    @Test
    public void no_referral_is_logged_when_user_types_another_question() {
        GIVEN.a_question_is_answered_with_high_confidence();
        WHEN.a_question_is_typed();
        THEN.two_queries_are_logged_with_no_referral();
    }

    @Test
    public void referral_is_logged_when_user_selects_refinement_question_after_I_still_need_help() {
        GIVEN.a_question_is_answered_with_high_confidence();
        AND.user_clicks_I_still_need_help();
        WHEN.user_clicks_a_refinement_question();
        THEN.the_refinement_click_is_logged();
        AND.the_refinement_query_has_a_referral();
    }


    protected abstract void a_question_is_answered_with_high_confidence();

    protected abstract void a_question_is_answered_with_low_confidence();
    
    protected abstract void a_question_is_not_answered();

    protected abstract void a_question_is_clicked();

    protected abstract void a_question_is_typed();

    protected abstract void none_of_the_above_is_logged();

    protected abstract void the_canonical_questions_are_logged();

    protected abstract void the_feedback_buttons_are_logged();

    protected abstract void the_forum_visit_is_logged();

    protected abstract void the_negative_feedback_is_logged();

    protected abstract void the_positive_feedback_is_logged();

    protected abstract void the_recorded_mode_is(InputMode mode);

    protected abstract void the_refinement_click_is_logged();

    protected abstract void the_refinement_query_has_a_referral();

    protected abstract void the_top_answer_text_is_logged();

    protected abstract void the_top_questions_are_displayed();

    protected abstract void the_top_questions_are_redisplayed();

    protected abstract void the_two_questions_have_different_referrers();

    protected abstract void two_queries_are_logged_with_no_referral();

    protected abstract void user_clicks_a_refinement_question();

    protected abstract void user_clicks_I_still_need_help();

    protected abstract void user_clicks_none_of_the_above();

    protected abstract void user_clicks_this_is_helpful();

    protected abstract void user_clicks_visit_the_forum();
}
