package ru.yandex.practicum.filmorate;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class ControllerInitListener implements TestExecutionListener {
    @Override
    public void beforeTestClass(TestContext testContext) {
        NamedParameterJdbcOperations jdbcTemplate = testContext.getApplicationContext()
                .getBean(NamedParameterJdbcOperations.class);

        BaseIntegrationTest.initControllers(jdbcTemplate);// передаём jdbcTemplate в статический метод
    }
}
