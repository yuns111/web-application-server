package controller;

import webserver.HttpResponse;
import webserver.http.HttpMethod;
import webserver.http.HttpRequest;

public class AbstractController implements Controller {
	@Override
	public void service(HttpRequest request, HttpResponse response) {
		HttpMethod method = request.getMethod();

		if(method == HttpMethod.POST) {
			doPost(request, response);
		} else {
			doGet(request, response);
		}
	}

	protected void doPost(HttpRequest request, HttpResponse response) {
	}

	protected void doGet(HttpRequest request, HttpResponse response) {
	}
}
