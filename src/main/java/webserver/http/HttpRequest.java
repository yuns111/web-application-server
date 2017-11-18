package webserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	private RequestLine requestLine;
	private Map<String, String> headers = new HashMap<>();
	private Map<String, String> parameters = new HashMap<>();

	public HttpRequest(InputStream inputStream) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String line = bufferedReader.readLine();

			log.debug("request Line : {}", line);

			if (line == null) {
				return;
			}

			requestLine = new RequestLine(line);

			makeHeader(bufferedReader);

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void makeHeader(BufferedReader bufferedReader) throws IOException {
		String line = bufferedReader.readLine();

		while (!"".equals(line)) {

			log.debug("headers: {}", line);

			String[] splitedHeaders = line.split(":");
			headers.put(splitedHeaders[0].trim(), splitedHeaders[1].trim());

			line = bufferedReader.readLine();
		}

		if(requestLine.getMethod() == HttpMethod.POST) {
			String body = IOUtils.readData(bufferedReader, Integer.parseInt(headers.get("Content-Length")));
			parameters = HttpRequestUtils.parseQueryString(body);
		} else {
			parameters = requestLine.getParameters();
		}
	}

	public HttpMethod getMethod() {
		return requestLine.getMethod();
	}

	public String getPath() {
		return requestLine.getPath();
	}

	public String getHeader(String headerKey) {
		return headers.get(headerKey);
	}

	public String getParameter(String paramKey) {
		return parameters.get(paramKey);
	}
}
