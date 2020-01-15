import system.NESSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public final class ROMNestestTest {

    public static void run(){
        try {
            NESSystem system = new NESSystem("./nestest.nes");
            File logfile = new File("./nestest.log");
            FileReader fileReader = new FileReader(logfile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            system.cpu.programCounter = 0xC000;
            for (int i = 0; i < 7000; ++i) {
                String logProgramCounter = bufferedReader.readLine().split("\\s")[0];
                String nowProgramCounter = String.format("%04X", system.cpu.programCounter);
                if(!logProgramCounter.equals(nowProgramCounter)){
                    System.out.println((i+1) + " " + nowProgramCounter
                            + " A:" + String.format("%02X", system.cpu.getRegA())
                            + " X:" + String.format("%02X", system.cpu.getRegX())
                            + " Y:" + String.format("%02X", system.cpu.getRegY())
                            + " P:" + String.format("%02X", system.cpu.getRegP()));
                }
                system.cpu.nextStep();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
