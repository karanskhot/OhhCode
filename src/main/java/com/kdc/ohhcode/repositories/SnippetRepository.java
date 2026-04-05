package com.kdc.ohhcode.repositories;

import com.kdc.ohhcode.entities.SnippetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SnippetRepository extends JpaRepository<SnippetEntity, UUID> {

    boolean existsByHashCode(String hashCode);

    Optional<SnippetEntity> findByUserIdAndHashCode(UUID id, String hashCode);
    Optional<SnippetEntity> findByIdAndUserId(UUID id, UUID userId);

    Optional<SnippetEntity> findFirstByHashCode(String hashCode);

    List<SnippetEntity> findAllByUserId(UUID userId);

}
