package eu.europa.ec.fisheries.uvms.tools;

public class TestHornetActiveMQ {




	public static void main(String[] args) {
		System.setProperty("jmsBridgePropFile","C:\\kit\\workspace\\FluxBridge\\uvms-arhs.properties");
		PushMessageFromSourceToTarget scheduler = new PushMessageFromSourceToTarget();
		scheduler.moveMessageFromSourceToTarget();
	}
/**	
	public static void main4(String[] args) {
		System.setProperty("jmxToMqBrokerPropFile","C:\\kit\\workspace\\FluxBridge\\HornetToActiveMQ.properties");		
		PushMessageFromHornetToJmxBean scheduler = new PushMessageFromHornetToJmxBean();
		scheduler.moveMessagesFromHornetToJMXMBean();
	}
	
	public static void main3(String[] args) {		
		System.setProperty("mqBrokerToJmxPropFile","C:\\kit\\workspace\\FluxBridge\\activeMQToHornet.properties");
		PushMessagesFromJMXToHornet scheduler = new PushMessagesFromJMXToHornet();
		scheduler.moveCurrentMessagesFromJMXMBeanToHornet();
	}
	**/
	
}
