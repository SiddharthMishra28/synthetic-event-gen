package com.syntheticdata.expression;

import org.junit.Test;
import static org.junit.Assert.*;

public class FakerReflectionInvokerTest {

    @Test
    public void testInvoke() {
        FakerReflectionInvoker invoker = new FakerReflectionInvoker();

        // Test a simple Faker expression
        String firstName = invoker.invoke("faker.name().firstName()");
        assertNotNull(firstName);
        assertFalse(firstName.startsWith("[FAKER_ERR"));

        // Test another Faker expression
        String email = invoker.invoke("faker.internet().emailAddress()");
        assertNotNull(email);
        assertTrue(email.contains("@"));
        assertFalse(email.startsWith("[FAKER_ERR"));
    }
}
