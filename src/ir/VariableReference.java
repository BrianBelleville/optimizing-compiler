package ir;

import java.io.IOException;
import java.io.Writer;

import ir.base.Instruction;
import ir.base.Value;
import support.Identifier;

// this class wraps a value
public class VariableReference extends Value {
    private Value inner;
    private Identifier variable;

    public VariableReference(Identifier variable, Value value) {
        if(value instanceof VariableReference) {
            value = ((VariableReference)value).getInnerValue();
        }
        this.variable = variable;
        this.inner = value;
    }

    @Override
    public void addUse(Instruction i) {
        inner.addUse(i);    // pass to inner
    }

    @Override
    public Value getSubstitute() {
        // pass to inner, but still wrap in the variable reference
        return new VariableReference(variable, inner.getSubstitute()); 
    }

    @Override
    public void printAsArg(Writer w) throws IOException {
        inner.printAsArg(w);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof VariableReference) {
            VariableReference v = (VariableReference)o;
            return this.getInnerValue().equals(v.getInnerValue())
                && this.getVariable().equals(v.getVariable());
        }
        return false;
    }
    
    public Value getInnerValue() {
        return inner;
    }

    public Identifier getVariable() {
        return variable;
    }
}
