package com.codeit.otboo.domain.user.service;

import com.codeit.otboo.domain.user.exception.TemporaryPasswordMailSendFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class SmtpMailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private SmtpMailService smtpMailService;

    @BeforeEach
    void setUp() {
        // from은 application.yml value이기 때문에 강제 설정
        ReflectionTestUtils.setField(smtpMailService, "from", "test@otboo.com");
    }

    @Test
    @DisplayName("임시 비밀번호 메일 발송 성공")
    void sendTemporaryPassword_success() {
        // given
        String to = "test@codeit.com";
        String temporaryPassword = "tempPassword";
        String expiresAt = "2026-01-01 12:00:00";

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        // when
        smtpMailService.sendTemporaryPassword(to, temporaryPassword, expiresAt);

        // then
        then(mailSender).should().send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getFrom()).isEqualTo("test@otboo.com");
        assertThat(sentMessage.getTo()).containsExactly(to);
        assertThat(sentMessage.getSubject()).isEqualTo("Otboo 임시 비밀번호 발송");
        assertThat(sentMessage.getText()).contains(temporaryPassword);
        assertThat(sentMessage.getText()).contains(expiresAt);
    }

    @Test
    @DisplayName("임시 비밀번호 메일 발송 실패 시 예외 변환")
    void sendTemporaryPassword_fail() {
        // given
        String to = "test@codeit.com";
        String temporaryPassword = "tempPassword";
        String expiresAt = "2026-01-01 12:00:00";

        willThrow(new MailSendException("메일 발송 실패"))
                .given(mailSender)
                .send(any(SimpleMailMessage.class));

        // when & then
        assertThatThrownBy(() ->
                smtpMailService.sendTemporaryPassword(to, temporaryPassword, expiresAt))
                .isInstanceOf(TemporaryPasswordMailSendFailedException.class);

        then(mailSender).should().send(any(SimpleMailMessage.class));
    }
}
