package org.specs.MatlabAspects;
import java.util.ArrayList;



public class Quantizer {
	public ArrayList<Pair> properties;
	public int index;
	
	public int S=0, WL=0, FL=0;
	public Quantizer(int index){
		this.index = index;
		properties = new ArrayList<>();
	}
	
	@Override
	public String toString(){
		String ret = "Q"+index+": "+S+","+WL+","+FL;
		for(Pair p: properties){
			ret+=" "+p.toString();
		}
		return ret;
	}
	
	public void add(String prop, String value){
		properties.add(new Pair(prop, value));
	}
	
	public void set(int WL, int FL){
		this.WL = WL;
		this.FL = FL;
		add("format", "["+WL+" "+FL+"]");
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Quantizer q = (Quantizer) obj;
		return this.properties.containsAll(q.properties) && this.properties.size()==q.properties.size()
		&& (this.S == q.S) && (this.WL == q.WL) && (this.FL == q.FL);
	}
}
