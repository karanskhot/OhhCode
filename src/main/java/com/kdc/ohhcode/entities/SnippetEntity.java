package com.kdc.ohhcode.entities;

import com.kdc.ohhcode.entities.enums.Difficulty;
import com.kdc.ohhcode.entities.enums.Language;
import com.kdc.ohhcode.entities.enums.SnippetStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "snippets",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "hash_code"}
        )
)
public class SnippetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(name="url", nullable = false)
    private String url;

    @Column(name = "hash_code",  nullable = false)
    private String hashCode;

    private Boolean important;

    @Column(name = "memory_notes")
    private String memoryNotes;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SnippetStatus status;

    @Column(name = "difficulty")
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(name = "language")
    @Enumerated(EnumType.STRING)
    private Language language;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "snippet_tags", joinColumns = @JoinColumn(name = "snippet_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(mappedBy = "snippet", cascade = CascadeType.ALL, orphanRemoval = true)
    private AnalyzerEntity analysis;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
