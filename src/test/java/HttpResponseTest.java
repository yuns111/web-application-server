import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

import webserver.HttpResponse;

public class HttpResponseTest {
	private String testDirectory = "./src/test/resources/";

	@Test
	public void responseForward() throws Exception {
		HttpResponse httpResponse = new HttpResponse(createOutputStream("Http_Forward.txt"));
		httpResponse.forward("/index.html");
	}

	@Test
	public void responseRedirect() throws Exception {
		HttpResponse httpResponse = new HttpResponse(createOutputStream("Http_Redirect.txt"));
		httpResponse.sendRedirect("/index.html");
	}

	@Test
	public void responseCookies() throws Exception {
		HttpResponse httpResponse = new HttpResponse(createOutputStream("Http_Cookie.txt"));
		httpResponse.addHeader("Set-Cookie", "logined=true");
		httpResponse.sendRedirect("/index.html");
	}

	private OutputStream createOutputStream(String filename) throws FileNotFoundException {
		return new FileOutputStream(new File(testDirectory + filename));
	}
}
