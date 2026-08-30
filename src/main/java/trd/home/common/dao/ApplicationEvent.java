package trd.home.common.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trd.home.common.constant.EventStatus;
import trd.home.common.constant.EventType;
import trd.home.common.logging.LogMethodCall;

@Entity
@Table(name = "APPLICATION_EVENT")
@Getter
@NoArgsConstructor
public class ApplicationEvent extends AuditedEntity {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 4000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 64)
    private EventType type;

    @Column(name = "PROCESSED_AT")
    private Instant processedAt;

    @Column(name = "ERROR_MESSAGE", length = MAX_ERROR_MESSAGE_LENGTH)
    private String errorMessage;

    @Column(name = "MESSAGE", length = MAX_ERROR_MESSAGE_LENGTH)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private EventStatus status;

    public ApplicationEvent(EventType type) {
        this(type, null);
    }

    public ApplicationEvent(EventType type, String message) {
        this.type = type;
        this.message = message;
        this.status = EventStatus.TO_DO;
    }

    @LogMethodCall
    public void markProcessing() {
        status = EventStatus.PROCESSING;
    }

    @LogMethodCall
    public void markDone() {
        status = EventStatus.DONE;
        processedAt = Instant.now();
        errorMessage = null;
    }

    @LogMethodCall
    public void markFailed(Throwable throwable) {
        status = EventStatus.ERROR;
        String message = throwable.getMessage();
        if (Objects.isNull(message) || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        errorMessage = message.substring(0, Math.min(message.length(), MAX_ERROR_MESSAGE_LENGTH));
    }
}
