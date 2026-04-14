package com.codeit.otboo.domain.user.service;

public interface MailService {
    void sendTemporaryPassword(String to, String temporaryPassword, String expiresAt);

}
