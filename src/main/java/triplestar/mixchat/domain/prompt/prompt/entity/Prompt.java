package triplestar.mixchat.domain.prompt.prompt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PROMPTS")
@EntityListeners(AuditingEntityListener.class)
public class Prompt extends BaseEntity {
    public enum PromptType {
        PRE_SCRIPTED,
        CUSTOM
    }

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "prompt_type", nullable = false)
    private PromptType type;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "scenario_id", length = 50, nullable = false, unique = true)
    private String scenarioId;
}
