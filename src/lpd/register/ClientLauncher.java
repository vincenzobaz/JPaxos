package lpd.register;

import lsr.common.Configuration;
import lsr.paxos.client.Client;
import lsr.paxos.client.ReplicationException;

import java.io.IOException;
import java.util.Arrays;
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
            IntRegisterCommand c = input.equals("READ") ? new Read() : new Write(Integer.parseInt(input));
            byte[] response = client.execute(c.serialize());
            System.out.println("RAW RESPONSE: " + Arrays.toString(response));
            String output = response.length == 0 ?
                    "Wrote to replicas" :
                    "Read value " + Utils.deserializeInt(response);
            System.out.println("Response: " + output);
        }
    }
}
