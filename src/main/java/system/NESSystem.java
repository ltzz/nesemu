package system;

import system.cpu.cpu6502;
import system.joypad.JoyPad;
import system.ppu.Ppu;
import system.rom.Rom;

public final class NESSystem {

    public Rom rom;
    public cpu6502 cpu;
    public Ppu ppu;
    public Ram ram;
    public JoyPad joyPad;
    public int[] frameBuffer = new int[256*240];
    public int count; // TODO

    public NESSystem(String romFileName){
//        rom = new Rom("./palette.nes"); // FIXME: 一旦ハードコード
//        rom = new Rom("./full_palette.nes"); // FIXME: 一旦ハードコード
        rom = new Rom(romFileName);
        joyPad = new JoyPad();
        ppu = new Ppu(frameBuffer);
        ram = new Ram(ppu, rom, joyPad);
        ppu.ppuCHR_ROM = rom.CHR_ROM;
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
        if (count == 0) {
            count = (count + 1) % 2;
            cpu.nextStep();
            ppu.nextStep();
        } else {
            count = (count + 1) % 2;
            for (int i = 0; i < 4999; ++i) { // 暫定、5000命令ごとに1lineの設計 TODO: サイクル数計算する
                cpu.nextStep();
            }
        }
    }
}
