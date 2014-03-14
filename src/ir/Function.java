package ir;

import support.Identifier;
import support.InterferenceGraph;
import support.LocalMemoryRegion;
import java.io.Writer;

public class Function {
    public Identifier name;
    public BasicBlock entryPoint;
    public InterferenceGraph ig;
    public LocalMemoryRegion locals;
    public void printInterferenceGraph(Writer w) throws Exception {
        w.write("subgraph " + name.getString() + "{\n");
        ig.printGraph(w);
        w.write("}\n");
    }
    public void printFunc(Writer w) throws Exception {
	w.write(name.getString() + " -> " + entryPoint.getNodeName() + ";\n");
	entryPoint.printBlock(w);
    }

    public void printLayoutOrder(Writer w) throws Exception {
        w.write(name.getString() + " -> " + entryPoint.getNodeName() + ";\n");
	entryPoint.printBlockLayoutOrder(w);
    }
    
    public Function() {
        locals = new LocalMemoryRegion();
    }
}
