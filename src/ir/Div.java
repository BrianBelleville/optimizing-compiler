package ir;

import ir.base.ArithmeticInstruction;
import ir.base.Opcode;
import ir.base.Value;

public class Div extends ArithmeticInstruction {

    public Div(Value a1, Value a2) {
        super(Opcode.div, a1, a2);
    }

    @Override
    public Value performComputation() {
        Immediate a1 = (Immediate)getArg1();
        Immediate a2 = (Immediate)getArg2();
        int v1 = a1.getValue();
        int v2 = a2.getValue();
        return new Immediate(v1 / v2);
    }


}
