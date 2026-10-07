package trd.home.media.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import trd.home.common.dao.AuditedEntity;
import trd.home.media.constant.MediaStatus;

@Entity
@Table(
        name = "media_actor",
        check = @CheckConstraint(name = "ck_media_actor_status", constraint = "status IN ('ACTIVE', 'INACTIVE')"))
@Getter
@Setter
public class Actor extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @NonNull
    private String id;

    @Column(nullable = false, unique = true, length = 255)
    @NonNull
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    @NonNull
    private MediaStatus status = MediaStatus.ACTIVE;
}
