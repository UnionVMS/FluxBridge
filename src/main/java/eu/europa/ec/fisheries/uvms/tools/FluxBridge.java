package eu.europa.ec.fisheries.uvms.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.beanutils.PropertyUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FluxBridge {

	public static class Msg {
		String body;
		Map<String,String> properties;
	}

		

	
	public static BridgeConfiguration getBridgeConfiguration(Properties connectionProp){
		BridgeConfiguration bridgeConfiguration = new BridgeConfiguration();
		
		for (Map.Entry<Object,Object> entry : connectionProp.entrySet()){
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			try {
				PropertyUtils.setProperty(bridgeConfiguration, key, value);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage());
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage());
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage());
			}
			// do something 
		}
		return bridgeConfiguration;
	}
	
	public static void main1(String[] args) {
		args = new String[1];
		args[0] = "C:\\kit\\workspace\\FluxBridge\\FAQuery.properties";
		Properties connectionProp = loadProperties(args[0]);
		try {
			moveMessagesFromHornetToJMXMBean(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
	}

	
	public static void main(String[] args) {
		args = new String[1];
		args[0] = "C:\\kit\\workspace\\FluxBridge\\bridge.properties";
		Properties connectionProp = loadProperties(args[0]);
		try {
			moveCurrentMessagesFromJMXMBeanToHornet(null,null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
	}


	public static List<Msg> consumeMessagesFromJMSBroker(BridgeDestination bridgeDestination) throws Exception {
		List<Msg> msgs = new ArrayList<Msg>();
		
		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		MessageConsumer consumer = null;
		Queue destination = null;
		Context context = null;
		
		try {
			String user = bridgeDestination.getUser();
			String password = bridgeDestination.getPassword();
			String connectionFactoryName = bridgeDestination.getConnectionFactory();
			String queueName = bridgeDestination.getQueue();
			String initialContentFactory =  bridgeDestination.getInitialContextFactory();
			String url = bridgeDestination.getUrl();

			final Properties env = new Properties();
			env.put(Context.INITIAL_CONTEXT_FACTORY, initialContentFactory);
			env.put(Context.PROVIDER_URL, url);
			
			env.put(Context.SECURITY_PRINCIPAL, user);
			env.put(Context.SECURITY_CREDENTIALS, password);
			
			context = new InitialContext(env);

			connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryName);

			

			log.debug("lookup: "+connectionFactoryName+" success!");

			destination = (Queue) context.lookup(queueName);

			log.debug("lookup: "+queueName+" success!");
 
			log.debug("connectionFactory.createContext success!");

			connection = connectionFactory.createConnection(user, password);

			log.debug("connectionFactory.createConnection success!");

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			consumer = session.createConsumer(destination);

			log.debug("session.createConsumer success!");

			connection.start();
			
			Message jmsMessage = null;
			while (null!=(jmsMessage = consumer.receive(1))){
				Msg msg = new Msg();
				Map<String,String> properties = new HashMap<String,String>();				
				Enumeration<String> msgProperties = jmsMessage.getPropertyNames();
				while (msgProperties.hasMoreElements()){
					String propertyName = msgProperties.nextElement();
					properties.put(propertyName, jmsMessage.getStringProperty(propertyName));
				}
				
				msg.body = ((TextMessage) jmsMessage).getText();
				msg.properties = properties;
				msgs.add(msg);
			}
			
			
			
			log.debug("Working!");

		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			
			if (consumer!=null){
				consumer.close();				
			}

			if (session!=null){
				session.close();				
			}
			
			
			if (context != null) {
				context.close();
			}

			
			if (connection != null) {
				connection.stop();				
				connection.close();
				
			}

			// closing the connection takes care of the session, producer, and
			// consumer
			
		}
		
		return msgs;
		
		
	}


	public static void moveMessagesFromHornetToJMXMBean(BridgeDestination bridgeDestination) throws Exception {
		List<Msg> msgs = consumeMessagesFromJMSBroker(bridgeDestination);
		for (Msg msg:msgs){
			pushMessageToJMXMBean(bridgeDestination, msg);			
		}
	}

	

	
	public static JMXConnector getConnector(BridgeDestination bridgeDestination) throws IOException{
		String jmxUser = bridgeDestination.getUser();
		String jmxPassword = bridgeDestination.getPassword();
		
		String jmxURL = bridgeDestination.getUrl();
		
		JMXServiceURL url = new JMXServiceURL(jmxURL); 

		
		Hashtable env = new Hashtable();
		String[] credentials = new String[] { jmxUser, jmxPassword};
	    env.put("jmx.remote.credentials", credentials);
	        
		JMXConnector jmxc = JMXConnectorFactory.connect(url,env);
		
		return jmxc;
	}

	
	public static ObjectName createJMXQueueMBean(BridgeDestination bridgeDestination,MBeanServerConnection connection,ObjectName broker) throws Exception {	
		
		String queueName = bridgeDestination.getQueue();
		
		Object[] params = {queueName};
		String[] sig = {"java.lang.String"};
		connection.invoke(broker, "addQueue", params, sig);		    
	
		ObjectName queue = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName="+queueName);
		
		return queue;
	}	
	
	public static void pushMessageToJMXMBean(BridgeDestination bridgeDestination,Msg msg) throws Exception {
		JMXConnector jmxc = getConnector(bridgeDestination);
		
		MBeanServerConnection connection = jmxc.getMBeanServerConnection();		
		ObjectName broker = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost");

		ObjectName queue =  createJMXQueueMBean(bridgeDestination,connection,broker);
		
		Object[] msgParams = {msg.properties,msg.body};
		String[] msgSig = {"java.util.Map","java.lang.String"};		
		connection.invoke(queue, "sendTextMessage", msgParams, msgSig);		    

		jmxc.close();
		
	}

	

	public static void moveCurrentMessagesFromJMXMBeanToHornet(BridgeDestination sourceDestination,BridgeDestination targetDestination) throws Exception {
		JMXConnector jmxc = getConnector(sourceDestination);
		
		
		MBeanServerConnection connection = jmxc.getMBeanServerConnection();
		
		ObjectName broker = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost");

		ObjectName queue =  createJMXQueueMBean(sourceDestination,connection,broker);

		
		Object[] msgParams = {};
		String[] msgSig = {};
		
		CompositeData[]  compositeDataElements =  (CompositeData[]) connection.invoke(queue, "browse", msgParams, msgSig);		    
		
		for (CompositeData compositeData:compositeDataElements){
			Msg msg = new Msg();
			String msgId = (String) compositeData.get("JMSMessageID");
			msg.body =  (String) compositeData.get("Text");
			TabularDataSupport stringProperties = (TabularDataSupport)compositeData.get("StringProperties");			
			msg.properties = new HashMap<String,String>();
			
			for (Map.Entry<Object, Object> entry:stringProperties.entrySet()){
				CompositeData  compositeDataEntry= (CompositeData)entry.getValue();				
				String key = String.valueOf(compositeDataEntry.get("key"));
				String value = String.valueOf(compositeDataEntry.get("value"));		
				msg.properties.put(key, value);
			}

			removeMessageFromJMXMBean(sourceDestination,msgId);			
			postMesssageToJMSBroker(targetDestination,msg);
		}
		
		jmxc.close();		
	}
	
	
	
	public static void removeMessageFromJMXMBean(BridgeDestination bridgeDestination, String msgId) throws Exception {
	    
		JMXConnector jmxc = getConnector(bridgeDestination);

		String jmxQueue = bridgeDestination.getQueue();
		MBeanServerConnection connection = jmxc.getMBeanServerConnection();
		

		ObjectName queue = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName="+jmxQueue);
		
		Object[] params = {msgId};
		String[] sig = {"java.lang.String"};
		
		connection.invoke(queue, "removeMessage", params, sig);		    
		
		
		jmxc.close();
		
	}
	
	

		
	public static void postMesssageToJMSBroker(BridgeDestination bridgeDestination, Msg msg) throws NamingException, JMSException {

		ConnectionFactory connectionFactory = null;
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		Queue destination = null;
		TextMessage message = null;
		Context context = null;
		
		try {
			String user = bridgeDestination.getUser();
			String password = bridgeDestination.getPassword();
			String connectionFactoryName = bridgeDestination.getConnectionFactory();
			String queueName = bridgeDestination.getQueue();
			String url = bridgeDestination.getUrl();
			String initialContextFactory = bridgeDestination.getInitialContextFactory();
			
			final Properties env = new Properties();
			env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
			env.put(Context.PROVIDER_URL, url);
			
			env.put(Context.SECURITY_PRINCIPAL, user);
			env.put(Context.SECURITY_CREDENTIALS, password);
			
			context = new InitialContext(env);

			connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryName);

			log.debug("lookup: "+connectionFactoryName+" success!");

			destination = (Queue) context.lookup(queueName);

			log.debug("lookup: "+queueName+" success!");

			log.debug("connectionFactory.createContext success!");

			connection = connectionFactory.createConnection(user, password);

			log.debug("connectionFactory.createConnection success!");

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(destination);

			log.debug("session.createProducer success!");

			connection.start();
			
			message = session.createTextMessage(msg.body);
			
			for (Map.Entry<String, String> entry:msg.properties.entrySet()){
				message.setStringProperty(entry.getKey(), entry.getValue());				
			}
			
			producer.send(message);
			log.debug("Working!");
			producer.close();

		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			
			if (context!=null){
				context.close();
			}

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
	
	
	
	
	
	public static Properties loadProperties(String path){		
		Properties prop = new Properties();
		InputStream input = null;

		try {
			log.debug(path);
			input = new FileInputStream(path);

			// load a properties file
			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}
	
	
	
	
	
	
}
