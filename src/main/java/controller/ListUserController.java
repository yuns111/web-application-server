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
import webserver.http.HttpSession;

public class ListUserController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(ListUserController.class);

	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		if (!isLogin(request.getSession())) {
			response.sendRedirect("/user/login.html");
			return;
		}
		Collection<User> users = DataBase.findAll();

		String userListHtml = makeUserListHtml(users);
		response.forwardBody(userListHtml);
	}

	private boolean isLogin(HttpSession session) {
		User user = (User)session.getAttribute("user");

		if(user == null) {
			return false;
		}
		return true;
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
