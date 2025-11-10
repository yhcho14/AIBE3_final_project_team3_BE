USE mysql_db;

INSERT INTO members (
    email, password, name, nickname, country, english_level, interest, description,
    role, membership_grade, last_sign_in_at, is_blocked, blocked_at, block_reason,
    is_deleted, deleted_at, created_at, modified_at
) VALUES (
             'test@example.com', 'password', '테스트', '테스트닉', 'KOR', 'HIGH', '테스트', NULL,
             'USER', 'BASIC', NULL, FALSE, NULL, NULL, FALSE, NULL, NOW(), NOW()
         );

# INSERT INTO translation_tags (code) VALUES
#                                         ('GRAMMAR'),
#                                         ('SPELLING'),
#                                         ('STRUCTURE'),
#                                         ('WORD_UNKNOWN');

INSERT INTO prompts (member_id, prompt_type, title, content, scenario_id, created_at, modified_at)
VALUES (1, 'PRE_SCRIPTED', '테스트 프롬프트', '테스트 프롬프트입니다.', 'SCENARIO_001', NOW(), NOW());

