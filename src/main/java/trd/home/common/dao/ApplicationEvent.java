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
import lombok.Getter;
import lombok.NoArgsConstructor;
import trd.home.common.constant.EventType;

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

    public ApplicationEvent(EventType type) {
        this.type = type;
    }

    public void markProcessed() {
        processedAt = Instant.now();
        errorMessage = null;
    }

    public void markFailed(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.getClass().getSimpleName();
        }
        errorMessage = message.substring(0, Math.min(message.length(), MAX_ERROR_MESSAGE_LENGTH));
    }
}
