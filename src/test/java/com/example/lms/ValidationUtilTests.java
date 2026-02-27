package com.example.lms;

import com.example.lms.common.validation.ValidationUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTests {

    @Test
    void role_validation_works() {
        assertEquals("STUDENT", ValidationUtil.role("student"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.role("guest"));
    }

    @Test
    void score_validation_works() {
        assertEquals(100, ValidationUtil.score(100));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.score(101));
    }
}
