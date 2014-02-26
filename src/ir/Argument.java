package ir;

import support.Identifier;

public class Argument {
    public Value value;
    public Identifier variable;
    public Argument() {
        value = null;
        variable = null;
    }
    public Argument(Value value, Identifier variable) {
        this.value = value;
        this.variable = variable;
    }
}
