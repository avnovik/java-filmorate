package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Модель для возврата информации об ошибке
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
    private final String error;
    private final String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
}
