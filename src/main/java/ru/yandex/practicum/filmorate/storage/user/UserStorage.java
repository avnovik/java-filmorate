package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

/**
 * Интерфейс для хранилища пользователей.
 * Определяет методы для добавления, обновления и получения пользователей.
 */
public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    Collection<User> getAllUsers();

    User getUserById(Long id);
}
