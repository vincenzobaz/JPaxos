package lpd.register;

import lsr.common.Configuration;
import lsr.paxos.client.Client;
import lsr.paxos.client.ReplicationException;

import java.io.IOException;
import java.util.Scanner;

public class ClientLauncher {

    public static void main(String[] args) throws IOException, ReplicationException {
        System.out.println("Launching client");
        Client client = new Client(new Configuration(args[0]));
        System.out.println("Created client object");
        client.connect();
        System.out.println("Connected to replicas");

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Ready to take orders");
            String input = sc.nextLine();
            Command c;
            if (input.equals("READ")) {
                c = new Command(CommandType.READ, -1);
            } else {
                c = new Command(CommandType.READ, Integer.parseInt(input));
            }
            Response response = new Response(client.execute(c.toByteArray()));

            String output = response.isRead() ?
                    "Read value " + response.getValue() :
                    "Wrote to replicas";

            System.out.println("Response: " + output);
        }
    }
}
