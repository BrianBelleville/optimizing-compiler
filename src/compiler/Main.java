package compiler;

import java.io.File;
import support.IdentifierTable;

public class Main {

    public static void main(String[] args) {
        try {
            File f = new File(args[0]);
	    IdentifierTable t = new IdentifierTable();
            Parser parse = new Parser(f, t);
            parse.parse();
            System.out.println("Good parse?");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
