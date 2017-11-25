package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import webserver.HttpResponse;
import webserver.http.HttpRequest;
import webserver.http.HttpSession;

public class LoginController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);

	@Override
	public void doPost(HttpRequest request, HttpResponse response) {
		String id = request.getParameter("userId");
		String password = request.getParameter("password");

		User user = DataBase.findUserById(id);
		if (user == null || !user.getPassword().equals(password)) {
			response.forward("/user/login_failed.html");
			return;
		}

		HttpSession session = request.getSession();
		session.setAttribute("user", user);
		response.sendRedirect("/index.html");
		return;
	}
}
