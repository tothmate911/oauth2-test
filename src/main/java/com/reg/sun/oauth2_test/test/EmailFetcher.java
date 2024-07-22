package com.reg.sun.oauth2_test.test;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import java.util.Arrays;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailFetcher {

    private static final String MAIL_ADDRESS = "mvm.partner.reply@solenostrum.hu";
    private static final String HOST = "outlook.office365.com";
    private static final String DEFAULT_INBOX_FOLDER = "INBOX";

    private final TokenGetter tokenGetter;

    @EventListener(ApplicationReadyEvent.class)
    public void getEmails() {
        log.info("Started application");

        var maybeToken = tokenGetter.getToken();
        if (maybeToken.isEmpty()) {
            log.error("Emails cannot be fetched, reason: empty token");
            return;
        }
        var token = maybeToken.get();
        log.info("Token acquired: {}", token);

        var props = getProps();
        var session = Session.getInstance(props);
        session.setDebug(true);

        try {
            var store = session.getStore("imap");
            store.connect(HOST, MAIL_ADDRESS, token);
            if (store.isConnected()) {
                log.info("Connected");
                var folder = store.getFolder(DEFAULT_INBOX_FOLDER);
                folder.open(Folder.READ_WRITE);
                var messages = (MimeMessage[]) folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), true));
                Arrays.stream(messages).forEach(message -> log.info("Message: {}", message));
            } else {
                log.warn("Could nto connect");
            }
        } catch (NoSuchProviderException e) {
            log.error("Exception while getting store", e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties getProps() {
        Properties props = new Properties();

//        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", HOST);
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.starttls.enable", "true");
        props.put("mail.imap.auth", "true");
        props.put("mail.imap.auth.mechanisms", "XOAUTH2");
        props.put("mail.imap.user", MAIL_ADDRESS);
        props.put("mail.debug", "true");
        props.put("mail.debug.auth", "true");

//        props.put("mail.imap.auth.login.disable", "true");
//        props.put("mail.imap.auth.plain.disable", "true");
//        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.setProperty("mail.imap.socketFactory.fallback", "false");
//        props.setProperty("mail.imap.socketFactory.port", "993");

        return props;
    }

}
