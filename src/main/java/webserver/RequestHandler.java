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
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

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
            if(line == null) {
                return;
            }

            String requestPath = getUrl(line);
            int length = 0;
            while(!"".equals(line)) {
                line = bufferedReader.readLine();
                String contentLength = "Content-Length: ";

                if(line.contains(contentLength)) {
                    length = Integer.parseInt(line.split(contentLength)[1]);
                }
            }

            if(requestPath.endsWith(".css")) {
                byte[] body =  Files.readAllBytes(new File("./webapp" + requestPath).toPath());
                DataOutputStream dos = new DataOutputStream(out);
                response200HeaderCSS(dos, body.length);
                responseBody(dos, body);
            }
            if(requestPath.equals("/user/create")) {
                Map<String, String> parsedParam = HttpRequestUtils.parseQueryString(IOUtils.readData(bufferedReader, length));
                if(parsedParam != null) {
                    String id = parsedParam.get("userId");
                    String password = parsedParam.get("password");
                    String name = parsedParam.get("name");
                    String email = parsedParam.get("email");

                    User user = new User(id, password, name, email);

                    DataBase.addUser(user);
                }

                byte[] body =  Files.readAllBytes(new File("./webapp/index.html" ).toPath());
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, body.length,"/index.html");
                responseBody(dos, body);
            }

            if(requestPath.equals("/user/login")) {
                Map<String, String> parsedParam = HttpRequestUtils.parseQueryString(IOUtils.readData(bufferedReader, length));
                if(parsedParam != null) {
                    String id = parsedParam.get("userId");
                    String password = parsedParam.get("password");

                    User user = DataBase.findUserById(id);
                    if(user == null || !user.getPassword().equals(password)) {
                        byte[] body =  Files.readAllBytes(new File("./webapp/user/login_failed.html" ).toPath());
                        DataOutputStream dos = new DataOutputStream(out);
                        response302Header(dos, body.length, "/user/login_failed.html", false);
                        responseBody(dos, body);
                    } else {
                        byte[] body =  Files.readAllBytes(new File("./webapp/index.html" ).toPath());
                        DataOutputStream dos = new DataOutputStream(out);
                        response302Header(dos, body.length,"/index.html", true);
                        responseBody(dos, body);
                    }
                }
            }

            if(requestPath.equals("/user/list.html")) {
                Map<String, String> parsedCookies = HttpRequestUtils.parseCookies(IOUtils.readData(bufferedReader, length));

                if(parsedCookies != null) {
                    boolean logined = Boolean.parseBoolean(parsedCookies.get("logined"));
                    if(logined) {
                        StringBuilder sb = new StringBuilder();
                        Collection<User> users = DataBase.findAll();

                        for(User user : users) {
                            sb.append(user.toString() + "\n");
                        }
                        // 리스트 추가
                        byte[] body =  Files.readAllBytes(new File("./webapp" + requestPath).toPath());
                        DataOutputStream dos = new DataOutputStream(out);
                        response200Header(dos, body.length);
                        responseBody(dos, body);

                    } else {
                        byte[] body =  Files.readAllBytes(new File("./webapp/index.html" ).toPath());
                        DataOutputStream dos = new DataOutputStream(out);
                        response302Header(dos, body.length,"/index.html");
                        responseBody(dos, body);
                    }
                }

            }

            byte[] body =  Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200HeaderCSS(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
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

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, boolean login) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + login);
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

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String destination) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Location: http://localhost:8080" + destination + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String destination, boolean login) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Location: http://localhost:8080" + destination + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + login);
            dos.writeBytes("\r\n");
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
