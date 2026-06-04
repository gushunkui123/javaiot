package com.agileboot.common.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${factory-link.emqx.email.from:ftihb-it03@ficinjection.com}")
    private String fromEmail;

    @Value("${factory-link.emqx.email.to}")
    private String toEmail;

    /**
     * 发送告警邮件
     */                                                                                             
    public void sendAlarmEmail(String alarmTitle, String alarmContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(alarmTitle);
            helper.setText(alarmContent, true);
            mailSender.send(message);
            log.info("告警邮件发送成功: to={}, subject={}", toEmail, alarmTitle);
        } catch (MessagingException e) {
            log.error("告警邮件发送失败: to={}, subject={}", toEmail, alarmTitle, e);
        }
    }
}