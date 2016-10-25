import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by slj on 16/10/22.
 */
public class SocketHttpServer {

    private static Logger logger = Logger.getLogger(SocketHttpServer.class);

    private static final int DEFAULT_PORT =8080;

    public static void main(String[] args) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(DEFAULT_PORT));
            serverSocketChannel.configureBlocking(false);
            logger.info("SocketHttpServer is start on "+DEFAULT_PORT);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("register serverChannel "+serverSocketChannel.getLocalAddress());

            while (true) {
                if(selector.select(500)==0){
                    logger.info("timeout no connection");
                    continue;
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    if (selectionKey.isAcceptable()) {
                        SocketChannel clientChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                        if(clientChannel !=null){
                            clientChannel.configureBlocking(false);
                            clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(512));
                            logger.info("register new clientChannel " + clientChannel.getRemoteAddress());
                        }
                    }

                    if (selectionKey.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        long read = clientChannel.read(byteBuffer);
                        if (read == -1) {
                            clientChannel.close();
                        }
                        if (read > 0) {
                            logger.info("client write to server " + new String(byteBuffer.array()));
                            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                    }

                    if(selectionKey.isValid() && selectionKey.isWritable()){
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        byteBuffer.flip();
                        SocketChannel clientChannel =(SocketChannel)selectionKey.channel();
                        clientChannel.write(ByteBuffer.wrap(hander(new String(byteBuffer.array())).getBytes()));
                        logger.info("server write to client "+new String(byteBuffer.array()));

                        selectionKey.interestOps(SelectionKey.OP_READ);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String hander(String result){
        try {
            StringBuilder resultAll = new StringBuilder();
            List<String> headers = getHeader();
            for (String header : headers){
                resultAll.append(header);
                resultAll.append("\r\n");
            }
            resultAll.append("\r\n");
            resultAll.append(result);
            resultAll.append("\r\n");

            logger.info("response"+resultAll);
            return resultAll.toString();
        } catch (Exception e) {

            e.printStackTrace();
            return e.toString();
        }
    }


    private static List<String> getHeader(){
       return  Arrays.asList("HTTP/1.1 200 OK",
                "Server: SimpleWebServer",
               "Content-Type: text/html;charset=UTF-8");
    }
}
