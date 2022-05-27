package xyz.bobkinn.factoriomodsdownloader;

import java.awt.GraphicsEnvironment;
import java.io.Console;

public class Start {
    public static void main(String[] args) throws Exception{
        Console console = System.console();
        StringBuilder argsS = new StringBuilder();
        for (String arg : args){
            argsS.append(arg);
        }


        if (console == null && !GraphicsEnvironment.isHeadless()) {
            String filename = Start.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
//            System.out.println(Arrays.toString(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar " + filename + " " + argsS + "\""}));
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar " + filename +" "+argsS+"\""});

        }
        else {
            Main.main(args);
        }
    }
}
