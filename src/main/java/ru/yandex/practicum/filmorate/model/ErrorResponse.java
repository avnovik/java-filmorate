package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

/**
 * Модель для возврата информации об ошибке
 */
@Getter
public class ErrorResponse {
    private final String error;
    private final String description;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }
}
