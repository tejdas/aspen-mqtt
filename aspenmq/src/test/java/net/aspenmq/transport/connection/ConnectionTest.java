package net.aspenmq.transport.connection;

public class ConnectionTest {
    public static void main(String[] args) throws InterruptedException {
        AMQListener listener = new AMQListener();
        listener.start();

        AMQConnectionFactory cf = new AMQConnectionFactory();
        cf.initialize();
        AMQConnection connection = cf.createConnection("localhost");
        Thread.sleep(5000);
        connection.close();
        cf.shutdown();
        Thread.sleep(5000);
        listener.shutdown();
    }
}
