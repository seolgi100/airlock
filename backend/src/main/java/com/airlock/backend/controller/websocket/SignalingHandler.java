package com.airlock.backend.controller.websocket;

import com.airlock.backend.dto.signal.SignalingMessage;
import com.airlock.backend.service.SignalingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

//Controller
public class SignalingHandler extends TextWebSocketHandler {

    private final SignalingService signalingService;

    //ObjectMapper의 기능(JSON 파싱/직렬화)을 사용
    private final ObjectMapper objectMapper;

    public SignalingHandler(SignalingService signalingService) {
        this.signalingService = signalingService;
        this.objectMapper = new ObjectMapper();
    }

    //handleTextMessage 메소드는 TextMessage message로 받기로 정해져 있음
     @Override//문자열로 된 JSON 을 message로 받음
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {

        //message(JSON 문자열)를 getPayload()로 꺼냄
        String payload = message.getPayload();

        //readValue()에서 발생하는 예외를 처리하기 위해 try-catch 사용
        try {

            //readValue()가 JSON의 key와 SignalingMessage의 필드 이름을 매칭해서 값을 채움
            SignalingMessage signalingMessage = objectMapper.readValue(payload, SignalingMessage.class);

            signalingService.handleMessage(session, signalingMessage);
        } catch (JsonProcessingException e) {

            //JSON 구조가 잘못됐거나 파싱 실패했을 때, 에러 로그 출력
            e.printStackTrace();
        }

    }
}
