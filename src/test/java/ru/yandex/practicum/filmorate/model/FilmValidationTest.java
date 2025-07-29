package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.BaseTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FilmValidationTest extends BaseTest {

    @Test
    @DisplayName("Не пропускает пустое название фильма (проверка аннотации @NotBlank)")
    void shouldFailValidationIfNameIsBlank() {
        Film film = Film.builder()
                .name(" ") // Пробел не считается за валидное значение
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        var violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает описание длиннее 200 символов (проверка аннотации @Size)")
    void shouldFailValidationIfDescriptionTooLong() {
        Film film = Film.builder()
                .name("Valid name")
                .description("A".repeat(201)) // 201 символ
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        var violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Описание не должно превышать 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает отрицательную продолжительность фильма (проверка аннотации @Positive)")
    void shouldFailValidationIfDurationNegative() {
        Film film = Film.builder()
                .name("Valid name")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-1)
                .build();

        var violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность должна быть положительной", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает null для даты релиза (проверка аннотации @NotNull)")
    void shouldFailValidationIfReleaseDateIsNull() {
        Film film = Film.builder()
                .name("Valid name")
                .description("Valid description")
                .releaseDate(null)
                .duration(120)
                .build();

        var violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза обязательна", violations.iterator().next().getMessage());
    }
}
