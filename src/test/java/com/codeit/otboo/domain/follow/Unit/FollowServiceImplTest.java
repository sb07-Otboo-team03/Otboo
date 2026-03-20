package com.codeit.otboo.domain.follow.Unit;

import com.codeit.otboo.domain.directmessage.util.TestFixture;
import com.codeit.otboo.domain.follow.mapper.FollowMapper;
import com.codeit.otboo.domain.follow.repository.FollowRepository;
import com.codeit.otboo.domain.follow.service.FollowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("🎯Unit Test >>> FollowServiceImpl")
@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    FollowMapper followMapper;

    @InjectMocks
    private FollowServiceImpl followService;

    private final TestFixture fixture = new TestFixture();

    @BeforeEach
    void setUp() {
    }


}