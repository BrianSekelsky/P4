import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Mini program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }

    public void nameAnalyze(SymTable t){
	// t.addScope();	
    }    

}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    public void nameAnalyze(){
	SymTable t = new SymTable();
	myDeclList.nameAnalyze(t);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public void nameAnalyze(SymTable t){
	nameAnalyze(t, t);	
    }

    public void nameAnalyze(SymTable t, SymTable t2){
	for(DeclNode d: myDecls){
	    d.nameAnalyze(t, t2);
	}
    }

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    public void nameAnalyze(SymTable t){
	myDeclList.nameAnalyze(t);
	myStmtList.nameAnalyze(t);
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
}

    public void nameAnalyze(SymTable t){
	for(StmtNode s : myStmts){
	    s.nameAnalyze(t);
	}
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    public void nameAnalyze(SymTable t){
	for(ExpNode e : myExps){
	    e.nameAnalyze(t);
        }
    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {

    public void nameAnalyze(SymTable t, SymTable t2){}
    public void nameAnalyze(SymTable t){}

}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
	
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.println(";");
    }

    public void nameAnalyze(SymTable t, SymTable tGlobal){
	
	boolean works = false;
	String type = myType.getType();
	SemSym s = new SemSym(myType.getType());
	IdNode myIdNode = null;
	SemSym symbol = null;	
	IdNode id = null;
	//String type = myType.getId(); 
	//kkkkkk
	//
	// int bool string struct
	// if(int/bool/string){
	// myId.nameAnalyze_DeclVar
	// }else{
	// myId.nameAnalyze_DeclStruct
        // }


	if(type.equals("void")){
	    System.out.println("Bad decleration (variable of type void)");	    
	    works = false;
	}
	else if(type.equals("struct")){
	    id = ((StructNode)myType).getId();
	    symbol = tGlobal.lookupGlobal(id.toString());       
	    	    
	    if(symbol == null){
		System.out.println("Bad declaration" 
			+ "(attempt to declare variable of a bad struct type)");
		works = false;
	    }	
	
	}
	else {
	    works = true;
	}
	if(works){
	    if (type.equals("struct")) {
                //s = (SemSym)id;
            }
            else {
                //s = new Sym(myType.getType());
            }
	    // t.addDecl(myId.toString(), (SemSym(s)));
	    // myId.link(s);
	}

    }
    public void nameAnalyze(SymTable t){
	nameAnalyze(t,t);

    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    public void nameAnalyze(SymTable t, SymTable t2){
    }
    public void nameAnalyze(SymTable t){
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        myId.unparse(p, 0);
    }

    public void nameAnalyze(SymTable t, SymTable t2){
    }
    public void nameAnalyze(SymTable t){
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
		myId.unparse(p, 0);
		p.println("{");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("};\n");

    }

    public void nameAnalyze(SymTable t, SymTable t2){
    }
    public void nameAnalyze(SymTable t){
    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
	abstract public String getType(); 
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }

    public String getType(){
	return "int";
    }

}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }

    public String getType(){
	return "bool";
    }

}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }

    public String getType(){
	return "void";
    }

}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
	myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
	myId.unparse(p, 0);
    }

    public String getType(){
	return "struct";
    }

    public IdNode getId(){
	return myId;
    }
	
	// 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {

    abstract public void nameAnalyze(SymTable t);

}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    public void nameAnalyze(SymTable t){
	myAssign.nameAnalyze(t);
    }

    // 1 kid
    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    public void nameAnalyze(SymTable t){
	myExp.nameAnalyze(t);
    }

    // 1 kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    public void nameAnalyze(SymTable t){
	myExp.nameAnalyze(t);
    }

    // 1 kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    public void nameAnalyze(SymTable t){
	myExp.nameAnalyze(t);
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    public void nameAnalyze(SymTable t){
	myExp.nameAnalyze(t);
    }   

    // 1 kid
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    public void nameAnalyze(SymTable t){
	myDeclList.nameAnalyze(t);
	myExp.nameAnalyze(t);
	myStmtList.nameAnalyze(t);
    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");        
    }

    public void nameAnalyze(SymTable t){
	myExp.nameAnalyze(t);
        myThenDeclList.nameAnalyze(t);
        myThenStmtList.nameAnalyze(t);
        myElseDeclList.nameAnalyze(t);
        myElseStmtList.nameAnalyze(t);
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
	
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    public void nameAnalyze(SymTable t){
	myExp.nameAnalyze(t);
	myDeclList.nameAnalyze(t);
	myStmtList.nameAnalyze(t);	
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    public void nameAnalyze(SymTable t){
	myCall.nameAnalyze(t);
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    public void nameAnalyze(SymTable t){
	myExp.nameAnalyze(t);
    }

    // 1 kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {

    abstract public void nameAnalyze(SymTable t);

}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    public void nameAnalyze(SymTable t){}
   
    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }
 
    public void nameAnalyze(SymTable t){}

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }
 
    public void nameAnalyze(SymTable t){}

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    public void nameAnalyze(SymTable t){}

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    public void link(Sym s){
	mySym = s;
    }


    public Sym getSym(){
	return mySym;
    }

    public String getStrVal(){
	return myStrVal;
    }

    public int getLineNum(){
	return myLineNum;
    }

    public int getCharNum(){
	return myCharNum;
    }

    public void nameAnalyze(SymTable t){
	SemSym s = t.lookupGlobal(myStrVal);

	if(s == null){
	    ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
	}
	else {
	    idType = s.getType();      
        }
    }

    public void nameAnalyze_DeclFunc(SymTable t, List<String> params, String type){

    }

    public void nameAnalyze_DeclVar(SymTable t, String type){

    }

    public void nameAnalyze_DeclStruct(SymTable t, HashMap<String, Sym> symList){
	
    }

    private Sym mySym;
    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private String idType;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;	
        myId = id;
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myLoc.unparse(p, 0);
		p.print(").");
		myId.unparse(p, 0);
    }
    public void nameAnalyze(SymTable t){
	
		
	
    }
    // 2 kids
    private ExpNode myLoc;	
    private IdNode myId;
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
		if (indent != -1)  p.print("(");
	    myLhs.unparse(p, 0);
		p.print(" = ");
		myExp.unparse(p, 0);
		if (indent != -1)  p.print(")");
    }
 
    public void nameAnalyze(SymTable t){
	myLhs.nameAnalyze(t);
	myExp.nameAnalyze(t);
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }
 

    public void nameAnalyze(SymTable t){
	myId.nameAnalyze(t);
	myExpList.nameAnalyze(t);
    }

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
	    myId.unparse(p, 0);
		p.print("(");
		if (myExpList != null) {
			myExpList.unparse(p, 0);
		}
		p.print(")");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }
    
    public void nameAnalyze(SymTable t){
	myExp.nameAnalyze(t);
    }
    
    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }
    
    public void nameAnalyze(SymTable t){
	myExp1.nameAnalyze(t);
	myExp2.nameAnalyze(t);
    }
    
    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }
    public void unparse(PrintWriter p, int indent) {
	    p.print("(-");
		myExp.unparse(p, 0);
		p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }
    public void unparse(PrintWriter p, int indent) {
	    p.print("(!");
		myExp.unparse(p, 0);
		p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" + ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" - ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" * ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" / ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" && ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" || ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" == ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" != ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" < ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" > ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" <= ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
	    p.print("(");
		myExp1.unparse(p, 0);
		p.print(" >= ");
		myExp2.unparse(p, 0);
		p.print(")");
    }
}
