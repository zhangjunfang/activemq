package openwire.example;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.*;

class Listener {

	public static void main(String[] args) throws JMSException {

		String user = env("ACTIVEMQ_USER", "admin");
		String password = env("ACTIVEMQ_PASSWORD", "password");
		String host = env("ACTIVEMQ_HOST", "localhost");
		int port = Integer.parseInt(env("ACTIVEMQ_PORT", "61616"));
		String destination = arg(args, 0, "event");

		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);

		Connection connection = factory.createConnection(user, password);
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination dest = new ActiveMQTopic(destination);

		MessageConsumer consumer = session.createConsumer(dest);
		long start = System.currentTimeMillis();
		long count = 1;
		System.out.println("Waiting for messages...");
		while (true) {
			Message msg = consumer.receive();
			if (msg instanceof TextMessage) {
				String body = ((TextMessage) msg).getText();
				if ("SHUTDOWN".equals(body)) {
					long diff = System.currentTimeMillis() - start;
					System.out.println(String.format("Received %d in %.2f seconds", count, (1.0 * diff / 1000.0)));
					break;
				} else {
					if (count != msg.getIntProperty("id")) {
						System.out.println("mismatch: " + count + "!=" + msg.getIntProperty("id"));
					}
					count = msg.getIntProperty("id");

					if (count == 0) {
						start = System.currentTimeMillis();
					}
					if (count % 1000 == 0) {
						System.out.println(String.format("Received %d messages.", count));
					}
					count++;
				}

			} else {
				System.out.println("Unexpected message type: " + msg.getClass());
			}
		}
		connection.close();
	}

	private static String env(String key, String defaultValue) {
		String rc = System.getenv(key);
		if (rc == null)
			return defaultValue;
		return rc;
	}

	private static String arg(String[] args, int index, String defaultValue) {
		if (index < args.length)
			return args[index];
		else
			return defaultValue;
	}
}