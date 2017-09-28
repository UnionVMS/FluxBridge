package eu.europa.ec.fisheries.uvms.tools;

import java.util.ArrayList;
import java.util.List;

public class BridgeConfiguration {
	private List<Bridge> bridges;

	public BridgeConfiguration(){
		this.bridges = new FlexibileList<Bridge>(Bridge.class);
	}

	public List<Bridge> getBridges(){
		return bridges;
	}
	
	
	public void setBridges(List<Bridge> bridges){
		this.bridges = bridges;
	}

}
