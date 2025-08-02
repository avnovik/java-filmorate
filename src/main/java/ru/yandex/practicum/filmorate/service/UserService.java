package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

/**
 * Сервис для обработки операций с друзьями пользователей.
 * Реализует бизнес-логику добавления/удаления друзей и поиска общих друзей.
 */
@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Для пользователя {} установлен login как имя", user.getEmail());
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        getUserByIdOrThrow(user.getId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Для пользователя {} установлен login как имя", user.getEmail());
        }
        return userStorage.updateUser(user);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUserByIdOrThrow(userId);
        User friend = getUserByIdOrThrow(friendId);

        user.getFriends().add(friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
        friend.getFriends().add(userId); // Дружба взаимная
        log.info("Пользователь {} добавил в друзья {}", friendId, userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserByIdOrThrow(userId);
        User friend = getUserByIdOrThrow(friendId);

        user.getFriends().remove(friendId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь {} удалил из друзей {}", friendId, userId);
    }

    public List<User> getFriends(Long userId) {
        User user = getUserByIdOrThrow(userId);
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserByIdOrThrow(userId);
        User otherUser = getUserByIdOrThrow(otherId);

        Set<Long> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(otherUser.getFriends()); // Оставляем только общих

        return commonIds.stream()
                .map(userStorage::getUserById)  // ID → User
                .filter(Objects::nonNull)
                .toList();
    }

    protected User getUserByIdOrThrow(Long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }
}
