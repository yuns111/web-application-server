package controller;

import webserver.HttpResponse;
import webserver.http.HttpRequest;

public interface Controller {
	void service(HttpRequest request, HttpResponse response);
}
