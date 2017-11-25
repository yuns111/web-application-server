package webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.Controller;
import util.HttpRequestUtils;
import webserver.HttpResponse;
import webserver.RequestMapping;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
			connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

			HttpRequest request = new HttpRequest(in);
			HttpResponse response = new HttpResponse(out);
			HttpCookie cookies = request.getCookies();

			if(cookies.getCookie("JSESSIONID") == null) {
				response.addHeader("Set-Cookie", "JSESSIONID=" + UUID.randomUUID());
			}

			Controller controller = RequestMapping.getController(request.getPath());

			if(controller == null) {
				String path = getDefaultPath(request.getPath());
				response.forward(path);
				return;
			}

			controller.service(request, response);

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private String getDefaultPath(String path) {
		if (path.equals("/")) {
			return "/index.html";
		}
		return path;
	}
}
