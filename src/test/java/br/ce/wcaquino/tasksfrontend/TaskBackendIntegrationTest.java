package br.ce.wcaquino.tasksfrontend;

import br.ce.wcaquino.tasksfrontend.model.Todo;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode;

@Ignore
@AutoConfigureStubRunner(stubsMode = StubsMode.LOCAL, ids = "br.ce.wcaquino:tasks-backend:+:stubs:8091")
@SpringBootTest
@RunWith(SpringRunner.class)
public class TaskBackendIntegrationTest {

    private RestTemplate restTemplate;

    private final String url = "http://localhost:8091/todo";

    @Before
    public void setup() {
        restTemplate = new RestTemplate();
    }

    @Test
    public void shouldReturnAllTasks() {

        HttpEntity<String> request = new HttpEntity<>(getHttpHeaders());

        ResponseEntity<Todo> responseEntity = restTemplate.exchange(url, HttpMethod.GET,
                request, Todo.class);

        Todo todo = responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(todo);
        assertNotNull(todo.getId());
        assertNotNull(todo.getDescription());
        assertNotNull(todo.getDueDate());
    }

    @Test
    public void shouldSaveWithSuccess() throws Exception {

        HttpEntity<String> request = new HttpEntity<>(buildRequest(), getHttpHeaders());

        ResponseEntity<Todo> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                request, Todo.class);

        Todo todo = responseEntity.getBody();
        assertNotNull(todo);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(todo.getId());
        assertNotNull(todo.getDescription());
        assertNotNull(todo.getDueDate());
    }

    private String buildRequest() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("description", "task 1");
        jsonObject.put("dueDate", "2025-01-01");
        return jsonObject.toString();
    }

    private HttpHeaders getHttpHeaders() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }
}
