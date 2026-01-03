package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemberRemoveE2ETest extends BaseE2ETest {

    @Test
    void shouldRemoveMemberFromWorkspace() {

        String ownerEmail = "owner_" + System.currentTimeMillis() + "@test.com";
        String memberEmail = "member_" + System.currentTimeMillis() + "@test.com";


        openRegisterPage();
        register(memberEmail);
        logout();


        openRegisterPage();
        register(ownerEmail);


        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='workspace-name-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='workspace-name-input']"))
                .sendKeys("WS");

        driver.findElement(By.cssSelector("[data-testid='workspace-create-btn']"))
                .click();


        WebElement workspaceCard = wait.withTimeout(Duration.ofSeconds(15)).until(driver -> {
            return driver.findElements(By.cssSelector("[data-testid^='workspace-']")).stream()
                    .filter(el -> el.getAttribute("data-testid").matches("workspace-\\d+"))
                    .findFirst()
                    .orElse(null);
        });

        if (workspaceCard == null)
            throw new RuntimeException("Workspace card not found");

        String workspaceId =
                workspaceCard.getAttribute("data-testid").replace("workspace-", "");

        driver.get(baseUrl + "/workspaces/" + workspaceId);


        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='member-email-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='member-email-input']"))
                .sendKeys(memberEmail);

        driver.findElement(By.cssSelector("[data-testid='member-add-btn']")).click();


        WebElement memberRow = wait.withTimeout(Duration.ofSeconds(15)).until(driver -> {
            List<WebElement> rows = driver.findElements(By.cssSelector(".member-row"));
            return rows.stream()
                    .filter(r -> r.getText().contains(memberEmail))
                    .findFirst()
                    .orElse(null);
        });

        if (memberRow == null)
            throw new RuntimeException("Member row not found after add");

        assertTrue(memberRow.getText().contains(memberEmail));


        WebElement removeBtn =
                memberRow.findElement(By.cssSelector(".icon-btn.danger"));

        removeBtn.click();

        driver.switchTo().alert().accept();


        wait.withTimeout(Duration.ofSeconds(15)).until(driver ->
                driver.findElements(By.cssSelector(".member-row")).stream()
                        .noneMatch(r -> r.getText().contains(memberEmail))
        );

        assertFalse(
                driver.findElements(By.cssSelector(".member-row")).stream()
                        .anyMatch(r -> r.getText().contains(memberEmail))
        );
    }


    private void register(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='register-email']")
        ));

        driver.findElement(By.cssSelector("[data-testid='register-email']"))
                .sendKeys(email);

        driver.findElement(By.cssSelector("[data-testid='register-password']"))
                .sendKeys("123456");

        driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();

        wait.until(ExpectedConditions.urlContains("/"));
    }

    private void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".logout-btn")
        ));
        driver.findElement(By.cssSelector(".logout-btn")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
    }
}
