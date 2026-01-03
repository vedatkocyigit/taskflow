package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommentE2ETest extends BaseE2ETest {

    @Test
    void shouldAddComment() {

        openRegisterPage();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='register-email']")
        ));

        driver.findElement(By.cssSelector("[data-testid='register-email']"))
                .sendKeys("comment+" + System.currentTimeMillis() + "@test.com");

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
            throw new RuntimeException("Workspace not found");
        }

        String workspaceId = workspaceCard.getAttribute("data-testid")
                .replace("workspace-", "");

        driver.get(baseUrl + "/workspaces/" + workspaceId);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='project-name-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='project-name-input']"))
                .sendKeys("PRJ");

        driver.findElement(By.cssSelector("[data-testid='project-create-btn']")).click();

        WebElement projectItem = wait.until(driver ->
                driver.findElements(By.cssSelector("[data-testid^='project-']")).stream()
                        .filter(el -> el.getAttribute("data-testid").matches("project-\\d+"))
                        .findFirst()
                        .orElse(null)
        );

        if (projectItem == null) {
            throw new RuntimeException("Project not found");
        }

        projectItem.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='New task']")
        ));

        driver.findElement(By.cssSelector("input[placeholder='New task']"))
                .sendKeys("My Task");

        driver.findElement(By.cssSelector(".send-btn")).click();

        WebElement taskCard = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".task-card")
        ));
        taskCard.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".comment-input")
        ));

        driver.findElement(By.cssSelector(".comment-input"))
                .sendKeys("Hello Selenium\n");

        assertTrue(driver.getPageSource().contains("Hello Selenium"));
    }
}
