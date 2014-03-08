package compiler;

import java.util.ArrayList;
import ir.Function;

public abstract class CodeGenerator {
    public abstract int[] generate(ArrayList<Function> program);
}
