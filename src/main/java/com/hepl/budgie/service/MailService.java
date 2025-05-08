package com.hepl.budgie.service;

import jakarta.mail.MessagingException;

public interface MailService {

    void sendMailByTemplate(String content, String recepient, String subject) throws MessagingException;

}
