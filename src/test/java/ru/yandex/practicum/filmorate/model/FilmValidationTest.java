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
        Film film = new Film();
        film.setName(" "); // Пробел не считается за валидное значение
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        var violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает описание длиннее 200 символов (проверка аннотации @Size)")
    void shouldFailValidationIfDescriptionTooLong() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("A".repeat(201)); // 201 символ
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        var violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Описание не должно превышать 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает отрицательную продолжительность фильма (проверка аннотации @Positive)")
    void shouldFailValidationIfDurationNegative() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);

        var violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность должна быть положительной", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает null для даты релиза (проверка аннотации @NotNull)")
    void shouldFailValidationIfReleaseDateIsNull() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(null);
        film.setDuration(120);

        var violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза обязательна", violations.iterator().next().getMessage());
    }
}
