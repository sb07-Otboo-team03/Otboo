package com.codeit.otboo.domain.user.fixture;

import com.codeit.otboo.domain.user.dto.request.UserCreateRequest;

public class UserCreateRequestFixture {
    public static UserCreateRequest create() {
        return new UserCreateRequest("test", "test12345@codeit.com", "password12345");
    }

    public static UserCreateRequest create(String name, String email, String password) {
        return new UserCreateRequest(name, email, password);
    }
}
