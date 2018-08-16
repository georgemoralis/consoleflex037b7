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

public class specpls3 {
    
    	public static int spectrum_plus3_port_1ffd_data = -1;
        
	/*static nec765_interface spectrum_plus3_nec765_interface = 
	{
			null,
			null
	};*/
	static nec765_interface spectrum_plus3_nec765_interface = new nec765_interface() {
            @Override
            public void interrupt(int state) {
                /*TODO*/////throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                //spectrum_plus3_port_1ffd_w(0, state);
            }

            @Override
            public void dma_drq(int state, int read_write) {
                /*TODO*/////throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                //nec765_data_w.handler(state,read_write);
            }
        };
	
	public static InitMachinePtr spectrum_plus3_init_machine = new InitMachinePtr() { 
            public void handler() 
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
	
			cpu_setbankhandler_w(5, MWA_BANK5);
			cpu_setbankhandler_w(6, MWA_BANK6);
			cpu_setbankhandler_w(7, MWA_BANK7);
			cpu_setbankhandler_w(8, MWA_BANK8);
	
			nec765_init(spectrum_plus3_nec765_interface, NEC765A);
	
			floppy_drive_set_geometry(0, floppy_type.FLOPPY_DRIVE_SS_40);
			floppy_drive_set_geometry(1, floppy_type.FLOPPY_DRIVE_SS_40);
			//floppy_drive_set_flag_state(0, FLOPPY_DRIVE_PRESENT, 1);
			//floppy_drive_set_flag_state(1, FLOPPY_DRIVE_PRESENT, 1);
	
			/* Initial configuration */
			spectrum_128_port_7ffd_data = 0;
			spectrum_plus3_port_1ffd_data = 0;
			spectrum_plus3_update_memory();
	
			//spectrum_init_machine();
                        spectrum_init_machine.handler();
	} };

	static void spectrum_plus3_port_3ffd_w(int offset, int data)
	{
			if ((~readinputport(16) & 0x20) != 0)
					nec765_data_w.handler(0,data);
	}
	
	static int spectrum_plus3_port_3ffd_r(int offset)
	{
			if ((readinputport(16) & 0x20) != 0)
					return 0xff;
                        else {
                            
					return nec765_data_r.handler(0);
                        }
            
	}
	
	
	static int spectrum_plus3_port_2ffd_r(int offset)
	{
			if ((readinputport(16) & 0x20) != 0)
					return 0xff;
			else
					return nec765_status_r.handler(0);
                        
	}
	
	static int spectrum_plus3_memory_selections[]=
	{
			0,1,2,3,
			4,5,6,7,
			4,5,6,3,
			4,7,6,3
	};
	
	
	public static void spectrum_plus3_update_memory()
	{
			                        
                        if ((spectrum_128_port_7ffd_data & 8) != 0)
			{
					//logerror("+3 SCREEN 1: BLOCK 7\n");
					spectrum_128_screen_location = new UBytePtr(spectrum_128_ram, (7<<14));
			}
			else
			{
					//logerror("+3 SCREEN 0: BLOCK 5\n");
					spectrum_128_screen_location = new UBytePtr(spectrum_128_ram, (5<<14));
			}
	
			if ((spectrum_plus3_port_1ffd_data & 0x01)==0)
			{
					int ram_page;
					UBytePtr ram_data;
	
					
					UBytePtr ChosenROM;
					int ROMSelection=0;
	
					ram_page = spectrum_128_port_7ffd_data & 0x07;
					ram_data = new UBytePtr(spectrum_128_ram, (ram_page<<14));
	
					cpu_setbank(4, ram_data);
					cpu_setbank(8, ram_data);
	
					//logerror("RAM at 0xc000: %02x\n",ram_page);
	
					cpu_setbank(2, new UBytePtr(spectrum_128_ram, (5<<14)));
					cpu_setbank(6, new UBytePtr(spectrum_128_ram, (5<<14)));
	
					cpu_setbank(3, new UBytePtr(spectrum_128_ram, (2<<14)));
					cpu_setbank(7, new UBytePtr(spectrum_128_ram, (2<<14)));
	
                                        ROMSelection = ((spectrum_128_port_7ffd_data>>4) & 0x01) |
					((spectrum_plus3_port_1ffd_data>>1) & 0x02);
						
					ChosenROM = new UBytePtr(memory_region(REGION_CPU1), 0x010000 + (ROMSelection<<14));
	
					cpu_setbank(1, ChosenROM);
					cpu_setbankhandler_w(5, MWA_ROM);
	
					//logerror("rom switch: %02x\n", ROMSelection);
			}
			else
			{
					System.out.println("DDDDDD");
                                        int MemorySelection;
					UBytePtr ram_data;
	
					MemorySelection = (spectrum_plus3_port_1ffd_data>>1) & 0x03;
	
					spectrum_plus3_memory_selections[0] = spectrum_plus3_memory_selections[(MemorySelection<<2)];
	
					ram_data = new UBytePtr(spectrum_128_ram, (spectrum_plus3_memory_selections[0]<<14));
					cpu_setbank(1, ram_data);
					cpu_setbank(5, ram_data);
					
                                        cpu_setbankhandler_w(5, MWA_BANK5);
	
					ram_data = new UBytePtr(spectrum_128_ram, (spectrum_plus3_memory_selections[1]<<14));
					cpu_setbank(2, ram_data);
					cpu_setbank(6, ram_data);
	
					ram_data = new UBytePtr(spectrum_128_ram, (spectrum_plus3_memory_selections[2]<<14));
					cpu_setbank(3, ram_data);
					cpu_setbank(7, ram_data);
	
					ram_data = new UBytePtr(spectrum_128_ram, (spectrum_plus3_memory_selections[3]<<14));
					cpu_setbank(4, ram_data);
					cpu_setbank(8, ram_data);
	
					//logerror("extended memory paging: %02x\n",MemorySelection);
			 }
	}
	
	static void spectrum_plus3_port_7ffd_w(int offset, int data)
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
			spectrum_plus3_update_memory();
                        
	}
	
	static void spectrum_plus3_port_1ffd_w(int offset, int data)
	{
	//System.out.println("OffsetW: "+offset);
        //System.out.println("dataW: "+data);
			/* D0-D1: ROM/RAM paging */
			/* D2: Affects if d0-d1 work on ram/rom */
			/* D3 - Disk motor on/off */
			/* D4 - parallel port strobe */
	
			floppy_drive_set_motor_state(0, data & (1<<3));
			floppy_drive_set_motor_state(1, data & (1<<3));
			floppy_drive_set_ready_state(0, 1, 1);
			floppy_drive_set_ready_state(1, 1, 1);
	
			spectrum_plus3_port_1ffd_data = data;
	
			/* disable paging? */
			if ((spectrum_128_port_7ffd_data & 0x20)==0)
			{
					/* no */
					spectrum_plus3_update_memory();
			}
	};
	
	//READ_HANDLER ( spectrum_plus3_port_r )
        public static ReadHandlerPtr spectrum_plus3_port_r = new ReadHandlerPtr() {
            public int handler(int offset) {
		if ((offset & 1)==0)
                {
                        return spectrum_port_fe_r(offset);
                }

                if ((offset & 2)==0)
                {
                        switch ((offset>>8) & 0xff)
		 {
				case 0xff: return spectrum_128_port_fffd_r(offset);
				case 0x2f: return spectrum_plus3_port_2ffd_r(offset);
				case 0x3f: return spectrum_plus3_port_3ffd_r(offset);
				case 0x1f: return spectrum_port_1f_r(offset);
				case 0x7f: return spectrum_port_7f_r(offset);
				case 0xdf: return spectrum_port_df_r(offset);
		 }
                }
	
		 logerror("Read from +3 port: %04x\n", offset);
	
		 return 0xff;
	}};
	
	//WRITE_HANDLER ( spectrum_plus3_port_w )
        public static WriteHandlerPtr spectrum_plus3_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
                if ((offset & 1)==0)
                    spectrum_port_fe_w(offset,data);

		/* the following is not decoded exactly, need to check
		what is correct! */
		
		if ((offset & 2)==0)
		{
			switch ((offset>>8) & 0xf0)
				{
						case 0x70:
								spectrum_plus3_port_7ffd_w(offset, data);
								break;
						case 0xb0:
								spectrum_128_port_bffd_w(offset, data);
								break;
						case 0xf0:
								spectrum_128_port_fffd_w(offset, data);
								break;
						case 0x10:
								spectrum_plus3_port_1ffd_w(offset, data);
								break;
						case 0x30:
								spectrum_plus3_port_3ffd_w(offset, data);
						default:
								logerror("Write %02x to +3 port: %04x\n", data, offset);
				}                        
		}
                else
		{
			logerror("Write %02x to +3 port: %04x\n", data, offset);
		}
	}};

	/* KT: Changed it to this because the ports are not decoded fully.
	The function decodes the ports appropriately */
	static IOReadPort spectrum_plus3_readport[] ={
			new IOReadPort(0x0000, 0xffff, spectrum_plus3_port_r),
			new IOReadPort( -1 )
	};
	
	/* KT: Changed it to this because the ports are not decoded fully.
	The function decodes the ports appropriately */
	static IOWritePort spectrum_plus3_writeport[] ={
			new IOWritePort(0x0000, 0xffff, spectrum_plus3_port_w),
			new IOWritePort( -1 )
	};
        
	static IODevice io_specpls3[] = {
		new IODevice(
			IO_SNAPSHOT,		/* type */
			1,					/* count */
			"sna\0z80\0",       /* file extensions */
			IO_RESET_ALL,		/* reset if file changed */
			spectrum_rom_id,	/* id */
			spectrum_rom_load,	/* init */
			spectrum_rom_exit,	/* exit */
			//null, /* exit */
                        null, /* info */
                        null, /* open */
                        null, /* close */
                        null, /* status */
                        null, /* seek */
                        null, /* tell */
                        null, /* input */
                        null, /* output */
                        null, /* input_chunk */
                        null /* output_chunk */
                ),
			IODEVICE_SPEC_QUICK,
			new IODevice(
                IO_CASSETTE,		/* type */
		1,					/* count */
		"wav\0tap\0",       /* file extensions */
		IO_RESET_NONE,		/* reset if file changed */
		null,	/* id */
		spectrum_cassette_init,	/* init */
		spectrum_cassette_exit,	/* exit */
                wave_info,			/* info */						
                wave_open,			/* open */						
                wave_close, 		/* close */ 					
                wave_status,		/* status */					
                wave_seek,			/* seek */						
                wave_tell,			/* tell */						
                wave_input, 		/* input */ 					
                wave_output,		/* output */					
                wave_input_chunk,	/* input_chunk */				
                wave_output_chunk	/* output_chunk */
                ),
		new IODevice(
			IO_FLOPPY,			/* type */
			2,					/* count */
			"dsk\0",            /* file extensions */
			IO_RESET_NONE,		/* reset if file changed */
			null,		/* id */
			dsk_floppy_load,	/* init */
			dsk_floppy_exit,	/* exit */
			null, /* info */
                        null, /* open */
                        null, /* close */
                        floppy_status, /* status */
                        null, /* seek */
                        null, /* tell */
                        null, /* input */
                        null, /* output */
                        null, /* input_chunk */
                        null /* output_chunk */
                ),
		new IODevice(IO_END)
	};
	
	static MachineDriver machine_driver_spectrum_plus3 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80|CPU_16BIT_PORT,
                                3546900,		/* 3.54690 Mhz */
                                spectrum_128_readmem,spectrum_128_writemem,
                                spectrum_plus3_readport,spectrum_plus3_writeport,
                                spec_interrupt,1
			),
		},
		50, 2500,		/* frames per second, vblank duration */
		1,
			spectrum_plus3_init_machine,
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

        static RomLoadPtr rom_specpls3 = new RomLoadPtr(){ public void handler(){ 
                        ROM_REGION(0x20000,REGION_CPU1);
                        ROM_LOAD("pl3-0.rom",0x10000,0x4000, 0x17373da2);
                        ROM_LOAD("pl3-1.rom",0x14000,0x4000, 0xf1d1d99e);
                        ROM_LOAD("pl3-2.rom",0x18000,0x4000, 0x3dbf351d);
                        ROM_LOAD("pl3-3.rom",0x1c000,0x4000, 0x04448eaa);
	ROM_END(); }}; 
	

	static RomLoadPtr rom_specp3sp = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x20000,REGION_CPU1);
			ROM_LOAD("plus3sp0.rom",0x10000,0x4000, 0x1f86147a);
			ROM_LOAD("plus3sp1.rom",0x14000,0x4000, 0xa8ac4966);
			ROM_LOAD("plus3sp2.rom",0x18000,0x4000, 0xf6bb0296);
			ROM_LOAD("plus3sp3.rom",0x1c000,0x4000, 0xf6d25389);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_specpl3e = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x20000,REGION_CPU1);
			//ROM_LOAD("roma.bin",0x10000,0x8000, 0x7c20e2c9);
                        ROM_LOAD("roma-en.rom",0x10000,0x8000, 0x2d533344);
			//ROM_LOAD("romb.bin",0x18000,0x8000, 0x4a700c7e);
                        ROM_LOAD("romb-en.rom",0x18000,0x8000, 0xef8d5d92);
	ROM_END(); }};
                
        //COMPX( 1987, specpls3, spec128,  spectrum_plus3, spectrum, 0,			 "Amstrad plc",          "ZX Spectrum +3" ,GAME_NOT_WORKING)
	public static GameDriver driver_specpls3 = new GameDriver("1987", "specpls3", "spectrum.java", rom_specpls3, null, machine_driver_spectrum_plus3, input_ports_spectrum, null, io_specpls3, "Amstrad plc", "ZX Spectrum +3");
        
        //COMPX( 1987, specp3sp, spec128,  spectrum_plus3, spectrum, 0,			 "Amstrad plc",          "ZX Spectrum +3 (Spain)" ,GAME_NOT_WORKING)
	public static GameDriver driver_specp3sp = new GameDriver("1987", "specp3sp", "spectrum.java", rom_specp3sp, null, machine_driver_spectrum_plus3, input_ports_spectrum, null, io_specpls3, "Amstrad plc", "ZX Spectrum +3 (Spain)");
        
        //COMPX( 2000, specpl3e, spec128,  spectrum_plus3, spectrum, 0,			 "Amstrad plc",          "ZX Spectrum +3e" , GAME_NOT_WORKING|GAME_COMPUTER_MODIFIED )
	public static GameDriver driver_specpl3e = new GameDriver("2000", "specpl3e", "spectrum.java", rom_specpl3e, null, machine_driver_spectrum_plus3, input_ports_spectrum, null, io_specpls3, "Amstrad plc", "ZX Spectrum +3e");
        
        //COMPX( 1987, specpl2a, spec128,  spectrum_plus3, spectrum, 0,			 "Amstrad plc",          "ZX Spectrum +2a" ,GAME_NOT_WORKING)
        public static GameDriver driver_specpl2a = new GameDriver("1987", "specpl2a", "spectrum.java", rom_specpl2a, null, machine_driver_spectrum_plus3, input_ports_spectrum, null, io_specpls3, "Amstrad plc", "ZX Spectrum +2a");
}
