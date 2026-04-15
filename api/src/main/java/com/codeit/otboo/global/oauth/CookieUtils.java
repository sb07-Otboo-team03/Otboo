package com.codeit.otboo.global.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Optional;
import org.springframework.util.SerializationUtils;
import org.springframework.web.util.WebUtils;

public final class CookieUtils {

    private CookieUtils() {
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        return Optional.ofNullable(WebUtils.getCookie(request, name));
    }

    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        return getCookie(request, name).map(Cookie::getValue);
    }

    public static String serialize(Object object) {
        byte[] bytes = SerializationUtils.serialize(object);
        if (bytes == null) {
            throw new IllegalStateException("직렬화 결과가 null 입니다.");
        }
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
        Object deserialized = SerializationUtils.deserialize(bytes);

        if (deserialized == null) {
            throw new IllegalStateException("역직렬화 결과가 null 입니다.");
        }

        return cls.cast(deserialized);
    }
}