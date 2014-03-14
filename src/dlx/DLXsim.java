package dlx;

public class DLXsim {
    public static void main(String[] args) {
        try {
            String input = null;
            for(int i = 0; i<args.length; i++) {
                switch(args[i]) {
                default:
                    // default is intput file name, will only execute
                    // a single file, so last one read will win.
                    input = args[i];
                    break;
                }
            }
            if(input == null) {
                System.out.println("No input file provided");
                System.exit(1);
            }

            int[] code = DLXutil.readCode(input);
            DLX.load(code);
            DLX.execute();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("An error occured");
            System.exit(-1);
        }
    }
}
