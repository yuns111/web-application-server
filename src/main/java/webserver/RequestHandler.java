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
import java.nio.file.Paths;
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

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = bufferedReader.readLine();
            log.debug("request Line : {}", line);

            if(line == null) {
                return;
            }

            String requestPath = getUrl(line);
            int contentLength = 0;
            boolean logined = false;

            while(!"".equals(line)) {

                log.debug("header: {}", line);
                line = bufferedReader.readLine();

                String contentLengthLine = "Content-Length: ";
                if(line.contains(contentLengthLine)) {
                    contentLength = Integer.parseInt(line.split(contentLengthLine)[1]);
                }

                if(line.contains("Cookie")) {
                    logined = isLogin(line);
                }
            }

            if(requestPath.endsWith(".css")) {
                byte[] body =  Files.readAllBytes(Paths.get("./webapp", requestPath));
                DataOutputStream dos = new DataOutputStream(out);
                response200HeaderCSS(dos, body.length);
                responseBody(dos, body);
            }

            if(requestPath.equals("/user/create")) {
                Map<String, String> parsedParam = HttpRequestUtils.parseQueryString(IOUtils.readData(bufferedReader, contentLength));

                if(parsedParam != null) {
                    String id = parsedParam.get("userId");
                    String password = parsedParam.get("password");
                    String name = parsedParam.get("name");
                    String email = parsedParam.get("email");

                    User user = new User(id, password, name, email);
                    log.debug("create user : {}", user.toString());
                    DataBase.addUser(user);
                }

                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos, "/index.html", false);
            }

            if(requestPath.equals("/user/login")) {
                Map<String, String> parsedParam = HttpRequestUtils.parseQueryString(IOUtils.readData(bufferedReader, contentLength));

                if(parsedParam != null) {
                    String id = parsedParam.get("userId");
                    String password = parsedParam.get("password");

                    User user = DataBase.findUserById(id);
                    if(user == null || !user.getPassword().equals(password)) {
                        callResponse200(out, "/user/login_failed.html");

                    } else {
                        DataOutputStream dos = new DataOutputStream(out);
                        response302Header(dos,"/index.html", true);
                    }
                }
            }

            if(requestPath.equals("/user/list.html")) {
                if(logined) {
                    Collection<User> users = DataBase.findAll();

                    String userListHtml = makeUserListHtml(users);

                    byte[] body =  userListHtml.getBytes();
                    DataOutputStream dos = new DataOutputStream(out);
                    response200Header(dos, body.length);
                    responseBody(dos, body);

                } else {
                    DataOutputStream dos = new DataOutputStream(out);
                    response302Header(dos, "/index.html", false);
                }
            }
            callResponse200(out, requestPath);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void callResponse200(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String destination, boolean login) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + destination + "\r\n");
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

    private String makeUserListHtml(Collection<User> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");

        for(User user : users) {
            sb.append("<tr>");
            sb.append("<td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");

        return sb.toString();
    }

    private boolean isLogin(String line) {
        String[] headerTokens = line.split(":");
        Map<String, String> parsedCookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());

        String loginValue = parsedCookies.get("logined");
        System.out.println(loginValue);
        if(loginValue == null) {
            return false;
        }

        return Boolean.parseBoolean(loginValue);
    }
}
