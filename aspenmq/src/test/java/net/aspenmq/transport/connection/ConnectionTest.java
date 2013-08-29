package net.aspenmq.transport.connection;

public class ConnectionTest {
    public static void main(String[] args) throws InterruptedException {
        AMQListener listener = new AMQListener();
        listener.start();

        try {
            AMQConnectionFactory cf = new AMQConnectionFactory();
            cf.initialize();
            AMQConnection connection = cf.createConnection("localhost");
            Thread.sleep(10000);
            connection.close();
            cf.shutdown();
            Thread.sleep(5000);
        } finally {
            listener.shutdown();
        }
    }
}
