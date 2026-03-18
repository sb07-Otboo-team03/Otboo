package com.codeit.otboo.domain.util;


import com.codeit.otboo.domain.BaseEntity;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.notification.entity.Level;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.profile.entity.Gender;
import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.User;
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

    public DirectMessage mockDirectMessage(User sender, User receiver, String content, LocalDateTime createdAt) {
        DirectMessage dm = new DirectMessage(sender, receiver, content);

        return (DirectMessage) setReflection(dm, createdAt);
    }

    public Notification mockNotification(String title, String content, Level level, User receiver, LocalDateTime createdAt) {
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
            .name("testName")
            .build();

        ReflectionTestUtils.setField(profile, "gender", Gender.FEMALE);
        ReflectionTestUtils.setField(profile, "binaryContent", binaryContent);

        return (Profile) setReflection(profile,  profileTime);
    }

    public BinaryContent mockBinaryContent(LocalDateTime createdAt) {
        BinaryContent binaryContent = new BinaryContent("test", "image/png", 64L);

        return (BinaryContent) setReflection(binaryContent,  createdAt);
    }
}
