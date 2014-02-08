package ir;

import support.Identifier;
import java.io.Writer;
import java.io.IOException;

public class Function {
    public Identifier name;
    public BasicBlock entryPoint;

    public void printFunc(Writer w) throws IOException {
	w.write(name.getString() + " -> " + entryPoint.getNodeName() + ";\n");
	entryPoint.printBlock(w);
    }
}
