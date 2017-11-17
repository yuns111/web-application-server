package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequest {
	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	private String method;
	private String path;
	private Map<String, String> header;
	private Map<String, String> parameter;

	public HttpRequest(InputStream inputStream) {
		header = new HashMap<>();
		parameter = new HashMap<>();
		seperateRequest(inputStream);
	}

	private void seperateRequest(InputStream inputStream) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

			String line = bufferedReader.readLine();
			log.debug("request Line : {}", line);

			if (line == null) {
				return;
			}

			String[] tokens = line.split(" ");
			method = tokens[0];
			String[] url = tokens[1].split("\\?");
			path = url[0];

			makeHeader(bufferedReader);

			if(url.length == 2) {
				String queryString = url[1];
				makeParameter(queryString);
			} else {
				line = bufferedReader.readLine();
				makeParameter(line);
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void makeHeader(BufferedReader bufferedReader) throws IOException {
		String line = bufferedReader.readLine();

		while(!"".equals(line)) {

			log.debug("header: {}", line);

			String[] splitedHeaders = line.split(":");
			header.put(splitedHeaders[0], splitedHeaders[1].trim());

			line = bufferedReader.readLine();
		}
	}

	private void makeParameter(String queryString) throws IOException {

		System.out.println(queryString);
		String[] splitedParams = queryString.split("&");
		for(String param: splitedParams) {

			String[] keyValue = param.split("=");
			parameter.put(keyValue[0], keyValue[1].trim());
		}
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public String getHeader(String headerKey) {
		return header.get(headerKey);
	}

	public String getParameter(String paramKey) {
		return parameter.get(paramKey);
	}
}
