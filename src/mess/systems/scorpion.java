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
import static mess.systems.spec128.*;

/*TODO*/////import static mess.machine.wd17xx.h.*;

public class scorpion {

        /****************************************************************************************************/
        /* Zs Scorpion 256 */

        /*
        port 7ffd. full compatibility with Zx spectrum 128. digits are:

        D0-D2 - number of RAM page to put in C000-FFFF
        D3    - switch of address for RAM of screen. 0 - 4000, 1 - c000
        D4    - switch of ROM : 0-zx128, 1-zx48
        D5    - 1 in this bit will block further output in port 7FFD, until reset.
        */

        /*
        port 1ffd - additional port for resources of computer.

        D0    - block of ROM in 0-3fff. when set to 1 - allows read/write page 0 of RAM
        D1    - selects ROM expansion. this rom contains main part of service monitor.
        D2    - not used
        D3    - used for output in RS-232C
        D4    - extended RAM. set to 1 - connects RAM page with number 8-15 in
                C000-FFFF. number of page is given in gidits D0-D2 of port 7FFD
        D5    - signal of strobe for interface centronics. to form the strobe has to be
                set to 1.
        D6-D7 - not used. ( yet ? )
        */

        /* rom 0=zx128, 1=zx48, 2 = service monitor, 3=tr-dos */

        public static int scorpion_256_port_1ffd_data = 0;

        static void scorpion_update_memory()
        {
                UBytePtr ChosenROM;
                int ROMSelection=0;
                
                /*TODO*/////ReadHandlerPtr rh;
                /*TODO*/////WriteHandlerPtr wh;
                int rh;
                int wh;

                if ((spectrum_128_port_7ffd_data & 8) != 0)
                {
                        logerror("SCREEN 1: BLOCK 7\n");
                        spectrum_128_screen_location = new UBytePtr(spectrum_128_ram, (7<<14));
                }
                else
                {
                        logerror("SCREEN 0: BLOCK 5\n");
                        spectrum_128_screen_location = new UBytePtr(spectrum_128_ram, (5<<14));
                }

                /* select ram at 0x0c000-0x0ffff */
                
                int ram_page;
                UBytePtr ram_data;

                ram_page = (spectrum_128_port_7ffd_data & 0x07) | ((scorpion_256_port_1ffd_data & (1<<4))>>1);
                ram_data = new UBytePtr(spectrum_128_ram, (ram_page<<14));

                cpu_setbank(4, ram_data);
                cpu_setbank(8, ram_data);

                logerror("RAM at 0xc000: %02x\n",ram_page);
                

                if ((scorpion_256_port_1ffd_data & (1<<0)) != 0)
                {
                        /* ram at 0x0000 */
                        logerror("RAM at 0x0000\n");

                        /* connect page 0 of ram to 0x0000 */
                        rh = MRA_BANK1;
                        wh = MWA_BANK5;
                        cpu_setbank(1, new UBytePtr(spectrum_128_ram,(8<<14)));
                        cpu_setbank(5, new UBytePtr(spectrum_128_ram,(8<<14)));
                }
                else
                {
                        /* rom at 0x0000 */
                        logerror("ROM at 0x0000\n");

                        /* connect page 0 of rom to 0x0000 */
                        rh = MRA_BANK1;
                        wh = MWA_NOP;

                        if ((scorpion_256_port_1ffd_data & (1<<1)) != 0)
                        {
                                ROMSelection = 2;
                        }
                        else
                        {

                                /* ROM switching */
                                ROMSelection = ((spectrum_128_port_7ffd_data>>4) & 0x01);
                        }

                        /* rom 0 is 128K rom, rom 1 is 48 BASIC */
                        ChosenROM = new UBytePtr(memory_region(REGION_CPU1), 0x010000 + (ROMSelection<<14));

                        cpu_setbank(1, ChosenROM);

                        logerror("rom switch: %02x\n", ROMSelection);
                }
                /*TODO*/////memory_install_read8_handler(0, ADDRESS_SPACE_PROGRAM, 0x0000, 0x3fff, 0, 0, rh);
                cpu_setbankhandler_r(1, rh);
                /*TODO*/////memory_install_write8_handler(0, ADDRESS_SPACE_PROGRAM, 0x0000, 0x3fff, 0, 0, wh);
                cpu_setbankhandler_w(1, wh);
        }


        //static WRITE8_HANDLER(scorpion_port_7ffd_w)
        public static void scorpion_port_7ffd_w(int offset, int data){
            
                logerror("scorpion 7ffd w: %02x\n", data);

                /* disable paging? */
                if ((spectrum_128_port_7ffd_data & 0x20) != 0)
                        return;

                /* store new state */
                spectrum_128_port_7ffd_data = data;

                /* update memory */
                scorpion_update_memory();
        }

        //static WRITE8_HANDLER(scorpion_port_1ffd_w)
        public static void scorpion_port_1ffd_w(int offset, int data){
                        
                logerror("scorpion 1ffd w: %02x\n", data);

                scorpion_256_port_1ffd_data = data;

                /* disable paging? */
                if ((spectrum_128_port_7ffd_data & 0x20)==0)
                {
                        scorpion_update_memory();
                }
        }
    
	//static MACHINE_RESET( scorpion )
        public static InitMachinePtr scorpion_init_machine = new InitMachinePtr() { public void handler() 
	{
                spectrum_128_ram = new UBytePtr(256*1024);
                
                if(spectrum_128_ram==null) return;
                memset(spectrum_128_ram, 0, 256*1024);

                /* Bank 5 is always in 0x4000 - 0x7fff */
                cpu_setbank(2, new UBytePtr(spectrum_128_ram, (5<<14)));
                cpu_setbank(6, new UBytePtr(spectrum_128_ram, (5<<14)));

                /* Bank 2 is always in 0x8000 - 0xbfff */
                cpu_setbank(3, new UBytePtr(spectrum_128_ram, (2<<14)));
                cpu_setbank(7, new UBytePtr(spectrum_128_ram, (2<<14)));

                spectrum_128_port_7ffd_data = 0;
                scorpion_256_port_1ffd_data = 0;

                scorpion_update_memory();

                betadisk_init();
        }};

        static MemoryReadAddress scorpio_readmem[] ={
            /*TODO*/////new MemoryReadAddress( 0x0000, 0xffff, scorpion_port_r ),
            new MemoryReadAddress( 0x0000, 0x3fff, MRA_BANK1 ),
            new MemoryReadAddress( 0x4000, 0x7fff, MRA_BANK2 ),
            new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK3 ),
            new MemoryReadAddress( 0xc000, 0xffff, MRA_BANK4 ),
            new MemoryReadAddress( -1 )	/* end of table */ 
        };
        
        static MemoryWriteAddress scorpio_writemem[] ={
                /*TODO*/////new MemoryReadAddress(0, 255, scorpion_port_w),
                new MemoryWriteAddress( 0x0000, 0x3fff, MWA_BANK5 ),
                new MemoryWriteAddress( 0x4000, 0x7fff, MWA_BANK6 ),
                new MemoryWriteAddress( 0x8000, 0xbfff, MWA_BANK7 ),
                new MemoryWriteAddress( 0xc000, 0xffff, MWA_BANK8 ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};

        //static  READ8_HANDLER(scorpion_port_r)
        public static ReadHandlerPtr scorpion_port_r = new ReadHandlerPtr() {
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
                                        case 0xff: return spectrum_128_port_fffd_r(offset);
                                        case 0x1f: return spectrum_port_1f_r(offset);
                                        case 0x7f: return spectrum_port_7f_r(offset);
                                        case 0xdf: return spectrum_port_df_r(offset);
                         }
                 }
 
                 switch (offset & 0x0ff)
                 {
                        case 0x01f:
                                /*TODO*/////return wd179x_status_r(offset);
                                return 0xff;
                        case 0x03f:
                                /*TODO*/////return wd179x_track_r(offset);
                                return 0xff;
                        case 0x05f:
                                /*TODO*/////return wd179x_sector_r(offset);
                                return 0xff;
                        case 0x07f:
                                /*TODO*/////return wd179x_data_r(offset);
                                return 0xff;
                        case 0x0ff:
                                return betadisk_status;
                 }

                 logerror("Read from scorpion port: %04x\n", offset);

                 return 0xff;
        }};


        //static WRITE8_HANDLER(scorpion_port_w)
        public static WriteHandlerPtr scorpion_port_w = new WriteHandlerPtr() {
            public void handler(int offset, int data) {
        
                if ((offset & 1)==0)
                        spectrum_port_fe_w(offset,data);

                else if ((offset & 2)==0)
                {
                                switch ((offset>>8) & 0xf0)
                                {
                                        case 0x70:
                                                        scorpion_port_7ffd_w(offset, data);
                                                        break;
                                        case 0xb0:
                                                        spectrum_128_port_bffd_w(offset, data);
                                                        break;
                                        case 0xf0:
                                                        spectrum_128_port_fffd_w(offset, data);
                                                        break;
                                        case 0x10:
                                                        scorpion_port_1ffd_w(offset, data);
                                                        break;
                                        default:
                                                        logerror("Write %02x to scorpion port: %04x\n", data, offset);
                                }
                }
                else
                {
                        logerror("Write %02x to scorpion port: %04x\n", data, offset);
                }
        }};
        static IOReadPort scorpion_readport[] ={
			new IOReadPort(0x0000, 0xffff, scorpion_port_r),
			new IOReadPort( -1 )
	};
	
	static IOWritePort scorpion_writeport[] ={
			new IOWritePort(0x0000, 0xffff, scorpion_port_w),
			new IOWritePort( -1 )
	};
	
        static MachineDriver machine_driver_scorpio = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80|CPU_16BIT_PORT,
                                3546900,		/* 3.54690 Mhz */
                                scorpio_readmem,scorpio_writemem,
                                scorpion_readport,scorpion_writeport,
                                spec_interrupt,1
			),
		},
		50, 2500,		/* frames per second, vblank duration */
		1,
                scorpion_init_machine,
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

        static RomLoadPtr rom_scorpio = new RomLoadPtr(){ public void handler(){
                        ROM_REGION(0x020000, REGION_CPU1);
                        ROM_LOAD("scorp0.rom",0x010000, 0x4000, 0x0eb40a09);
                        ROM_LOAD("scorp1.rom",0x014000, 0x4000, 0x9d513013);
                        ROM_LOAD("scorp2.rom",0x018000, 0x4000, 0xfd0d3ce1);
                        ROM_LOAD("scorp3.rom",0x01c000, 0x4000, 0x1fe1d003);
                        //ROM_CART_LOAD(0, "rom\0", 0x0000, 0x4000, ROM_NOCLEAR | ROM_NOMIRROR | ROM_OPTIONAL);
	ROM_END(); }};

        //COMP( ????, scorpion, 0,		 0,		scorpion,		spectrum,	0,		specpls3,	"Zonov and Co.",		"Zs Scorpion 256", GAME_NOT_WORKING)
        public static GameDriver driver_scorpion = new GameDriver("????", "scorpio", "spectrum.java", rom_scorpio, null, machine_driver_scorpio, input_ports_spectrum, null, io_spectrum, "Zonov and Co.", "Zs Scorpion 256");

}
