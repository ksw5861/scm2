package com.yedam.scm.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class IamportService {

    private final RestTemplate restTemplate = new RestTemplate();

    // application.properties에서 값 가져오기
    @Value("${iamport.api.key}")
    private String apiKey;

    @Value("${iamport.api.secret}")
    private String apiSecret;

    /**
     * 1) 아임포트 AccessToken 발급
     */
  public String getAccessToken() {
    String url = "https://api.iamport.kr/users/getToken";

    try {
        // 1. JSON body 생성
        Map<String, String> body = new HashMap<>();
        body.put("imp_key", apiKey);
        body.put("imp_secret", apiSecret);

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(body);

        System.out.println("아임포트 요청 JSON = " + jsonBody);

        // 2. 헤더 설정 (반드시 JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 3. HttpEntity에 JSON String 직접 넣기
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        // 4. 요청 전송
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            Map.class
        );

        System.out.println("아임포트 응답 = " + response.getBody());

        // 5. access_token 파싱
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.get("response") != null) {
            Map<String, Object> inner = (Map<String, Object>) responseBody.get("response");
            return (String) inner.get("access_token");
        } else {
            throw new RuntimeException("토큰 발급 실패: " + responseBody);
        }

    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("아임포트 토큰 요청 실패: " + e.getMessage());
    }
}


    /**
     * 2) 결제 검증 (imp_uid 기준)
     */
    public Map<String, Object> verifyPayment(String impUid) {
        // AccessToken 먼저 발급
        String accessToken = getAccessToken();

        String url = "https://api.iamport.kr/payments/" + impUid;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken); // 인증 헤더 추가

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 아임포트 서버에 요청
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }
}

