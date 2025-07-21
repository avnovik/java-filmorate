package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @GetMapping
    public Collection<User> getAllUsers() {
        log.info("Запрошен список всех пользователей, текущее количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        validateBirthday(user.getBirthday());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin()); // Подставляем login, если name пустой
            log.info("Для пользователя {} установлен login как имя", user.getEmail());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        validateBirthday(user.getBirthday());
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id={} не найден", user.getId());
            throw new ValidationException("Пользователь с id=" + user.getId() + " не найден");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Для пользователя {} установлен login как имя", user.getEmail());
        }
        users.put(user.getId(), user);
        log.info("Обновлён пользователь: {}", user);
        return user;
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday == null || birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}