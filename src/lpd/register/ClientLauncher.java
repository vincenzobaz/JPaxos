package lpd.register;

import lsr.paxos.client.Client;
import lsr.paxos.client.ReplicationException;

import java.io.IOException;
import java.util.Scanner;

public class ClientLauncher {

    public static void main(String[] args) throws IOException, ReplicationException {
        Client client = new Client();
        client.connect();

        Scanner sc = new Scanner(System.in);

        while (true) {
            String input = sc.nextLine();
            IntRegisterCommand c = input.isEmpty() ? new Read() : new Write(Integer.parseInt(input));
            System.out.println("Request: " + c.toString());
            byte[] response = client.execute(c.serialize());
            String output = response.length == 0 ?
                    "Wrote to replicas" :
                    "Read value " + Utils.deserializeInt(response);
            System.out.println("Response: " + output);
        }
    }
}
