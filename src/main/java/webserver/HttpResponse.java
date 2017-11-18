package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
	private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

	private DataOutputStream dos;
	private Map<String, String> headers;

	public HttpResponse(OutputStream outputStream) {
		dos = new DataOutputStream(outputStream);
		headers = new HashMap<>();

	}

	public void forward(String url) {
		try {
			byte[] body = Files.readAllBytes(Paths.get("./webapp" + url));
			if (url.endsWith(".css")) {
				headers.put("Content-Type", "text/css");
			} else if (url.endsWith(".js")) {
				headers.put("Content-Type", "application/javascript");
			} else {
				headers.put("Content-Type", "text/html;charset=utf-8");
			}
			headers.put("Content-Length", body.length + "");
			response200Header();
			responseBody(body);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public void forwardBody(String bodyData) {
		byte[] body = bodyData.getBytes();
		headers.put("Content-Type", "text/html;charset=utf-8");
		headers.put("Content-Length", body.length + "");
		response200Header();
		responseBody(body);
	}

	public void sendRedirect(String redirectUrl) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			addHeaders();
			dos.writeBytes("Location: " + redirectUrl + " \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response200Header() {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			addHeaders();
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void addHeaders() {
		try {
			Set<String> keys = headers.keySet();
			for (String key : keys) {
				dos.writeBytes(key + ": " + headers.get(key) + " \r\n");
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
