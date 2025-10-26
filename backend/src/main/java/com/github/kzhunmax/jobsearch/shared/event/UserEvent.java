package com.github.kzhunmax.jobsearch.shared.event;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record UserEvent(
        String email,
        @Enumerated(EnumType.STRING) EventType eventType
) {
}
