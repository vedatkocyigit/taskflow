package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkspaceE2ETest extends BaseE2ETest {

    @Test
    void shouldCreateWorkspace() {

        openRegisterPage();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='register-email']")
        ));

        driver.findElement(By.cssSelector("[data-testid='register-email']"))
                .sendKeys("ws+" + System.currentTimeMillis() + "@test.com");

        driver.findElement(By.cssSelector("[data-testid='register-password']"))
                .sendKeys("123456");

        driver.findElement(By.cssSelector("[data-testid='register-submit']"))
                .click();

        wait.until(ExpectedConditions.urlToBe("http://localhost:3000/"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='workspace-name-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='workspace-name-input']"))
                .sendKeys("Selenium WS");

        driver.findElement(By.cssSelector("[data-testid='workspace-create-btn']"))
                .click();

        assertTrue(driver.getPageSource().contains("Selenium WS"));
    }

}
