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
import java.nio.file.Path;

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
            Path file = getFile(in);
            byte[] body = Files.readAllBytes(file);
            // end of todo
            
            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private Path getFile(InputStream in) throws IOException {
        InputStreamReader inputReader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(inputReader);
        
        String line = "start";
        StringBuilder result = new StringBuilder();
        while(!"".equals(line)) {
            line = br.readLine();
            result.append(line);
        }
        
        String requestInfo = result.toString();
        String[] token = requestInfo.split(" "); 
        String url = token[1];
        
        String requestPath = url;
        int idx = url.indexOf("?");
        if(idx > -1) {
        	requestPath = url.substring(0, idx);	
        }
        
        // String params = url.substring(idx);
        
        Path file = new File("./webapp" + requestPath).toPath();
        return file;
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
