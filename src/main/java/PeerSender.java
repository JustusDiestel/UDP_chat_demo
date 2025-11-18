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

    public void sendReliable(Packet pkt, InetSocketAddress addr) throws Exception {

        int tries = 0;
        String id = pkt.id;

        while (tries < 5) {
            send(pkt, addr);  // UDP send
            long start = System.currentTimeMillis();

            // warten auf ACK
            while (System.currentTimeMillis() - start < 300) {
                if (peer.acked.contains(id)) return;  // ACK angekommen
                Thread.sleep(5);
            }

            tries++;
        }

        System.out.println("Paket " + id + " verloren (keine ACK)");
    }

}