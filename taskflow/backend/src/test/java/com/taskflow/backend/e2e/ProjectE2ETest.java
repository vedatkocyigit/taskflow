package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectE2ETest extends BaseE2ETest {

    @Test
    void shouldCreateProject() {

        openRegisterPage();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='register-email']")
        ));

        driver.findElement(By.cssSelector("[data-testid='register-email']"))
                .sendKeys("project+" + System.currentTimeMillis() + "@test.com");

        driver.findElement(By.cssSelector("[data-testid='register-password']"))
                .sendKeys("123456");

        driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='workspace-name-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='workspace-name-input']"))
                .sendKeys("WS");

        driver.findElement(By.cssSelector("[data-testid='workspace-create-btn']")).click();

        WebElement workspaceCard = wait.until(driver ->
                driver.findElements(By.cssSelector("[data-testid^='workspace-']")).stream()
                        .filter(el -> el.getAttribute("data-testid").matches("workspace-\\d+"))
                        .findFirst()
                        .orElse(null)
        );

        if (workspaceCard == null) {
            throw new RuntimeException("Workspace card not found");
        }

        String workspaceTestId = workspaceCard.getAttribute("data-testid");
        String workspaceId = workspaceTestId.replace("workspace-", "");

        driver.get(baseUrl + "/workspaces/" + workspaceId);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='project-name-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='project-name-input']"))
                .sendKeys("My Project");

        driver.findElement(By.cssSelector("[data-testid='project-create-btn']"))
                .click();

        assertTrue(driver.getPageSource().contains("My Project"));
    }
}
