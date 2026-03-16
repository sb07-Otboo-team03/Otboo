package com.codeit.otboo.domain.directmessage.repository;

import com.codeit.otboo.domain.directmessage.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {
}
