public class RouteEntry {
    public String nextHop;
    public int cost;

    public RouteEntry(String nextHop, int cost) {
        this.nextHop = nextHop;
        this.cost = cost;
    }

    @Override
    public String toString() {
        return nextHop + " cost=" + cost;
    }
}