package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для операций с фильмами.
 * Реализует бизнес-логику добавление и удаление лайка, вывод 10 наиболее популярных фильмов по количеству лайков.
 */
@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film addFilm(Film film) {
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        getFilmOrThrow(film.getId());
        validateReleaseDate(film.getReleaseDate());
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        return new ArrayList<>(filmStorage.getAllFilms());
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getFilmOrThrow(filmId);
        userService.getUserByIdOrThrow(userId);

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже ставил лайк этому фильму");
        }

        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmOrThrow(filmId);
        userService.getUserByIdOrThrow(userId);
        if (!film.getLikes().remove(userId)) {
            throw new NotFoundException("Лайк не найден");
        }
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count > 0 ? count : 10)
                .collect(Collectors.toList());
    }

    private Film getFilmOrThrow(Long id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(MIN_RELEASE_DATE)) {
            log.warn("Неверная дата релиза: {}", releaseDate);
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }
}
