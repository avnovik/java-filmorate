package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.annotation.MinReleaseDate;

import java.time.LocalDate;

/**
 * Валидатор для проверки даты релиза фильма.
 * Гарантирует, что дата не раньше 28 декабря 1895 года (день рождения кино).
 */
public class MinReleaseDateValidator implements ConstraintValidator<MinReleaseDate, LocalDate> {

    public static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        return date == null || !date.isBefore(CINEMA_BIRTHDAY);
    }
}
