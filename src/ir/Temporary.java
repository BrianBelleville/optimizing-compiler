package ir;

public class Temporary extends NamedValue {
    public Temporary() {
	super("__T");
    }
    @Override
    public Integer getColor() {
        return -2;
    }
}
