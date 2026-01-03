package com.taskflow.backend.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskAuthorizationE2ETest extends BaseE2ETest {

    @Test
    void taskPermissionMatrix_shouldWorkCorrectly() {

        System.out.println("=== TASK MULTI MEMBER AUTH TEST START ===");

        String owner   = "owner_"   + System.currentTimeMillis() + "@test.com";
        String member2 = "member2_" + System.currentTimeMillis() + "@test.com";
        String member3 = "member3_" + System.currentTimeMillis() + "@test.com";


        registerAndLogout(owner);
        registerAndLogout(member2);
        registerAndLogout(member3);


        login(owner);
        System.out.println("OWNER LOGGED IN");

        driver.findElement(By.cssSelector("[data-testid='workspace-name-input']"))
                .sendKeys("TASK-AUTH-WS");
        driver.findElement(By.cssSelector("[data-testid='workspace-create-btn']")).click();

        String workspaceId = waitAndGetWorkspaceId();
        System.out.println("WORKSPACE ID = " + workspaceId);

        driver.get(baseUrl + "/workspaces/" + workspaceId);


        addMember(member2);

        driver.findElement(By.cssSelector("[data-testid='project-name-input']"))
                .sendKeys("PRJ");
        driver.findElement(By.cssSelector("[data-testid='project-create-btn']")).click();

        waitForProjectListRender();
        selectProjectCard();


        createTaskIfPossible("OWNER TASK");
        logout();


        login(member2);
        driver.get(baseUrl + "/workspaces/" + workspaceId);

        waitForProjectListRender();
        selectProjectCard();

        boolean member2CanCreate = canSeeTaskInput();
        System.out.println("MEMBER2 CAN CREATE TASK = " + member2CanCreate);

        assertTrue(member2CanCreate, "MEMBER2 should be able to create task");

        if (member2CanCreate) {
            createTaskIfPossible("MEMBER2 TASK");
        }

        logout();


        login(member3);
        driver.get(baseUrl + "/workspaces/" + workspaceId);

        boolean projectVisible =
                driver.findElements(By.cssSelector("[data-testid^='project-']")).stream()
                        .anyMatch(el ->
                                el.getAttribute("data-testid").matches("project-\\d+"));

        boolean taskInputVisible = canSeeTaskInput();

        System.out.println("MEMBER3 PROJECT VISIBLE = " + projectVisible);
        System.out.println("MEMBER3 TASK INPUT VISIBLE = " + taskInputVisible);

        assertFalse(projectVisible, "MEMBER3 should NOT see project cards");
        assertFalse(taskInputVisible, "MEMBER3 should NOT be able to create task");

        System.out.println("=== TASK MULTI MEMBER AUTH TEST PASSED ===");
    }

    private void registerAndLogout(String email) {
        openRegisterPage();

        driver.findElement(By.cssSelector("[data-testid='register-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='register-password']")).sendKeys("123456");
        driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("[data-testid='register-page']")
        ));

        logout();
    }

    private void login(String email) {
        openLoginPage();

        driver.findElement(By.cssSelector("[data-testid='login-email']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='login-password']")).sendKeys("123456");
        driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='workspace-name-input']")
        ));
    }

    private void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".logout-btn")
        )).click();

        wait.until(ExpectedConditions.urlContains("/login"));
    }

    private void addMember(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='member-email-input']")
        ));

        driver.findElement(By.cssSelector("[data-testid='member-email-input']")).sendKeys(email);
        driver.findElement(By.cssSelector("[data-testid='member-add-btn']")).click();

        wait.until(d -> d.getPageSource().contains(email));
    }

    private boolean canSeeTaskInput() {
        return driver.findElements(By.cssSelector("input[placeholder='New task']")).size() > 0;
    }

    private void createTaskIfPossible(String name) {
        if (!canSeeTaskInput()) return;

        driver.findElement(By.cssSelector("input[placeholder='New task']")).sendKeys(name);
        driver.findElement(By.cssSelector(".send-btn")).click();

        wait.until(d -> d.getPageSource().contains(name));
    }

    private void waitForProjectListRender() {
        wait.until(ExpectedConditions.textToBe(
                By.cssSelector("[data-testid='project-create-btn']"),
                "Create"
        ));

        wait.until(driver ->
                driver.findElements(By.cssSelector("[data-testid^='project-']")).stream()
                        .anyMatch(el ->
                                el.getAttribute("data-testid").matches("project-\\d+"))
        );
    }

    private void selectProjectCard() {
        WebElement project = wait.until(driver ->
                driver.findElements(By.cssSelector("[data-testid^='project-']")).stream()
                        .filter(el -> el.getAttribute("data-testid").matches("project-\\d+"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Project card not found"))
        );

        project.click();
    }

    private String waitAndGetWorkspaceId() {
        wait.until(ExpectedConditions.textToBe(
                By.cssSelector("[data-testid='workspace-create-btn']"),
                "Create"
        ));

        WebElement ws = wait.until(driver ->
                driver.findElements(By.cssSelector("[data-testid^='workspace-']")).stream()
                        .filter(el -> el.getAttribute("data-testid").matches("workspace-\\d+"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Workspace not found"))
        );

        return ws.getAttribute("data-testid").replace("workspace-", "");
    }
}
