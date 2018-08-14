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

public class pentagon {
    
        /****************************************************************************************************/
        /* pentagon */

        //static  READ8_HANDLER(pentagon_port_r)
        public static ReadHandlerPtr pentagon_port_r = new ReadHandlerPtr() {
            public int handler(int offset) {
                    return 0x0ff;
        }};


        //static WRITE8_HANDLER(pentagon_port_w)
        public static WriteHandlerPtr pentagon_port_w = new WriteHandlerPtr() {
            public void handler(int offset, int data) {
        }};

        static IOReadPort pentagon_readport[] ={
		new IOReadPort(0x0000, 0xffff, pentagon_port_r),
		new IOReadPort( -1 )
	};
	
	static IOWritePort pentagon_writeport[] ={
		new IOWritePort(0x0000, 0xffff, pentagon_port_w),
		new IOWritePort( -1 )
	};

        //static MACHINE_RESET( pentagon )
        public static InitMachinePtr pentagon_init_machine = new InitMachinePtr() { public void handler() 
	{
                spectrum_128_ram = new UBytePtr(128*1024);
                
                if(spectrum_128_ram==null) return;
                memset(spectrum_128_ram, 0, 128*1024);

                /* Bank 5 is always in 0x4000 - 0x7fff */
                cpu_setbank(2, new UBytePtr(spectrum_128_ram, (5<<14)));
                cpu_setbank(6, new UBytePtr(spectrum_128_ram, (5<<14)));

                /* Bank 2 is always in 0x8000 - 0xbfff */
                cpu_setbank(3, new UBytePtr(spectrum_128_ram, (2<<14)));
                cpu_setbank(7, new UBytePtr(spectrum_128_ram, (2<<14)));

                betadisk_init();
        }};
        
        static RomLoadPtr rom_pentagon = new RomLoadPtr(){ public void handler(){
                        ROM_REGION(0x020000, REGION_CPU1);
                        ROM_LOAD("pentagon.rom",0x010000, 0x8000, 0xaa1ce4bd);
                        //ROM_CART_LOAD(0, "rom\0", 0x0000, 0x4000, ROM_NOCLEAR | ROM_NOMIRROR | ROM_OPTIONAL);
        ROM_END(); }};
	


        /****************************************************************************************************/


        static MachineDriver machine_driver_pentagon = new MachineDriver
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
    
        //COMP( ????, pentagon, spectrum, 0,		pentagon,		spectrum,	0,		specpls3,	"???",		"Pentagon", GAME_NOT_WORKING)
        public static GameDriver driver_pentagon = new GameDriver("????", "pentagon", "spectrum.java", rom_pentagon, null, machine_driver_pentagon, input_ports_spectrum, null, io_spectrum, "???", "Pentagon");
}
