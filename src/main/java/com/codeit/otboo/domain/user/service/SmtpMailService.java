package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpMailService implements MailService{

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String from;

    public void sendTemporaryPassword(String to, String temporaryPassword, String expiresAt) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Otboo 임시 비밀번호 발송");
        message.setText(buildTemporaryPasswordContent(temporaryPassword, expiresAt));
        mailSender.send(message);

    }

    private String buildTemporaryPasswordContent(String temporaryPassword, String expiresAt) {
        return """
                안녕하세요!
                
                요청하신 임시 비밀번호가 발급되었습니다. 
                
                아래 임시 비밀번호를 사용하여 로그인 후 새로운 비밀번호로 변경해주세요.

                임시 비밀번호: %s
                
                ⚠️ 중요 안내사항
                • 이 임시 비밀번호는 %s까지만 유효합니다
                • 보안을 위해 로그인 후 즉시 새로운 비밀번호로 변경해주세요
                • 임시 비밀번호는 다른 사람과 공유하지 마세요
                본 메일은 발신전용이므로 회신되지 않습니다.
                문의사항이 있으시면 고객센터로 연락해주세요.
                """.formatted(temporaryPassword, expiresAt);
    }

}