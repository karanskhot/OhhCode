package com.kdc.ohhcode.repositories;

import com.kdc.ohhcode.entities.AnalyzerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalyzerRepository extends JpaRepository<AnalyzerEntity, UUID> {

    Optional<AnalyzerEntity> findByIdAndUserId(UUID snippetAnalyzerId, UUID userId);
    List<AnalyzerEntity> findAllByUserId(UUID userId);
    Optional<AnalyzerEntity> findByUserId(UUID uuid);
    Optional<AnalyzerEntity> findBySnippetIdAndUserId(UUID snippetId, UUID userId);
    Optional<AnalyzerEntity> findByIdAndSnippetIdAndUserId(UUID id,  UUID snippetId, UUID userId);
}
