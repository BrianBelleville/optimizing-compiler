package ir;


public class Mul extends OrderIndependentArithmeticInstruction {

    public Mul(Value a1, Value a2) {
        super(Opcode.mul, a1, a2);
    }

    @Override
    public Value performComputation() {
        Immediate a1 = (Immediate)getArg1();
        Immediate a2 = (Immediate)getArg2();
        int v1 = a1.getValue();
        int v2 = a2.getValue();
        return new Immediate(v1 * v2);
    }

}
