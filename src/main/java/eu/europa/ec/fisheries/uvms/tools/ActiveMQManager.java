package eu.europa.ec.fisheries.uvms.tools;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTextMessage;

import eu.europa.ec.fisheries.uvms.tools.FluxBridge.Msg;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ActiveMQManager {
	
	private static String nodeNamePrefix = " nodeName=\"";
	
	
	private static String getNodeName(Msg msg){
		String nodeName = null;
		if (msg.body!=null){
			int index = msg.body.indexOf(nodeNamePrefix);
			if (index!=-1){
				String rest = msg.body.substring(index+nodeNamePrefix.length());
				index = rest.indexOf("\"");
				if (index!=-1){
					nodeName = rest.substring(0,index);				
				}				
			}
		}		
		return nodeName;
	}
	
	
	public static void bridgeJMSQueues(Bridge bridge) throws Exception {
		List<Msg> msgs = null;
		
		if (bridge.getSource()!=null && bridge.getTargets()!=null){
			if ((BridgeDestination.ACTIVEMQ).equals(bridge.getSource().getType()))
				msgs = consumeActiveMqMessages(bridge.getSource());			
			else
				msgs = FluxBridge.consumeMessagesFromJMSBroker(bridge.getSource());
				
	        
			log.info("Fetched messages from source "+msgs.size());
			
			for (BridgeDestination target:bridge.getTargets()){
				for (Msg msg:msgs){
//						String nodeName = getNodeName(msg);
					    String nodeName = msg.properties.get("CT");
						if (nodeName==null || nodeName.equals(target.getNodeName())){
							if ((BridgeDestination.ACTIVEMQ).equals(target.getType()))			
								postMsgToActiveMQ(target, msg);			
							else
								FluxBridge.postMesssageToJMSBroker(target, msg);										
						}
						
							
				}
			}
		}
	}
	
	
	public static List<String> getActiveMQmsgList(BridgeDestination bridgeDestination) throws JMSException {
		String url= bridgeDestination.getUrl();
		String queueName  = bridgeDestination.getQueue();
		Enumeration msgList = null;
		List<String> idList = new ArrayList<String>();
		
		log.info("Consuming activeMQ messages from "+queueName);
		
		
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		
		ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();

		Session session = null;
		
		try{
		    connection.start();
			
			DestinationSource ds = connection.getDestinationSource();
			Set<ActiveMQQueue> queues = ds.getQueues();
	        log.debug("Number of queus found : "+queues.size()+" Queues");
	
			for(ActiveMQQueue queue : queues){
			    try {
			        log.debug("Queue "+queue.getQueueName());
			    } catch (JMSException e) {
			        e.printStackTrace();
			    }
			}
			
			session = connection.createSession(false,
			Session.AUTO_ACKNOWLEDGE);
	
			
			Queue destination = session.createQueue(queueName);
	
			// MessageConsumer is used for receiving (consuming) messages
			
		    QueueBrowser queueBrowser = session.createBrowser(destination);
		     
		     
		    msgList  = queueBrowser.getEnumeration();

		    
			while(msgList.hasMoreElements()) {
			    TextMessage textMessage = (TextMessage)msgList.nextElement();
			    idList.add(textMessage.getJMSMessageID());
			}
		    
		    
	    
		}
		catch(Exception ex){
			log.error(ex.getMessage());
		}
		finally{
            
            if(session != null) {
            	session.close(); 
            }            
            
            if (connection != null) {
            	connection.stop();
            	connection.close();
            }			
		}
	    
	    // Here we receive the message.
		// By default this call is blocking, which means it will wait
		// for a message to arrive on the queue.
	    return idList;
	}
	
	
	public static List<Msg> consumeActiveMqMessages(BridgeDestination bridgeDestination) throws JMSException {
		// Getting JMS connection from the server
		String url= bridgeDestination.getUrl();
		String queueName  = bridgeDestination.getQueue();

		List<Msg> msgs = new ArrayList<Msg>();

		List<String> msgList = getActiveMQmsgList(bridgeDestination); 

		log.info("Consuming activeMQ "+queueName+" messages .Is there a msg in queue? "+ (!msgList.isEmpty()));
		
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		
		ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();

		Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
		
		Queue destination = session.createQueue(queueName);
		connection.start();			

		for (String id : msgList) {
			    MessageConsumer consumer = session.createConsumer(destination, "JMSMessageID='" +  id + "'");
			    //You can try starting the connection outside while loop as well, I think I started it inside while loop by mistake, but since this code worked I am hence letting you know what worked  
			    ActiveMQTextMessage message = (ActiveMQTextMessage) consumer.receive() ;
			    if ( message != null ) {

					Msg msg = new Msg();
					Map<String,String> properties = new HashMap<String,String>();				
					Enumeration<String> msgProperties = message.getPropertyNames();
					while (msgProperties.hasMoreElements()){
						String propertyName = msgProperties.nextElement();
						properties.put(propertyName, message.getStringProperty(propertyName));
					}
					
					msg.properties = properties;
					msg.body = message.getText();
					msgs.add(msg);
			    	
			    }
			    
			    if (consumer!=null)
			    	consumer.close();
		}

		if (session!=null){
	        session.close();			
		}

		if (connection!=null){
			connection.stop();
	        connection.close();			
		}
		log.info("Consuming activeMQ messages");		
		return msgs;
		}
	
	
	public static void postMsgToActiveMQ(BridgeDestination bridgeDestination,Msg msg) throws JMSException {
		// Getting JMS connection from the server and starting it
		
		String url  = bridgeDestination.getUrl();
		String queueName= bridgeDestination.getQueue();
		MessageProducer producer = null;
		Connection connection = null;
		Session session = null;
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
		
		try{
			connection = connectionFactory.createConnection();
			connection.start();
			
			session = connection.createSession(false,
			Session.AUTO_ACKNOWLEDGE);
			// Destination represents here our queue 'VALLYSOFTQ' on the
			// JMS server. You don't have to do anything special on the
			// server to create it, it will be created automatically.
			Destination destination = session.createQueue(queueName);
			// MessageProducer is used for sending messages (as opposed
			// to MessageConsumer which is used for receiving them)
			producer = session.createProducer(destination);
			// We will send a small text message saying 'Hello' in Japanese
			
			TextMessage message = session.createTextMessage(msg.body);
			
			for (Map.Entry<String, String> entry:msg.properties.entrySet()){
				message.setStringProperty(entry.getKey(), entry.getValue());				
			}		
			
			// Here we are sending the message!
			producer.send(message);
			log.info("Sending '" + message.getText() + "'");

		}
		catch(JMSException jmsException){
			log.error(jmsException.getMessage());
		}
		finally{
            if(producer != null) {
            	producer.close(); 
            }
            
            if(session != null) {
                session.close(); 
            }            
            
            if (connection != null) {
            	connection.stop();
            	connection.close();
            }			
		}

	
	
	
	
	}
	
	
	
}
