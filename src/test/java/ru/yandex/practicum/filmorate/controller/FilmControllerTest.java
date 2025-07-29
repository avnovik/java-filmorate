package ru.yandex.practicum.filmorate.controller;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private final Faker faker = new Faker();
    private FilmController filmController;
    private UserController userController;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        FilmService filmService = new FilmService(filmStorage, userService);

        filmController = new FilmController(filmStorage, filmService);
        userController = new UserController(userStorage, userService);
    }

    @Test
    @DisplayName("Принимает дату релиза ровно 28 декабря 1895 года")
    void shouldAcceptReleaseDateExactly1895() {
        Film film = Film.builder()
                .name("Valid name")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(120)
                .build();

        assertDoesNotThrow(() -> filmController.addFilm(film));
    }

    @Test
    @DisplayName("Отклоняет дату релиза раньше 28 декабря 1895 года")
    void shouldThrowExceptionIfReleaseDateBefore1895() {
        Film film = Film.builder()
                .name("Valid name")
                .description("Valid description")
                .releaseDate(LocalDate.of(1890, 1, 1))
                .duration(120)
                .build();

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    @DisplayName("При обновлении выбрасывает исключение, если фильм с id не существует")
    void shouldThrowExceptionIfFilmNotFound() {
        Film film = Film.builder()
                .id(999L)
                .name("Valid name")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
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
        User createdUser = userController.addUser(user);

        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film createdFilm = filmController.addFilm(film);

        filmController.addLike(createdFilm.getId(), createdUser.getId());

        assertEquals(1, createdFilm.getLikes().size(), "Фильм должен иметь 1 лайк");
        assertTrue(createdFilm.getLikes().contains(createdUser.getId()),
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
        User createdUser = userController.addUser(user);

        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        Film createdFilm = filmController.addFilm(film);

        filmController.addLike(createdFilm.getId(), createdUser.getId());
        assertEquals(1, createdFilm.getLikes().size(), "Фильм должен иметь 1 лайк");

        filmController.removeLike(createdFilm.getId(), createdUser.getId());
        assertEquals(0, createdFilm.getLikes().size(), "Фильм не должен иметь лайк");
    }

    @Test
    @DisplayName("GET /popular возвращает список популярных фильмов")
    void shouldGetPopularFilms() {
        Film film1 = Film.builder()
                .name("Test 1")
                .description("Test Description 1")
                .releaseDate(LocalDate.of(2000, 1, 11))
                .duration(120)
                .build();
        filmController.addFilm(film1);

        Film film2 = Film.builder()
                .name("Test 2")
                .description("Test Description 2")
                .releaseDate(LocalDate.of(2000, 11, 1))
                .duration(120)
                .build();
        filmController.addFilm(film2);

        List<Film> popularFilms = filmController.getPopular(10);
        assertEquals(2, popularFilms.size());
    }

    @Test
    @DisplayName("GET /popular с параметром count возвращает указанное количество фильмов")
    void shouldGetPopularFilmsWithCountParameter() {
        for (int i = 1; i <= 5; i++) {
            Film film = Film.builder()
                    .name("Test Film" + i)
                    .description("Test Description" + i)
                    .releaseDate(LocalDate.of(2000 + i, 1 + i, 1))
                    .duration(120)
                    .build();
            filmController.addFilm(film);
        }

        List<Film> popularFilms = filmController.getPopular(3);
        assertEquals(3, popularFilms.size());
    }

    @Test
    @DisplayName("GET /popular с count=5 возвращает 5 самых популярных фильмов")
    void shouldReturnTop5PopularFilms() {

        for (int i = 1; i <= 7; i++) {
            User user = User.builder()
                    .email("user" + i + "@test.com")
                    .login("user" + i)
                    .name(faker.name().fullName())
                    .birthday(LocalDate.now().minusYears(33 + i))
                    .build();
            userController.addUser(user);
        }

        List<Film> films = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Film film = Film.builder()
                    .name(faker.funnyName().name())
                    .description(faker.lorem().sentence())
                    .releaseDate(LocalDate.of(2000 + i, 1 + i, 1))
                    .duration(faker.number().numberBetween(60, 180))
                    .build();
            filmController.addFilm(film);
            films.add(film);
        }

        for (Film film : films) {
            int userLikesCount = faker.number().numberBetween(2, 8);

            // Добавляем лайки, используя существующих пользователей
            for (int userId = 1; userId <= userLikesCount; userId++) {
                filmController.addLike(film.getId(), (long) userId);
            }
        }

        List<Film> popularFilms = filmController.getPopular(5);
        List<Film> allFilms = filmController.getAllFilms();
        List<Film> expectedTop5 = allFilms.stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(5)
                .collect(Collectors.toList());

        assertEquals(expectedTop5.size(), popularFilms.size());


        assertIterableEquals(expectedTop5, popularFilms,
                "Списки фильмов должны полностью совпадать");

        for (int i = 0; i < expectedTop5.size(); i++) {
            Film expected = expectedTop5.get(i);
            Film actual = popularFilms.get(i);

            assertEquals(expected.getId(), actual.getId(),
                    "Фильм на позиции " + i + " не совпадает");
            assertEquals(expected.getLikes().size(), actual.getLikes().size(),
                    "Количество лайков у фильма " + expected.getName() + " не совпадает");
        }
    }
}
