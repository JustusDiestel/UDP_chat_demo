import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.UUID;

public class PeerSender {

    private final DatagramSocket socket;
    private final Peer peer;

    public PeerSender(DatagramSocket socket, Peer peer) {
        this.socket = socket;
        this.peer = peer;
    }

    public String newId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public void send(Packet pkt, InetSocketAddress addr) throws Exception {
        byte[] data = pkt.encode().getBytes();
        DatagramPacket dp = new DatagramPacket(data, data.length, addr);
        socket.send(dp);
    }
}