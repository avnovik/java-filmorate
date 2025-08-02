package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private InMemoryUserStorage userStorage;
    private UserService userService;
    private UserController controller;
    private User testUser;
    private User friendUser;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        controller = new UserController(userService);

        testUser = new User();
        testUser.setEmail("test@mail.ru");
        testUser.setLogin("testLogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));

        friendUser = new User();
        friendUser.setEmail("friend@mail.ru");
        friendUser.setLogin("friendLogin");
        friendUser.setName("Friend User");
        friendUser.setBirthday(LocalDate.of(2001, 2, 2));
    }

    @Test
    @DisplayName("Добавляет пользователя с валидными данными")
    void shouldAddUserWithValidData() {
        User addedUser = controller.addUser(testUser);

        assertNotNull(addedUser.getId());
        assertEquals(testUser.getEmail(), addedUser.getEmail());
    }

    @Test
    @DisplayName("Подставляет login как name, если name пустое")
    void shouldSetLoginAsNameIfNameIsEmpty() {
        testUser.setName("");

        User savedUser = controller.addUser(testUser);
        assertEquals(testUser.getLogin(), savedUser.getName());
    }

    @Test
    @DisplayName("Отклоняет обновление несуществующего пользователя")
    void shouldThrowExceptionIfUserNotFound() {
        testUser.setId(999L);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> controller.updateUser(testUser)
        );
        assertEquals("Пользователь с id=999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("При обновлении подставляет login, если name пустое")
    void shouldSetLoginAsNameIfNameIsEmptyDuringUpdate() {
        User existingUser = controller.addUser(testUser);

        User updatedUser = new User();
        updatedUser.setId(existingUser.getId());
        updatedUser.setEmail("new@mail.ru");
        updatedUser.setLogin("new_login");
        updatedUser.setName(" ");
        updatedUser.setBirthday(LocalDate.of(2000, 1, 1));

        User result = controller.updateUser(updatedUser);
        assertEquals("new_login", result.getName());
    }

    @Test
    @DisplayName("Возвращает всех добавленных пользователей")
    void shouldReturnAllAddedUsers() {
        controller.addUser(testUser);

        Collection<User> users = controller.getAllUsers();
        assertEquals(1, users.size());
    }

    @Test
    @DisplayName("Добавляет пользователя в друзья")
    void shouldAddFriend() {
        controller.addUser(testUser);
        controller.addUser(friendUser);
        controller.addFriend(testUser.getId(), friendUser.getId());

        List<User> friends = controller.getFriends(testUser.getId());
        assertEquals(1, friends.size());
        assertEquals(friendUser.getId(), friends.get(0).getId());
    }

    @Test
    @DisplayName("Удаляет пользователя из друзей")
    void shouldRemoveFriend() {
        controller.addUser(testUser);
        controller.addUser(friendUser);
        controller.addFriend(testUser.getId(), friendUser.getId());
        controller.removeFriend(testUser.getId(), friendUser.getId());

        List<User> friends = controller.getFriends(testUser.getId());
        assertTrue(friends.isEmpty());
    }

    @Test
    @DisplayName("Возвращает список друзей")
    void shouldReturnFriendsList() {
        controller.addUser(testUser);
        controller.addUser(friendUser);
        controller.addFriend(testUser.getId(), friendUser.getId());

        List<User> friends = controller.getFriends(testUser.getId());
        assertEquals(1, friends.size());
        assertEquals(friendUser.getId(), friends.get(0).getId());
    }

    @Test
    @DisplayName("Возвращает общих друзей")
    void shouldReturnCommonFriends() {
        controller.addUser(testUser);
        controller.addUser(friendUser);
        User commonFriend = new User();
        commonFriend.setEmail("common@mail.ru");
        commonFriend.setLogin("commonLogin");
        commonFriend = controller.addUser(commonFriend);

        controller.addFriend(testUser.getId(), commonFriend.getId());
        controller.addFriend(friendUser.getId(), commonFriend.getId());

        List<User> commonFriends = controller.getCommonFriends(testUser.getId(), friendUser.getId());
        assertEquals(1, commonFriends.size());
        assertEquals(commonFriend.getId(), commonFriends.getFirst().getId());
    }

    @Test
    @DisplayName("Отклоняет добавление несуществующего друга")
    void shouldThrowWhenAddingNonExistingFriend() {
        controller.addUser(friendUser);
        assertThrows(NotFoundException.class,
                () -> controller.addFriend(testUser.getId(), 999L));
    }

    @Test
    @DisplayName("Отклоняет добавление друга к несуществующему пользователю")
    void shouldThrowWhenAddingFriendToNonExistingUser() {
        controller.addUser(friendUser);
        assertThrows(NotFoundException.class,
                () -> controller.addFriend(999L, friendUser.getId()));
    }
}
