package lsr.paxos.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import lsr.common.KillOnExceptionHandler;
import lsr.common.ProcessDescriptor;
import lsr.paxos.messages.Message;

public class TcpNetwork extends Network implements Runnable {
    private final TcpConnection[] connections;
    private final ProcessDescriptor p;
    private final ServerSocket server;
    private final Thread thread;

    /**
     * Creates new network for handling connections with other replicas.
     * 
     * @throws IOException if opening server socket fails
     */
    public TcpNetwork() throws IOException {
        this.p = ProcessDescriptor.getInstance();
        connections = new TcpConnection[p.numReplicas];
        for (int i = 0; i < connections.length; i++) {
            if (i < p.localId) {
                connections[i] = new TcpConnection(this, p.config.getProcess(i), false);
                connections[i].start();
            }
            if (i > p.localId) {
                connections[i] = new TcpConnection(this, p.config.getProcess(i), true);
                connections[i].start();
            }
        }
        logger.fine("Opening port: " + p.getLocalProcess().getReplicaPort());
        // _server = new ServerSocket(_p.getLocalProcess().getReplicaPort());
        server = new ServerSocket();
        server.setReceiveBufferSize(256 * 1024);
        server.bind(new InetSocketAddress((InetAddress) null, p.getLocalProcess().getReplicaPort()));

        thread = new Thread(this, "TcpNetwork");
        thread.setUncaughtExceptionHandler(new KillOnExceptionHandler());
        thread.start();
    }

    /**
     * Sends binary data to specified destination.
     * 
     * @param message - binary data to send
     * @param destination - id of replica to send data to
     * @return true if message was sent; false if some error occurred
     */
    boolean send(byte[] message, int destination) {
        assert destination != p.localId;
        return connections[destination].send(message);
    }

    /**
     * Main loop which accepts incoming connections.
     */
    public void run() {
        logger.info("TcpNetwork started");
        while (true) {
            try {
                Socket socket = server.accept();
                initializeConnection(socket);
            } catch (IOException e) {
                // TODO: probably too many open files exception occurred;
                // should we open server socket again or just wait ant ignore
                // this exception?
                throw new RuntimeException(e);
            }
        }
    }

    private void initializeConnection(Socket socket) {
        try {
            logger.info("Received connection from " + socket.getRemoteSocketAddress());
            socket.setSendBufferSize(256 * 1024);
            socket.setTcpNoDelay(true);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            int replicaId = input.readInt();

            if (replicaId < 0 || replicaId >= p.numReplicas) {
                logger.warning("Remoce host id is out of range: " + replicaId);
                socket.close();
                return;
            }
            if (replicaId == p.localId) {
                logger.warning("Remote replica has same id as local: " + replicaId);
                socket.close();
                return;
            }

            connections[replicaId].setConnection(socket, input, output);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Initialization of accepted connection failed.", e);
            try {
                socket.close();
            } catch (IOException e1) {
            }
        }
    }

    public void sendMessage(Message message, BitSet destinations) {
        assert !destinations.isEmpty() : "Sending a message to noone";

        byte[] bytes = message.toByteArray();

        // do not send message to us (just fire event)
        if (destinations.get(p.localId))
            fireReceiveMessage(message, p.localId);

        for (int i = destinations.nextSetBit(0); i >= 0; i = destinations.nextSetBit(i + 1))
            if (i != p.localId)
                send(bytes, i);

        // Not really sent, only queued for sending,
        // but it's good enough for the notification
        fireSentMessage(message, destinations);
    }

    public void sendMessage(Message message, int destination) {
        BitSet target = new BitSet();
        target.set(destination);
        sendMessage(message, target);
    }

    public void sendToAll(Message message) {
        BitSet all = new BitSet(p.numReplicas);
        all.set(0, p.numReplicas);
        sendMessage(message, all);
    }

    private final static Logger logger = Logger.getLogger(TcpNetwork.class.getCanonicalName());
}
