package com.codeit.otboo.domain.notification.Unit;

import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.notification.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("🎯Unit Test >>> NotificationServiceImpl")
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private final TestFixture fixture = new TestFixture();

    @BeforeEach
    void setUp() {
    }



}