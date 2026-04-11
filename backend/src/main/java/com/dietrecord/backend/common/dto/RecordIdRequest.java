package com.dietrecord.backend.common.dto;

import jakarta.validation.constraints.NotNull;

public record RecordIdRequest(
        @NotNull(message = "recordId is required")
        Long recordId
) {
}
