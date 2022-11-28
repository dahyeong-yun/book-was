package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            Map<String, String> requestMap = getRequestMap(in);

            // path에 알맞는 서비스 로직 혹은 뷰를 뿌려주는 역할
            String path = requestMap.get("path");

            // TODO path + method + parameter에 따른 처리를 고르는 역할

            // 뷰를 선택 하는 역할
            if("/".equals(path)) path = "/index.html";
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
//            byte[] body = requestMap.toString().getBytes();

            // 응답 헤더를 생성하는 역할
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static Map<String, String> getRequestMap(InputStream in) throws IOException {
        Map<String, String> requestMap = new HashMap<>();

        // 입력 스트림을 받는 역할
        InputStreamReader rd = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(rd);

        String line = "s";
        StringBuilder header = new StringBuilder();
        while(!"".equals(line)) {
            line = br.readLine();
            header.append(line).append("\n");
        }

        // 입력 값에 대해 정제하는 역할
        String result = header.toString();
        String[] tokens = result.split("\n");

        String[] info = tokens[0].split(" ");
        int index = info[1].indexOf("?");
        String requestPath = info[1].substring(0, index);
        String params = info[1].substring(index+1);

        Map<String, String> parameterMap = HttpRequestUtils.parseQueryString(params);

        requestMap.put("method", info[0]);
        requestMap.put("path", requestPath);
        // TODO 파라미터를 String Object 형태로 변환하여 저장 -> 이게 컨트롤러에서 지정한 오브젝트로 리플렉션의 형태로 변경

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
