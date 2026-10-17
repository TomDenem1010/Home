package trd.home.media.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;

@Entity
@Table(
        name = "media_video_tag",
        uniqueConstraints = @UniqueConstraint(name = "uq_media_video_tag_name", columnNames = "name"))
@Getter
@Setter
public class VideoTag extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NonNull
    private String id;

    @Column(nullable = false, length = 255)
    @NonNull
    private String name;
}
