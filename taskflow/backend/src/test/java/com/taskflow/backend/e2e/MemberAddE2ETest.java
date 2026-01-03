package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemberAddE2ETest extends BaseE2ETest {

    @Test
    void shouldAddMemberToWorkspace() {

        openRegisterPage();

        String ownerEmail = "owner_" + System.currentTimeMillis() + "@test.com";

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='register-email']")
        ));

        driver.findElement(By.cssSelector("[data-testid='register-email']"))
                .sendKeys(ownerEmail);

        driver.findElement(By.cssSelector("[data-testid='register-password']"))
                .sendKeys("123456");

        driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='workspace-name-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='workspace-name-input']"))
                .sendKeys("Workspace");

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
                By.cssSelector("[data-testid='member-email-input']")
        ));

        String memberEmail = "member_" + System.currentTimeMillis() + "@test.com";

        driver.findElement(By.cssSelector("[data-testid='member-email-input']"))
                .sendKeys(memberEmail);

        driver.findElement(By.cssSelector("[data-testid='member-add-btn']")).click();

        wait.until(driver -> driver.getPageSource().contains(memberEmail));

        assertTrue(driver.getPageSource().contains(memberEmail));
    }
}
