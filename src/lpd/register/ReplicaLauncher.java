package lpd.register;

import lsr.common.Configuration;
import lsr.paxos.replica.Replica;

import java.io.IOException;

public class ReplicaLauncher {
    public static void main(String[] args) throws IOException {
        int localId = Integer.parseInt(args[0]);

        Replica replica = new Replica(new Configuration(), localId, new IntRegisterService());

        replica.start();
        System.in.read();
        System.exit(0);
    }
}
