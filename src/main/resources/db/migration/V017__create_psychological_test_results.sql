CREATE TABLE psychological_test_results (
                                            id BIGINT NOT NULL AUTO_INCREMENT,
                                            user_id BIGINT NOT NULL,

                                            mood_type VARCHAR(30) NOT NULL,
                                            location_type VARCHAR(30) NOT NULL,
                                            region VARCHAR(30),
                                            preparation_type VARCHAR(30) NOT NULL,

                                            budget_range VARCHAR(40) NOT NULL,
                                            schedule_range VARCHAR(30) NOT NULL,
                                            partner_mbti VARCHAR(10) NOT NULL,

                                            created_at DATETIME(6) NOT NULL,
                                            updated_at DATETIME(6) NOT NULL,

                                            PRIMARY KEY (id)
);


CREATE TABLE psychological_test_required_services (
                                                      psychological_test_result_id BIGINT NOT NULL,
                                                      required_service VARCHAR(30) NOT NULL,

                                                      CONSTRAINT uk_psychological_test_required_services
                                                          UNIQUE (psychological_test_result_id, required_service),

                                                      CONSTRAINT fk_psychological_test_required_services_result
                                                          FOREIGN KEY (psychological_test_result_id)
                                                              REFERENCES psychological_test_results (id)
                                                              ON DELETE CASCADE
);


CREATE TABLE psychological_test_priorities (
                                               psychological_test_result_id BIGINT NOT NULL,
                                               priority_value VARCHAR(30) NOT NULL,
                                               priority_order INT NOT NULL,

                                               CONSTRAINT fk_psychological_test_priorities_result
                                                   FOREIGN KEY (psychological_test_result_id)
                                                       REFERENCES psychological_test_results (id)
                                                       ON DELETE CASCADE
);


CREATE TABLE psychological_test_excluded_elements (
                                                      psychological_test_result_id BIGINT NOT NULL,
                                                      excluded_element VARCHAR(40) NOT NULL,

                                                      CONSTRAINT uk_psychological_test_excluded_elements
                                                          UNIQUE (psychological_test_result_id, excluded_element),

                                                      CONSTRAINT fk_psychological_test_excluded_elements_result
                                                          FOREIGN KEY (psychological_test_result_id)
                                                              REFERENCES psychological_test_results (id)
                                                              ON DELETE CASCADE
);