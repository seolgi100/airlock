package com.airlock.backend.dto.signal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "WebRTC 시그널링 메시지")
public class SignalingMessage {

    @Schema(description = "메시지 타입 (join, offer, answer, candidate, chat)", example = "offer")
    private String type;

    @Schema(description = "보내는 username", example = "userA")
    private String from;

    @Schema(description = "받는 username", example = "userB")
    private String to;

    @Schema(description = "SDP 내용 (offer/answer일 때 사용)", example = "v=0\no=- 46117326 2 IN IP4 127.0.0.1 ...")
    private String sdp;

    @Schema(description = "ICE candidate 문자열 (candidate일 때 사용)", example = "candidate:0 1 UDP 2122252543 192.168.0.10 54321 typ host")
    private String candidate;

    @Schema(description = "ICE candidate의 sdpMid (candidate일 때 사용)", example = "0")
    private String sdpMid;

    @Schema(description = "ICE candidate의 sdpMLineIndex (candidate일 때 사용)", example = "0")
    private Integer sdpMLineIndex;

    @Schema(description = "채팅 텍스트")
    private String message;
}