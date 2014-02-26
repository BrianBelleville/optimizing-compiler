package ir;


public class ArithmeticInstruction extends BinaryInstruction {
    public ArithmeticInstruction(Opcode o, Value a1, Value a2) {
        super(o, a1, a2);
    }
    
    public boolean isComputable() {
        return (getArg1() instanceof Immediate) && (getArg2() instanceof Immediate);
    }
    
    public Value performComputation() throws Exception {
        throw new Exception("performComputation not implemented");
    }
}
