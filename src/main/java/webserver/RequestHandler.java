package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            Map<String, String> requestMap = getRequestMap(in);

            String path = requestMap.get("path");
            if("/".equals(path)) path = "/index.html";
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
//            byte[] body = requestMap.toString().getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static Map<String, String> getRequestMap(InputStream in) throws IOException {
        Map<String, String> requestMap = new HashMap<>();

        InputStreamReader rd = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(rd);

        String line = "s";
        StringBuilder header = new StringBuilder();
        while(!"".equals(line)) {
            line = br.readLine();
            header.append(line).append("\n");
        }

        String result = header.toString();
        String[] tokens = result.split("\n");

        String[] info = tokens[0].split(" ");
        requestMap.put("method", info[0]);
        requestMap.put("path", info[1]);

        for(int i = 1; i < tokens.length-1; i++) {
            String[] split = tokens[i].split(": ");
            requestMap.put(split[0], split[1]);
        }
        return requestMap;
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
}
