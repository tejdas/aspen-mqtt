package net.aspenmq.transport.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SAMQConnectionManager {
    private static final Map<String, AMQConnection> openConnections = new ConcurrentHashMap<>();

    static void registerConnection(String clientID, AMQConnection connection) {
        System.out.println("registered connection");
        openConnections.put(clientID, connection);
    }
}
