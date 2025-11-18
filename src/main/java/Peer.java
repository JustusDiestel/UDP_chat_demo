import java.net.InetSocketAddress;
import java.net.DatagramSocket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Peer {

    String name;
    DatagramSocket socket;
    Router router;
    PeerSender sender;

    Set<String> acked = ConcurrentHashMap.newKeySet();

    public Peer(String name, int port) throws Exception {
        this.name = name;
        socket = new DatagramSocket(port);
        router = new Router(name);
        sender = new PeerSender(socket, this);
    }

    public void handle(Packet pkt, InetSocketAddress senderAddr) throws Exception {

        if (pkt.type != PacketType.ACK
                && pkt.type != PacketType.JOIN_ACK
                && pkt.type != PacketType.ROUTE_UPDATE) {
            sendAck(pkt.id, senderAddr);
        }

        switch (pkt.type) {

            case ACK -> acked.add(pkt.id);

            case JOIN -> {
                System.out.println(pkt.src + " joined");
                router.addNeighbor(pkt.src, senderAddr);

                // Antworte mit JOIN_ACK (löst KEINEN neuen JOIN aus)
                Packet ack = new Packet(PacketType.JOIN_ACK,
                        pkt.id,     // gleiche ID wie JOIN
                        name,
                        "ALL",
                        ""
                );
                sender.send(ack, senderAddr);

                broadcastRouting();
            }
            case JOIN_ACK -> {
                // Derjenige, den ich kontaktiert habe, bestätigt jetzt:
                router.addNeighbor(pkt.src, senderAddr);
                broadcastRouting();
            }

            case ROUTE_UPDATE -> {
                boolean changed = router.processVector(pkt.src, pkt.payload);
                if (changed) broadcastRouting();
            }

            case MSG -> {
                if (pkt.dest.equals(name)) {
                    System.out.println(pkt.src + " → " + pkt.payload);
                } else {
                    forward(pkt);
                }
            }
        }
    }

    public void forward(Packet pkt) throws Exception {
        InetSocketAddress next = router.nextHop(pkt.dest);
        if (next != null) sender.sendReliable(pkt, next);
    }

    public void sendAck(String id, InetSocketAddress addr) throws Exception {
        Packet ack = new Packet(PacketType.ACK, id, name, "", "");
        sender.send(ack, addr);
    }

    public void sendMessage(String dest, String msg) throws Exception {

        InetSocketAddress next = router.nextHop(dest);

        if (next == null) {
            System.out.println("Ziel nicht erreichbar");
            return;
        }

        Packet p = new Packet(PacketType.MSG, sender.newId(), name, dest, msg);
        sender.sendReliable(p, next);
    }

    public void connect(String ip, int port) throws Exception {

        InetSocketAddress addr = new InetSocketAddress(ip, port);

        Packet join = new Packet(
                PacketType.JOIN,
                sender.newId(),
                name,
                "ALL",
                ""
        );

        sender.send(join, addr);
    }

    public void broadcastRouting() throws Exception {
        Packet vec = new Packet(
                PacketType.ROUTE_UPDATE,
                sender.newId(),
                name,
                "ALL",
                router.exportVector()
        );

        for (var n : router.neighbors().values()) {
            sender.send(vec, n);
        }
    }
}