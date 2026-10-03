package com.example.webservice.data;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class IotDataRequestTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void requiresAnIotId() {
        IotDataRequest request = new IotDataRequest(null, null);

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void rejectsJsondataLongerThanMysqlColumn() {
        IotDataRequest request = new IotDataRequest(1, "x".repeat(121));

        assertThat(validator.validate(request)).hasSize(1);
    }

    @Test
    void acceptsValidData() {
        IotDataRequest request = new IotDataRequest(1, "{\"temperature\":23}");

        assertThat(validator.validate(request)).isEmpty();
    }
}