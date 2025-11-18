public class Packet {

    public PacketType type;
    public String id;
    public String src;
    public String dest;
    public String payload;

    public Packet(PacketType t, String id, String src, String dest, String payload) {
        this.type = t;
        this.id = id;
        this.src = src;
        this.dest = dest;
        this.payload = payload;
    }

    public String encode() {
        return type + "|" + id + "|" + src + "|" + dest + "|" + payload;
    }

    public static Packet decode(String raw) {
        String[] p = raw.split("\\|", 5);
        return new Packet(
                PacketType.valueOf(p[0]),
                p[1],
                p[2],
                p[3],
                p[4]
        );
    }
}