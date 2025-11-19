import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, RouteEntry> table = new HashMap<>();
    private final Map<String, InetSocketAddress> neighbors = new HashMap<>();

    // -------- für Dead-Node-Detection --------
    private final Map<String, Long> lastSeen = new HashMap<>();
    private static final long TIMEOUT = 15000; // 15 Sekunden

    public Router(String myName) {
        table.put(myName, new RouteEntry(myName, 0));
    }

    public void addNeighbor(String name, InetSocketAddress addr) {
        neighbors.put(name, addr);
        table.put(name, new RouteEntry(name, 1));
        lastSeen.put(name, System.currentTimeMillis());
    }

    public Map<String, InetSocketAddress> getNeighbors() {
        return neighbors;
    }

    public InetSocketAddress nextHop(String dest) {
        if (!table.containsKey(dest)) return null;
        String hop = table.get(dest).nextHop;
        return neighbors.get(hop);
    }

    // ----------------------------------------------------
    // Process incoming Distance Vector
    // ----------------------------------------------------
    public boolean processVector(String from, String vector) {

        lastSeen.put(from, System.currentTimeMillis());

        boolean changed = false;

        // determine first hop to "from"
        String firstHop = table.containsKey(from)
                ? table.get(from).nextHop
                : from;

        String[] entries = vector.split(",");
        for (String e : entries) {
            if (!e.contains(":")) continue;

            String[] p = e.split(":");
            String node = p[0];
            int cost = Integer.parseInt(p[1]) + 1;

            if (!table.containsKey(node)) {
                table.put(node, new RouteEntry(firstHop, cost));
                changed = true;

            } else if (cost < table.get(node).cost) {
                table.put(node, new RouteEntry(firstHop, cost));
                changed = true;
            }
        }

        return changed;
    }

    // --------------------------------------------------------------------
    // ---- DEAD NODE TIMEOUT (wird vom HEARTBEAT aufgerufen!) ------------
    // --------------------------------------------------------------------
    public boolean checkTimeouts() {
        boolean changed = false;

        for (String n : neighbors.keySet().toArray(new String[0])) {

            long seen = lastSeen.getOrDefault(n, 0L);

            if (System.currentTimeMillis() - seen > TIMEOUT) {

                System.out.println("[TIMEOUT] " + n + " ist tot → entferne Routen");

                neighbors.remove(n);
                table.remove(n);

                table.entrySet().removeIf(e -> e.getValue().nextHop.equals(n));

                changed = true;
            }
        }

        return changed;
    }

    public String exportVector() {
        StringBuilder sb = new StringBuilder();
        for (var e : table.entrySet()) {
            sb.append(e.getKey()).append(":").append(e.getValue().cost).append(",");
        }
        return sb.toString();
    }

    public void printTable() {
        System.out.println("---- ROUTING TABLE ----");
        for (var e : table.entrySet()) {
            System.out.println(e.getKey() + " → " + e.getValue().nextHop + " cost=" + e.getValue().cost);
        }
        System.out.println("-----------------------");
    }
}