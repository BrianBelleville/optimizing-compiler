package ir;

import ir.Store;
import ir.Call;
import support.Identifier;

public class Load extends UnaryInstruction {
    private Identifier variable;
    private boolean global;
    public Load(Identifier var, boolean global, Value arg) {
	super(Opcode.load, arg);
        variable = var;
        this.global = global;
    }

    public Identifier getVariable() {
	return variable;
    }

    @Override
    public boolean isCommonSubexpression(Instruction i) {
        if(i instanceof Load) {
            Load l = (Load)i;
            // checks after the first are probably redundant, but are
            // a requirement for correctness.
            return this.getArg().equals(l.getArg())
                && this.getVariable().equals(l.getVariable())
                && this.global == l.global;
        }
        return false;
    }

    @Override
    public void performCSE() throws Exception {
        Instruction i = getDominating();
        while(i != null) {
            if(this.isCommonSubexpression(i)) {
                this.delete((Instruction)i.getSubstitute());
                return;
            } else if (i instanceof Store) {
                Store s = (Store)i;
                if(this.getVariable().equals(s.getVariable())) {
                    return;     // any store to the same variable kills the search
                }
            } else if (i instanceof Call && global) {
                // global variables can be modified by function calls,
                // so kill search on call if global
                return;
            } else if (i instanceof Kill) {
                Identifier killVar = ((Kill)i).getVariable();
                if(killVar.equals(this.getVariable())) {
                    return;
                }
            }
            i = i.getDominating();
        }
    }
}
