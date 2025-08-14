package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.BaseIntegrationTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Добавление фильма с валидными данными")
    void shouldAddValidFilm() {
        Film film = Film.builder()
                .name("Test Film ")
                .description("Test shouldAddValidFilm")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1L, "G", "General Audiences"))
                .genres(Set.of(new Genre(1L, "Комедия")))
                .build();

        assertDoesNotThrow(() -> filmController.createFilm(film));
    }


    @Test
    @DisplayName("Принимает дату релиза ровно 28 декабря 1895 года")
    void shouldAcceptReleaseDateExactly1895() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test shouldAcceptReleaseDateExactly1895")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(120)
                .mpa(new MpaRating(1L, "G", "General Audiences"))
                .genres(Set.of(new Genre(1L, "Комедия")))
                .build();
        assertDoesNotThrow(() -> filmController.createFilm(film));
    }

    @Test
    @DisplayName("Отклоняет дату релиза раньше 28 декабря 1895 года")
    void shouldThrowExceptionIfReleaseDateBefore1895() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test shouldThrowExceptionIfReleaseDateBefore1895")
                .releaseDate(LocalDate.of(1890, 12, 28))
                .duration(120)
                .mpa(new MpaRating(1L, "G", "General Audiences"))
                .genres(Set.of(new Genre(1L, "Комедия")))
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации");

        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("При обновлении выбрасывает исключение, если фильм с id не существует")
    void shouldThrowExceptionIfFilmNotFound() {
        Film film = Film.builder()
                .id(999L)
                .name("Valid name")
                .description("Valid shouldThrowExceptionIfFilmNotFound")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1L, "G", "General Audiences"))
                .genres(Set.of(new Genre(1L, "Комедия")))
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.updateFilm(film)
        );

        assertEquals("Фильм с id=999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("PUT /{id}/like/{userId} добавляет лайк фильму")
    void shouldAddLikeToFilm() {
        User user = User.builder()
                .email("test@ya.ru")
                .login("testLogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userController.createUser(user);

        Film film = Film.builder()
                .name("Test Film")
                .description("Test shouldAddLikeToFilm")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1L, "G", "General Audiences"))
                .genres(Set.of(new Genre(1L, "Комедия")))
                .build();
        Film createdFilm = filmController.createFilm(film);

        filmController.addLike(createdFilm.getId(), createdUser.getId());
        Film updatedFilm = filmController.getFilmById(createdFilm.getId());

        assertEquals(1, updatedFilm.getLikes().size(), "Фильм должен иметь 1 лайк");
        assertTrue(updatedFilm.getLikes().contains(createdUser.getId()),
                "Лайк должен быть от пользователя с ID " + createdUser.getId());
    }

    @Test
    @DisplayName("DELETE /{id}/like/{userId} удаляет лайк у фильма")
    void shouldRemoveLikeFromFilm() {
        User user = User.builder()
                .email("test@ya.ru")
                .login("testLogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = userController.createUser(user);

        Film film = Film.builder()
                .name("Test Film")
                .description("Test shouldRemoveLikeFromFilm")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1L, "G", "General Audiences"))
                .genres(Set.of(new Genre(1L, "Комедия")))
                .build();
        Film createdFilm = filmController.createFilm(film);

        filmController.addLike(createdFilm.getId(), createdUser.getId());
        Film likedFilm = filmController.getFilmById(createdFilm.getId());
        assertEquals(1, likedFilm.getLikes().size(), "Фильм должен иметь 1 лайк");

        filmController.removeLike(createdFilm.getId(), createdUser.getId());
        Film unlikedFilm = filmController.getFilmById(createdFilm.getId());
        assertEquals(0, unlikedFilm.getLikes().size(), "Фильм не должен иметь лайков");
    }

    @Test
    @DisplayName("GET /popular возвращает список популярных фильмов")
    void shouldGetPopularFilms() {
        Film film1 = Film.builder()
                .name("Test 1")
                .description("Test Description 1")
                .releaseDate(LocalDate.of(2000, 1, 11))
                .duration(120)
                .mpa(new MpaRating(1L, "G", "General Audiences"))
                .genres(Set.of(new Genre(1L, "Комедия")))
                .build();
        filmController.createFilm(film1);

        Film film2 = Film.builder()
                .name("Test 2")
                .description("Test Description 2")
                .releaseDate(LocalDate.of(2000, 11, 1))
                .duration(120)
                .mpa(new MpaRating(1L, "G", "General Audiences"))
                .genres(Set.of(new Genre(1L, "Комедия")))
                .build();
        filmController.createFilm(film2);

        Collection<Film> popularFilms = filmController.getPopulateFilms(10);
        assertEquals(2, popularFilms.size());
    }

    @Test
    @DisplayName("GET /films/{id} возвращает фильм по id")
    void shouldGetFilmById() {
        Film film = Film.builder()
                .name("Test Film!!!")
                .description("Test shouldGetFilmById")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1L, "G", "General Audiences"))
                .genres(Set.of(new Genre(1L, "Комедия")))
                .build();
        Film createdFilm = filmController.createFilm(film);

        Film foundFilm = filmController.getFilmById(createdFilm.getId());
        assertEquals(createdFilm.getId(), foundFilm.getId());
        assertEquals("Test Film!!!", foundFilm.getName());
    }

    @Test
    @DisplayName("GET /popular с параметром count возвращает указанное количество фильмов")
    void shouldGetPopularFilmsWithCountParameter() {
        addUser();
        for (int i = 1; i <= 5; i++) {
            Film film = Film.builder()
                    .name("Test Film" + i)
                    .description("Test shouldGetPopularFilmsWithCountParameter" + i)
                    .releaseDate(LocalDate.of(2000 + i, 1 + i, 1))
                    .duration(120)
                    .mpa(new MpaRating(1L, "G", "General Audiences"))
                    .genres(Set.of(new Genre(1L, "Комедия")))
                    .build();

            Film createdFilm = filmController.createFilm(film);

            for (int j = 1; j <= i; j++) {
                filmController.addLike(createdFilm.getId(), (long) j);
            }
        }

        Collection<Film> popularFilms = filmController.getPopulateFilms(3);
        assertEquals(3, popularFilms.size());
    }

    @Test
    @DisplayName("GET /popular с count=5 возвращает 5 самых популярных фильмов")
    void shouldReturnTop5PopularFilms() {

        addUser();

        List<Film> films = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Film film = Film.builder()
                    .name("Film " + i)
                    .description("Description " + i)
                    .releaseDate(LocalDate.of(2000 + i, 1 + i, 1))
                    .duration(120)
                    .mpa(new MpaRating(1L, "G", "General Audiences"))
                    .genres(Set.of(new Genre(1L, "Комедия")))
                    .build();

            Film createdFilm = filmController.createFilm(film);
            films.add(createdFilm);
        }

        for (Film film : films) {
            int userLikesCount = faker.number().numberBetween(0, 6);

            for (int userId = 1; userId <= userLikesCount; userId++) {
                filmController.addLike(film.getId(), (long) userId);
            }
        }

        ArrayList<Film> popularFilms = (ArrayList) filmController.getPopulateFilms(5);

        assertEquals(5, popularFilms.size());

        // Проверяем порядок по убыванию лайков
        for (int i = 0; i < popularFilms.size() - 1; i++) {
            assertTrue(
                    popularFilms.get(i).getLikes().size() >= popularFilms.get(i + 1).getLikes().size(),
                    "Фильмы должны быть отсортированы по убыванию лайков"
            );
        }
    }
}
