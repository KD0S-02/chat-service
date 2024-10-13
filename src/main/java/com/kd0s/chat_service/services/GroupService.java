package com.kd0s.chat_service.services;

import java.util.List;
import java.util.Optional;

import com.kd0s.chat_service.models.GroupEntity;

public interface GroupService {

    public List<GroupEntity> getGroups();

    public Optional<GroupEntity> getGroup(String id);

    public GroupEntity saveGroup(GroupEntity groupEntity);

    public GroupEntity partialUpdate(GroupEntity groupEntity, String id);
}
