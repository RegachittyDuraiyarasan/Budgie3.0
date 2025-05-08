package com.hepl.budgie.service.impl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hepl.budgie.service.MailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;

    public MailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendMailByTemplate(String content, String recepient, String subject) throws MessagingException {
        log.info("Sending mail ... {}", recepient);
        final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart
        message.setSubject(subject);
        message.setFrom("noreply@hepl.com");
        message.setTo(recepient);

        message.setText(content, true); // true = isHtml

        // Add the inline image, referenced from the HTML code as
        // "cid:${imageResourceName}"
        message.addInline("logo", new ClassPathResource("static/images/logo.png"), "image/png");
        message.addInline("header", new ClassPathResource("static/images/logo-name.png"), "image/png");
        message.addInline("background", new ClassPathResource("static/images/abstract-background.jpg"), "image/png");

        // Send mail
        javaMailSender.send(mimeMessage);
    }

}
