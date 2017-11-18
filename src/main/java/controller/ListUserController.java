package controller;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import webserver.HttpResponse;
import webserver.http.HttpRequest;

public class ListUserController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(ListUserController.class);

	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		if (!isLogin(request.getHeader("Cookie"))) {
			response.sendRedirect("/user/login.html");
			return;
		}
		Collection<User> users = DataBase.findAll();

		String userListHtml = makeUserListHtml(users);
		response.forwardBody(userListHtml);
	}

	private boolean isLogin(String cookieValue) {
		Map<String, String> parsedCookies = HttpRequestUtils.parseCookies(cookieValue);
		String loginValue = parsedCookies.get("logined");

		if (loginValue == null) {
			return false;
		}
		return Boolean.parseBoolean(loginValue);
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
}
