package com.airlock.backend.dto.signal;

import lombok.Data;

@Data
public class SignalingMessage {
    private String type;        //offer, answer, candidate
    private String from;        //보내는 사람 userId
    private String to;          //받는 사람 userId

    //offer / answer 일 때
    private String sdp;

    //ICE candidate 일 때
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
}