package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
class UserBulkInsertTest {

    // 최종 생성 유저의 수
    private static final int TOTAL_COUNT = 5_000_000;

    // 한 번의 JDBC batch insert에서 처리할 데이터 수
    private static final int BATCH_SIZE = 10_000;

    // 해시 문자열 고정값
    private static final String DEFAULT_PASSWORD = "$2a$10$7EqJtq98hPqEX7fNZaFWoOHiVfG2sM4bxN8H4x0Q0M0Lr0JH01W9a";

    // 기본 권한
    private static final String DEFAULT_ROLE = "USER";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void bulkInsertFiveMillionUsers() {
        // 테스트 시작
        long startedAt = System.currentTimeMillis();

        for (int start = 0; start < TOTAL_COUNT; start += BATCH_SIZE) {
            int currentBatchSize = Math.min(BATCH_SIZE, TOTAL_COUNT - start);
            int batchStart = start;

            // insert 시작
            jdbcTemplate.batchUpdate(
                    """
                    INSERT INTO users (email, nickname, password, user_role)
                    VALUES (?, ?, ?, ?)
                    """,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            long sequence = (long) batchStart + index + 1;
                            String nickname = generateNickname(sequence);

                            ps.setString(1, "bulk_user_" + sequence + "@example.com");
                            ps.setString(2, nickname);
                            ps.setString(3, DEFAULT_PASSWORD);
                            ps.setString(4, DEFAULT_ROLE);
                        }

                        @Override
                        public int getBatchSize() {
                            return currentBatchSize;
                        }
                    }
            );

            int inserted = batchStart + currentBatchSize;
            System.out.println("insert 진행: " + inserted + "/" + TOTAL_COUNT);
        }

        long elapsedMillis = System.currentTimeMillis() - startedAt;
        System.out.println("bulk insert 완료, 총 소요 시간은 " + elapsedMillis + " ms");
    }

    // 닉네임 생성
    private String generateNickname(long sequence) {
        String randomPrefix = Long.toString(ThreadLocalRandom.current().nextLong(36L * 36L * 36L * 36L), 36);
        return "user_" + padLeft(randomPrefix, 4) + "_" + Long.toString(sequence, 36);
    }

    private String padLeft(String value, int targetLength) {
        if (value.length() >= targetLength) {
            return value;
        }

        // 부족한 길이만큼 앞에 0 채움
        StringBuilder builder = new StringBuilder(targetLength);
        for (int index = value.length(); index < targetLength; index++) {
            builder.append('0');
        }

        builder.append(value);
        return builder.toString();
    }
}
