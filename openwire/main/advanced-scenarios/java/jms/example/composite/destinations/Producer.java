package jms.example.composite.destinations;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * @author <a href="http://www.christianposta.com/blog">Christian Posta</a>
 */
public class Producer {

	private static final String BROKER_URL = "tcp://localhost:61616";
	private static final Boolean NON_TRANSACTED = false;
	private static final int NUM_MESSAGES_TO_SEND = 100;
	private static final long DELAY = 100;

	public static void main(String[] args) {
		String url = BROKER_URL;
		if (args.length > 0) {
			url = args[0].trim();
		}
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "password", url);
		Connection connection = null;

		try {

			connection = connectionFactory.createConnection();
			connection.start();

			Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
			Destination destination = session
					.createQueue("test-queue,test-queue-foo,test-queue-bar,topic://test-topic-foo");
			MessageProducer producer = session.createProducer(destination);

			for (int i = 0; i < NUM_MESSAGES_TO_SEND; i++) {
				TextMessage message = session.createTextMessage("Message #" + i);
				System.out.println("Sending message #" + i);
				producer.send(message);
				Thread.sleep(DELAY);
			}

			producer.close();
			session.close();

		} catch (Exception e) {
			System.out.println("Caught exception!");
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
					System.out.println("Could not close an open connection...");
				}
			}
		}
	}

}