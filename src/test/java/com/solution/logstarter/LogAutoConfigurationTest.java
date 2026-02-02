package com.solution.logstarter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LogAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LogAutoConfiguration.class));

    @Test
    void shouldRegisterLogPropertiesBean() {
        contextRunner
                .withPropertyValues("smart-logs.api-key=secret")
                .run(context -> {
                    assertThat(context).hasSingleBean(LogProperties.class);
                    assertThat(context.getBean(LogProperties.class).getApiKey()).isEqualTo("secret");
                });
    }
}
