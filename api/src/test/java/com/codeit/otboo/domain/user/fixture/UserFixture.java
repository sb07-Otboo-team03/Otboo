package com.codeit.otboo.domain.user.fixture;

import com.codeit.otboo.domain.profile.entity.Profile;
import com.codeit.otboo.domain.user.entity.Role;
import com.codeit.otboo.domain.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserFixture {
    private static int num = 1;

    public static User create() {
        User newUser = User.builder()
                .email("tester"+ num +"@test.com")
                .password("Qwer123$")
                .build();
        ReflectionTestUtils.setField(newUser, "id", UUID.randomUUID());
        num++;

        return newUser;
    }

    public static User createAdmin(){
        User newUser = create();
        ReflectionTestUtils.setField(newUser, "role", Role.ADMIN);
        return newUser;
    }

    public static User create(String email, String password){
        User newUser = User.builder()
                .email(email)
                .password(password)
                .build();
        ReflectionTestUtils.setField(newUser, "id", UUID.randomUUID());
        num++;

        return newUser;
    }

    public static User create(UUID userId, String email, String password) {
        User user = User.builder()
                .email(email)
                .password(password)
                .build();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        ReflectionTestUtils.setField(user,"createdAt", createdAt);
        ReflectionTestUtils.setField(user,"id", userId);

        return user;
    }

    public static List<User> createUserCursor(int count) {
        List<User> userList = new ArrayList<>();

        for(int i=0 ;i<count;i++) {
            User user = UserFixture.create("test" + i + "@test.com", "test12345");
            Profile profile = new Profile(user, "test");
            user.setProfile(profile);
            ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now().minusDays(i));
            userList.add(user);
        }

        return userList;
    }
}
