package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.BaseTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTest extends BaseTest {

    @Test
    @DisplayName("Не пропускает email без символа @  (проверка аннотации @Email)")
    void shouldFailValidationIfEmailInvalid() {
        User user = User.builder()
                .email("invalid-email") // Нет @
                .login("validLogin")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Email должен содержать символ @", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает логин с пробелами (проверка аннотации @Pattern)")
    void shouldFailValidationIfLoginHasSpaces() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("invalid login") // Пробел
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает пустой login (проверка аннотации @NotBlank)")
    void shouldFailValidationIfLoginIsBlank() {
        User user = User.builder()
                .email("test@mail.ru")
                .login(" ") // Пробел
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает null-значение для login (проверка аннотации @NotBlank)")
    void shouldFailValidationIfLoginIsNull() {
        User user = User.builder()
                .email("test@mail.ru")
                .login(null) // Явный null
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Принимает пустое имя (подставится login)")
    void shouldAcceptEmptyName() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("validLogin")
                .name("") // Пустое имя
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        var violations = validator.validate(user);
        assertTrue(violations.isEmpty()); // Нет ошибок
    }

    @Test
    @DisplayName("Принимает валидные email и login")
    void shouldAcceptValidEmailAndLogin() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("validLogin")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        var violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Отклоняет дату рождения в будущем (проверка аннотации @PastOrPresent)")
    void shouldThrowExceptionIfBirthdayInFuture() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("login")
                .birthday(LocalDate.now().plusDays(1)) // Завтрашняя дата
                .build();

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }
}
