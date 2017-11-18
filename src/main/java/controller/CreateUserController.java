package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import webserver.HttpResponse;
import webserver.http.HttpRequest;

public class CreateUserController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

	@Override
	public void doPost(HttpRequest request, HttpResponse response) {
		String id = request.getParameter("userId");
		String password = request.getParameter("password");
		String name = request.getParameter("name");
		String email = request.getParameter("email");

		User user = new User(id, password, name, email);
		log.debug("create user : {}", user.toString());
		DataBase.addUser(user);
		response.sendRedirect("/index.html");
	}
}
