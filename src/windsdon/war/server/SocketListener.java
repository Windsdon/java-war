package windsdon.war.server;

import java.net.Socket;

/**
 *
 * @author Windsdon
 */
class SocketListener extends Thread {
    private static int CURRENT_ID = 0;
    private SocketReceiver subject;
    public SocketListener(SocketReceiver object){
        super("SocketListener-"+(CURRENT_ID++));
        subject = object;
    }
}

interface SocketReceiver {
    public void socketConnection(Socket connection);
}
