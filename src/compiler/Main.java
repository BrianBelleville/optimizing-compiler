package compiler;

import java.io.File;
import java.io.FileWriter;

import support.IdentifierTable;
import java.util.ArrayList;
import ir.Function;

public class Main {

    public static void main(String[] args) {
        try {
            File f = new File(args[0]);
	    IdentifierTable t = new IdentifierTable();
            Parser parse = new Parser(f, t);
            ArrayList<Function> program = parse.parse();
            System.out.println("Good parse?");
	    FileWriter out = new FileWriter("out.gv");
	    out.write("digraph Computation {\nnode [shape=box];\n");
	    for(Function func : program) {
		func.printFunc(out);
	    }
	    out.write("}");
	    out.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
