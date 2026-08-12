ALTER TABLE psychological_test_results
    ADD CONSTRAINT uk_psychological_test_results_user_id UNIQUE (user_id);