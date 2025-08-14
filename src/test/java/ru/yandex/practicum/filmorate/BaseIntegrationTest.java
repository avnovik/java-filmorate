package ru.yandex.practicum.filmorate;

import com.github.javafaker.Faker;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.service.*;
import ru.yandex.practicum.filmorate.storage.film.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.storage.friend.JdbcFriendRepository;
import ru.yandex.practicum.filmorate.storage.genre.JdbcGenreRepository;
import ru.yandex.practicum.filmorate.storage.like.JdbcLikeRepository;
import ru.yandex.practicum.filmorate.storage.mpa.JdbcMpaRepository;
import ru.yandex.practicum.filmorate.storage.user.JdbcUserRepository;

import java.util.Random;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestExecutionListeners(
        listeners = ControllerInitListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public abstract class BaseIntegrationTest {
    protected final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static NamedParameterJdbcOperations jdbcTemplate;

    protected Random random = new Random();
    protected Faker faker = new Faker();

    @Autowired
    protected JdbcOperations jdbcOperations;

    protected static FilmController filmController;
    protected static UserController userController;
    protected static GenreController genreController;
    protected static MpaController mpaController;

    @BeforeEach
    void cleanDatabase() {
        // Отключаем проверку внешних ключей
        jdbcOperations.execute("SET REFERENTIAL_INTEGRITY=FALSE");

        jdbcOperations.execute("TRUNCATE TABLE likes RESTART IDENTITY");
        jdbcOperations.execute("TRUNCATE TABLE film_genre RESTART IDENTITY");
        jdbcOperations.execute("TRUNCATE TABLE friends RESTART IDENTITY");
        jdbcOperations.execute("TRUNCATE TABLE films RESTART IDENTITY");
        jdbcOperations.execute("TRUNCATE TABLE users RESTART IDENTITY");

        // Включаем проверку обратно
        jdbcOperations.execute("SET REFERENTIAL_INTEGRITY=TRUE");
    }

    protected void addUser() {
        jdbcOperations.update("""
                MERGE INTO users (user_id, email, login, name, birthday)
                KEY (user_id)
                VALUES
                    (1, 'user1@mail.ru', 'user1', 'User One', '1990-01-01'),
                    (2, 'user2@mail.ru', 'user2', 'User Two', '1995-05-15'),
                    (3, 'user3@mail.ru', 'user3', 'User Three', '2000-10-20'),
                    (4, 'user4@mail.ru', 'user4', 'User Four', '2005-10-20'),
                    (5, 'user5@mail.ru', 'user5', 'User Five', '2010-10-20'),
                    (6, 'user6@mail.ru', 'user6', 'User Six', '2015-10-20'),
                    (7, 'user7@mail.ru', 'user7', 'User Seven', '2020-10-20')
                """);
    }

    public static void initControllers(NamedParameterJdbcOperations jdbcTemplate) {
        BaseIntegrationTest.jdbcTemplate = jdbcTemplate;

        UserRowMapper userMapper = new UserRowMapper();
        FilmRowMapper filmMapper = new FilmRowMapper();
        GenreRowMapper genreMapper = new GenreRowMapper();
        MpaRatingRowMapper mpaMapper = new MpaRatingRowMapper();

        JdbcUserRepository userRepo = new JdbcUserRepository(jdbcTemplate, userMapper);
        JdbcGenreRepository genreRepo = new JdbcGenreRepository(jdbcTemplate, genreMapper);
        JdbcMpaRepository mpaRepo = new JdbcMpaRepository(jdbcTemplate, mpaMapper);
        JdbcFilmRepository filmRepo = new JdbcFilmRepository(jdbcTemplate, filmMapper, genreRepo);
        JdbcFriendRepository friendRepo = new JdbcFriendRepository(jdbcTemplate, userMapper);
        JdbcLikeRepository likeRepo = new JdbcLikeRepository(jdbcTemplate);

        ValidationService validationService = new ValidationService(userRepo, filmRepo, genreRepo, mpaRepo);
        LikeService likeService = new LikeService(likeRepo);
        FriendService friendService = new FriendService(friendRepo, validationService);
        UserService userService = new UserService(userRepo, validationService);
        FilmService filmService = new FilmService(validationService, filmRepo, likeService);
        GenreService genreService = new GenreService(genreRepo);
        MpaRatingService mpaService = new MpaRatingService(mpaRepo);

        filmController = new FilmController(filmService);
        userController = new UserController(userService, friendService);
        genreController = new GenreController(genreService);
        mpaController = new MpaController(mpaService);
    }
}
