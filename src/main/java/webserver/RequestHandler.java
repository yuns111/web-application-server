package webserver;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;

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

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = bufferedReader.readLine();

            String[] tokens = line.split(" ");
            String url = tokens[1];
            int index = url.indexOf("?");

            String requestPath = getUrl(line);
            Map<String, String> parsedParam = HttpRequestUtils.parseQueryString(line);

            if(parsedParam != null) {
                String id = parsedParam.get("userId");
                String password = parsedParam.get("password");
                String name = parsedParam.get("name");
                String email = parsedParam.get("email");

                User user = new User(id, password, name, email);

                System.out.println(user.toString());
            }

            byte[] body =  Files.readAllBytes(new File("./webapp" + requestPath).toPath());

            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);

            while(!"".equals(line)) {
                line = bufferedReader.readLine();
                if(line == null) {
                    return;
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getUrl(String line) {

        String[] tokens = line.split(" ");
        String url = tokens[1];
        int index = url.indexOf("?");

        String requestPath = url;

        if(index != -1) {
            requestPath = url.substring(0, index);
        }

        return requestPath;
    }

    private Map<String, String> getParams(String line) {

        String[] tokens = line.split(" ");
        String url = tokens[1];
        int index = url.indexOf("?");

        if(index == -1) {
            return null;
        }
        String params = url.substring(index + 1);
        Map<String, String> parsedParam = HttpRequestUtils.parseQueryString(params);

        return parsedParam;
    }
}
