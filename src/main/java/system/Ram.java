package system;

import system.ppu.Ppu;

public class Ram {
    byte[] wram;
    Ppu ppu;
    byte[] apuIoReg;
    public byte[] PRG_ROM;
    public byte[] CHR_ROM;

    public Ram(Ppu ppu) {
        wram = new byte[0x800];
        this.ppu = ppu;
        apuIoReg = new byte[0x020];
    };

    public byte getRAMValue(int address){
        if(0x0000 <= address && address < 0x2000){
            //WRAM MIRROR * 3
            return wram[address % 0x800];
        }
        else if (address < 0x2008){
            // ppu i/o
            if(address == 0x2006 || address == 0x2007){
                ppu.IOAccess();
            }
        }
        else if (address < 0x4000){
            // ppu i/o mirror * 1023
        }
        else if (address < 0x4020){
            // apu i/o, pad
        }
        else if (address < 0x6000){
            // exrom
        }
        else if (address < 0x8000){
            // exram
        }
        else if (address < 0xC000){
            // prg-rom low
            return PRG_ROM[address - 0x8000];
        }
        else if (address <= 0xFFFF){
            // prg-ram high
            return PRG_ROM[address - 0x8000];
        }
        else{
            // ppu
        }
        return 0x00;
    }

    public int getRAMValue16(int address){
        int lower, upper;
        int value;
        lower = getRAMValue(address + 0) & 0xFF;
        upper = getRAMValue(address + 1) & 0xFF;
        value = (upper << 8) | lower;
        return value;
    }

    public void setRAMValue(int address, byte value) {
        if (0x0000 <= address && address < 0x2000) {
            //WRAM MIRROR * 3
            wram[address % 0x800] = value;
        } else if (address < 0x2008) {
            // ppu i/o
            ppu.ppuReg[address - 0x2000] = value;
            if (address == 0x2006) {
                ppu.writePpuAddr();
            }
            if(address == 0x2007) {
                ppu.writePpuData();
            }
        }
    }
}