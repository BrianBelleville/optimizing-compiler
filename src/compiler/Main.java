package compiler;

import java.io.File;
import java.io.FileWriter;

import support.IdentifierTable;
import java.util.ArrayList;
import ir.Function;

public class Main {

    public static void main(String[] args) {
        try {
            String input = null, cfgOut = null;
            for(int i = 0; i < args.length; i++) {
                switch(args[i]) {
                case "-cfg":
                    // next item will be the filename for CFG output
                    i++;
                    cfgOut = args[i];
                    break;
                default:
                    // interpret as an input filename
                    input = args[i];
                    break;
                }
            }
            if(input == null) {
                throw new Exception("No input file provided");
            }
            File f = new File(input);
	    IdentifierTable t = new IdentifierTable();
            Parser parse = new Parser(f, t);
            ArrayList<Function> program = parse.parse();
            if(cfgOut != null) {
                outputCFG(cfgOut, program);
            }
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        }
    }

    public static void outputCFG(String filename, ArrayList<Function> program)
        throws Exception {
        FileWriter out = new FileWriter(filename);
        out.write("digraph Computation {\nnode [shape=box];\n");
        for(Function func : program) {
            func.printFunc(out);
        }
        out.write("}");
        out.close();
    }
}
