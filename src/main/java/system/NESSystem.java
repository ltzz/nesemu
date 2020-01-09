package system;

import system.cpu.cpu6502;
import system.joypad.JoyPad;
import system.ppu.Ppu;
import system.rom.Rom;

public final class NESSystem {

    public Rom rom;
    cpu6502 cpu;
    public Ppu ppu;
    public Ram ram;
    public JoyPad joyPad;
    public int[] frameBuffer = new int[256*240];

    public NESSystem(){
        rom = new Rom("./sample1.nes"); // FIXME: 一旦ハードコード
//        rom = new Rom("./SHOOT.nes"); // FIXME: 一旦ハードコード
        joyPad = new JoyPad();
        ppu = new Ppu(frameBuffer);
        ram = new Ram(ppu, rom, joyPad);
        ppu.ppuCHR_ROM = rom.CHR_ROM;
        ppu.refreshColorTables();
        cpu = new cpu6502(ram);

        reset();
    };

    public void reset(){
        int upper = cpu.ram.getRAMValue(0xFFFD) & 0xFF;
        int lower =  cpu.ram.getRAMValue(0xFFFC) & 0xFF;
        int addr = (upper << 8) | lower;
        cpu.programCounter = addr;
        cpu.init();
    }

    public void systemExecute(){
        cpu.interpret(cpu.ram.getRAMValue(cpu.programCounter));
    }
}
