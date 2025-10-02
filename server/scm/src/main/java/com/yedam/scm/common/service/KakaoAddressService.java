package com.yedam.scm.common.service;

import java.util.List;

import com.yedam.scm.dto.AddressRes;

import reactor.core.publisher.Mono;

public interface KakaoAddressService {

    /**
     * 네이버 주소 검색 API를 호출하여 결과를 반환
     *
     * @param query 검색어
     * @return 주소 검색 결과
     */
    Mono<List<AddressRes>> searchAddress(String query);
}
