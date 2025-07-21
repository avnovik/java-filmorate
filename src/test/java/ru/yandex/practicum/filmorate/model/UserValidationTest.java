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
        User user = new User();
        user.setEmail("invalid-email"); // Нет @
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Email должен содержать символ @", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает логин с пробелами (проверка аннотации @Pattern)")
    void shouldFailValidationIfLoginHasSpaces() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("invalid login"); // Пробел
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает пустой login (проверка аннотации @NotBlank)")
    void shouldFailValidationIfLoginIsBlank() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin(" ");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Не пропускает null-значение для login (проверка аннотации @NotBlank)")
    void shouldFailValidationIfLoginIsNull() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin(null);
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Принимает пустое имя (подставится login)")
    void shouldAcceptEmptyName() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("validLogin");
        user.setName(""); // Пустое имя
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user);
        assertTrue(violations.isEmpty()); // Нет ошибок
    }

    @Test
    @DisplayName("Принимает валидные email и login")
    void shouldAcceptValidEmailAndLogin() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        var violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Отклоняет дату рождения в будущем (проверка аннотации @PastOrPresent)")
    void shouldThrowExceptionIfBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1)); // Завтрашняя дата

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }
}
