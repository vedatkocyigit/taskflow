package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogoutE2ETest extends BaseE2ETest {

    @Test
    void shouldLogoutAndRedirectToLogin() {


        openRegisterPage();

        driver.findElement(By.cssSelector("[data-testid='register-email']"))
                .sendKeys("logout+" + System.currentTimeMillis() + "@test.com");

        driver.findElement(By.cssSelector("[data-testid='register-password']"))
                .sendKeys("123456");

        driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".logout-btn")
        ));

        assertTrue(
                driver.findElement(By.cssSelector(".logout-btn")).isDisplayed()
        );


        driver.findElement(By.cssSelector(".logout-btn")).click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='login-email']")
        ));

        assertTrue(
                driver.findElement(By.cssSelector("[data-testid='login-email']")).isDisplayed()
        );


        driver.get(baseUrl + "/workspaces");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='login-email']")
        ));

        assertTrue(
                driver.findElement(By.cssSelector("[data-testid='login-email']")).isDisplayed()
        );
    }
}
