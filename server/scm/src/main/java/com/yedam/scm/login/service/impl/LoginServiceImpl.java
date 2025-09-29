package com.yedam.scm.login.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.yedam.scm.dto.AuthRes;
import com.yedam.scm.dto.LoginDTO;
import com.yedam.scm.dto.LoginRes;
import com.yedam.scm.login.mapper.LoginMapper;
import com.yedam.scm.login.service.LoginService;
import com.yedam.scm.login.service.RecaptchaService;
import com.yedam.scm.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final LoginMapper mapper;
    private final JwtUtil jwtUtil;
    private final RecaptchaService recaptchaService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String serverEmail;

    @Override
    public LoginRes loginByEmailAndPassword(LoginDTO login) {
        if (!recaptchaService.verifyToken(login.getRecaptcha())) {
            LoginRes res = new LoginRes();
            res.setVerifyRecaptcha(false);
            return res;
        }

        LoginRes dbUser = mapper.selectAccountByEmailAndPassword(login);

        if (dbUser == null) {
            return null;
        }

        boolean isMatch = passwordEncoder.matches(login.getPassword(), dbUser.getPasswordHash());

        if (isMatch) {
            dbUser.setAccessToken(jwtUtil.generateToken(dbUser));
            dbUser.setVerifyRecaptcha(true);
            return dbUser;
        } else {
            return null;
        }
    }


    @Override
    public boolean modifyAccountPassword(String accountId, String password) {

        return mapper.updateAccountPassword(accountId ,passwordEncoder.encode(password)) > 0;

    }

    @Override
    public String generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    @Override
    public AuthRes processTempLogin(LoginDTO login) throws Exception {
        LoginRes result = loginByEmailAndPassword(login);

        if (result != null && Boolean.FALSE.equals(result.getVerifyRecaptcha())) {
            throw new IllegalArgumentException("reCAPTCHA 검증 실패");
        }

        if (result == null) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String tempToken = jwtUtil.generateTempToken(result.getAccountId());
        String smsUrl = "sms:" + serverEmail + "?body=" + java.net.URLEncoder.encode(tempToken, "UTF-8");
        String qrCodeBase64 = generateQRCodeImage(smsUrl, 300, 300);

        AuthRes response = new AuthRes();
        response.setMessage("2차 인증 필요");
        response.setTempToken(tempToken);
        response.setSmsUrl(smsUrl);
        response.setQrCodeImage(qrCodeBase64);

        return response;
    }


    @Override
    public LoginRes getAccountByAccountId(String accountId) {
        return mapper.selectAccountByAccountId(accountId);
    }

    
}
