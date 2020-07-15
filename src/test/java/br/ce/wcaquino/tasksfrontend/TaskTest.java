package br.ce.wcaquino.tasksfrontend;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TaskTest {

    private final String url = "http://localhost";

    @LocalServerPort
    private int port;

    private final int backendPort = 8001;

    @Value("${server.servlet.contextPath}")
    private String contextPath;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(backendPort);

    @Before
    public void setup() {

        stubFor(get(urlEqualTo("/tasks-backend/todo"))
                .willReturn(
                        aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.JSON.getMimeType()))
                                .withBody("[]")));

        stubFor(post(urlEqualTo("/tasks-backend/todo"))
                .willReturn(
                        aResponse()
                                .withStatus(HttpStatus.CREATED.value())
                                .withHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.JSON.getMimeType()))
                                .withBody("{ \"description\": \"Test task 1\", \"dueDate\": \"2032-01-01\" }")));
    }

    @Test
    public void shouldSaveWithSuccess() {

        WebDriver webDriver = getWebDriver();

        try {
            webDriver.navigate().to(url + ":" + port + contextPath);
            webDriver.findElement(By.id("addTodo")).click();
            webDriver.findElement(By.id("description")).sendKeys("Teste Selenium");
            webDriver.findElement(By.id("dueDate")).sendKeys("12/07/2025");
            webDriver.findElement(By.id("saveButton")).click();
            String msg = webDriver.findElement(By.id("message")).getText();
            assertEquals("Success!", msg);
        } finally {
            webDriver.quit();
        }
    }

    private WebDriver getWebDriver() {
        WebDriver webDriver = new ChromeDriver();
        webDriver.manage()
                .timeouts()
                .implicitlyWait(3, TimeUnit.SECONDS);
        return webDriver;
    }
}
