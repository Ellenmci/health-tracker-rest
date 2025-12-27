package ie.setu.selenium

import ie.setu.config.JavalinConfig
import ie.setu.helpers.TestDatabaseConfig
import ie.setu.helpers.populateActivityTable
import ie.setu.helpers.populateUserTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.time.Duration
import kotlin.test.fail

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddUserTest {

    private lateinit var driver: WebDriver
    private lateinit var app: io.javalin.Javalin
    private val verificationErrors = StringBuffer()
    private var acceptNextAlert = true

    // ---------------------------------------------------------
    // RUN ONCE — CONNECT TO IN-MEMORY H2
    // ---------------------------------------------------------
    @BeforeAll
    fun setupDatabase() {
        TestDatabaseConfig.connect()
    }

    // ---------------------------------------------------------
    // RUN BEFORE EACH TEST — RESET DB + START JETTY + START CHROME
    // ---------------------------------------------------------
    @BeforeEach
    fun setUp() {
        // Reset DB and seed data
        TestDatabaseConfig.reset()
        transaction {
            populateUserTable()
            populateActivityTable()
        }

        // Start Javalin on a random free port
        app = JavalinConfig().getJavalinService().start(0)

        // Configure headless Chrome for CI
        val options = ChromeOptions().apply {
            addArguments("--headless=new")
            addArguments("--no-sandbox")
            addArguments("--disable-dev-shm-usage")
        }

        driver = ChromeDriver(options)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(60))
    }

    // ---------------------------------------------------------
    // THE TEST
    // ---------------------------------------------------------
    @Test
    fun testAddUser() {
        val baseUrl = "http://localhost:${app.port()}"

        driver.get(baseUrl)
        driver.findElement(By.linkText("More Details...")).click()

        driver.findElement(
            By.xpath("//main[@id='main-vue']/div/div/div/div/div/div/div/div[2]/button")
        ).click()

        driver.findElement(By.name("name")).apply {
            click()
            clear()
            sendKeys("Lisa Simpson")
        }

        driver.findElement(By.name("email")).apply {
            click()
            clear()
            sendKeys("lisa@simpson.com")
        }

        driver.findElement(
            By.xpath("//main[@id='main-vue']/div/div/div/div/div/div[2]/button")
        ).click()

        driver.findElement(By.linkText("Lisa Simpson (lisa@simpson.com)")).click()
    }

    // ---------------------------------------------------------
    // CLEANUP AFTER EACH TEST
    // ---------------------------------------------------------
    @AfterEach
    fun tearDown() {
        driver.quit()
        app.stop()

        val errors = verificationErrors.toString()
        if (errors.isNotEmpty()) {
            fail(errors)
        }
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------
    private fun isElementPresent(by: By): Boolean =
        try {
            driver.findElement(by)
            true
        } catch (e: NoSuchElementException) {
            false
        }

    private fun isAlertPresent(): Boolean =
        try {
            driver.switchTo().alert()
            true
        } catch (e: NoAlertPresentException) {
            false
        }

    private fun closeAlertAndGetItsText(): String {
        return try {
            val alert = driver.switchTo().alert()
            val text = alert.text
            if (acceptNextAlert) alert.accept() else alert.dismiss()
            text
        } finally {
            acceptNextAlert = true
        }
    }
}
