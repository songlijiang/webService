import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * Created by slj on 16/10/22.
 */
public class SocketHttpServer {

    private static Logger logger = Logger.getLogger(SocketHttpServer.class);

    private static final int DEFAULT_PORT =8080;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
            logger.info("SocketHttpServer is start on "+DEFAULT_PORT);

            while (true){
              Socket socket =  serverSocket.accept();  //waiting new connection
              BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
               String line= bufferedReader.readLine();
                while (line != null && !line.isEmpty()){
                    logger.info(line);
                    line = bufferedReader.readLine();
                    stringBuilder.append(line).append("\r\n");
                }
                hander(socket,stringBuilder.toString());
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void hander(Socket socket,String result){
        try {
            OutputStream outputStream = socket.getOutputStream();
            List<String> headers = getHeader();
            for (String header : headers){
                outputStream.write(header.getBytes());
                outputStream.write("\r\n".getBytes());
            }
            outputStream.write("\r\n".getBytes());
            outputStream.write(result.getBytes());
            outputStream.write("\r\n".getBytes());

            logger.info("response"+result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static List<String> getHeader(){
       return  Arrays.asList("HTTP/1.1 200 OK",
                "Server: SimpleWebServer",
               "Content-Type: text/html;charset=UTF-8");
    }
}
