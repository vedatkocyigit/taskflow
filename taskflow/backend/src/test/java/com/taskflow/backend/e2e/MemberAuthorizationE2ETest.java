package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemberAuthorizationE2ETest extends BaseE2ETest {

    @Test
    void memberShouldNotAddAnotherMember() {

        System.out.println("=== MEMBER AUTH TEST START ===");

        String owner = "owner_" + System.currentTimeMillis() + "@test.com";
        String member2 = "member2_" + System.currentTimeMillis() + "@test.com";
        String member3 = "member3_" + System.currentTimeMillis() + "@test.com";


        openRegisterPage();
        register(owner);
        logout();

        openRegisterPage();
        register(member2);
        logout();

        openRegisterPage();
        register(member3);
        logout();


        openLoginPage();
        login(owner);
        System.out.println("OWNER LOGGED IN");


        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='workspace-name-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='workspace-name-input']"))
                .sendKeys("AUTH-WS");

        driver.findElement(By.cssSelector("[data-testid='workspace-create-btn']")).click();
        System.out.println("WORKSPACE CREATE CLICKED");

        WebElement workspaceCard = wait.until(driver -> {
            List<WebElement> cards =
                    driver.findElements(By.cssSelector("[data-testid^='workspace-']"));

            System.out.println("---- WORKSPACE CARD DEBUG ----");
            for (WebElement c : cards) {
                System.out.println(
                        "CARD testid = " + c.getAttribute("data-testid") +
                                " | text = [" + c.getText() + "]"
                );
            }
            System.out.println("-----------------------------");

            return cards.stream()
                    .filter(el -> el.getAttribute("data-testid").matches("workspace-\\d+"))
                    .findFirst()
                    .orElse(null);
        });

        if (workspaceCard == null) {
            throw new RuntimeException("Workspace card not found after wait");
        }

        String workspaceId =
                workspaceCard.getAttribute("data-testid").replace("workspace-", "");

        System.out.println("WORKSPACE ID = " + workspaceId);

        driver.get(baseUrl + "/workspaces/" + workspaceId);


        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='member-email-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='member-email-input']"))
                .sendKeys(member2);

        driver.findElement(By.cssSelector("[data-testid='member-add-btn']")).click();

        wait.until(d -> d.getPageSource().contains(member2));
        assertTrue(driver.getPageSource().contains(member2));

        logout();


        openLoginPage();
        login(member2);
        System.out.println("MEMBER2 LOGGED IN");

        driver.get(baseUrl + "/workspaces/" + workspaceId);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".members-panel")
        ));


        driver.findElement(By.cssSelector("[data-testid='member-email-input']"))
                .sendKeys(member3);

        driver.findElement(By.cssSelector("[data-testid='member-add-btn']")).click();

        sleep(1000);

        boolean member3Exists =
                driver.findElements(By.cssSelector("[data-testid^='member-row-']")).stream()
                        .anyMatch(el -> el.getText().contains(member3));

        System.out.println("MEMBER3 EXISTS = " + member3Exists);

        assertFalse(
                member3Exists,
                "MEMBER role user should NOT be able to add another member"
        );
    }


    private void register(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='register-email']")
        ));
        driver.findElement(By.cssSelector("[data-testid='register-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='register-password']")).sendKeys("123456");
        driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();
        sleep(800);
    }

    private void login(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='login-email']")
        ));
        driver.findElement(By.cssSelector("[data-testid='login-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='login-password']")).sendKeys("123456");
        driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();
        sleep(800);
    }

    private void logout() {
        driver.findElement(By.cssSelector(".logout-btn")).click();
        sleep(800);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
