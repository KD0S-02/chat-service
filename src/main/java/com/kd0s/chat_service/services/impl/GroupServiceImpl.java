package com.kd0s.chat_service.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import com.kd0s.chat_service.models.GroupEntity;
import com.kd0s.chat_service.repositories.GroupRepository;
import com.kd0s.chat_service.services.GroupService;

@Service
public class GroupServiceImpl implements GroupService {

    private GroupRepository groupRepository;

    public GroupServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Override
    public List<GroupEntity> getGroups() {
        return StreamSupport.stream(groupRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public Optional<GroupEntity> getGroup(String id) {
        return groupRepository.findByGroupId(id);
    }

    @Override
    public GroupEntity saveGroup(GroupEntity group) {
        return groupRepository.save(group);
    }

    @Override
    public GroupEntity partialUpdate(GroupEntity group, String id) {
        group.setGroupId(id);

        return groupRepository.findByGroupId(id).map(
                existingGroup -> {
                    Optional.ofNullable(group.getGroupAdmin()).ifPresent(
                            existingGroup::setGroupAdmin);
                    Optional.ofNullable(group.getGroupsize()).ifPresent(
                            existingGroup::setGroupsize);
                    Optional.ofNullable(group.getGroupname()).ifPresent(
                            existingGroup::setGroupname);
                    return groupRepository.save(existingGroup);
                }).orElseThrow(() -> new RuntimeException("Group does not exist"));
    }
}