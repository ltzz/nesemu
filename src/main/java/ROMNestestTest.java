import system.NESSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ROMNestestTest {

    public static void run(){
        try {
            NESSystem system = new NESSystem("./nestest.nes");
            File logfile = new File("./nestest.log");
            FileReader fileReader = new FileReader(logfile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            system.cpu.programCounter = 0xC000;
            system.cpu.setRegP((byte) 0x24);
            for (int i = 0; i < 8991; ++i) {
                String line = bufferedReader.readLine();

                Matcher mRegA = Pattern.compile("(A:[0-9A-F]{2})").matcher(line);
                Matcher mRegX = Pattern.compile("(X:[0-9A-F]{2})").matcher(line);
                Matcher mRegP = Pattern.compile("(P:[0-9A-F]{2})").matcher(line);

                String logProgramCounter = line.split("\\s")[0];
                String nowProgramCounter = String.format("%04X", system.cpu.programCounter);
                String expectedRegA = mRegA.find() ? mRegA.group(1) : "A:00";
                String actualRegA = String.format("A:%02X", system.cpu.getRegA());
                String expectedRegX = mRegX.find() ? mRegX.group(1) : "P:00";
                String actualRegX = String.format("X:%02X", system.cpu.getRegX());
                String expectedRegP = mRegP.find() ? mRegP.group(1) : "P:00";
                String actualRegP = String.format("P:%02X", system.cpu.getRegP());

                if(!logProgramCounter.equals(nowProgramCounter)
                        || !expectedRegA.equals(actualRegA)
                        || !expectedRegX.equals(actualRegX)
                        || !expectedRegP.equals(actualRegP)){
                    System.out.println((i+1) + " " + nowProgramCounter
                            + " " + actualRegA
                            +  " " + actualRegX
                            + " Y:" + String.format("%02X", system.cpu.getRegY())
                            + " " + actualRegP);
                }
                system.cpu.nextStep();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
