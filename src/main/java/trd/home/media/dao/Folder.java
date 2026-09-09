package trd.home.media.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;
import trd.home.media.constant.MediaStatus;

@Entity
@Table(name = "media_folder")
@Getter
@Setter
public class Folder extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 1000)
    private String path;

    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private MediaStatus status = MediaStatus.ACTIVE;
}
