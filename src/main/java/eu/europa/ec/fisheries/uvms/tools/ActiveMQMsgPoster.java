package eu.europa.ec.fisheries.uvms.tools;

import javax.jms.JMSException;

import eu.europa.ec.fisheries.uvms.tools.FluxBridge.Msg;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActiveMQMsgPoster {
	public static void  main(String[] args){
		BridgeDestination target = new BridgeDestination();
		target.setConnectionFactory("ConnectionFactory");
		target.setQueue("jms/queue/bridge");
		target.setInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		target.setUrl("tcp://localhost:61617?jms.rmIdFromConnectionId=true");
					
		Msg msg = new Msg(); 

		
		try {
			ActiveMQManager.postMsgToActiveMQ(target, msg);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

	}
}
