package com.codeit.otboo.domain.directmessage.util;


import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageDto;
import com.codeit.otboo.domain.directmessage.dto.DirectMessageResponse;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.profile.dto.response.ProfileResponse;
import com.codeit.otboo.domain.profile.entity.Gender;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.dto.response.UserResponse;
import com.codeit.otboo.domain.user.dto.response.UserSummaryResponse;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

@Component
public class TestFixture {

    public UUID getRandomID(){
        return UUID.randomUUID();
    }

    public BaseEntity setReflection(BaseEntity entity, LocalDateTime createdAt) {
        ReflectionTestUtils.setField(entity, "id", getRandomID());
        ReflectionTestUtils.setField(entity, "createdAt", createdAt);
        return entity;
    }

    public User mockUserWithProfile(LocalDateTime createdAt) {
        User user = new User(getRandomID().toString() + "@naver.com", "123");

        setReflection(user, createdAt);
        mockProfile(user, createdAt.minusSeconds(1));

        return user;
    }

    public UserResponse mockUserResponse(
        User user,
        Profile profile,
        LocalDateTime createdAt) {

        return new UserResponse(
            user.getId(),
            createdAt,
            user.getEmail(),
            profile.getName(),
            Role.USER,
            false
        );
    }

    public DirectMessage mockDirectMessage(
        User sender,
        User receiver,
        String content,
        LocalDateTime createdAt) {

        DirectMessage directMessage = new DirectMessage(sender, receiver, content);

        return (DirectMessage) setReflection(directMessage, createdAt);
    }

    public DirectMessageDto mockDirectMessageDto(
        DirectMessage directMessage,
        User sender,
        User receiver,
        LocalDateTime createdAt) {

        UserSummaryResponse senderSummary = mockUserSummaryResponse(sender);
        UserSummaryResponse receiverSummary = mockUserSummaryResponse(receiver);

        DirectMessageDto directMessageDto = new DirectMessageDto(
            directMessage.getId(),
            createdAt,
            sender.getId(),
            senderSummary.name(),
            sender.getProfile().getBinaryContent().getId(),
            receiver.getId(),
            receiverSummary.name(),
            receiver.getProfile().getBinaryContent().getId(),
            "content..content..content.."
            );

        return directMessageDto;
    }

    public DirectMessageDto mockDirectMessageDtoWithTime(LocalDateTime createdAt) {

        User sender = mockUserWithProfile(createdAt.minusSeconds(1));
        User receiver = mockUserWithProfile(createdAt.minusSeconds(2));

        DirectMessage directMessage = mockDirectMessage(
            sender,
            receiver,
            "test message",
            createdAt
        );

        return mockDirectMessageDto(
            directMessage,
            sender,
            receiver,
            createdAt
        );
    }

    public UserSummaryResponse mockUserSummaryResponse(User user) {

        String senderProfileImageUrl = "/images/" + user.getProfile().getBinaryContent().getId().toString();

        UserSummaryResponse userSummaryResponse = new UserSummaryResponse(
            user.getId(),
            user.getProfile().getName(),
            senderProfileImageUrl
        );

        return userSummaryResponse;
    }


    public DirectMessageResponse mockDirectMessageResponse(LocalDateTime createdAt) {
        User sender = mockUserWithProfile(createdAt.minusSeconds(1));
        User receiver = mockUserWithProfile(createdAt.minusSeconds(2));

        UserSummaryResponse senderSummary = mockUserSummaryResponse(sender);
        UserSummaryResponse receiverSummary = mockUserSummaryResponse(receiver);

        DirectMessage directMessage = mockDirectMessage(
            sender,
            receiver,
            "directMessage.content",
            createdAt
        );

        return new DirectMessageResponse(
            directMessage.getId(),
            directMessage.getCreatedAt(),
            senderSummary,
            receiverSummary,
            directMessage.getContent()
        );
    }

    public Notification mockNotification(
        String title,
        String content,
        NotificationLevel level,
        User receiver,
        LocalDateTime createdAt) {

        Notification notification = new Notification(title, content, level, receiver);

        return (Notification) setReflection(notification, createdAt);
    }

    public Follow mockFollow(User follower, User followee, LocalDateTime createdAt) {
        Follow follow = new Follow(follower, followee);

        return (Follow) setReflection(follow, createdAt);
    }

    public Profile mockProfile(User user, LocalDateTime createdAt) {
        LocalDateTime profileTime = createdAt.minusSeconds(1);
        LocalDateTime binaryTime = createdAt.minusSeconds(2);

        BinaryContent binaryContent = mockBinaryContent(binaryTime);

        Profile profile = Profile.builder()
            .user(user)
            .name("BOOM")
            .build();

        ReflectionTestUtils.setField(profile, "gender", Gender.FEMALE);
        ReflectionTestUtils.setField(profile, "binaryContent", binaryContent);

        return (Profile) setReflection(profile,  profileTime);
    }

    public ProfileResponse mockProfileResponse(Profile profile, User user, LocalDateTime createdAt) {
        UserResponse userResponse = mockUserResponse(user, profile, createdAt);

        return new ProfileResponse(
            userResponse.id(),
            profile.getName(),
            Gender.FEMALE,
            LocalDate.now(),
            null,
            0,
            "/imageUrl/Gasp.png"
        );
    }

    public BinaryContent mockBinaryContent(LocalDateTime createdAt) {
        BinaryContent binaryContent = new BinaryContent("BOOM", "Oh/png", 64L);

        return (BinaryContent) setReflection(binaryContent,  createdAt);
    }

    public NotificationDto.NotificationDtoBuilder notificationDtoBuilder() {
        return NotificationDto.builder()
            .id(UUID.randomUUID())
            .createdAt(LocalDateTime.now())
            .receiverId(UUID.randomUUID())
            .title("test title")
            .content("test content")
            .level(NotificationLevel.INFO);
    }

    public FollowDto followDto() {
        return new FollowDto(
            UUID.randomUUID(),
            LocalDateTime.now(),
            UUID.randomUUID(),
            "followerName",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "followeeName",
            UUID.randomUUID()
        );
    }
}
