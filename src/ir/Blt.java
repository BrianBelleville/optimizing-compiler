package ir;


public class Blt extends BranchInstruction {

    public Blt( BasicBlock target) {
        super(Opcode.blt, target);
    }
}
