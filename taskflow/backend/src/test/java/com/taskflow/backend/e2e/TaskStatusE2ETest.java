package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskStatusE2ETest extends BaseE2ETest {

    @Test
    void shouldChangeTaskStatusToDone() {


        openRegisterPage();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='register-email']")
        ));

        driver.findElement(By.cssSelector("[data-testid='register-email']"))
                .sendKeys("status+" + System.currentTimeMillis() + "@test.com");

        driver.findElement(By.cssSelector("[data-testid='register-password']"))
                .sendKeys("123456");

        driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='workspace-name-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='workspace-name-input']"))
                .sendKeys("WS");

        driver.findElement(By.cssSelector("[data-testid='workspace-create-btn']")).click();


        WebElement workspaceCard = wait.withTimeout(Duration.ofSeconds(15)).until(driver -> {
            List<WebElement> cards =
                    driver.findElements(By.cssSelector("[data-testid^='workspace-']"));

            return cards.stream()
                    .filter(el -> el.getAttribute("data-testid").matches("workspace-\\d+"))
                    .findFirst()
                    .orElse(null);
        });

        if (workspaceCard == null) {
            throw new RuntimeException("Workspace card not found");
        }

        String workspaceId =
                workspaceCard.getAttribute("data-testid").replace("workspace-", "");

        driver.get(baseUrl + "/workspaces/" + workspaceId);


        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='project-name-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='project-name-input']"))
                .sendKeys("PRJ");

        driver.findElement(By.cssSelector("[data-testid='project-create-btn']")).click();


        WebElement projectItem = wait.withTimeout(Duration.ofSeconds(15)).until(driver -> {
            List<WebElement> projects =
                    driver.findElements(By.cssSelector("[data-testid^='project-']"));

            return projects.stream()
                    .filter(el -> el.getAttribute("data-testid").matches("project-\\d+"))
                    .findFirst()
                    .orElse(null);
        });

        if (projectItem == null) {
            throw new RuntimeException(
                    "Project not found. Page source:\n" + driver.getPageSource()
            );
        }

        projectItem.click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='New task']")
        ));

        driver.findElement(By.cssSelector("input[placeholder='New task']"))
                .sendKeys("Status Task");

        driver.findElement(By.cssSelector(".send-btn")).click();

        WebElement taskCard = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".task-card"))
        );
        taskCard.click();


        WebElement doneBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//span[normalize-space()='DONE']")
                )
        );
        doneBtn.click();


        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".status-pill.active"),
                "DONE"
        ));

        assertTrue(driver.getPageSource().contains("DONE"));
    }
}
