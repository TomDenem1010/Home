package trd.home.common.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import trd.home.common.logging.LogMethodCall;

@Entity
@Table(name = "APPLICATION_LOG")
@Getter
@NoArgsConstructor
public class ApplicationLog extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "METHOD_NAME", nullable = false, length = 1000)
    private String method;

    @Lob
    @Column(name = "METHOD_INPUT")
    private String input;

    @Lob
    @Column(name = "METHOD_OUTPUT")
    private String output;

    @Lob
    @Column(name = "ERROR")
    private String error;

    @Column(name = "DURATION_MS", nullable = false)
    private long durationMs;

    private ApplicationLog(String method, String input, String output, String error, long durationMs) {
        this.method = method;
        this.input = input;
        this.output = output;
        this.error = error;
        this.durationMs = durationMs;
    }

    @LogMethodCall
    public static ApplicationLog successful(String method, String input, Object output, long durationMs) {
        return new ApplicationLog(method, input, Objects.isNull(output) ? null : output.toString(), null, durationMs);
    }

    @LogMethodCall
    public static ApplicationLog failed(String method, String input, Throwable throwable, long durationMs) {
        return new ApplicationLog(method, input, null, throwable.toString(), durationMs);
    }
}
