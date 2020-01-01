package system;

import system.cpu.cpu6502;
import system.rom.Rom;

public class NESSystem {

    Rom rom;
    cpu6502 cpu;

    public NESSystem(){
        rom = new Rom("./sample1.nes"); // FIXME: 一旦ハードコード
        cpu = new cpu6502();
        cpu.ram.PRG_ROM = rom.PRG_ROM;
        cpu.ram.CHR_ROM = rom.CHR_ROM;
        reset();
    };

    public void reset(){
        int upper = cpu.ram.getRAMValue(0xFFFD) & 0xFF;
        int lower =  cpu.ram.getRAMValue(0xFFFC) & 0xFF;
        int addr = (upper << 8) | lower;
        cpu.programCounter = addr;
    }

    public void systemExecute(){
        for(int i=0; i < 200; ++i){
            cpu.interpret(cpu.ram.getRAMValue(cpu.programCounter));
        }
    }
}
