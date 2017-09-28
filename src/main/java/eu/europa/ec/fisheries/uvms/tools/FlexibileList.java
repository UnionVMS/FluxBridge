package eu.europa.ec.fisheries.uvms.tools;

import java.util.ArrayList;
import java.util.List;

public class FlexibileList<Entity> extends ArrayList<Entity>{
	Class<Entity> entityClass;
	
	public FlexibileList(Class<Entity> myclass){
		this.entityClass = myclass;
	}
	
	
	public static void  main(String[] args){
		
		List<Bridge> bridges = new FlexibileList<Bridge>(Bridge.class);
		
		Bridge bridge = bridges.get(0);
		
		bridge = bridges.get(2);
		bridge = bridges.get(4);
		
	}
	
	
	
	public Entity get(int index){
		Entity entity = null;
		int end = this.size()-1;
		if (index>end){
			int diff = index - end;			
			
			for (int i=0;i<diff;i++){
				
				

				try {
					  entity = entityClass.newInstance();
					} 
					catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				add(entity);
			}
			
		}
		else{
			Object[] entities =  toArray();
			entity = (Entity) entities[index];
		}
		
		return entity;

	}
}
