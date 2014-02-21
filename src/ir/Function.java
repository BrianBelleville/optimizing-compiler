package ir;

import support.Identifier;
import java.io.Writer;

public class Function {
    public Identifier name;
    public BasicBlock entryPoint;

    public void printFunc(Writer w) throws Exception {
	w.write(name.getString() + " -> " + entryPoint.getNodeName() + ";\n");
	entryPoint.printBlock(w);
    }
}
