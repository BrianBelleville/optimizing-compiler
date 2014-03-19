package compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import transform.*;
import support.IdentifierTable;
import java.util.ArrayList;
import ir.Function;

public class Main {

    public static void main(String[] args) {
        try {
            String input = null, cfgOut = null, igOut = null, layOut = null, codeOut = "a.out";
            for(int i = 0; i < args.length; i++) {
                switch(args[i]) {
                case "-cfg":
                    // next item will be the filename for CFG output
                    i++;
                    cfgOut = args[i];
                    break;
                case "-ig":
                    // next item will be the filename for the
                    // interference graph output
                    i++;
                    igOut = args[i];
                    break;
                case "-o":
                    // next item will be the filename of the compiler
                    // output
                    i++;
                    codeOut = args[i];
                    break;
                case "-layout":
                    // next item will be the filename of the layout
                    // output
                    i++;
                    layOut = args[i];
                    break;
                case "-no-cse":
                    Globals.performCSE = false;
                    break;
                case "-cse":
                    Globals.performCSE = true;
                    break;
                case "-dead-code-elim":
                    Globals.performDeadCodeElimination = true;
                    break;
                case "-no-dead-code-elim":
                    Globals.performDeadCodeElimination = false;
                    break;
                case "-zero-initialize-variables":
                    Globals.zeroInitializeVariables = true;
                    break;
                case "-no-zero-initialize-variables":
                    Globals.zeroInitializeVariables = false;
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

            // create list of passes based on program arguments
            ArrayList<Pass> passes = new ArrayList<Pass>();
            passes.add(new StripVariableReference());

            passes.add(new DeleteMove());
            passes.add(new UpdateArguments());

            passes.add(new ConstantArithmetic());

            passes.add(new GenerateArrayIndexingComputations());
            passes.add(new UpdateArguments());

            if(Globals.performCSE) {
                passes.add(new CommonSubexpressionElimination());
                passes.add(new UpdateArguments());
            }
            // the CSE pass will make the dominator information change
            // as instructions are deleted, so this pass will clear
            // that information. Even if CSE is not performed it is
            // good to do this to make the compilation options as
            // consistent as possible. This just allows bugs to be
            // caught more easily if subsequent passes make use of the
            // dominator information.
            passes.add(new RemoveKill());
            passes.add(new InvalidateDominatorInformation());
            passes.add(new ConstructUseChain());

            // This depends on having the use information to determine
            // what is dead code
            if(Globals.performDeadCodeElimination) {
                passes.add(new DeadCodeElimination());
                passes.add(new RemoveDeletedInstructions());
            }

            File f = new File(input);
            IdentifierTable t = new IdentifierTable();
            Parser parse = new Parser(f, t);
            ArrayList<Function> program = parse.parse();
            for(Pass p : passes) {
                for(Function func : program) {
                    func.entryPoint.runPass(p);
                }
            }

            if(cfgOut != null) {
                outputCFG(cfgOut, program);
            }

            // determine the interference graph for all functions
            for(Function func : program) {
                func.ig = func.entryPoint.calcLiveRange();
                func.ig.colorGraph();
                func.entryPoint.makeMovesForPhis();
            }

            if(igOut != null) {
                outputIG(igOut, program);
            }

            if(layOut != null) {
                outputLayout(layOut, program);
            }

            CodeGenerator codeGen = new DLXGenerator();
            int[] code = codeGen.generate(program);
            byte[] codeBytes = new byte[code.length * 4];
            int i = 0;
            for(int word : code) {
                for(int byteNum = 0; byteNum < 4; byteNum++) {
                    codeBytes[i] = (byte)((word >> (byteNum * 8)) & 0xFF);
                    i++;
                }
            }
            FileOutputStream codeOutStream = new FileOutputStream(codeOut);
            codeOutStream.write(codeBytes);
            codeOutStream.close();

            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(1);
        }
    }

    public static void outputIG(String filename,  ArrayList<Function> program)
        throws Exception {
        FileWriter out = new FileWriter(filename);
        out.write("strict graph InterferenceGraph {\n");
        for(Function func : program) {
            func.printInterferenceGraph(out);
        }
        out.write("}");
        out.close();
    }

    public static void outputLayout(String filename,  ArrayList<Function> program)
        throws Exception {
        FileWriter out = new FileWriter(filename);
        out.write("digraph Computation {\nnode [shape=box];\n");
        for(Function func : program) {
            func.printLayoutOrder(out);
        }
        out.write("}");
        out.close();
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
