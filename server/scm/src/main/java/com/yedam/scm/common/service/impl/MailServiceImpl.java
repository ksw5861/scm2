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
import java.util.stream.Collectors;

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

        int messageCount = inbox.getMessageCount();
        int start = Math.max(1, messageCount - 9); // 최신 10개만
        Message[] messages = inbox.getMessages(start, messageCount);

        FetchProfile fetchProfile = new FetchProfile();
        fetchProfile.add(FetchProfile.Item.ENVELOPE);
        fetchProfile.add(FetchProfile.Item.CONTENT_INFO);
        inbox.fetch(messages, fetchProfile);

        // 최신순으로 정렬
        List<Message> latestMessages = Arrays.asList(messages);
        Collections.reverse(latestMessages);

        // 병렬로 안전하게 DTO 변환
        List<EmailDTO> emailList = latestMessages.parallelStream()
            .map(this::toEmailDtoSafely)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        inbox.close(false);
        store.close();

        return emailList;
    }

    /**
     * 메시지를 안전하게 EmailDTO로 변환 (예외 처리 포함)
     */
    private EmailDTO toEmailDtoSafely(Message message) {
        try {
            String from = Arrays.stream(message.getFrom())
                    .filter(InternetAddress.class::isInstance)
                    .map(addr -> ((InternetAddress) addr).getAddress())
                    .findFirst()
                    .orElse("unknown");

            String subject = message.getSubject();
            String body = extractTextFromMessage(message);

            // 본문 미리보기 길이 제한
            if (body.length() > 500) {
                body = body.substring(0, 500) + "...";
            }

            EmailDTO dto = new EmailDTO();
            dto.setFrom(from);
            dto.setSubject(subject);
            dto.setBody(body);

            return dto;
        } catch (Exception e) {
            System.err.println("메일 파싱 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * 메일 내용 텍스트 추출 (text/plain 우선, 없으면 html)
     */
    private String extractTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return message.getContent().toString(); // 필요 시 HTML 제거 가능
        } else if (message.isMimeType("multipart/*")) {
            return extractFromMultipart((MimeMultipart) message.getContent());
        }
        return "";
    }

    private String extractFromMultipart(MimeMultipart mimeMultipart) throws Exception {
        String html = null;

        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);

            if (bodyPart.isMimeType("text/plain")) {
                return bodyPart.getContent().toString();
            } else if (bodyPart.isMimeType("text/html")) {
                if (html == null) {
                    html = bodyPart.getContent().toString();
                }
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                return extractFromMultipart((MimeMultipart) bodyPart.getContent());
            }
        }

        return html != null ? html : "";
    }
}