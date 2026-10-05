package com.example.webservice.user;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserRequestTest {
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
    void acceptsValidUserAndOptionalFields() {
        UserRequest request = new UserRequest("Ada", "Lovelace", "ada", "secret", null, null, null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsBlankRequiredFieldsAndValuesTooLongForColumns() {
        UserRequest request = new UserRequest(" ", "Lovelace", "ada", "secret", null, null, "x".repeat(46));

        assertThat(validator.validate(request)).hasSize(2);
    }

    @Test
    void rejectsMalformedEmail() {
        UserRequest request = new UserRequest("Ada", "Lovelace", "ada", "secret", "not-an-email", null, null);

        assertThat(validator.validate(request)).isNotEmpty();
    }
}