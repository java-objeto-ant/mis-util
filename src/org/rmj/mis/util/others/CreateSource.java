package org.rmj.mis.util.others;

import java.io.IOException;

public class CreateSource {
    public static void main(String [] args){
        try {
            Process process = new ProcessBuilder("C:\\GGC_Systems\\vb.net\\CreateSource.exe").start();
            System.out.println("Program initialized...");
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
