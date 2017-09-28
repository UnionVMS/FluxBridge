package eu.europa.ec.fisheries.uvms.tools;

public class BridgeDestination {
		public static String ACTIVEMQ = "activemq";
		public static String HORNET = "hornet";
		
		private String url;
		private String queue;
		private String connectionFactory;
		private String user;
		private String password;
		private String initialContextFactory;	
		private String nodeName;	
		
		
		public void setConnectionFactory(String connectionFactory){
			this.connectionFactory = connectionFactory;
		}
		
		public String getType(){
			if ("org.apache.activemq.jndi.ActiveMQInitialContextFactory".equals(initialContextFactory))				
				return ACTIVEMQ;
			else if ("org.jboss.naming.remote.client.InitialContextFactory".equals(initialContextFactory))				
				return HORNET;
			else
				return null;

		}
		
		public String getUrl(){
			return this.url;
		}
		
		public String getQueue(){
			return queue;
		}
		
		public String getConnectionFactory(){
			return connectionFactory;
		}
		
		public String getUser(){
			return this.user;
		}
		
		public String getPassword(){
			return this.password;
		}
		
		public String getInitialContextFactory(){
			return this.initialContextFactory;
		}
		
		public void setPassword(String password){
			this.password = password;
		}
		
		public void setQueue(String queue){
			this.queue = queue;
		}
		
		
		public void setUser(String user){
			this.user = user;
		}
		
		public void setInitialContextFactory(String initialContextFactory){
			this.initialContextFactory = initialContextFactory;
		}
		
		
		public void setUrl(String url){
			this.url = url;
		}
		
		
		public void setNodeName(String nodeName){
			this.nodeName = nodeName;
		}

		public String getNodeName(){
			return this.nodeName;
		}

}
