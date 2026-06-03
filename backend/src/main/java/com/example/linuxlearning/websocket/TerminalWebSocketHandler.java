package com.example.linuxlearning.websocket;

import com.example.linuxlearning.service.TerminalTranscriptService;
import com.example.linuxlearning.service.VmSandboxClient;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Map;

@Component
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private static final UriTemplate TERMINAL_URI = new UriTemplate("/ws/lab-sessions/{sessionId}/terminal");

    private final VmSandboxClient vmSandboxClient;
    private final TerminalTranscriptService transcriptService;

    public TerminalWebSocketHandler(VmSandboxClient vmSandboxClient, TerminalTranscriptService transcriptService) {
        this.vmSandboxClient = vmSandboxClient;
        this.transcriptService = transcriptService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long labSessionId = resolveLabSessionId(session);
        session.getAttributes().put("labSessionId", labSessionId);
        String intro = vmSandboxClient.terminalIntro(labSessionId);
        transcriptService.append(labSessionId, intro);
        session.sendMessage(new TextMessage(intro));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long labSessionId = (Long) session.getAttributes().get("labSessionId");
        String response = vmSandboxClient.handleTerminalInput(labSessionId, message.getPayload());
        transcriptService.append(labSessionId, message.getPayload());
        transcriptService.append(labSessionId, response);
        session.sendMessage(new TextMessage(response));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        session.getAttributes().remove("labSessionId");
    }

    private Long resolveLabSessionId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            throw new IllegalArgumentException("WebSocket URI 为空");
        }
        Map<String, String> variables = TERMINAL_URI.match(uri.getPath());
        return Long.valueOf(variables.get("sessionId"));
    }
}
