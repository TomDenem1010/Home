package trd.home.media.dao;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;
import trd.home.media.constant.MediaStatus;

@Entity
@Table(
        name = "media_video",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_media_video_identity",
                        columnNames = {"folder_id", "name", "actor_key"}))
@Getter
@Setter
public class Video extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    @ManyToMany
    @JoinTable(
            name = "media_video_actor",
            joinColumns = @JoinColumn(name = "video_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id"))
    private Set<Actor> actors = new HashSet<>();

    @Column(name = "file_name", nullable = false, length = 1000)
    private String fileName;

    @Column(name = "actor_key", nullable = false, length = 64)
    private String actorKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private MediaStatus status = MediaStatus.ACTIVE;
}
