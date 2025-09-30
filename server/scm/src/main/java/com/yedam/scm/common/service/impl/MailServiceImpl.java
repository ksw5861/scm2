package com.yedam.scm.common.service.impl;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.yedam.scm.common.service.MailService;
import com.yedam.scm.dto.EmailDTO;

import java.util.*;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 비동기 이메일 발송
     */
    @Async
    @Override
    public void sendMailAsync(String to, String subject, String htmlContent, String from) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(from);

            mailSender.send(message);

        } catch (MessagingException e) {
            System.err.println("메일 발송 실패: " + e.getMessage());
        }
    }

    /**
     * 최근 수신된 이메일 목록 (최신 10개까지) 가져오기
     */
    @Override
    public List<EmailDTO> fetchRecentEmails() throws Exception {
        List<EmailDTO> emailList = new ArrayList<>();

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", username, password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
        int count = messages.length;

        // 최신 메일 10개만 가져오기 (역순)
        for (int i = count - 1; i >= Math.max(0, count - 10); i--) {
            Message message = messages[i];
            System.out.println(message);

            String from = Arrays.stream(message.getFrom())
                                .filter(InternetAddress.class::isInstance)
                                .map(addr -> ((InternetAddress) addr).getAddress())
                                .findFirst()
                                .orElse("unknown");

            String subject = message.getSubject();
            String body = extractTextFromMessage(message);

            EmailDTO dto = new EmailDTO();
            dto.setFrom(from);
            dto.setSubject(subject);
            dto.setBody(body);

            emailList.add(dto);
        }

        inbox.close(false);
        store.close();

        return emailList;
    }

    /**
     * 메일 내용 텍스트 추출
     */
    private String extractTextFromMessage(Message message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        }

        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);
                result.append(part.getContent().toString());
            }
            return result.toString();
        }

        return "";
    }
}
