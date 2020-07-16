package br.ce.wcaquino.tasksfrontend;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TaskSeleniumTest {

    private final String url = "http://jenkins";

    private String taskBaseUrl;

    @LocalServerPort
    private int port;

    private final String seleniumHub = "http://selenium-hub:4444/wd/hub";

    private final int backendPortWiremock = 8001;

    @Value("${server.servlet.contextPath}")
    private String contextPath;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(backendPortWiremock);

    @Before
    public void setup() {

        taskBaseUrl = url + ":" + port + contextPath;

        stubFor(get(urlEqualTo("/tasks-backend/todo"))
                .willReturn(
                        aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.JSON.getMimeType()))
                                .withBody("[]")));

        //Succes
        stubFor(post(urlEqualTo("/tasks-backend/todo"))
                .withRequestBody(equalToJson("{ \"id\" : null, \"description\": \"Test success\", \"dueDate\": [ 2030, 1, 1 ] }"))
                .willReturn(
                        aResponse()
                                .withStatus(HttpStatus.CREATED.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.JSON.getMimeType()))
                                .withBody("{ \"id\" : 1, \"description\": \"Test success\", \"dueDate\": \"2032-01-01\" }")));

        //No description
        stubFor(post(urlEqualTo("/tasks-backend/todo"))
                .withRequestBody(equalToJson("{ \"id\" : null, \"description\" : \"\", \"dueDate\" : [ 2025, 7, 12 ] }"))
                .willReturn(
                        aResponse()
                                .withStatus(HttpStatus.BAD_REQUEST.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.JSON.getMimeType()))
                                .withBody("{\"message\":\"Fill the task description\",}")));

        //Invalid date
        stubFor(post(urlEqualTo("/tasks-backend/todo"))
                .withRequestBody(equalToJson("{ \"id\" : null, \"description\" : \"Invalid date test\", \"dueDate\" : [ 2025, 10, 10 ] }"))
                .willReturn(
                        aResponse()
                                .withStatus(HttpStatus.BAD_REQUEST.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.JSON.getMimeType()))
                                .withBody("{\"message\":\"Due date must not be in past\",}")));
    }

    @Test
    public void shouldSaveWithSuccess() {

        WebDriver webDriver = getWebDriver();

        try {
            webDriver.navigate().to(taskBaseUrl);
            webDriver.findElement(By.id("addTodo")).click();
            webDriver.findElement(By.id("description")).sendKeys("Test success");
            webDriver.findElement(By.id("dueDate")).sendKeys("01/01/2030");
            webDriver.findElement(By.id("saveButton")).click();
            String msg = webDriver.findElement(By.id("message")).getText();
            assertEquals("Success!", msg);
        } finally {
            webDriver.quit();
        }
    }

    @Test
    public void shouldFailInvalidDescription() {

        WebDriver webDriver = getWebDriver();

        try {
            webDriver.navigate().to(taskBaseUrl);
            webDriver.findElement(By.id("addTodo")).click();
            webDriver.findElement(By.id("dueDate")).sendKeys("12/07/2025");
            webDriver.findElement(By.id("saveButton")).click();
            String msg = webDriver.findElement(By.id("message")).getText();
            assertEquals("Fill the task description", msg);
        } finally {
            webDriver.quit();
        }
    }

    @Test
    public void shouldFailInvalidDate() {

        WebDriver webDriver = getWebDriver();

        try {
            webDriver.navigate().to(taskBaseUrl);
            webDriver.findElement(By.id("addTodo")).click();
            webDriver.findElement(By.id("description")).sendKeys("Invalid date test");
            webDriver.findElement(By.id("dueDate")).sendKeys("10/10/2025");
            webDriver.findElement(By.id("saveButton")).click();
            String msg = webDriver.findElement(By.id("message")).getText();
            assertEquals("Due date must not be in past", msg);
        } finally {
            webDriver.quit();
        }
    }

    private WebDriver getWebDriver() {
        WebDriver webDriver = null;
        try {
            webDriver = new RemoteWebDriver(new URL(seleniumHub), new ChromeOptions());

            webDriver.manage()
                    .timeouts()
                    .implicitlyWait(3, TimeUnit.SECONDS);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return webDriver;
    }
}
