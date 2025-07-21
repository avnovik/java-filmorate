package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {
    private final UserController controller = new UserController();

    @Test
    @DisplayName("Подставляет login как name, если name пустое")
    void shouldSetLoginAsNameIfNameIsEmpty() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setName(""); // Пустое имя
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User savedUser = controller.addUser(user);
        assertEquals("login", savedUser.getName());
    }

    @Test
    @DisplayName("Отклоняет обновление несуществующего пользователя")
    void shouldThrowExceptionIfUserNotFound() {
        User user = new User();
        user.setId(999L);
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> controller.updateUser(user)
        );
        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("При обновлении подставляет login, если name пустое")
    void shouldSetLoginAsNameIfNameIsEmptyDuringUpdate() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("old@mail.ru");
        existingUser.setLogin("old_login");
        existingUser.setName("Old Name");
        existingUser.setBirthday(LocalDate.of(2000, 1, 1));
        controller.addUser(existingUser);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("new_login");
        updatedUser.setName(" "); // Пустое имя
        updatedUser.setBirthday(LocalDate.of(2000, 1, 1));

        User result = controller.updateUser(updatedUser);
        assertEquals("new_login", result.getName());
    }
}
