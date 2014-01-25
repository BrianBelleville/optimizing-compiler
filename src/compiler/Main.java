package compiler;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        try {
            File f = new File(args[0]);
            Parser parse = new Parser(f);
            parse.parse();
            System.out.println("Good parse?");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
