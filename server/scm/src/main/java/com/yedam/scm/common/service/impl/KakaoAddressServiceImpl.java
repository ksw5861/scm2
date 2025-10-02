package com.yedam.scm.common.service.impl;

import com.yedam.scm.common.service.KakaoAddressService;
import com.yedam.scm.dto.AddressRes;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KakaoAddressServiceImpl implements KakaoAddressService {

    @Value("${kakao.client.key}")
    private String kakaoApiKey;

    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<List<AddressRes>> searchAddress(String query) {
        return searchAddressWithPagination(query, 1, 15, new ArrayList<>());
    }

    private Mono<List<AddressRes>> searchAddressWithPagination(String query, int page, int size, List<AddressRes> accumulatedResults) {
        return webClientBuilder.baseUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)  // Authorization 헤더 추가
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("size", size)  // 페이지네이션 파라미터 추가
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(new RuntimeException("API 호출 실패")))
                .bodyToMono(Map.class)
                .doOnNext(response -> {
                    System.out.println("응답 전체: " + response);
                })
                .flatMap(response -> {
                    if (response.containsKey("documents")) {
                        List<Map<String, Object>> documents = (List<Map<String, Object>>) response.get("documents");
                        if (documents != null && !documents.isEmpty()) {
                            List<AddressRes> addressList = documents.stream()
                                    .map(firstAddress -> {
                                        AddressRes addressRes = new AddressRes();
                                        addressRes.setAddress((String) firstAddress.get("address_name"));

                                        // 도로명 주소 처리 (존재할 경우만 처리)
                                        Map<String, Object> roadAddress = (Map<String, Object>) firstAddress.get("road_address");
                                        if (roadAddress != null && roadAddress.containsKey("address_name")) {
                                            addressRes.setRoadAddress((String) roadAddress.get("address_name"));
                                        } else {
                                            addressRes.setRoadAddress(null);  // 도로명 주소가 없으면 null 처리
                                        }

                                        // x, y 좌표 처리
                                        addressRes.setX((String) firstAddress.get("x"));
                                        addressRes.setY((String) firstAddress.get("y"));

                                        return addressRes;
                                    })
                                    .collect(Collectors.toList());

                            // 이전 결과와 합치기
                            accumulatedResults.addAll(addressList);

                            // 페이지네이션 정보 처리 (다음 페이지가 있으면 재귀 호출)
                            Map<String, Object> meta = (Map<String, Object>) response.get("meta");
                            int totalCount = (Integer) meta.get("total_count");
                            int currentPage = (Integer) meta.get("pageable_count"); // 현재까지 조회된 페이지 수

                            if (accumulatedResults.size() < totalCount) {
                                return searchAddressWithPagination(query, page + 1, size, accumulatedResults);  // 다음 페이지로 이동
                            }

                            return Mono.just(accumulatedResults);  // 모든 페이지가 끝난 경우
                        }
                    }
                    // 검색 결과가 없을 경우 에러 반환
                    return Mono.error(new RuntimeException("검색 결과가 없습니다."));
                })
                .switchIfEmpty(Mono.error(new RuntimeException("검색 결과가 없습니다.")));
    }
}
