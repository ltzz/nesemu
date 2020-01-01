package system.ppu;

import system.Ram;

public class Ppu {
    public byte[] ppuRam;
    public byte[] ppuReg;
    public int ppuAddrCount; //TODO: 直せたら直す
    public int ppuAddr;

    public Ppu(){
        ppuReg = new byte[8];
        ppuRam = new byte[0x4000]; // TODO: 今は容量適当
        ppuAddrCount = 0;
    }

    public void IOAccess(){
        System.out.println("ppu_addr " + Integer.toHexString(ppuAddr));
        System.out.println("ppu_data " + Integer.toHexString(ppuReg[7] & 0Xff));
    }

    public void writePpuAddr(){
        if( ppuAddrCount == 0 ){
            ppuAddr = (ppuReg[6] & 0xFF) << 8;
        } else if( ppuAddrCount == 1 ){
            ppuAddr = ppuAddr + (ppuReg[6] & 0xFF);
            IOAccess();
        }
        ppuAddrCount = (ppuAddrCount + 1) % 2;
    }

    public void writePpuData(){
        ppuRam[ppuAddr] = ppuReg[7];
        ppuAddr += 1; // TODO: $2000の値によって32byteインクリメント
        IOAccess();
    }

    public void nextStep(){
    }
}
