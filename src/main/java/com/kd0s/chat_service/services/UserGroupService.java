package com.kd0s.chat_service.services;

import java.util.List;

import com.kd0s.chat_service.models.UserGroupEntity;

public interface UserGroupService {

    public List<UserGroupEntity> getGroupUsers(String groupId);

    public List<UserGroupEntity> getUserGroups(String username);

    public UserGroupEntity AddUserGroup(UserGroupEntity userGroup);

    // public UserGroupEntity RemoveGroupUser(String groupId, String username);
}