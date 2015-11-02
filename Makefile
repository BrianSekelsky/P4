###
# This Makefile can be used to make a parser for the CFlat language
# (parser.class) and to make a program (P3.class) that tests the parser and
# the unparse methods in ast.java.
#
# make clean removes all generated files.
#
###

JC = javac
CP = ~cs536-1/public/tools/deps_src/java-cup-11b.jar:~cs536-1/public/tools/deps_src/java-cup-11b-runtime.jar:~cs536-1/public/tools/deps:.
CP2 = ~cs536-1/public/tools/deps:.

P4.class: P4.java parser.class Yylex.class ASTnode.class
	$(JC)    P4.java

parser.class: parser.java ASTnode.class Yylex.class ErrMsg.class
	$(JC)      parser.java

parser.java: CFlat.cup
	java   java_cup.Main < CFlat.cup

Yylex.class: CFlat.jlex.java sym.class ErrMsg.class
	$(JC)   CFlat.jlex.java

ASTnode.class: ast.java
	$(JC)  ast.java

CFlat.jlex.java: CFlat.jlex sym.class
	java    JLex.Main CFlat.jlex

sym.class: sym.java
	$(JC)    sym.java

sym.java: CFlat.cup
	java    java_cup.Main < CFlat.cup

ErrMsg.class: ErrMsg.java
	$(JC) ErrMsg.java

##test
test:
	java   P4 test.cf test.out

###
# clean
###
clean:
	rm -f *~ *.class parser.java CFlat.jlex.java sym.java
