package com.labndbnb.landbnb.service.implement;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Value("${mail.smtp.host}")
    private String host;

    @Value("${mail.smtp.port}")
    private int port;

    @Value("${mail.smtp.username}")
    private String username;

    @Value("${mail.smtp.password}")
    private String password;

    private Mailer getMailer() {
        return MailerBuilder
                .withSMTPServer(host, port, username, password)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(10 * 1000)
                .buildMailer();
    }

    // Email simple
    public void sendSimpleEmail(String to, String subject, String text) {
        var email = EmailBuilder.startingBlank()
                .from("Mi App", username)
                .to(to)
                .withSubject(subject)
                .withPlainText(text)
                .buildEmail();

        getMailer().sendMail(email);
    }

    // Email HTML personalizado
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        var email = EmailBuilder.startingBlank()
                .from("Mi App", username)
                .to(to)
                .withSubject(subject)
                .withHTMLText(htmlContent)
                .buildEmail();

        getMailer().sendMail(email);
    }

    // Email con nombre personalizado del destinatario
    public void sendPersonalizedEmail(String toName, String toEmail, String subject, String htmlContent) {
        var email = EmailBuilder.startingBlank()
                .from("Mi App", username)
                .to(toName, toEmail)
                .withSubject(subject)
                .withHTMLText(htmlContent)
                .buildEmail();

        getMailer().sendMail(email);
    }
}
