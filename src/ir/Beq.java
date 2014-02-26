package ir;


public class Beq extends BranchInstruction {

    public Beq(BasicBlock target) {
        super(Opcode.beq, target);
    }
}
