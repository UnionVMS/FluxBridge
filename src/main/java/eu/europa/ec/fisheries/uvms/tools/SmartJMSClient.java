package eu.europa.ec.fisheries.uvms.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class SmartJMSClient
{
  public SmartJMSClient() {
	  
  }
  
  public static Properties loadProperties(String path)
  {
    Properties prop = new Properties();
    InputStream input = null;
    
    try
    {
      input = new FileInputStream(path);
      

      prop.load(input);
    }
    catch (IOException ex) {
      ex.printStackTrace();
      
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    finally
    {
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
  
  public static String readPayloadFile(String path) {
    StringBuffer sb = new StringBuffer();
    try {
      File fileDir = new File(path);
      
      BufferedReader in = new BufferedReader(
        new InputStreamReader(
        new FileInputStream(fileDir), "UTF8"));
      
      String str;
      
      while ((str = in.readLine()) != null) { 
        sb.append(str);
      }
      
      in.close();
    }
    catch (UnsupportedEncodingException e)
    {
      System.out.println(e.getMessage());
    }
    catch (IOException e)
    {
      System.out.println(e.getMessage());
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
    }
    return sb.toString();
  }
  
  public static void main(String[] args) throws NamingException, JMSException
  {
    postMesssage(args[0], args[1], args[2]);
  }
  
  
  public static void main2(String[] args) throws NamingException, JMSException
  {
    postMesssage("C:\\kit\\jmsclient\\connection.properties", "C:\\kit\\jmsclient\\msg.properties", "C:\\kit\\jmsclient\\payload.xml");
  }

	  
  

  public static void postMesssage(String connectionPropFile, String msgPropertiesFile, String payloadFile) throws NamingException, JMSException
  {
	  Properties connectionProp = loadProperties(connectionPropFile);
	  Properties msgProp = loadProperties(msgPropertiesFile);
//	  for (int i=0;i<10000;i++)
		  postMesssage(connectionProp, msgProp,payloadFile);
  }
  
  
  public static void postMesssage(Properties connectionProp ,Properties msgProp,String payloadFile) throws NamingException, JMSException{

  {
    ConnectionFactory connectionFactory = null;
    Connection connection = null;
    Session session = null;
    MessageProducer producer = null;
    Queue destination = null;
    TextMessage message = null;
    Context context = null;
    try
    {
      String user = connectionProp.getProperty("user");
      String password = connectionProp.getProperty("password");
      String connectionFactoryName = connectionProp.getProperty("connectionFactory");
      String queueName = connectionProp.getProperty("queue");
      Properties env = new Properties();
      env.put("java.naming.factory.initial", connectionProp.getProperty("initialContextFactory"));
      env.put("java.naming.provider.url", connectionProp.getProperty("url"));
      
      env.put("java.naming.security.principal", user);
      env.put("java.naming.security.credentials", password);
      
      context = new InitialContext(env);
      
      connectionFactory = (ConnectionFactory)context.lookup(connectionFactoryName);
      
      System.out.println("lookup: " + connectionFactoryName + " success!");
      
      destination = (Queue)context.lookup(queueName);
      
      System.out.println("lookup: " + queueName + " success!");
      
//      JMSContext createContext = connectionFactory.createContext(user, password, 
 //       1);
      
      System.out.println("connectionFactory.createContext success!");
      
      connection = connectionFactory.createConnection(user, password);
      
      System.out.println("connectionFactory.createConnection success!");
      
      session = connection.createSession(false, 1);
      producer = session.createProducer(destination);
      
      System.out.println("session.createProducer success!");
      
      connection.start();
      
      String payload = readPayloadFile(payloadFile);
      

      message = session.createTextMessage(payload);
      
      for (Map.Entry<Object, Object> entry : msgProp.entrySet()) {
        message.setStringProperty((String)entry.getKey(), (String)entry.getValue());
      }
      
      producer.send(message);
      System.out.println("Working!");
      producer.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally {
      session.close();
      if (connection != null) {
        connection.close();
      }
      

      if (context != null) {
        context.close();
      }
    }
  }
  }
}