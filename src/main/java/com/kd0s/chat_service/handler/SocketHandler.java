package com.kd0s.chat_service.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd0s.chat_service.entities.Message;
import com.kd0s.chat_service.utils.RandomIDGenerator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class SocketHandler extends TextWebSocketHandler {
    private Map<String, List<WebSocketSession>> rooms = new HashMap<String, List<WebSocketSession>>();
    private List<WebSocketSession> allUsers = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.getAttributes().put("active", true);
        allUsers.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        session.getAttributes().put("active", false);
        allUsers.remove(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Message parsedMessage = objectMapper.readValue(message.getPayload(), Message.class);

        if (parsedMessage.getType().equals("authentication")) {
            session.getAttributes().put("username", parsedMessage.getData());
        }

        else if (parsedMessage.getType().equals("joinRoom")) {
            if (rooms.get(parsedMessage.getRoomId()).contains(session))
                return;
            rooms.get(parsedMessage.getRoomId()).add(session);
            String response = "{\"type\":\"joinRoom\", \"roomId\":\"%s\", \"data\":\"joined\"}";
            String formattedResponse = String.format(response, parsedMessage.getRoomId());
            session.sendMessage(new TextMessage(formattedResponse));
            for (WebSocketSession s : rooms.get(parsedMessage.getRoomId())) {
                if (s != session) {
                    String newMessage = session.getAttributes().get("username") + " has joined chat";
                    String joinMessage = "{\"type\":\"message\", \"roomId\":null, \"data\":\"%s\"}";
                    String formattedJoinMessage = String.format(joinMessage, newMessage);
                    s.sendMessage(new TextMessage(formattedJoinMessage));
                }
            }
        }

        else if (parsedMessage.getType().equals("createRoom")) {
            String roomId = RandomIDGenerator.generateRoomId();
            rooms.put(roomId, new ArrayList<WebSocketSession>());
            String response = "{\"type\":\"createRoom\",\"roomId\":null, \"data\":\"%s\"}";
            String formattedResponse = String.format(response, roomId);
            session.sendMessage(new TextMessage(formattedResponse));
        }

        else if (parsedMessage.getType().equals("message")) {
            System.out.println(rooms.get(parsedMessage.getRoomId()).toString());
            for (WebSocketSession s : rooms.get(parsedMessage.getRoomId())) {
                if (s != session) {
                    if (s.isOpen()) {
                        String newMessage = session.getAttributes().get("username") + " : " + parsedMessage.getData();
                        String response = "{\"type\":\"message\", \"roomId\":null, \"data\":\"%s\"}";
                        String formattedResponse = String.format(response, newMessage);
                        s.sendMessage(new TextMessage(formattedResponse));
                    } else {
                        rooms.get(parsedMessage.getRoomId()).remove(s);
                    }
                }
            }
        }
    }
}
