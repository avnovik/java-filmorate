package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.BaseIntegrationTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest extends BaseIntegrationTest {
    private User testUser;
    private User friendUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@mail.ru")
                .login("testLogin")
                .name("Test User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        friendUser = User.builder()
                .email("friend@mail.ru")
                .login("friendLogin")
                .name("Friend User")
                .birthday(LocalDate.of(2001, 2, 2))
                .build();
    }

    @Test
    @DisplayName("Добавляет пользователя с валидными данными")
    void shouldAddUserWithValidData() {
        User addedUser = userController.createUser(testUser);

        assertNotNull(addedUser.getId());
        assertEquals(testUser.getEmail(), addedUser.getEmail());
    }

    @Test
    @DisplayName("Подставляет login как name, если name пустое")
    void shouldSetLoginAsNameIfNameIsEmpty() {
        testUser.setName("");

        User savedUser = userController.createUser(testUser);
        assertEquals(testUser.getLogin(), savedUser.getName());
    }

    @Test
    @DisplayName("Отклоняет обновление несуществующего пользователя")
    void shouldThrowExceptionIfUserNotFound() {
        testUser.setId(999L);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.updateUser(testUser)
        );
        assertEquals("Пользователь с ID 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("При обновлении подставляет login, если name пустое")
    void shouldSetLoginAsNameIfNameIsEmptyDuringUpdate() {
        User existingUser = userController.createUser(testUser);

        User updatedUser = new User();
        updatedUser.setId(existingUser.getId());
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("new_login");
        updatedUser.setName(" ");
        updatedUser.setBirthday(LocalDate.of(2000, 1, 1));

        User result = userController.updateUser(updatedUser);
        assertEquals("new_login", result.getName());
    }

    @Test
    @DisplayName("Возвращает всех добавленных пользователей")
    void shouldReturnAllAddedUsers() {
        userController.createUser(testUser);

        Collection<User> users = userController.findAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    @DisplayName("Добавляет пользователя в друзья")
    void shouldAddFriend() {
        userController.createUser(testUser);
        userController.createUser(friendUser);
        userController.addFriend(testUser.getId(), friendUser.getId());

        List<User> friends = (List) userController.getFriends(testUser.getId());
        assertEquals(1, friends.size());
        assertEquals(friendUser.getId(), friends.get(0).getId());
    }

    @Test
    @DisplayName("Удаляет пользователя из друзей")
    void shouldRemoveFriend() {
        userController.createUser(testUser);
        userController.createUser(friendUser);
        userController.addFriend(testUser.getId(), friendUser.getId());
        userController.removeFriend(testUser.getId(), friendUser.getId());

        List<User> friends = (List) userController.getFriends(testUser.getId());
        assertTrue(friends.isEmpty());
    }

    @Test
    @DisplayName("Возвращает список друзей")
    void shouldReturnFriendsList() {
        userController.createUser(testUser);
        userController.createUser(friendUser);
        userController.addFriend(testUser.getId(), friendUser.getId());

        List<User> friends = (List) userController.getFriends(testUser.getId());
        assertEquals(1, friends.size());
        assertEquals(friendUser.getId(), friends.get(0).getId());
    }

    @Test
    @DisplayName("Возвращает общих друзей")
    void shouldReturnCommonFriends() {
        userController.createUser(testUser);
        userController.createUser(friendUser);
        User commonFriend = new User();
        commonFriend.setEmail("common@mail.ru");
        commonFriend.setLogin("commonLogin");
        commonFriend = userController.createUser(commonFriend);

        userController.addFriend(testUser.getId(), commonFriend.getId());
        userController.addFriend(friendUser.getId(), commonFriend.getId());

        List<User> commonFriends = (List) userController.getCommonFriends(testUser.getId(), friendUser.getId());
        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.getFirst().getId());
    }

    @Test
    @DisplayName("Отклоняет добавление несуществующего друга")
    void shouldThrowWhenAddingNonExistingFriend() {
        userController.createUser(friendUser);
        assertThrows(ValidationException.class,
                () -> userController.addFriend(testUser.getId(), 999L));
    }

    @Test
    @DisplayName("Отклоняет добавление друга к несуществующему пользователю")
    void shouldThrowWhenAddingFriendToNonExistingUser() {
        userController.createUser(friendUser);
        assertThrows(NotFoundException.class,
                () -> userController.addFriend(999L, friendUser.getId()));
    }
}
