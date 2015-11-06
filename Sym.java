// This class defines a Sym
public class Sym{

	private String type = new String();

	// Initialize the Sym with the correct type
	public Sym(String type){
		this.type = type; 		
	}

	// Return this Sym's type
	public String getType(){
		return this.type;
	}

	// Return this Sym's type
	public String toString(){
		return this.type; 
	}

}
