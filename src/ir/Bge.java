package ir;


public class Bge extends BranchInstruction {

    public Bge(BasicBlock target) {
        super(Opcode.bge, target);
    }
}
