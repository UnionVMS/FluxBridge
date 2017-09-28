package eu.europa.ec.fisheries.uvms.tools;

import java.util.List;
import java.util.Properties;

import javax.ejb.Schedule;
import javax.ejb.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class PushMessageFromSourceToTarget {
	 
// typically it is the setting of the bridge	 

	 @Schedule(second="10,50", minute="*", hour="*")
	 public void moveMessageFromSourceToTarget() {
		 
			String propFile =  System.getProperty("jmsBridgePropFile");
			if (propFile!=null){

				Properties connectionProp = FluxBridge.loadProperties(propFile);
				BridgeConfiguration bridgeConfiguration = FluxBridge.getBridgeConfiguration(connectionProp);
				List<Bridge> bridges =  bridgeConfiguration.getBridges();
				
				for (Bridge bridge:bridges){
					try {	
						ActiveMQManager.bridgeJMSQueues(bridge);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else{
				log.debug("Could not find properties file");
			}
	 }


	 
}
