package com.airlock.backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    //(토큰, 유저ID) 저장
    private final Map<String, Long> store = new ConcurrentHashMap<>();

    //로그인 성공 시 토큰 새로 발급
    public String issueToken(long userId) {
        String token = "dev-" + UUID.randomUUID();
        store.put(token, userId);
        return token;
    }

    //토큰으로 유저ID 찾기
    public Long resolveUserId(String token) {
        return store.get(token);
    }

    //로그아웃 시 토큰 삭제
    public void revoke(String token) {
        store.remove(token);
    }
}