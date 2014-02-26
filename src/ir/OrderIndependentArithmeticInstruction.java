package ir;

public class OrderIndependentArithmeticInstruction extends ArithmeticInstruction {
    public OrderIndependentArithmeticInstruction(Opcode o, Value a1, Value a2) {
        super(o, a1, a2);
    }
    
    @Override
    public boolean isCommonSubexpression(Instruction i) {
        if(i instanceof BinaryInstruction) {
            BinaryInstruction b = (BinaryInstruction)i;
            // argument order doesn't matter, so check both matchings
            return this.getOpcode() == b.getOpcode()
                && ((this.getArg1().equals(b.getArg1())
                     && this.getArg2().equals(b.getArg2()))
                    || (this.getArg1().equals(b.getArg2())
                        && this.getArg2().equals(b.getArg1())));
        }
        return false;
    }

}
