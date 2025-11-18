import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("Usage: java Main <name> <port>");
            return;
        }

        Peer p = new Peer(args[0], Integer.parseInt(args[1]));

        System.out.println("Peer gestartet: " + p.name);

        new Thread(new PeerListener(p.socket, p)).start();

        Scanner sc = new Scanner(System.in);

        while (true) {
            String line = sc.nextLine();

            if (line.startsWith("/connect")) {
                String[] parts = line.split(" ");
                p.connect(parts[1], Integer.parseInt(parts[2]));
            }

            if (line.startsWith("/msg")) {
                String[] parts = line.split(" ", 3);
                p.sendMessage(parts[1], parts[2]);
            }

            if (line.equals("/routes")) {
                p.router.printTable();
            }
        }
    }
}