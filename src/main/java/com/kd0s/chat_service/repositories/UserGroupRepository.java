package com.kd0s.chat_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kd0s.chat_service.models.UserGroupEntity;

public interface UserGroupRepository extends JpaRepository<UserGroupEntity, Long> {
    List<UserGroupEntity> findAllByUsername(String username);

    List<UserGroupEntity> findAllByGroupId(String groupId);
}
