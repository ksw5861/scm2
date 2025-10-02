package com.yedam.scm.web;

import com.yedam.scm.common.service.KakaoAddressService;
import com.yedam.scm.dto.AddressRes;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AddressController {

    private final KakaoAddressService addressSvc;

    @GetMapping("/search/address")
    public Mono<List<AddressRes>> searchAddress(@RequestParam String query) {
        return addressSvc.searchAddress(query);
    }
}