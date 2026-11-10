package com.labndbnb.landbnb.service.implement;

import com.labndbnb.landbnb.service.definition.MailService;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
    @Value("${mail.enabled:true}")
    private boolean enableEmail;
    @Value("${mail.smtp.host}")
    private String host;

    @Value("${mail.smtp.port}")
    private int port;

    @Value("${mail.smtp.username}")
    private String username;

    @Value("${mail.smtp.password}")
    private String password;

    Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    /** Un único Mailer (con su pool de conexiones) para toda la aplicación, creado la primera vez que se usa. */
    private volatile Mailer mailer;

    private Mailer getMailer() {
        Mailer local = mailer;
        if (local == null) {
            synchronized (this) {
                local = mailer;
                if (local == null) {
                    local = MailerBuilder
                            .withSMTPServer(host, port, username, password)
                            .withTransportStrategy(TransportStrategy.SMTP_TLS)
                            .withSessionTimeout(10 * 1000)
                            .buildMailer();
                    mailer = local;
                }
            }
        }
        return local;
    }

    @Override
    @Async
    // Email simple
    public void sendSimpleEmail(String to, String subject, String text) {
        if(!enableEmail) return;
        try{
            log.info("Enviando email a: {} con asunto: {}", to, subject);
            var email = EmailBuilder.startingBlank()
                    .from("land-bnb", username)
                    .to(to)
                    .withSubject(subject)
                    .withPlainText(text)
                    .buildEmail();

            getMailer().sendMail(email);
        }catch (Exception e){
            log.error("Error al registrar el envío de email: {}", e.getMessage());
        }

    }

    @Override
    @Async
    // Email HTML personalizado
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if(!enableEmail) return;
        try {
            var email = EmailBuilder.startingBlank()
                    .from("land-bnb", username)
                    .to(to)
                    .withSubject(subject)
                    .withHTMLText(htmlContent)
                    .buildEmail();
            getMailer().sendMail(email);
        } catch (Exception e) {
            log.error("Error al enviar email HTML: {}", e.getMessage());
        }
    }


    @Override
    @Async
    // Email con nombre personalizado del destinatario
    public void sendPersonalizedEmail(String toName, String toEmail, String subject, String htmlContent) {
        if(!enableEmail) return;
        try {
            var email = EmailBuilder.startingBlank()
                    .from("land-bnb", username)
                    .to(toName, toEmail)
                    .withSubject(subject)
                    .withHTMLText(htmlContent)
                    .buildEmail();
            getMailer().sendMail(email);
        } catch (Exception e) {
            log.error("Error al enviar email personalizado: {}", e.getMessage());
        }
    }
}
