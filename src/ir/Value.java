package ir;

import java.io.Writer;

// possibly use this as a more lightweight interface for things that
// can act as values, such as immediate numbers, rather than treating
// everthing as an instruction.
public abstract class Value {
    public void addUse(Instruction i) {} // default is do nothing
    public void removeUse(Instruction i)
        throws Exception {} // default is do nothing

    public Value getSubstitute() throws Exception {
	return this;		// default, no substitution
    }
    public Value stripVariableReference() {
        return this;            // default, return this, no variable reference
    }
    abstract public void printAsArg(Writer w) throws Exception;

    public boolean needsRegister() {
        return false;           // default for values is that they don't need to be in a register
    }
}
