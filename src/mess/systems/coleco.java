/*
 * ported to v0.37b6
 * using automatic conversion tool v0.01
 */ 
package mess.systems;

import static WIP.mame.memoryH.*;
import static sound.sn76496.*;
import static sound.sn76496H.*;
import static mess.machine.coleco.*;
import static old.mame.inptportH.*;
import static WIP.arcadeflex.fucPtr.*;
import WIP.mame.sndintrfH.MachineSound;
import static WIP.mame.sndintrfH.SOUND_SN76496;
import static mame.commonH.REGION_CPU1;
import static old.mame.cpuintrf.nmi_interrupt;
import old.mame.drawgfxH.GfxDecodeInfo;
import old.mame.drawgfxH.rectangle;
import static old.mame.inputH.*;
import static old.mame.driverH.*;
import static mame.commonH.*;
import static mess.vidhrdw.coleco.*;
import static mess.vidhrdw.tms9928aH.*;
import static mess.vidhrdw.tms9928a.*;
import static mess.messH.*;

public class coleco
{
	
	static MemoryReadAddress readmem[] =
	{
	    new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),  /* COLECO.ROM */
	    new MemoryReadAddress( 0x6000, 0x63ff, coleco_ram_r ),
	    new MemoryReadAddress( 0x6400, 0x67ff, coleco_ram_r ),
	    new MemoryReadAddress( 0x6800, 0x6bff, coleco_ram_r ),
	    new MemoryReadAddress( 0x6c00, 0x6fff, coleco_ram_r ),
	    new MemoryReadAddress( 0x7000, 0x73ff, coleco_ram_r ),
	    new MemoryReadAddress( 0x7400, 0x77ff, coleco_ram_r ),
	    new MemoryReadAddress( 0x7800, 0x7bff, coleco_ram_r ),
	    new MemoryReadAddress( 0x7c00, 0x7fff, coleco_ram_r ),
	    new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),  /* Cartridge */
		new MemoryReadAddress( -1 )	 /* end of table */
	};
	
	
	static MemoryWriteAddress writemem[] =
	{
	    new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ), /* COLECO.ROM */
	    new MemoryWriteAddress( 0x6000, 0x63ff, coleco_ram_w, coleco_ram ),
	    new MemoryWriteAddress( 0x6400, 0x67ff, coleco_ram_w ),
	    new MemoryWriteAddress( 0x6800, 0x6bff, coleco_ram_w ),
	    new MemoryWriteAddress( 0x6c00, 0x6fff, coleco_ram_w ),
	    new MemoryWriteAddress( 0x7000, 0x73ff, coleco_ram_w ),
	    new MemoryWriteAddress( 0x7400, 0x77ff, coleco_ram_w ),
	    new MemoryWriteAddress( 0x7800, 0x7bff, coleco_ram_w ),
	    new MemoryWriteAddress( 0x7c00, 0x7fff, coleco_ram_w ),
	    new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM, coleco_cartridge_rom ), /* Cartridge */
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	
	static IOReadPort readport[] =
	{
		new IOReadPort( 0xA0, 0xBF, coleco_VDP_r ),
		new IOReadPort( 0xE0, 0xFF, coleco_paddle_r ),
		new IOReadPort( -1 )	/* end of table */
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort( 0x80, 0x9F, coleco_paddle_toggle_1_w ),
		new IOWritePort( 0xA0, 0xBF, coleco_VDP_w ),
		new IOWritePort( 0xC0, 0xDF, coleco_paddle_toggle_2_w ),
		new IOWritePort( 0xE0, 0xFF, SN76496_0_w ),
		new IOWritePort( -1 )	/* end of table */
	};
	
	
	
	
	static InputPortPtr input_ports_coleco = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* IN0 */
	    PORT_BITX( 0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "0", KEYCODE_0, IP_JOY_DEFAULT);
	    PORT_BITX( 0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "1", KEYCODE_1, IP_JOY_DEFAULT);
	    PORT_BITX( 0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "2", KEYCODE_2, IP_JOY_DEFAULT);
	    PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "3", KEYCODE_3, IP_JOY_DEFAULT);
	    PORT_BITX( 0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "4", KEYCODE_4, IP_JOY_DEFAULT);
	    PORT_BITX( 0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "5", KEYCODE_5, IP_JOY_DEFAULT);
	    PORT_BITX( 0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "6", KEYCODE_6, IP_JOY_DEFAULT);
	    PORT_BITX( 0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "7", KEYCODE_7, IP_JOY_DEFAULT);
	
	
		PORT_START(); 	/* IN1 */
	    PORT_BITX( 0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "8", KEYCODE_8, IP_JOY_DEFAULT);
	    PORT_BITX( 0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "9", KEYCODE_9, IP_JOY_DEFAULT);
	    PORT_BITX( 0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "#", KEYCODE_MINUS, IP_JOY_DEFAULT);
	    PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, ".", KEYCODE_EQUALS, IP_JOY_DEFAULT);
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT ( 0xB0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT ( 0xB0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN3 */
	    PORT_BITX( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 0", KEYCODE_0_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 1", KEYCODE_1_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 2", KEYCODE_2_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 3", KEYCODE_3_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 4", KEYCODE_4_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 5", KEYCODE_5_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 6", KEYCODE_6_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 7", KEYCODE_7_PAD, IP_JOY_DEFAULT );
	
		PORT_START(); 	/* IN4 */
	    PORT_BITX( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 8", KEYCODE_8_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 9", KEYCODE_9_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 #", KEYCODE_PLUS_PAD, IP_JOY_DEFAULT );
	    PORT_BITX( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN, "2 .", KEYCODE_MINUS_PAD, IP_JOY_DEFAULT );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT ( 0xB0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 	/* IN5 */
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2 );
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2 );
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT ( 0xB0, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
	INPUT_PORTS_END(); }}; 
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static SN76496interface sn76496_interface = new SN76496interface
	(
		1,	/* 1 chip */
		new int[] {3579545},	/* 3.579545 MHz */
		new int[] { 100 }
	);
	
	public static InterruptPtr coleco_interrupt = new InterruptPtr() { public int handler() 
	{
		if (TMS9928A_interrupt()!=0)
			return nmi_interrupt.handler();
	
		return 0;
	} };
	
	static MachineDriver machine_driver_coleco = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3579545,	/* 3.579545 Mhz */
				readmem,writemem,readport,writeport,
				coleco_interrupt,1
			)
		},
		60, DEFAULT_REAL_60HZ_VBLANK_DURATION,	/* frames per second, vblank duration */
		1,
		null, /* init_machine */
		null, /* stop_machine */
	
		/* video hardware */
		32*8, 24*8, new rectangle( 0*8, 32*8-1, 0*8, 24*8-1 ),
		gfxdecodeinfo,
		TMS9928A_PALETTE_SIZE,TMS9928A_COLORTABLE_SIZE,
		tms9928A_init_palette,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		null,
		coleco_vh_start,
		coleco_vh_stop,
		coleco_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_SN76496,
				sn76496_interface
			)
		}
	);
	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
        static RomLoadPtr coleco = new RomLoadPtr() {
            public void handler() {
		ROM_REGION(0x10000,REGION_CPU1);
		ROM_LOAD ("coleco.rom", 0x0000, 0x2000, 0x3aa93ef3);
                ROM_END();
	 }}; 
	
	//ROM_START (colecofb_rom)
	//	ROM_REGIONX(0x10000,REGION_CPU1)
	//	ROM_LOAD ("colecofb.rom", 0x0000, 0x2000, 0x640cf85b);/* fast screen */
	//ROM_END(); }}; 
	
	//ROM_START (coleconb_rom)
	//	ROM_REGIONX(0x10000,REGION_CPU1)
	//	ROM_LOAD ("coleconb.rom", 0x0000, 0x2000, 0x66cda476);/* no screen */
	//ROM_END(); }}; 
	
	static IODevice io_coleco[] = {
		new IODevice(
			IO_CARTSLOT,		/* type */
			1,					/* count */
			"rom\0",            /* file extensions */
			IO_RESET_CPU,		/* reset if file changed */
                        coleco_id_rom,      /* id */
			coleco_load_rom,	/* init */
			null,				/* exit */
			null,				/* info */
			null,				/* open */
			null,				/* close */
			null,				/* status */
			null,				/* seek */
			null,				/* tell */
                        null,               /* input */
			null,				/* output */
			null,				/* input_chunk */
			null				/* output_chunk */
                ),
		new IODevice( IO_END )
	};
	
	/*    YEAR  NAME      PARENT    MACHINE   INPUT     INIT      COMPANY   FULLNAME */
	CONS( 1982, coleco,   0,		coleco,   coleco,	0,		  "Coleco", "Colecovision" )
	
	//#ifdef COLECO_HACKS
	//CONS( 1982, colecofb, coleco,	coleco,   coleco,	0,		  "Coleco", "Colecovision (Fast BIOS Hack)" )
	//CONS( 1982, coleconb, coleco,	coleco,   coleco,	0,		  "Coleco", "Colecovision (NO BIOS Hack)" )
	//#endif
	
}
