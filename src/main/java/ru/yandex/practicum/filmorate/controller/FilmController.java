package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

/**
 * Контроллер для работы с фильмами.
 * Позволяет добавлять, обновлять, удалять фильмы, ставить лайки и получать списки фильмов.
 */
@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAllFilms() {
        return filmService.findAllFilms();
    }

    @GetMapping("/popular")
    public Collection<Film> getPopulateFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        return filmService.updateFilm(newFilm);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable Long filmId,
                        @PathVariable Long userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable Long filmId,
                           @PathVariable Long userId) {
        filmService.removeLike(filmId, userId);
    }
}
