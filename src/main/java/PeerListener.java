import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class PeerListener implements Runnable {

    private final DatagramSocket socket;
    private final Peer peer;

    public PeerListener(DatagramSocket s, Peer p) {
        this.socket = s;
        this.peer = p;
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[4096];
            while (true) {
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                socket.receive(dp);

                String raw = new String(dp.getData(), 0, dp.getLength());
                Packet pkt = Packet.decode(raw);

                peer.handle(pkt, new InetSocketAddress(dp.getAddress(), dp.getPort()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}