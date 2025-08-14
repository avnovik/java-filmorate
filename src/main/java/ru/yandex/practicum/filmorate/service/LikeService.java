package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.like.LikeRepository;

/**
 * Сервис для управления лайками фильмов.
 * Делегирует операции добавления/удаления лайков в репозиторий.
 */
@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

    public void addLike(Long filmId, Long userId) {
        likeRepository.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        likeRepository.removeLike(filmId, userId);
    }
}
