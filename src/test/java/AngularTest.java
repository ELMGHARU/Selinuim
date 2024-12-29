import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AngularTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "http://localhost:4200";

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    public void testHomePage() {
        driver.get(BASE_URL + "/home");
        waitForAngular();

        // Test des liens de navigation
        assertTrue(driver.findElement(By.linkText("Se connecter")).isDisplayed());
        assertTrue(driver.findElement(By.linkText("S'inscrire")).isDisplayed());
    }

    @Test
    public void testLoginPage() {
        driver.get(BASE_URL + "/login");
        waitForAngular();

        // Trouver les éléments du formulaire de login
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='email']")));
        WebElement passwordInput = driver.findElement(
                By.cssSelector("input[name='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        // Test du login
        emailInput.sendKeys("test@example.com");
        passwordInput.sendKeys("password123");
        loginButton.click();

        // Attendre la redirection
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    @Test
    public void testPasswordReset() {
        driver.get(BASE_URL + "/login");
        waitForAngular();

        // Cliquer sur "Mot de passe oublié?"
        WebElement forgotPasswordLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".forgot-password a")));
        forgotPasswordLink.click();

        // Vérifier que le mode réinitialisation est actif
        WebElement oldPasswordInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='oldPassword']")));
        WebElement newPasswordInput = driver.findElement(
                By.cssSelector("input[name='newPassword']"));

        assertTrue(oldPasswordInput.isDisplayed());
        assertTrue(newPasswordInput.isDisplayed());
    }

    @Test
    public void testRegistration() {
        driver.get(BASE_URL + "/register");
        waitForAngular();

        // Trouver les éléments du formulaire d'inscription
        WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='username']")));
        WebElement emailInput = driver.findElement(
                By.cssSelector("input[name='email']"));
        WebElement passwordInput = driver.findElement(
                By.cssSelector("input[name='password']"));
        WebElement registerButton = driver.findElement(By.cssSelector("button[type='submit']"));

        // Remplir le formulaire
        usernameInput.sendKeys("John Doe");
        emailInput.sendKeys("john.doe" + System.currentTimeMillis() + "@example.com");
        passwordInput.sendKeys("password123");
        registerButton.click();

        // Vérifier la redirection vers login
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    @Test
    public void testErrorMessages() {
        driver.get(BASE_URL + "/login");
        waitForAngular();

        // Test avec champs vides
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")));
        loginButton.click();

        // Vérifier le message d'erreur
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".alert.alert-danger")));
        assertTrue(errorMessage.isDisplayed());
    }

    @Test
    public void testNavigationAfterLogin() {
        // D'abord se connecter
        testLoginPage();

        // Tester les pages protégées
        String[] protectedPages = {
                "/dashboard",
                "/decks",
                "/library",
                "/setting",
                "/practice",
                "/deck-list",
                "/statistics"
        };

        for (String page : protectedPages) {
            driver.get(BASE_URL + page);
            waitForAngular();
            assertTrue(driver.getCurrentUrl().contains(page),
                    "Navigation vers " + page + " échouée");
        }
    }

    private void waitForAngular() {
        try {
            Thread.sleep(1000); // Petit délai pour s'assurer que Angular est stable
            wait.until(driver -> {
                try {
                    return (Boolean) ((JavascriptExecutor) driver)
                            .executeScript("return window.getAllAngularTestabilities()" +
                                    ".findIndex(x => !x.isStable()) === -1");
                } catch (Exception e) {
                    return true;
                }
            });
        } catch (Exception e) {
            System.out.println("Erreur lors de l'attente d'Angular: " + e.getMessage());
        }
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}