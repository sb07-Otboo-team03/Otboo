package com.codeit.otboo.domain.directmessage.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

// @DataJdbcTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DisplayName("🎯Unit Test> DirectMessageRepository")
class DirectMessageRepositoryTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void findDirectMessageDtos() {
    }
}