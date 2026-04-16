package com.codeit.otboo.global.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OidcUserServiceImpl extends OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        log.debug("OIDC 로그인 성공. registrationId={}, claims={}", registrationId, oidcUser.getClaims());
        log.debug("OIDC 유저 정보 추출. email={}, name={}", email, name);

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("구글 계정 이메일 실패 - notFound");
        }

        return oidcUser;
    }
}