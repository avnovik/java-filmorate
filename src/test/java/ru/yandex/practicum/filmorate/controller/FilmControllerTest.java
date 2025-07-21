package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private final FilmController controller = new FilmController();

    @Test
    @DisplayName("Принимает дату релиза ровно 28 декабря 1895 года")
    void shouldAcceptReleaseDateExactly1895() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // Граничная дата
        film.setDuration(120);

        assertDoesNotThrow(() -> controller.addFilm(film));
    }

    @Test
    @DisplayName("Отклоняет дату релиза раньше 28 декабря 1895 года")
    void shouldThrowExceptionIfReleaseDateBefore1895() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1890, 1, 1)); // Невалидная дата
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    @DisplayName("При обновлении выбрасывает исключение, если фильм с id не существует")
    void shouldThrowExceptionIfFilmNotFound() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Invalid Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.updateFilm(film)
        );

        assertEquals("Фильм с id=999 не найден", exception.getMessage());
    }
}
