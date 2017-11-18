package webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import webserver.HttpResponse;

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

			String requestPath = getDefaultPath(request.getPath());

			if (requestPath.equals("/user/create")) {
				String id = request.getParameter("userId");
				String password = request.getParameter("password");
				String name = request.getParameter("name");
				String email = request.getParameter("email");

				User user = new User(id, password, name, email);
				log.debug("create user : {}", user.toString());
				DataBase.addUser(user);

				response.sendRedirect("/index.html");
				return;
			}

			if (requestPath.equals("/user/login")) {
				String id = request.getParameter("userId");
				String password = request.getParameter("password");

				User user = DataBase.findUserById(id);
				if (user == null || !user.getPassword().equals(password)) {
					response.forward("/user/login_failed.html");
					return;
				}
				response.addHeader("set-Cookie", "logined=true");
				response.sendRedirect("/index.html");
				return;
			}

			if (requestPath.equals("/user/list")) {
				if (!isLogin(request.getHeader("Cookie"))) {
					response.sendRedirect("/user/login.html");
					return;
				}
				Collection<User> users = DataBase.findAll();

				String userListHtml = makeUserListHtml(users);
				response.forwardBody(userListHtml);
				return;
			}

			response.forward(requestPath);
			return;

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private String makeUserListHtml(Collection<User> users) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table border='1'>");

		for (User user : users) {
			sb.append("<tr>");
			sb.append("<td>" + user.getUserId() + "</td>");
			sb.append("<td>" + user.getName() + "</td>");
			sb.append("<td>" + user.getEmail() + "</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");

		return sb.toString();
	}

	private boolean isLogin(String cookieValue) {
		Map<String, String> parsedCookies = HttpRequestUtils.parseCookies(cookieValue);
		String loginValue = parsedCookies.get("logined");

		if (loginValue == null) {
			return false;
		}
		return Boolean.parseBoolean(loginValue);
	}

	private String getDefaultPath(String path) {
		if (path.equals("/")) {
			return "/index.html";
		}
		return path;
	}
}
