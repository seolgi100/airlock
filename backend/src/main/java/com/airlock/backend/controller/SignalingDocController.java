package com.airlock.backend.controller;

import com.airlock.backend.dto.signal.SignalingMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/docs")
@Tag(
        name = "WebSocket Signaling",
        description = "WebRTC 시그널링 WebSocket 사용 설명 전용 API (실제 WebSocket 아님)"
)
public class SignalingDocController {

    @Operation(
            summary = "시그널링 WebSocket 사용 가이드",
            description = """
                    WebRTC 1:1 채팅을 위한 시그널링 WebSocket 사용 안내입니다.
                    
                    ■ WebSocket 엔드포인트 예시
                    - ws://<서버도메인 or IP>/ws
                    
                    ■ 전체 흐름
                    
                    1) 각 클라이언트는 WebSocket으로 연결을 연 뒤, 먼저 join 메시지 전송
                       {
                         "type": "join",
                         "from": "userA"
                       }
                       - SignalingService.handleJoin() 에서 sessions.put(from, session) 으로
                         username -> WebSocketSession 을 매핑합니다.
                    
                    2) 통화/채팅을 시작하려는 쪽에서 offer 메시지 전송
                       {
                         "type": "offer",
                         "from": "userA",
                         "to": "userB",
                         "sdp": "<SDP 문자열>"
                       }
                       - SignalingService.handleOffer() -> forwardToTarget()
                       - sessions.get(to) 로 상대 세션을 찾아 TextMessage로 그대로 전달합니다.
                    
                    3) 상대방(userB)은 answer 메시지 전송
                       {
                         "type": "answer",
                         "from": "userB",
                         "to": "userA",
                         "sdp": "<SDP 문자열>"
                       }
                       - SignalingService.handleAnswer() -> forwardToTarget()
                    
                    4) ICE candidate 교환
                       {
                         "type": "candidate",
                         "from": "userA",
                         "to": "userB",
                         "candidate": "<candidate 문자열>",
                         "sdpMid": "0",
                         "sdpMLineIndex": 0
                       }
                       - SignalingService.handleCandidate() -> forwardToTarget()
                    
                    서버 쪽 흐름 요약:
                    - SignalingHandler.handleTextMessage() 에서 JSON 문자열을 수신
                    - ObjectMapper.readValue(...) 로 SignalingMessage 로 파싱
                    - signalingService.handleMessage(session, signalingMessage) 호출
                    - 타입에 따라 join / offer / answer / candidate 처리
                    """
    )
    @GetMapping("/signaling")
    public String signalingDoc() {
        // 실제로는 아무 의미 없는 문자열 리턴, 설명은 Swagger description에 다 적어둠.
        return "Swagger UI에서 설명을 확인하세요.";
    }

    @Operation(
            summary = "시그널링 메시지 예시 (문서/테스트용)",
            description = """
                    이 API는 WebSocket이 아니라 HTTP POST 입니다.
                    
                    - 목적: Swagger에서 SignalingMessage의 구조와 예시 JSON을 눈으로 확인하기 위함
                    - 실제 시그널링은 WebSocket(ws://...) 을 통해 이와 동일한 JSON을 보내야 합니다.
                    
                    아래 예시들을 참고해서 프론트에서 WebSocket 메시지를 구성하면 됩니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SignalingMessage.class),
                            examples = {
                                    @ExampleObject(
                                            name = "join 예시",
                                            summary = "WebSocket 연결 후 세션 등록(join)",
                                            value = """
                                                    {
                                                      "type": "join",
                                                      "from": "userA",
                                                      "to": null,
                                                      "sdp": null,
                                                      "candidate": null,
                                                      "sdpMid": null,
                                                      "sdpMLineIndex": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "offer 예시",
                                            summary = "userA -> userB SDP offer",
                                            value = """
                                                    {
                                                      "type": "offer",
                                                      "from": "userA",
                                                      "to": "userB",
                                                      "sdp": "v=0\\no=- 46117326 2 IN IP4 127.0.0.1 ...",
                                                      "candidate": null,
                                                      "sdpMid": null,
                                                      "sdpMLineIndex": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "answer 예시",
                                            summary = "userB -> userA SDP answer",
                                            value = """
                                                    {
                                                      "type": "answer",
                                                      "from": "userB",
                                                      "to": "userA",
                                                      "sdp": "v=0\\no=- 46117326 2 IN IP4 127.0.0.1 ...",
                                                      "candidate": null,
                                                      "sdpMid": null,
                                                      "sdpMLineIndex": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "candidate 예시",
                                            summary = "ICE candidate 교환",
                                            value = """
                                                    {
                                                      "type": "candidate",
                                                      "from": "userA",
                                                      "to": "userB",
                                                      "sdp": null,
                                                      "candidate": "candidate:0 1 UDP 2122252543 192.168.0.10 54321 typ host",
                                                      "sdpMid": "0",
                                                      "sdpMLineIndex": 0
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "요청으로 보낸 SignalingMessage를 그대로 돌려줍니다 (문서용 echo API).",
                            content = @Content(schema = @Schema(implementation = SignalingMessage.class))
                    )
            }
    )
    @PostMapping("/signaling/example")
    public SignalingMessage signalingExample(@RequestBody SignalingMessage message) {
        // 그냥 받은 걸 그대로 반환 (echo). 실제 시그널링은 WebSocket에서 처리.
        return message;
    }
}
