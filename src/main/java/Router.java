import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, RouteEntry> table = new HashMap<>();
    private final Map<String, InetSocketAddress> neighbors = new HashMap<>();

    public Router(String myName) {
        table.put(myName, new RouteEntry(myName, 0));
    }

    // add direct neighbors
    public void addNeighbor(String name, InetSocketAddress addr) {
        neighbors.put(name, addr);
        table.put(name, new RouteEntry(name, 1));
    }

    public Map<String, InetSocketAddress> getNeighbors() {
        return neighbors;
    }

    public InetSocketAddress nextHop(String dest) {
        if (!table.containsKey(dest)) return null;
        String hop = table.get(dest).nextHop;
        return neighbors.get(hop);
    }

    public boolean processVector(String from, String vector) {

        boolean changed = false;

        // first-hop bestimmen:
        String firstHop = table.containsKey(from)
                ? table.get(from).nextHop     // bereits bekannte Route → first hop übernehmen
                : from;                       // direkter Nachbar → Hop ist der Sender

        String[] entries = vector.split(",");
        for (String e : entries) {
            if (!e.contains(":")) continue;

            String[] p = e.split(":");
            String node = p[0];
            int cost = Integer.parseInt(p[1]) + 1; // +1 hop

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