package com.kd0s.chat_service.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd0s.chat_service.entities.Message;
import com.kd0s.chat_service.models.GroupEntity;
import com.kd0s.chat_service.models.UserGroupEntity;
import com.kd0s.chat_service.services.GroupService;
import com.kd0s.chat_service.services.UserGroupService;
import com.kd0s.chat_service.utils.RandomIDGenerator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SocketHandler extends TextWebSocketHandler {
    private Map<String, WebSocketSession> userSessionMap = new HashMap<String, WebSocketSession>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private UserGroupService userGroupService;
    private GroupService groupService;

    public void sendGroups(WebSocketSession session, String username) throws IOException {
        List<UserGroupEntity> userGroups = userGroupService.getUserGroups(username);
        List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
        for (UserGroupEntity userGroup : userGroups) {
            Optional<GroupEntity> groupDetails = groupService.getGroup(userGroup.getGroupId());
            Map<String, String> groupMap = new HashMap<>();
            groupMap.put("groupId", userGroup.getGroupId());
            groupMap.put("groupName", groupDetails.get().getGroupname());
            groups.add(groupMap);
        }
        Map<String, Object> messageBody = new HashMap<>();
        messageBody.put("type", "groupList");
        messageBody.put("data", groups);
        String response = objectMapper.writeValueAsString(messageBody);
        session.sendMessage(new TextMessage(response));
    }

    public SocketHandler(UserGroupService userGroupService, GroupService groupService) {
        this.userGroupService = userGroupService;
        this.groupService = groupService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
            throws Exception {
        session.getAttributes().put("active", true);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
            CloseStatus closeStatus) {
        session.getAttributes().put("active", false);
    }

    @Override
    public void handleTextMessage(WebSocketSession session,
            TextMessage message) throws Exception {

        Message parsedMessage = objectMapper.readValue(message.getPayload(), Message.class);

        if (parsedMessage.getType().equals("authentication")) {
            userSessionMap.put(parsedMessage.getUsername(), session);
        }

        else if (parsedMessage.getType().equals("groupList")) {
            sendGroups(session, parsedMessage.getUsername());
        }

        else if (parsedMessage.getType().equals("joinRoom")) {
            UserGroupEntity groupUser = UserGroupEntity.builder()
                    .groupId(parsedMessage.getRoomId())
                    .username(parsedMessage.getUsername())
                    .build();
            userGroupService.AddUserGroup(groupUser);
            String response = "{\"type\":\"joinRoom\", \"roomId\":\"%s\", \"data\":\"joined\"}";
            String formattedResponse = String.format(response, parsedMessage.getRoomId());
            session.sendMessage(new TextMessage(formattedResponse));
        }

        else if (parsedMessage.getType().equals("createRoom")) {
            String roomId = RandomIDGenerator.generateRoomId();

            GroupEntity group = GroupEntity.builder()
                    .groupAdmin(parsedMessage.getUsername())
                    .groupname(parsedMessage.getData())
                    .groupsize(1)
                    .groupId(roomId)
                    .build();
            groupService.saveGroup(group);

            UserGroupEntity groupUser = UserGroupEntity.builder()
                    .groupId(roomId)
                    .username(parsedMessage.getUsername())
                    .build();
            userGroupService.AddUserGroup(groupUser);

            String response = "{\"type\":\"createRoom\", \"data\":\"%s\"}";
            String formattedResponse = String.format(response, roomId);
            session.sendMessage(new TextMessage(formattedResponse));
            sendGroups(session, parsedMessage.getUsername());
        }

        else if (parsedMessage.getType().equals("message")) {
            List<UserGroupEntity> usersInGroup = userGroupService.getGroupUsers(parsedMessage.getRoomId());
            for (UserGroupEntity user : usersInGroup) {
                WebSocketSession s = userSessionMap.get(user.getUsername());
                if (s != session) {
                    if (s != null && s.isOpen()) {
                        String response = "{\"type\":\"message\", \"data\":\"%s\", \"username\": \"%s\"}";
                        String formattedResponse = String.format(response, parsedMessage.getData(),
                                parsedMessage.getUsername());
                        s.sendMessage(new TextMessage(formattedResponse));
                    }
                }
            }
        }
    }
}
