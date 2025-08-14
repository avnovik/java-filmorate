package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreRepository;

import java.util.List;
import java.util.Set;

/**
 * Сервис для работы с жанрами фильмов.
 * Предоставляет методы для поиска жанров по ID, получения всех жанров и жанров конкретного фильма.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public Genre findGenreById(Long genreId) {
        return genreRepository.findGenreById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с ID: " + genreId + " не найден."));
    }

    public List<Genre> findAllGenres() {
        return genreRepository.findAllGenres();
    }

    public Set<Genre> findGenreByFilmId(Long filmId) {
        return genreRepository.findGenreByFilmId(filmId);
    }
}