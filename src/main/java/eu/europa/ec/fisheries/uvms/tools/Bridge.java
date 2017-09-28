package eu.europa.ec.fisheries.uvms.tools;

import java.util.List;

public class Bridge {
	private BridgeDestination source;
	private List<BridgeDestination> targets;
	
	public Bridge(){
		source = new BridgeDestination();
		targets = new FlexibileList<BridgeDestination>(BridgeDestination.class);
	}
	
	public void setSource(BridgeDestination source){
		this.source = source;
	}
	
	
	public void setTargets(List<BridgeDestination> targets){
		this.targets = targets;
	}
	
	public BridgeDestination getSource(){
		return this.source;
	}

	
	public List<BridgeDestination> getTargets(){
		return targets;
	}

}
