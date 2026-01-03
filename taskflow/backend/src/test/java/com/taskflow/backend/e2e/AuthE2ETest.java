package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthE2ETest extends BaseE2ETest {

    @Test
    void register_shouldLoginAutomatically() {

        openRegisterPage();

        driver.findElement(By.cssSelector("[data-testid='register-email']"))
                .sendKeys("selenium+" + System.currentTimeMillis() + "@test.com");

        driver.findElement(By.cssSelector("[data-testid='register-password']"))
                .sendKeys("123456");

        driver.findElement(By.cssSelector("[data-testid='register-submit']"))
                .click();

        assertTrue(driver.getCurrentUrl().contains("/"));
    }
}
