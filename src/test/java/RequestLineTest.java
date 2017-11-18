import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import webserver.http.HttpMethod;
import webserver.http.RequestLine;

public class RequestLineTest {
	@Test
	public void create_method() {
		RequestLine line = new RequestLine("GET /index.html HTTP/1.1");
		assertEquals(HttpMethod.GET, line.getMethod());
		assertEquals("/index.html", line.getPath());
	}


	@Test
	public void create_path_and_params() {
		RequestLine line = new RequestLine("GET /user/create?userId=javajigi&password=pass HTTP/1.1");
		assertEquals(HttpMethod.GET, line.getMethod());
		assertEquals("/user/create", line.getPath());

		Map<String, String> params = line.getParameters();
		assertEquals(2, params.size());
	}
}
