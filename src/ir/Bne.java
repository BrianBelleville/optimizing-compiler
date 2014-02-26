package ir;


public class Bne extends BranchInstruction {

    public Bne(BasicBlock target) {
        super(Opcode.bne, target);
    }
}
