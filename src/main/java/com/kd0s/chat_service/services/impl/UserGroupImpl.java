package com.kd0s.chat_service.services.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.kd0s.chat_service.models.UserGroupEntity;
import com.kd0s.chat_service.repositories.UserGroupRepository;
import com.kd0s.chat_service.services.UserGroupService;

@Service
public class UserGroupImpl implements UserGroupService {

    private UserGroupRepository userGroupRepository;

    public UserGroupImpl(UserGroupRepository userGroupRepository) {
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    public List<UserGroupEntity> getGroupUsers(String groupId) {
        return StreamSupport.stream(userGroupRepository.findAllByGroupId(groupId).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserGroupEntity> getUserGroups(String username) {
        return StreamSupport.stream(userGroupRepository.findAllByUsername(username).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public void AddUserGroup(UserGroupEntity userGroup) {
        if (userGroupRepository.existsByGroupIdAndUsername(userGroup.getGroupId(), userGroup.getUsername()))
            return;
        userGroupRepository.save(userGroup);
    }
}
