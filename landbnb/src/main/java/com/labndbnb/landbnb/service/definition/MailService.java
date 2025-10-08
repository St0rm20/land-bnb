package com.labndbnb.landbnb.service.definition;

public interface MailService {
    void sendSimpleEmail(String to, String subject, String text);
    void sendHtmlEmail(String to, String subject, String htmlContent);
    void sendPersonalizedEmail(String toName, String toEmail, String subject, String htmlContent);

}
