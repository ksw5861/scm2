package com.yedam.scm.login.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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
    public LoginRes getAccountByAccountId(String accountId) {
        return mapper.selectAccountByAccountId(accountId);
    }

    
}
