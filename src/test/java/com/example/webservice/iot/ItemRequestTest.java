package com.example.webservice.iot;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ItemRequestTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validator.unwrap(jakarta.validation.Validator.class);
    }

    @Test
    void rejectsBlankName() {
        ItemRequest request = new ItemRequest(" ", "description", "AA:BB:CC:DD:EE:FF", null);

        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void acceptsValidItem() {
        ItemRequest request = new ItemRequest("Sensor", "Temperature sensor", "AA:BB:CC:DD:EE:FF", "{\"min\":0}");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsFieldsLongerThanMysqlColumns() {
        ItemRequest request = new ItemRequest("n".repeat(46), null, "m".repeat(46), null);

        assertThat(validator.validate(request)).hasSize(2);
    }
}