import java.util.*;

public class SemSym {
    private String type;
	private String name;
	private int scope;
	private String stuructType;
	private List<String> params;
	private HashMap<String, Sym> symList;

    
    public SemSym(String type) {
        this.type = type;
    }

    public SemSym(String type, List<String> params) {
		this.type = type;
		this.params = params;
	}	
    
    public String getType() {
        return type;
    }
    
    public String toString() {
        return type;
    }
	
	public List<String> getParams(){
		return this.params;
	}	

	public HashMap<String, Sym> getSymList(){
		return symList;
	}
}
