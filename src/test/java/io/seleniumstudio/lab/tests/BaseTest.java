package io.seleniumstudio.lab.tests;

import io.seleniumstudio.lab.fixture.TestSite;
import io.seleniumstudio.lab.pages.HomePage;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;

/**
 * Selenium Manager resolves the driver binary, so no WebDriverManager dependency is required on
 * Selenium 4.6+.
 */
public abstract class BaseTest {

    protected TestSite site;
    protected WebDriver driver;
    protected HomePage home;

    @BeforeClass(alwaysRun = true)
    public void startSite() throws IOException {
        site = TestSite.start();
    }

    @AfterClass(alwaysRun = true)
    public void stopSite() {
        if (site != null) site.stop();
    }

    @BeforeMethod(alwaysRun = true)
    public void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,900",
                "--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        driver = new ChromeDriver(options);
        driver.get(site.url("index.html"));
        home = new HomePage(driver).awaitLoaded();
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        if (driver != null) driver.quit();
    }
}
