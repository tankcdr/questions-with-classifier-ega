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

package com.ibm.watson.app.qaclassifier.util.rest;

import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

import com.ibm.watson.app.common.util.rest.WatsonMessage;


public enum MessageKey {
    AQWQAC12001W_no_classifier_service(0),
    AQWQAC10101I_using_classifier_service_1(1),
    AQWQAC12002W_missing_required_conf_parameter_classification_unavail_1(1),
    AQWQAC12003W_question_not_in_top_question_store(0),
    AQWQAC12004W_question_not_in_response_cache_1(1),    
    AQWQAC14000E_could_not_find_classifier_service_in_conf_classification_unavail_1(1),
    AQWQAC12100W_could_not_resolve_answer_1(1),
    AQWQAC24000E_unable_load_conf_from_stream_using_empty_conf_service(0),
    AQWQAC24001E_unable_load_file_from_classpath_1(1),
    AQWQAC24150E_could_not_find_key_in_db_1(1),
    AQWQAC24100E_field_name_required_field_on_object_and_not_null_2(2),
    AQWQAC24151E_property_must_be_set_in_conf_1(1),
    AQWQAC24152E_invalid_count_value_specified_must_in_range_1(1),
    AQWQAC14101E_could_not_find_path_on_classpath_1(1),
    AQWQAC24002E_property_must_be_set_in_conf_1(1),
    AQWQAC24003E_invalid_threshold_value_specified(0),
    AQWQAC24004E_unable_determine_hostname(0),
    AQWQAC14200E_must_specify_4_files(0),
    AQWQAC14201E_file_does_not_exist_1(1),
    AQWQAC14202E_unable_create_parent_dir_for_file_1(1),
    AQWQAC24005E_error_populating_answer_store(0),
    AQWQAC24007E_error_populating_answer_store_1(1),
    AQWQAC24006E_invalid_schema_answers_json(0),
    AQWQAC24008E_could_not_parse_cmd_line_args_1(1),
    AQWQAC24009E_label_not_found_in_answer_store_including_2(2),
    AQWQAC20000I_reading_and_setting_formated_text_from_1(1),
    AQWQAC20001I_answer_store_already_populated_doing_nothing(0),
    AQWQAC20002I_checking_answer_store_at_url_2(2),
    AQWQAC20003I_populating_answer_store_at_url_2(2),
    AQWQAC24010E_answer_store_unable_to_load(0),
    AQWQAC20004I_answer_input_file_read(0),
    AQWQAC24005I_question_input_file_read(0),
    AQWQAC24006I_answer_output_file_written(0),
    AQWQAC24007I_training_data_file_written(0),
    AQWQAC20007I_answer_text_is_empty_for_entry_2(2),
    AQWQAC22000W_no_top_questions_found_in_file_1(1),
    AQWQAC24011E_exception_parsing_file_1(1),
    AQWQAC20005I_done_population_answers(0),
    AQWQAC20006I_found_answers_in_stop_1(1),
    AQWQAC10100I_reading_from_file_1(1),
    AQWQAC10101I_reading_from_classpath_1(1),
    AQWQAC20007I_starting_generate_training_and_populating(0),
    AQWQAC20008I_cmd_line_param_read(0),
    AQWQAC14001E_error_selection_correct_classifier(0),
    AQWQAC14002E_no_classifier_instances(0),
    AQWQAC14003E_no_available_classifiers(0),
    AQWQAC24012E_conf_service_null(0),
    
    ;

    private static final Logger logger = LogManager.getLogger();

    final static ResourceBundle bundle = ResourceBundle.getBundle("messages.messages");
    // final static LocalizedMessageFactory defaultMessageFactory = new
    // LocalizedMessageFactory(ResourceBundle.getBundle("messages.Messages"));

    final int expectedArgs;

    MessageKey(int n) {
        expectedArgs = n;
    }

    public Message getMessage() {
        return getMessage((Object[]) null);
    }

    public Message getMessage(Object... params) {
        if (expectedArgs != 0 && (params == null || params.length == 0)) {
            logger.error("ERROR: No arguments passed for message although message requires arguments.... " + name(), new Exception());
        } else if (expectedArgs == 0 && !(params == null || params.length == 0)) {
            logger.error("ERROR: Arguments passed for message although message expects no arguments... " + name(), new Exception());
        }
        return new WatsonMessage(bundle, name(), params);
    }
}
