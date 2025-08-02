package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

/**
 * Интерфейс для хранилища фильмов.
 * Определяет методы для добавления, обновления и получения фильмов.
 */
public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();

    Film getFilmById(Long id);
}
