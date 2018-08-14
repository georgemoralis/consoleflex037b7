/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mess.systems;

import static sound.ay8910.*;
import static sound.ay8910H.*;
import static mess.machine.spectrum.*;
import static old.mame.inptportH.*;
import static old.mame.inptport.*;
import static old.mame.cpuintrf.*;
import static old.mame.common.*;
import old.mame.drawgfxH.*;
import static WIP.arcadeflex.fucPtr.*;
import WIP.mame.sndintrfH.MachineSound;

import static mame.commonH.REGION_CPU1;
import old.mame.drawgfxH.GfxDecodeInfo;
import old.mame.drawgfxH.rectangle;
import static old.mame.inputH.*;
import static old.mame.driverH.*;
import static mame.commonH.*;

import static mess.messH.*;
import static mess.machine.spectrum.*;
import static arcadeflex.libc.cstring.memset;

import WIP.arcadeflex.libc_v2.*;
import static old.arcadeflex.osdepend.logerror;
import static WIP.arcadeflex.libc.memcpy.*;
import static WIP.mame.memoryH.*;
import static WIP.mame.memory.*;

import static WIP.mame.sndintrfH.*;

import consoleflex.funcPtr.*;
import cpu.z80.z80H;

import static mess.includes.spectrumH.*;
import static mess.vidhrdw.spectrum.*;

import static mess.eventlst.*;
import static mess.eventlstH.*;
import static sound.speaker.*;
import sound.speakerH.Speaker_interface;
import static sound.wave.*;
import static sound.waveH.*;

import static mess.machine.nec765.*;
import static mess.includes.nec765H.*;
import static mess.includes.flopdrvH.*;
import static mess.machine.flopdrv.*;
import static mess.machine.dsk.*;
import static mess.mess.device_input;

import static mess.systems.spectrum.*;

public class spec128 {
    public static int spectrum_128_port_7ffd_data = -1;
    
    public static UBytePtr spectrum_128_ram = null;
    public static UBytePtr spectrum_128_screen_location = new UBytePtr(0x4000);
    
    public static InitMachinePtr spectrum_128_init_machine = new InitMachinePtr() { public void handler() 
    {
                    //spectrum_128_ram = (UBytePtr )malloc(128*1024);
                    spectrum_128_ram = new UBytePtr(128*1024);
                    //if(!spectrum_128_ram) return;
                    if(spectrum_128_ram==null) return;
                    memset(spectrum_128_ram, 0, 128*1024);

                    cpu_setbankhandler_r(1, MRA_BANK1);
                    cpu_setbankhandler_r(2, MRA_BANK2);
                    cpu_setbankhandler_r(3, MRA_BANK3);
                    cpu_setbankhandler_r(4, MRA_BANK4);

                    /* 0x0000-0x3fff always holds ROM */
                    cpu_setbankhandler_w(5, MWA_ROM);
                    cpu_setbankhandler_w(6, MWA_BANK6);
                    cpu_setbankhandler_w(7, MWA_BANK7);
                    cpu_setbankhandler_w(8, MWA_BANK8);

                    /* Bank 5 is always in 0x4000 - 0x7fff */
                    cpu_setbank(2, new UBytePtr(spectrum_128_ram, (5<<14)));
                    cpu_setbank(6, new UBytePtr(spectrum_128_ram, (5<<14)));

                    /* Bank 2 is always in 0x8000 - 0xbfff */                        
                    cpu_setbank(3, new UBytePtr(spectrum_128_ram, (2<<14)));
                    cpu_setbank(7, new UBytePtr(spectrum_128_ram, (2<<14)));

                    /* set initial ram config */
                    spectrum_128_port_7ffd_data = 0;
                    spectrum_128_update_memory();

                    //spectrum_init_machine();
                    spectrum_init_machine.handler();
    } };
    
    public static StopMachinePtr spectrum_128_exit_machine = new StopMachinePtr() {

        public void handler() {
            if (spectrum_128_ram!=null){
                //free(spectrum_128_ram);
                spectrum_128_ram = null;
            }
        }
    };
    
    static void spectrum_128_port_bffd_w(int offset, int data)
    {
                    AY8910_write_port_0_w.handler(0, data);                        
    }

    static void spectrum_128_port_fffd_w(int offset, int data)
    {
                    AY8910_control_port_0_w.handler(0, data);
    }

    /* +3 manual is confused about this */

    static int spectrum_128_port_fffd_r(int offset)
    {
                    return AY8910_read_port_0_r.handler(0);                        
    }

    public static void spectrum_128_update_memory()
    {
        UBytePtr ChosenROM;
        int ROMSelection;

        if (spectrum_128_ram != null){

                    if ((spectrum_128_port_7ffd_data & 8) != 0)
                    {
                                    logerror("SCREEN 1: BLOCK 7");
                                    spectrum_128_screen_location = new UBytePtr(spectrum_128_ram, (7<<14));
                    }
                    else
                    {
                                    logerror("SCREEN 0: BLOCK 5\n");
                                    spectrum_128_screen_location = new UBytePtr(spectrum_128_ram, (5<<14));

                    }

                    /* select ram at 0x0c000-0x0ffff */
                    {
                                    int ram_page;
                                    UBytePtr ram_data;

                                    ram_page = spectrum_128_port_7ffd_data & 0x07;
                                    ram_data=new UBytePtr(spectrum_128_ram, (ram_page<<14));

                                    cpu_setbank(4, ram_data);
                                    cpu_setbank(8, ram_data);

                                    logerror("RAM at 0xc000: %02x\n",ram_page);
                    }

                    /* ROM switching */
                    ROMSelection = ((spectrum_128_port_7ffd_data>>4) & 0x01);

                    /* rom 1 is 128K rom, rom 1 is 48 BASIC */

                    ChosenROM=new UBytePtr(memory_region(REGION_CPU1), 0x010000+(ROMSelection<<14));

                    cpu_setbank(1, ChosenROM);

                    logerror("rom switch: %02x\n", ROMSelection);
        }
    }

    static void spectrum_128_port_7ffd_w(int offset, int data)
    {
               /* D0-D2: RAM page located at 0x0c000-0x0ffff */
               /* D3 - Screen select (screen 0 in ram page 5, screen 1 in ram page 7 */
               /* D4 - ROM select - which rom paged into 0x0000-0x03fff */
               /* D5 - Disable paging */

                    /* disable paging? */
                    if ((spectrum_128_port_7ffd_data & 0x20) != 0)
                                    return;

                    /* store new state */
                    spectrum_128_port_7ffd_data = data;

                    /* update memory */
                    spectrum_128_update_memory();
    }

    static MemoryReadAddress spectrum_128_readmem[] ={
                    new MemoryReadAddress( 0x0000, 0x3fff, MRA_BANK1 ),
                    new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK2 ),
                    new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK3 ),
                    new MemoryReadAddress( 0xc000, 0xffff, MRA_BANK4 ),
            new MemoryReadAddress( -1 )	/* end of table */
    };

    static MemoryWriteAddress spectrum_128_writemem[] ={
                    new MemoryWriteAddress( 0x0000, 0x3fff, MWA_BANK5 ),
                    new MemoryWriteAddress( 0x4000, 0x7fff, MWA_BANK6 ),
                    new MemoryWriteAddress( 0x8000, 0xbfff, MWA_BANK7 ),
                    new MemoryWriteAddress( 0xc000, 0xffff, MWA_BANK8 ),
            new MemoryWriteAddress( -1 )	/* end of table */
    };
	
    //READ_HANDLER ( spectrum_128_port_r )
    public static ReadHandlerPtr spectrum_128_port_r = new ReadHandlerPtr() {
        public int handler(int offset) {

             if ((offset & 1)==0)
             {
                     return spectrum_port_fe_r(offset);
             }

             /* KT: the following is not decoded exactly, need to check what
             is correct */
             if ((offset & 2)==0)
             {
                     switch ((offset>>8) & 0xff)
                     {
                                    case 0xff:
                                                    return spectrum_128_port_fffd_r(offset);
                     }
             }

             if ((offset & 0xff)==0x1f)
                     return spectrum_port_1f_r(offset);

             if ((offset & 0xff)==0x7f)
                     return spectrum_port_7f_r(offset);

             if ((offset & 0xff)==0xdf)
                     return spectrum_port_df_r(offset);

             logerror("Read from 128 port: %04x\n", offset);

             return 0xff;
    }};

    //WRITE_HANDLER ( spectrum_128_port_w )
    public static WriteHandlerPtr spectrum_128_port_w = new WriteHandlerPtr() {
    public void handler(int offset, int data) {
                    if ((offset & 1)==0)
                                    spectrum_port_fe_w(offset,data);

                    /* Only decodes on A15, A14 & A1 */
                    else if ((offset & 2)==0)
                    {
                                    switch ((offset>>8) & 0xc0)
                                    {
                                                    case 0x40:
                                                                    spectrum_128_port_7ffd_w(offset, data);
                                                                    break;
                                                    case 0x80:
                                                                    spectrum_128_port_bffd_w(offset, data);
                                                                    break;
                                                    case 0xc0:
                                                                    spectrum_128_port_fffd_w(offset, data);
                                                                    break;
                                                    default:
                                                                    logerror("Write %02x to 128 port: %04x\n", data, offset);
                                    }
                    }
                    else
                    {
                            logerror("Write %02x to 128 port: %04x\n", data, offset);
                    }
    }};

    static IOReadPort spectrum_128_readport[] ={
                    new IOReadPort(0x0000, 0xffff, spectrum_128_port_r),
                    new IOReadPort( -1 )
    };

    static IOWritePort spectrum_128_writeport[] ={
                    new IOWritePort(0x0000, 0xffff, spectrum_128_port_w),
                    new IOWritePort( -1 )
    };
   
    static AY8910interface spectrum_128_ay_interface = new AY8910interface
    (
                    1,
                    1000000,
                    new int[] {25,25},
                    new ReadHandlerPtr[]{spectrum_128_port_r},
                    new ReadHandlerPtr[]{spectrum_128_port_r},
                    new WriteHandlerPtr[]{spectrum_128_port_w},
                    new WriteHandlerPtr[]{spectrum_128_port_w}
    );

    static MachineDriver machine_driver_spectrum_128 = new MachineDriver
    (
            /* basic machine hardware */
            new MachineCPU[] {
                    new MachineCPU(
                            CPU_Z80|CPU_16BIT_PORT,
                            3546900,		/* 3.54690 Mhz */
                            spectrum_128_readmem,spectrum_128_writemem,
                            spectrum_128_readport,spectrum_128_writeport,
                            spec_interrupt,1
                    ),
            },
            50, 2500,		/* frames per second, vblank duration */
            1,
            spectrum_128_init_machine,
            spectrum_128_exit_machine,

            /* video hardware */
            SPEC_SCREEN_WIDTH,				/* screen width */
            SPEC_SCREEN_HEIGHT, 			/* screen height */
            new rectangle( 0, SPEC_SCREEN_WIDTH-1, 0, SPEC_SCREEN_HEIGHT-1),  /* visible_area */
            spectrum_gfxdecodeinfo, 			 /* graphics decode info */
            16, 256,							 /* colors used for the characters */
            spectrum_init_palette,				 /* initialise palette */

            VIDEO_TYPE_RASTER,
            spectrum_eof_callback,
            spectrum_128_vh_start,
            spectrum_128_vh_stop,
            spectrum_128_vh_screenrefresh,

            /* sound hardware */
            0,0,0,0,
                    new MachineSound[] {
                                    /* +3 Ay-3-8912 sound */
                                    new MachineSound(
                                                    SOUND_AY8910,
                                                    spectrum_128_ay_interface
                                    ),
                                    /* standard spectrum buzzer sound */
                                    new MachineSound(
                                                    SOUND_SPEAKER,
                                                    spectrum_speaker_interface
                                    ),
                                    /*-----------------27/02/00 10:40-------------------
                                     cassette wave interface
                                    --------------------------------------------------*/
                                    new MachineSound(
                                                    SOUND_WAVE,
                                                    spectrum_wave_interface
                                    )

                    }

    );
    
    static RomLoadPtr rom_spec128 = new RomLoadPtr(){ public void handler(){ 
                    ROM_REGION(0x18000,REGION_CPU1);
            ROM_LOAD("zx128_0.rom",0x10000,0x4000, 0xe76799d2);
            ROM_LOAD("zx128_1.rom",0x14000,0x4000, 0xb96a36be);
    ROM_END(); }}; 

    static RomLoadPtr rom_spec128s = new RomLoadPtr(){ public void handler(){ 
                    ROM_REGION(0x18000,REGION_CPU1);
            ROM_LOAD("zx128s0.rom",0x10000,0x4000, 0x453d86b2);
            ROM_LOAD("zx128s1.rom",0x14000,0x4000, 0x6010e796);
    ROM_END(); }}; 

    static RomLoadPtr rom_specpls2 = new RomLoadPtr(){ public void handler(){ 
                    ROM_REGION(0x18000,REGION_CPU1);
            ROM_LOAD("zxp2_0.rom",0x10000,0x4000, 0x5d2e8c66);
            ROM_LOAD("zxp2_1.rom",0x14000,0x4000, 0x98b1320b);
    ROM_END(); }}; 

    static RomLoadPtr rom_specpl2a = new RomLoadPtr(){ public void handler(){ 
                    ROM_REGION(0x20000,REGION_CPU1);
                    ROM_LOAD("p2a41_0.rom",0x10000,0x4000, 0x30c9f490);
                    ROM_LOAD("p2a41_1.rom",0x14000,0x4000, 0xa7916b3f);
                    ROM_LOAD("p2a41_2.rom",0x18000,0x4000, 0xc9a0b748);
                    ROM_LOAD("p2a41_3.rom",0x1c000,0x4000, 0xb88fd6e3);
    ROM_END(); }}; 

    
    //COMPX( 1986, spec128,  0,		 spectrum_128,	 spectrum, 0,			 "Sinclair Research",    "ZX Spectrum 128" ,GAME_NOT_WORKING)*/
    public static GameDriver driver_spec128 = new GameDriver("1986", "spec128", "spectrum.java", rom_spec128, null, machine_driver_spectrum_128, input_ports_spectrum, null, io_spectrum, "Sinclair Research", "ZX Spectrum 128");
    //COMPX( 1985, spec128s, spec128,  spectrum_128,	 spectrum, 0,			 "Sinclair Research",    "ZX Spectrum 128 (Spain)" ,GAME_NOT_WORKING)*/
    public static GameDriver driver_spec128s = new GameDriver("1986", "spec128s", "spectrum.java", rom_spec128s, null, machine_driver_spectrum_128, input_ports_spectrum, null, io_spectrum, "Sinclair Research", "ZX Spectrum 128 (Spain)");
    //COMPX( 1986, specpls2, spec128,  spectrum_128,	 spectrum, 0,			 "Amstrad plc",          "ZX Spectrum +2" ,GAME_NOT_WORKING)
    public static GameDriver driver_specpls2 = new GameDriver("1986", "specpls2", "spectrum.java", rom_specpls2, null, machine_driver_spectrum_128, input_ports_spectrum, null, io_spectrum, "Amstrad plc", "ZX Spectrum +2");
    //COMPX( 1986, specp2fr, spec128,  spectrum_128,	 spectrum, 0,			 "Amstrad plc",          "ZX Spectrum +2 (France)" ,GAME_NOT_WORKING)
    public static GameDriver driver_specp2fr = new GameDriver("1986", "specp2fr", "spectrum.java", rom_specp2fr, null, machine_driver_spectrum_128, input_ports_spectrum, null, io_spectrum, "Amstrad plc", "ZX Spectrum +2 (France)");
    //COMPX( 1986, specp2sp, spec128,  spectrum_128,	 spectrum, 0,			 "Amstrad plc",          "ZX Spectrum +2 (Spain)" ,GAME_NOT_WORKING)
    public static GameDriver driver_specp2sp = new GameDriver("1986", "specp2sp", "spectrum.java", rom_specp2sp, null, machine_driver_spectrum_128, input_ports_spectrum, null, io_spectrum, "Amstrad plc", "ZX Spectrum +2 (Spain)");
    
}
