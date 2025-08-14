package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.base.BaseNamedParameterRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Репозиторий для работы с рейтингами MPA в БД.
 * Позволяет получить все рейтинги или конкретный по ID.
 */
@Repository
public class JdbcMpaRepository extends BaseNamedParameterRepository<MpaRating> implements MpaRepository {

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_ratings";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE mpa_id = :mpaId";

    public JdbcMpaRepository(NamedParameterJdbcOperations jdbc, RowMapper<MpaRating> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<MpaRating> findMpaById(Long mpaId) {
        Map<String, Object> params = new HashMap<>();
        params.put("mpaId", mpaId);
        return findOne(FIND_BY_ID_QUERY, params);
    }

    @Override
    public List<MpaRating> findAllMpa() {
        return findMany(FIND_ALL_QUERY, new HashMap<>());
    }
}
