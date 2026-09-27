package com.saucedemo.base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.saucedemo.config.TestConfig;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.nio.file.Paths;

// One browser per test class, a fresh context (clean cookies/storage) per test method.
public abstract class BaseTest {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    protected Page page;

    // "headless" comes from testng.xml; -Dheadless=... on the command line overrides it.
    @BeforeClass
    @Parameters("headless")
    public void launchBrowser(@Optional("true") String headless) {
        boolean runHeadless = Boolean.parseBoolean(System.getProperty("headless", headless));
        playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute("data-test");
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(runHeadless));
    }

    @BeforeMethod
    public void openPage() {
        context = browser.newContext(new Browser.NewContextOptions().setBaseURL(TestConfig.BASE_URL));
        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        page = context.newPage();
    }

    @AfterMethod(alwaysRun = true)
    public void closePage(ITestResult result) {
        if (context == null) {
            return;
        }
        if (result.isSuccess()) {
            context.tracing().stop();
        } else {
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get("target", "traces", result.getName() + ".zip")));
        }
        context.close();
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }
}
