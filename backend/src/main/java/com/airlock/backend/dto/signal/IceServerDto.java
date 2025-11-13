package com.airlock.backend.dto.signal;

import lombok.Data;

@Data
public class IceServerDto {
    private String ip;        //TURN/STUN 서버 주소
    private String username;
    private String password;

    public IceServerDto(String ip) {
        this.ip = ip;
    }

    public IceServerDto(String ip, String username, String password) {
        this.ip = ip;
        this.username = username;
        this.password = password;
    }
}
