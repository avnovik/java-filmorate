package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class User {

    private Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен содержать символ @")
    private String email;

    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы")
    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    private String name;

//    @NotNull
//    @PastOrPresent(message = "Дата рождения не может быть в будущем") //не работает, уточнить
    private LocalDate birthday;
}
