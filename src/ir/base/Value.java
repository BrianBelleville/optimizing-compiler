package ir.base;

import java.io.IOException;
import java.io.Writer;

// possibly use this as a more lightweight interface for things that
// can act as values, such as immediate numbers, rather than treating
// everthing as an instruction.
public abstract class Value {
    public void addUse(Instruction i) {} // default is do nothing
    
    public Value getSubstitute() throws Exception {
	return this;		// default, no substitution
    }

    abstract public void printAsArg(Writer w) throws IOException;
}
