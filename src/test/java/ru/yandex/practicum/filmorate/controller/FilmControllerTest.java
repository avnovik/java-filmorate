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

        filmController = new FilmController(filmService);
        userController = new UserController(userService);
    }

    @Test
    @DisplayName("Принимает дату релиза ровно 28 декабря 1895 года")
    void shouldAcceptReleaseDateExactly1895() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        assertDoesNotThrow(() -> filmController.addFilm(film));
    }

    @Test
    @DisplayName("Отклоняет дату релиза раньше 28 декабря 1895 года")
    void shouldThrowExceptionIfReleaseDateBefore1895() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1890, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    @DisplayName("При обновлении выбрасывает исключение, если фильм с id не существует")
    void shouldThrowExceptionIfFilmNotFound() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.updateFilm(film)
        );

        assertEquals("Фильм с id=999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("PUT /{id}/like/{userId} добавляет лайк фильму")
    void shouldAddLikeToFilm() {
        User user = new User();
        user.setEmail("test@ya.ru");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.addUser(user);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmController.addFilm(film);

        filmController.addLike(createdFilm.getId(), createdUser.getId());

        assertEquals(1, createdFilm.getLikes().size(), "Фильм должен иметь 1 лайк");
        assertTrue(createdFilm.getLikes().contains(createdUser.getId()),
                "Лайк должен быть от пользователя с ID " + createdUser.getId());
    }

    @Test
    @DisplayName("DELETE /{id}/like/{userId} удаляет лайк у фильма")
    void shouldRemoveLikeFromFilm() {
        User user = new User();
        user.setEmail("test@ya.ru");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.addUser(user);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmController.addFilm(film);

        filmController.addLike(createdFilm.getId(), createdUser.getId());
        assertEquals(1, createdFilm.getLikes().size(), "Фильм должен иметь 1 лайк");

        filmController.removeLike(createdFilm.getId(), createdUser.getId());
        assertEquals(0, createdFilm.getLikes().size(), "Фильм не должен иметь лайк");
    }

    @Test
    @DisplayName("GET /popular возвращает список популярных фильмов")
    void shouldGetPopularFilms() {
        Film film1 = new Film();
        film1.setName("Test 1");
        film1.setDescription("Test Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 11));
        film1.setDuration(120);
        filmController.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Test 2");
        film2.setDescription("Test Description 2");
        film2.setReleaseDate(LocalDate.of(2000, 11, 1));
        film2.setDuration(120);
        filmController.addFilm(film2);

        List<Film> popularFilms = filmController.getPopular(10);
        assertEquals(2, popularFilms.size());
    }

    @Test
    @DisplayName("GET /popular с параметром count возвращает указанное количество фильмов")
    void shouldGetPopularFilmsWithCountParameter() {
        for (int i = 1; i <= 5; i++) {
            Film film = new Film();
            film.setName("Test Film" + i);
            film.setDescription("Test Description" + i);
            film.setReleaseDate(LocalDate.of(2000 + i, 1 + i, 1));
            film.setDuration(120);
            filmController.addFilm(film);
        }

        List<Film> popularFilms = filmController.getPopular(3);
        assertEquals(3, popularFilms.size());
    }

    @Test
    @DisplayName("GET /popular с count=5 возвращает 5 самых популярных фильмов")
    void shouldReturnTop5PopularFilms() {

        for (int i = 1; i <= 7; i++) {
            User user = new User();
            user.setEmail("user" + i + "@test.com");
            user.setLogin("user" + i);
            user.setName(faker.name().fullName());
            user.setBirthday(LocalDate.now().minusYears(33 + i));
            userController.addUser(user);
        }

        List<Film> films = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Film film = new Film();
            film.setName(faker.funnyName().name());
            film.setDescription(faker.lorem().sentence());
            film.setReleaseDate(LocalDate.of(2000 + i, 1 + i, 1));
            film.setDuration(faker.number().numberBetween(60, 180));
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
