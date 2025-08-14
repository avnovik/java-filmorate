package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmRepository;

import java.util.Collection;

/**
 * Сервис для операций с фильмами.
 * Реализует бизнес-логику добавление и удаление лайка, вывод 10 наиболее популярных фильмов по количеству лайков.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final ValidationService validationService;
    private final FilmRepository filmRepository;
    private final LikeService likeService;

    public Collection<Film> findAllFilms() {
        log.info("Попытка получения всех фильмов");
        return filmRepository.findAllFilms();
    }

    public Film getFilmById(Long filmId) {
        log.info("Попытка получения фильма по ID: {}", filmId);
        validationService.validateFilmExists(filmId);
        return filmRepository.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }

    public Film createFilm(Film film) {
        log.info("Попытка создания фильма: {}", film.getName());
        validationService.validateFilm(film);
        Film createdFilm = filmRepository.createFilm(film);
        log.info("Создан фильм с ID: {}", createdFilm.getId());
        return createdFilm;
    }

    public Film updateFilm(Film newFilm) {
        log.info("Попытка обновления фильма с ID: {}", newFilm.getId());
        if (filmRepository.getFilmById(newFilm.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + newFilm.getId() + " не найден");
        }
        validationService.validateFilm(newFilm);
        Film updatedFilm = filmRepository.updateFilm(newFilm);
        log.info("Фильм с ID {} обновлен", newFilm.getId());
        return updatedFilm;
    }

    public Collection<Film> getPopularFilms(int count) {
        log.info("Попытка получения популярных фильмов в количестве {} штук", count);
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным числом.");
        }
        return filmRepository.getPopularFilms(count);
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Попытка добавления лайка фильму {} от пользователя {}", filmId, userId);
        validationService.validateFilmAndUserIds(filmId, userId);
        validationService.validateFilmExists(filmId);
        validationService.validateUserExists(userId);
        likeService.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Попытка удаления лайка у фильма {} от пользователя {}", filmId, userId);
        validationService.validateFilmAndUserIds(filmId, userId);
        likeService.removeLike(filmId, userId);
        log.info("Пользователь {} убрал лайк у фильма {}", userId, filmId);
    }
}
