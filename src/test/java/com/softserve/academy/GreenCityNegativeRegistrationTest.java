package com.softserve.academy;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

class GreenCityNegativeRegistrationTest {
    private static WebDriver driver;

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Check if we are running in CI (GitHub Actions)
        if (System.getenv("GITHUB_ACTIONS") != null) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
        }

        driver = WebDriverManager.chromedriver().capabilities(options).create();
        driver.manage().window().maximize();
        // At this stage, we are not using complex waits, so we just maximize the window
    }

    @BeforeEach
    void openRegistrationForm() throws InterruptedException {
        // 1. Open the main page
        driver.navigate().to("https://www.greencity.cx.ua/#/greenCity");

        // Bad practice: using a delay to allow the page to load completely.
        // This is necessary because the site may load slowly.
        Thread.sleep(5000);

        // 2. Click the "Sign Up" button to open the modal window
        driver.findElement(By.cssSelector(".header_sign-up-btn > span")).click();

        // Bad practice: using a delay to allow the modal window to open.
        Thread.sleep(2000);
    }

    // --- TESTS ---

    @Test
    @DisplayName("Invalid email format (without @) → email error")
    void shouldShowErrorForInvalidEmail() throws InterruptedException {
        // One test = one reason for failure. Other fields must be valid.
        typeEmail("invalid-email");
        typeUsername("ValidUsername");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        // Give the system some time to validate and display the error
        Thread.sleep(1000);

        // Check that the error for email appeared
        assertEmailErrorVisible();
        // Check that the registration button is disabled (or registration did not occur)
        assertSignUpButtonDisabled();
    }

    @Test
    @DisplayName("All fields empty → required errors shown")
    void shouldShowErrorsForAllEmptyFields() throws InterruptedException {
        findAndClickWebElementById("email");
        findAndClickWebElementById("firstName");
        findAndClickWebElementById("password");
        findAndClickWebElementById("repeatPassword");

        clickSignUp();

        Thread.sleep(3000);

        assertEmailErrorVisible();
        assertUsernameErrorVisible();
        assertPasswordErrorVisible();
        assertConfirmPasswordErrorVisible();
    }

    @Test
    @DisplayName("Empty username → username required")
    void shouldShowErrorForEmptyUsername() throws InterruptedException {
        typeEmail("iruska.m21@gmail.com");
        findAndClickWebElementById("firstName");
        typePassword("12345678Aa!");
        typeConfirm("12345678Aa!");
        Thread.sleep(2000);

        assertUsernameErrorVisible();
    }

    @Test
    @DisplayName("Short password (<8) → password rule error")
    void shouldShowErrorForShortPassword() throws InterruptedException {
        // TODO:
        // Enter a password like "123" and check for the error
        typeEmail("iruska.m21@gmail.com");
        typeUsername("Nikolas K");
        typePassword("1234567");
        typeConfirm("1234567");
        clickSignUp();
        Thread.sleep(2000);

        assertPasswordErrorVisible();
    }

    @Test
    @DisplayName("Password with space → password rule error")
    void shouldShowErrorForPasswordWithSpace() throws InterruptedException {
        typeEmail("iruska.m21@gmail.com");
        typeUsername("Nikolas K");
        typePassword("1 2345678Aa!");
        typeConfirm("1 2345678Aa!");
        clickSignUp();
        Thread.sleep(2000);

        assertPasswordErrorVisible();
    }

    @Test
    @DisplayName("Confirm password mismatch → confirm error")
    void shouldShowErrorForPasswordMismatch() throws InterruptedException {
        typeEmail("iruska.m21@gmail.com");
        typeUsername("Nikolas K");
        typePassword("12345678Aa!");
        typeConfirm("2345678Aa!");
        clickSignUp();
        Thread.sleep(2000);

        assertConfirmPasswordErrorVisible();
    }

    // --- HELPERS (Helper methods) ---
    // This is the first step towards structuring code before learning Page Object

    private void typeEmail(String value) {
        WebElement field = driver.findElement(By.id("email"));
        field.clear();
        field.sendKeys(value);
    }

    private void typeUsername(String value) {
        WebElement field = driver.findElement(By.id("firstName"));
        field.clear();
        field.sendKeys(value);
    }

    private void typePassword(String value) {
        WebElement field = driver.findElement(By.id("password"));
        field.clear();
        field.sendKeys(value);
    }

    private void typeConfirm(String value) {
        WebElement field = driver.findElement(By.id("repeatPassword"));
        field.clear();
        field.sendKeys(value);
    }

    private void clickSignUp() {
        driver.findElement(By.cssSelector("button[type='submit'].greenStyle")).click();
    }

    private void assertEmailErrorVisible() {
        WebElement error = driver.findElement(By.id("email-err-msg"));
        assertTrue(error.isDisplayed(), "Email error message should be visible");
        // contains("required") or other text to avoid dependency on the full phrase
        assertTrue(
                error.getText().toLowerCase().contains("check") ||
                        error.getText().toLowerCase().contains("correctly") ||
                        error.getText().toLowerCase().contains("email"));
    }

    private void assertUsernameErrorVisible() {
        WebElement nameInputError = driver.findElement(By.cssSelector("app-error[controlname='firstName'] div"));

        assertTrue(nameInputError.isDisplayed(), "Email error message should be visible");
        assertTrue(nameInputError.getText().toLowerCase().contains("user name"),
                "The text error does not correspond to first name field");
    }

    private void assertPasswordErrorVisible() {
        WebElement passwordError = driver.findElement(By.cssSelector("p.password-not-valid"));

        assertTrue(passwordError.isDisplayed(), "Email error message should be visible");
        assertTrue(passwordError.getText().toLowerCase().contains("password"),
                "The text error does not correspond to password field");
    }

    private void assertConfirmPasswordErrorVisible() {
        WebElement passwordConfirmError = driver.findElement(By.cssSelector("div#confirm-err-msg div"));

        assertTrue(passwordConfirmError.isDisplayed(), "Email error message should be visible");
        assertTrue(passwordConfirmError.getText().toLowerCase().contains("required") ||
                        passwordConfirmError.getText().toLowerCase().contains("match"),
                "The text error does not correspond to  repeat password field");
    }

    private void assertSignUpButtonDisabled() {
        WebElement btn = driver.findElement(By.cssSelector("button[type='submit'].greenStyle"));
        assertFalse(btn.isEnabled(), "The 'Sign Up' button should be disabled with invalid data");
    }

    private void findAndClickWebElementById(String element) {
        WebElement webelement = driver.findElement(By.id(element));
        webelement.click();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}