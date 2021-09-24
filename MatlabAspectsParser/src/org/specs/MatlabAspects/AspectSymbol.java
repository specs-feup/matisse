package org.specs.MatlabAspects;

public class AspectSymbol {
	public static int NONE = 0;
	public static int MULTI = 1;
	public static int HXW = 2;
	public static int DIM_STAR = -1;
	
	public boolean definer = false;
	public String name; // nome da variavel
	
	public String matlabType = ""; //se e' uint8 int32, etc.
	

	public int dimType = NONE; // int que indica se e' multi (2), HxW (1) ou nao tem nada (0)
	
	public int quantizerObject=-1;
	
	//so sao utilizados no caso de ser HxW:
	public int dimH =-2;
	public int dimW = -2;
	//
	
	public AspectSymbol(String name){
		this.name = name;
	}
	
	public AspectSymbol(String name, String matlabType, int dimType){
		this.name = name;
		this.matlabType = matlabType;
		this.dimType = dimType;
		this.definer = false;
	}
	
	public void setDefine(boolean def)
	{
		this.definer = def;
	}
	
	@Override
	public String toString(){
		
		String ret = "";
		if(definer ==  true){
			ret = "Definer: ";
		}
		ret+= name+"  ";
		if(dimType == MULTI){
			ret += "multi ";
		}
		else if(dimType == HXW){
			String aux1 = ""+dimH;
			String aux2 = ""+dimW;
			if(dimH == DIM_STAR){
				aux1 = "*";
			}
			if(dimW == DIM_STAR){
				aux2 = "*";
			}
			ret += aux1+"x"+aux2+" ";
		}
		ret += matlabType;
		if(matlabType.equals("fixed")||matlabType.equals("ufixed")){
			ret+= " props: "+quantizerObject;
		}
		
		return ret;
	}
	

	public static int getTypeValue(String type)
	{
		if(type.startsWith("int"))
		{
			type = type.substring(3);
			return Integer.parseInt(type);
		}
		else if(type.equals("double"))
			return 64;
		else if(type.equals("single") || type.equals("float"))
			return 32;
		else if(type.endsWith("fixed"))
			return 64;
		return -1;
	}
	
	
	
	public void copySymbol(AspectSymbol s)
	{
		this.matlabType = s.matlabType;
		this.dimType = s.dimType;
		this.quantizerObject = s.quantizerObject;
	}
}
