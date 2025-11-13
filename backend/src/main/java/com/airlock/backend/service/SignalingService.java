package com.airlock.backend.service;

import com.airlock.backend.dto.signal.SignalingMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SignalingService {

    private final ObjectMapper objectMapper;

    //userId -> session 매핑 저장
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public SignalingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handleMessage(WebSocketSession session, SignalingMessage message) {
        switch(message.getType()) {
            case "join": //WebSocket 연결될 때
                handleJoin(session, message);
                break;
            case "offer":
                handleOffer(message);
                break;
            case "answer":
                handleAnswer(message);
                break;
            case "candidate":
                handleCandidate(message);
                break;
            default:
                //unknown type 처리
                break;
        }
    }

    //sessions에 등록
    private void handleJoin(WebSocketSession session, SignalingMessage message) {
        //userId -> session
        sessions.put(message.getFrom(), session);
    }

    private void handleOffer(SignalingMessage message) {
        forwardToTarget(message);
    }

    private void handleAnswer(SignalingMessage message) {
        forwardToTarget(message);
    }

    private void handleCandidate(SignalingMessage message) {
        forwardToTarget(message);
    }

    private void forwardToTarget(SignalingMessage message) {
        WebSocketSession targetSession = sessions.get(message.getTo());

        //제대로된 세션인지, 종료된 세션인지 확인
        if (targetSession != null && targetSession.isOpen()) {

            //sendMessage()에서 발생하는 예외를 처리하기 위해 try-catch 사용
            try {
                targetSession.sendMessage(new TextMessage(toJson(message)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String toJson(SignalingMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e){
            e.printStackTrace();
            return "{}";
        }
    }
}
