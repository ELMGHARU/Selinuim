package org.example;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class MemoriaTests {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static ExtentReports extent;
    private static final String BASE_URL = "http://localhost:4200";
    private static String lastGeneratedEmail;
    private static String lastGeneratedPassword;

    public static void main(String[] args) {
        initializeReport();
        try {
            setupWebDriver();
            runAllTests();
        } catch (Exception e) {
            handleGlobalException(e);
        } finally {
            finalizeReport();
        }
    }

    private static void initializeReport() {
        String reportPath = "test-output/MemoriaTestReport_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html";
        extent = new ExtentReports(reportPath, true);
        extent.addSystemInfo("Test Environment", "Development");
        extent.addSystemInfo("Application", "Memoria");
    }

    private static void setupWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    private static String generateUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String generateEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    private static void runAllTests() {
        if (testHomePage()) {
            testRegistrationPage();
            testLoginPage();
            testDashboardPage();
        } else {
            ExtentTest errorTest = extent.startTest("Test Flow Error");
            errorTest.log(LogStatus.ERROR, "Home page test failed - stopping test execution");
            extent.endTest(errorTest);
        }
    }

    private static boolean testHomePage() {
        ExtentTest homeTest = extent.startTest("Test de la Page d'Accueil",
                "Vérification des éléments et fonctionnalités de la page d'accueil");
        try {
            driver.get(BASE_URL + "/home");
            homeTest.log(LogStatus.INFO, "Navigation vers la page d'accueil");
            wait.until(ExpectedConditions.urlContains("/home"));

            WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".nav-brand img")));
            WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.linkText("Login")));
            WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.linkText("Register")));
            WebElement startLearningBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".cta-button")));
            WebElement heroTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("hero-title")));
            WebElement heroDescription = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("hero-description")));
            WebElement getStartedBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".primary-button")));
            WebElement footer = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".footer")));

            assertTrue(logo.isDisplayed() &&
                    loginLink.isDisplayed() &&
                    registerLink.isDisplayed() &&
                    startLearningBtn.isDisplayed() &&
                    heroTitle.isDisplayed() &&
                    heroDescription.isDisplayed() &&
                    getStartedBtn.isDisplayed() &&
                    footer.isDisplayed());

            homeTest.log(LogStatus.PASS, "All page elements verified");
            captureScreenshot(homeTest, "HomePage_Elements");

            assertTrue(heroTitle.getText().contains("Master any subject"));
            homeTest.log(LogStatus.PASS, "Hero content verified");

            registerLink.click();
            wait.until(ExpectedConditions.urlContains("/register"));
            homeTest.log(LogStatus.PASS, "Navigation to Register page successful");

            extent.endTest(homeTest);
            return true;

        } catch (Exception e) {
            handleTestException(homeTest, "Test Page d'Accueil", e);
            extent.endTest(homeTest);
            return false;
        }
    }

    private static void testRegistrationPage() {
        ExtentTest registerTest = extent.startTest("Test d'Inscription");
        try {
            wait.until(ExpectedConditions.urlContains("/register"));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("form")));

            String username = generateUsername();
            lastGeneratedEmail = generateEmail();
            lastGeneratedPassword = "Password123456789@";

            WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[name='username']")));
            WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[name='email']")));
            WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[name='password']")));
            WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[type='submit']")));

            usernameInput.sendKeys(username);
            emailInput.sendKeys(lastGeneratedEmail);
            passwordInput.sendKeys(lastGeneratedPassword);

            registerTest.log(LogStatus.INFO, "Generated credentials - Username: " + username +
                    ", Email: " + lastGeneratedEmail);
            captureScreenshot(registerTest, "Register_Form");

            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", registerButton);

            registerTest.log(LogStatus.INFO, "Processing registration...");

            wait.until(ExpectedConditions.urlContains("/login"));
            registerTest.log(LogStatus.PASS, "Registration successful - redirected to login");

            Thread.sleep(1000);

        } catch (Exception e) {
            handleTestException(registerTest, "Test Inscription", e);
        } finally {
            extent.endTest(registerTest);
        }
    }
    private static void testLoginPage() {
        ExtentTest loginTest = extent.startTest("Test de Connexion");
        try {
            wait.until(ExpectedConditions.urlContains("/login"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));

            WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[name='email']")));
            WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[name='password']")));
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[type='submit']")));

            // Test de connexion avec champs valides
            emailInput.sendKeys(lastGeneratedEmail);
            passwordInput.sendKeys(lastGeneratedPassword);

            loginTest.log(LogStatus.INFO, "Attempting login with email: " + lastGeneratedEmail);
            captureScreenshot(loginTest, "Login_Form_Valid");

            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", loginButton);

            wait.until(ExpectedConditions.urlContains("/dashboard"));
            loginTest.log(LogStatus.PASS, "Login successful");

            // Test de connexion avec email invalide
            emailInput.clear();
            emailInput.sendKeys("invalid_email@example.com");
            passwordInput.clear();
            passwordInput.sendKeys(lastGeneratedPassword);

            executor.executeScript("arguments[0].click();", loginButton);

            WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".error-message")));
            assertTrue(errorMessage.isDisplayed());
            String expectedErrorMessage = "Adresse email ou mot de passe incorrect.";
            assertEquals(errorMessage.getText(), expectedErrorMessage);
            loginTest.log(LogStatus.PASS, "Login failed with invalid email, error message displayed correctly");
            captureScreenshot(loginTest, "Login_Form_InvalidEmail");

            // Test de connexion avec mot de passe invalide
            emailInput.clear();
            emailInput.sendKeys(lastGeneratedEmail);
            passwordInput.clear();
            passwordInput.sendKeys("invalidPassword");

            executor.executeScript("arguments[0].click();", loginButton);

            errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".error-message")));
            assertTrue(errorMessage.isDisplayed());
            assertEquals(errorMessage.getText(), expectedErrorMessage);
            loginTest.log(LogStatus.PASS, "Login failed with invalid password, error message displayed correctly");
            captureScreenshot(loginTest, "Login_Form_InvalidPassword");

        } catch (Exception e) {
            handleTestException(loginTest, "Test Connexion", e);
        } finally {
            extent.endTest(loginTest);
        }
    }



    private static void assertEquals(String actual, String expected) {
        if (!actual.equals(expected)) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String methodName = caller.getMethodName();
            int lineNumber = caller.getLineNumber();

            throw new AssertionError("Expected \"" + expected + "\", but got \"" + actual + "\" in method " + methodName +
                    " at line " + lineNumber);
        }
    }

    private static void testDashboardPage() {
        ExtentTest dashboardTest = extent.startTest("Test du Dashboard et Création de Cartes");
        try {
            wait.until(ExpectedConditions.urlContains("/dashboard"));

            WebElement sidebar = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("sidebar")));
            WebElement searchBar = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".search-bar input")));

            assertTrue(sidebar.isDisplayed() && searchBar.isDisplayed());
            dashboardTest.log(LogStatus.PASS, "Dashboard elements verified");
            captureScreenshot(dashboardTest, "Dashboard_Overview");

            dashboardTest.log(LogStatus.INFO, "Starting deck creation test");

            WebElement createNewDeckCard = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(@class, 'create-card')]//h3[contains(text(), 'Créer un nouveau paquet')]")));
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", createNewDeckCard);
            dashboardTest.log(LogStatus.INFO, "Clicked create new deck button");

            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.className("deck-modal")));
            assertTrue(modal.isDisplayed());
            dashboardTest.log(LogStatus.PASS, "Creation modal opened successfully");
            captureScreenshot(dashboardTest, "Create_Deck_Modal");

            String deckName = "Test Deck " + UUID.randomUUID().toString().substring(0, 8);

            WebElement deckNameInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.className("deck-name-input")));
            deckNameInput.sendKeys(deckName);
            dashboardTest.log(LogStatus.INFO, "Entered deck name: " + deckName);

            WebElement createDeckBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Créer un nouveau paquet')]")));
            executor.executeScript("arguments[0].click();", createDeckBtn);

            wait.until(ExpectedConditions.invisibilityOf(modal));
            dashboardTest.log(LogStatus.PASS, "Creation modal closed");

            Thread.sleep(2000); // Attente pour la création du paquet

            WebElement newDeck = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//h3[contains(@class, 'card-title') and contains(text(), '" + deckName + "')]")));
            assertTrue(newDeck.isDisplayed());
            dashboardTest.log(LogStatus.PASS, "New deck created and visible: " + deckName);
            captureScreenshot(dashboardTest, "New_Deck_Created");

            Thread.sleep(1000);
            WebElement addCardsButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(@class, 'button-secondary')]//i[contains(@class, 'fa-plus')]/parent::button")));
            executor.executeScript("arguments[0].click();", addCardsButton);
            dashboardTest.log(LogStatus.INFO, "Clicked Add Cards button");

            WebElement addCardTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//h2[text()='Ajouter une carte']")));
            assertTrue(addCardTitle.isDisplayed());
            dashboardTest.log(LogStatus.PASS, "Navigated to Add Card page");

            WebElement questionInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("question")));
            WebElement answerInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("answer")));
            WebElement difficultySelect = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("difficultyLevel")));

            String questionText = "Quest Test " + UUID.randomUUID().toString().substring(0, 8);
            String answerText = "Rép Test " + UUID.randomUUID().toString().substring(0, 8);

            questionInput.sendKeys(questionText);
            answerInput.sendKeys(answerText);
            difficultySelect.sendKeys("Facile");

            dashboardTest.log(LogStatus.INFO, "Filled flashcard form with question: " + questionText);
            captureScreenshot(dashboardTest, "Flashcard_Form_Filled");

            WebElement addCardButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[text()='Ajouter la carte']")));
            executor.executeScript("arguments[0].click();", addCardButton);
            dashboardTest.log(LogStatus.INFO, "Clicked add card button");

            // Attente plus longue pour la création de la carte
            Thread.sleep(2000);

            // Tentative de trouver la carte avec différents sélecteurs
            boolean isCardVisible = false;
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));

            try {
                By[] cardLocators = {
                        By.xpath("//div[contains(@class, 'flashcard')]//div[contains(text(), '" + questionText + "')]"),
                        By.xpath("//strong[contains(text(), '" + questionText + "')]"),
                        By.xpath("//*[contains(text(), '" + questionText + "')]")
                };

                for (By locator : cardLocators) {
                    try {
                        WebElement card = longWait.until(ExpectedConditions.presenceOfElementLocated(locator));
                        if (card.isDisplayed()) {
                            isCardVisible = true;
                            dashboardTest.log(LogStatus.INFO, "Card found with locator: " + locator);
                            break;
                        }
                    } catch (TimeoutException e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                dashboardTest.log(LogStatus.WARNING, "Error in additional card verification: " + e.getMessage());
            }

            // Capture d'écran pour le debugging
            captureScreenshot(dashboardTest, "After_Card_Creation");

            // Log de la source de la page
            try {
                String pageSource = driver.getPageSource();
                dashboardTest.log(LogStatus.INFO, "Page source after card creation: " +
                        pageSource.substring(0, Math.min(pageSource.length(), 500)));
            } catch (Exception e) {
                dashboardTest.log(LogStatus.WARNING, "Error capturing page source: " + e.getMessage());
            }

            assertTrue(isCardVisible, "La carte n'a pas été trouvée après sa création");
            dashboardTest.log(LogStatus.PASS, "Flashcard created successfully");

            WebElement backButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[text()='Retour aux decks']")));
            executor.executeScript("arguments[0].click();", backButton);
            dashboardTest.log(LogStatus.PASS, "Returned to dashboard");

            // Test de suppression du paquet
            WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//h3[contains(text(), '" + deckName + "')]/ancestor::div[contains(@class, 'deck-card')]//button[contains(@class, 'delete-btn')]")));

            dashboardTest.log(LogStatus.INFO, "Found delete button for deck: " + deckName);
            executor.executeScript("arguments[0].click();", deleteButton);
            dashboardTest.log(LogStatus.INFO, "Clicked delete button");

            try {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.xpath("//h3[contains(text(), '" + deckName + "')]")));
                dashboardTest.log(LogStatus.PASS, "Deck deleted successfully");
            } catch (Exception e) {
                dashboardTest.log(LogStatus.FAIL, "Deck deletion failed");
                throw e;
            }

            // Déconnexion
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(".logout-btn")));
            executor.executeScript("arguments[0].click();", logoutBtn);

            wait.until(ExpectedConditions.urlContains("/login"));
            dashboardTest.log(LogStatus.PASS, "Logout successful");

        } catch (Exception e) {
            handleTestException(dashboardTest, "Test Dashboard", e);
        } finally {
            extent.endTest(dashboardTest);
        }
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String methodName = caller.getMethodName();
            int lineNumber = caller.getLineNumber();

            throw new AssertionError("Assertion failed in method " + methodName +
                    " at line " + lineNumber);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
            String methodName = caller.getMethodName();
            int lineNumber = caller.getLineNumber();

            throw new AssertionError(message + " (in method " + methodName +
                    " at line " + lineNumber + ")");
        }
    }

    private static void captureScreenshot(ExtentTest test, String screenshotName) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path screenshotDir = Paths.get("test-output/screenshots");
            Files.createDirectories(screenshotDir);

            String uniqueFileName = screenshotName + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";
            Path destinationPath = screenshotDir.resolve(uniqueFileName);

            Files.copy(screenshot.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            test.log(LogStatus.INFO, "Screenshot: " + test.addScreenCapture("screenshots/" + uniqueFileName));
        } catch (IOException e) {
            test.log(LogStatus.WARNING, "Screenshot capture error: " + e.getMessage());
        }
    }

    private static void handleTestException(ExtentTest test, String testName, Exception e) {
        test.log(LogStatus.FAIL, "Error in " + testName + ": " + e.getMessage());
        try {
            captureScreenshot(test, "Error_" + testName.replace(" ", "_"));
        } catch (Exception screenshotError) {
            test.log(LogStatus.WARNING, "Could not capture error screenshot");
        }
    }

    private static void handleGlobalException(Exception e) {
        ExtentTest errorTest = extent.startTest("Global Error");
        errorTest.log(LogStatus.ERROR, "System error: " + e.getMessage());
        extent.endTest(errorTest);
    }

    private static void finalizeReport() {
        if (driver != null) {
            driver.quit();
        }
        if (extent != null) {
            extent.flush();
            extent.close();
        }
    }
}