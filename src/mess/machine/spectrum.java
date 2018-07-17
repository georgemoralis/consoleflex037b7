/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

  Changes:

  KT 31/1/00 - Added support for .Z80. At the moment only 48k files are supported!
  DJR 8/2/00 - Added checks to avoid trying to load 128K .Z80 files into 48K machine!
  DJR 20/2/00 - Added support for .TAP files.
  -----------------27/02/00 10:54-------------------
  KT 27/2/00 - Added my changes for the WAV support
  --------------------------------------------------
  DJR 14/3/00 - Fixed +3 tape loading and added option to 'rewind' tapes when end reached.
  DJR 21/4/00 - Added support for 128K .SNA and .Z80 files.
  DJR 21/4/00 - Ensure 48K Basic ROM is used when running 48K snapshots on 128K machine.
  DJR 03/5/00 - Fixed bug of not decoding last byte of .Z80 blocks.
  DJR 08/5/00 - Fixed TS2068 .TAP loading.
  DJR 19/5/00 - .TAP files are now classified as cassette files.
  DJR 02/6/00 - Added support for .SCR files (screendumps).

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.machine;

import WIP.arcadeflex.libc_v2.*;
import static WIP.arcadeflex.fucPtr.*;
import static WIP.mame.mame.Machine;
import static WIP.mame.osdependH.*;
import static WIP.mame.memory.*;
import static old.arcadeflex.osdepend.logerror;
import static old.mame.cpuintrf.*;
import static mess.messH.*;
import static mess.mess.*;
import static mess.osdepend.fileio.*;
import static cpu.z80.z80H.*;

import static arcadeflex.libc.cstring.*;
import consoleflex.funcPtr.*;

import static mess.systems.spectrum.*;
import static mess.includes.spectrumH.*;
import static old.arcadeflex.osdepend.logerror;

public class spectrum
{
	
	/*#ifndef MIN
	#define MIN(x,y) ((x)<(y)?(x):(y))
	#endif*/
	
	static UBytePtr pSnapshotData = null;
	static long SnapshotDataSize = 0;
	static  long TapePosition = 0;
	/*static void spectrum_setup_sna(UBytePtr pSnapshot, unsigned long SnapshotSize);
	static void spectrum_setup_z80(UBytePtr pSnapshot, unsigned long SnapshotSize);
	static int is48k_z80snapshot(UBytePtr pSnapshot, unsigned long SnapshotSize);
	static OPBASE_HANDLER(spectrum_opbaseoverride);
	static OPBASE_HANDLER(spectrum_tape_opbaseoverride);*/
	
	public static final int SPECTRUM_SNAPSHOT_NONE  =   0;
	public static final int SPECTRUM_SNAPSHOT_SNA   =   1;
	public static final int SPECTRUM_SNAPSHOT_Z80   =   2;
	public static final int SPECTRUM_TAPEFILE_TAP   =   3;
        
        public static int SPECTRUM_SNAPSHOT_TYPE = SPECTRUM_SNAPSHOT_NONE;
	
	
        
        public static class quick_struct {
            public String name;
            public short addr;
            public int u8_data;
            public int lenght;
        };
	
	public static InitMachinePtr spectrum_init_machine = new InitMachinePtr() { 
            public void handler() 
            {
		if (pSnapshotData != null)
		{
			if (SPECTRUM_SNAPSHOT_TYPE == SPECTRUM_TAPEFILE_TAP)
			{
				/*TODO*/////logerror(".TAP file support enabled\n") ;
				/*TODO*/////cpu_setOPbaseoverride(0, spectrum_tape_opbaseoverride);
			}
                        else
                        {
				/*TODO*/////cpu_setOPbaseoverride(0, spectrum_opbaseoverride);
                        }
		}
            } 
        };
	
	static void spectrum_shutdown_machine()
	{
	}
        
        public static StopMachinePtr spectrum_shutdown_machine = new StopMachinePtr() {
            
            public void handler() {
                spectrum_shutdown_machine();
            }
        };
        
        public static io_exitPtr spectrum_rom_exit = new io_exitPtr() {
            public int handler(int id) {
               spectrum_rom_exit(id);
               return 0;
            }
        };
        
        public static io_initPtr spectrum_rom_load = new io_initPtr() {
            public int handler(int id) {
	
		//void *file;
                Object file=null;
	
		//file = image_fopen(IO_SNAPSHOT, id, OSD_FILETYPE_IMAGE_RW, OSD_FOPEN_READ);
                
                System.out.println("No LOAD");
                //file = image_fopen(IO_SNAPSHOT, id, OSD_FILETYPE_IMAGE_RW, 0);
	
		//if (file != 0)
                if (file != null)
		{
                    logerror("---------zx spectrum_load_rom-----\n");
                    logerror("filetype is %d  \n", OSD_FILETYPE_IMAGE_R);
                    logerror("Machine.game.name is %s  \n", Machine.gamedrv.name);	
                    
                    int datasize;
			UBytePtr data;
	
			datasize = osd_fsize(file);
	
			if (datasize != 0)
			{
				//data = malloc(datasize);
                                data = new UBytePtr(datasize);
	
				if (data != null)
				{
					pSnapshotData = data;
					SnapshotDataSize = datasize;
	
					osd_fread(file, data, datasize);
					osd_fclose(file);
                                        
                                        System.out.println("KK ELIMINAR!!!!");
                                        System.out.println(device_filename(IO_SNAPSHOT, id));
                                        System.out.println(strlen(device_filename(IO_SNAPSHOT, id) ));
	
					//if (!stricmp(device_filename(IO_SNAPSHOT, id) + strlen(device_filename(IO_SNAPSHOT, id) ) - 4, ".sna"))
                                        if (true)
					{
						if ((SnapshotDataSize != 49179) && (SnapshotDataSize != 131103) && (SnapshotDataSize != 14787))
						{
							logerror("Invalid .SNA file size\n");
							return 1;
						}
						SPECTRUM_SNAPSHOT_TYPE = SPECTRUM_SNAPSHOT_SNA;
					}
					else
						SPECTRUM_SNAPSHOT_TYPE = SPECTRUM_SNAPSHOT_Z80;
	
					logerror("File loaded!\n");
					return 0;
				}
				osd_fclose(file);
			}
			return 1;
		}
		return 0;
            }
        };
	
	static void spectrum_rom_exit(int id)
	{
		if (pSnapshotData != null)
		{
			/* free snapshot/tape data */
			//free(pSnapshotData);
			pSnapshotData = null;
	
			/* ensure op base is cleared */
			/*TODO*/////cpu_setOPbaseoverride(0, 0);
		}
	
		/* reset type to none */
		SPECTRUM_SNAPSHOT_TYPE = SPECTRUM_SNAPSHOT_NONE;
	}
	
	
	/*TODO*/////static OPBASE_HANDLER(spectrum_opbaseoverride)
	/*TODO*/////{
		/* clear op base override */
	/*TODO*/////	cpu_setOPbaseoverride(0, 0);
	
	/*TODO*/////	if (pSnapshotData != 0)
	/*TODO*/////	{
			/* snapshot loaded setup */
	
	/*TODO*/////		switch (SPECTRUM_SNAPSHOT_TYPE)
	/*TODO*/////		{
	/*TODO*/////		case SPECTRUM_SNAPSHOT_SNA:
	/*TODO*/////			{
					/* .SNA */
	/*TODO*/////				spectrum_setup_sna(pSnapshotData, SnapshotDataSize);
	/*TODO*/////			}
	/*TODO*/////			break;
	
	/*TODO*/////		case SPECTRUM_SNAPSHOT_Z80:
	/*TODO*/////			{
					/* .Z80 */
	/*TODO*/////				spectrum_setup_z80(pSnapshotData, SnapshotDataSize);
	/*TODO*/////			}
	/*TODO*/////			break;
	
	/*TODO*/////		default:
				/* SPECTRUM_TAPEFILE_TAP is handled by spectrum_tape_opbaseoverride */
	/*TODO*/////			break;
	/*TODO*/////		}
	/*TODO*/////	}
	/*TODO*/////	logerror("Snapshot loaded - new PC = %04x\n", cpu_get_reg(Z80_PC) & 0x0ffff);
	
	/*TODO*/////	return (cpu_get_reg(Z80_PC) & 0x0ffff);
	/*TODO*/////}
	
	/*******************************************************************
	 *
	 *      Override load routine (0x0556 in 48K ROM) if loading .TAP files
	 *      Tape blocks are as follows.
	 *      2 bytes length of block excluding these 2 bytes (LSB first)
	 *      1 byte  flag byte (0x00 for headers, 0xff for data)
	 *      n bytes data
	 *      1 byte  checksum
	 *
	 *      The load routine uses the following registers:
	 *      IX              Start address for block
	 *      DE              Length of block
	 *      A               Flag byte (as above)
	 *      Carry Flag      Set for Load, reset for verify
	 *
	 *      On exit the carry flag is set if loading/verifying was successful.
	 *
	 *      Note: it is not always possible to trap the exact entry to the
	 *      load routine so things get rather messy!
	 *
	 *******************************************************************/
	/*TODO*/////static OPBASE_HANDLER(spectrum_tape_opbaseoverride)
	/*TODO*/////{
	/*TODO*/////	int i, tap_block_length, load_length;
	/*TODO*/////	char lo, hi, a_reg;
	/*TODO*/////	short load_addr, return_addr, af_reg, de_reg, sp_reg;
	/*TODO*/////	static int data_loaded = 0;			/* Whether any data files (not headers) were loaded */
	
	/*        logerror("PC=%02x\n", address); */
	
		/* It is not always possible to trap the call to the actual load
		 * routine so trap the LD-EDGE-1 and LD-EDGE-2 routines which
		 * check the earphone socket.
		 */
	/*TODO*/////	if (ts2068_port_f4_data == -1)
	/*TODO*/////	{
	/*TODO*/////		if ((address < 0x05e3) || (address > 0x0604))
	/*TODO*/////			return address;
	
			/* For Spectrum 128/+2/+3 check which rom is paged */
	/*TODO*/////		if ((spectrum_128_port_7ffd_data != -1) || (spectrum_plus3_port_1ffd_data != -1))
	/*TODO*/////		{
	/*TODO*/////			if (spectrum_plus3_port_1ffd_data != -1)
	/*TODO*/////			{
	/*TODO*/////				if (!spectrum_plus3_port_1ffd_data & 0x04)
	/*TODO*/////					return address;
	/*TODO*/////			}
	/*TODO*/////			if (!spectrum_128_port_7ffd_data & 0x10)
	/*TODO*/////				return address;
	/*TODO*/////		}
	/*TODO*/////	}
	/*TODO*/////	else
	/*TODO*/////	{
			/* For TS2068 also check that EXROM is paged into bottom 8K.
			 * Code is not relocatable so don't need to check EXROM in other pages.
			 */
	/*TODO*/////		if ((!ts2068_port_f4_data & 0x01) || (!ts2068_port_ff_data & 0x80))
	/*TODO*/////			return address;
	/*TODO*/////		if ((address < 0x018d) || (address > 0x01aa))
	/*TODO*/////			return address;
	/*TODO*/////	}
	
	/*TODO*/////	lo = pSnapshotData[TapePosition] & 0x0ff;
	/*TODO*/////	hi = pSnapshotData[TapePosition + 1] & 0x0ff;
	/*TODO*/////	tap_block_length = (hi << 8) | lo;
	
		/* By the time that load has been trapped the block type and carry
		 * flags are in the AF' register. */
	/*TODO*/////	af_reg = cpu_get_reg(Z80_AF2);
	/*TODO*/////	a_reg = (af_reg & 0xff00) >> 8;
	
	/*TODO*/////	if ((a_reg == pSnapshotData[TapePosition + 2]) && (af_reg & 0x0001))
	/*TODO*/////	{
			/* Correct flag byte and carry flag set so try loading */
	/*TODO*/////		load_addr = cpu_get_reg(Z80_IX);
	/*TODO*/////		de_reg = cpu_get_reg(Z80_DE);
	
	/*TODO*/////		load_length = MIN(de_reg, tap_block_length - 2);
	/*TODO*/////		load_length = MIN(load_length, 65536 - load_addr);
			/* Actual number of bytes of block that can be loaded -
			 * Don't try to load past the end of memory */
	
	/*TODO*/////		for (i = 0; i < load_length; i++)
	/*TODO*/////			cpu_writemem16(load_addr + i, pSnapshotData[TapePosition + i + 3]);
	/*TODO*/////		cpu_set_reg(Z80_IX, load_addr + load_length);
	/*TODO*/////		cpu_set_reg(Z80_DE, de_reg - load_length);
	/*TODO*/////		if (de_reg == (tap_block_length - 2))
	/*TODO*/////		{
				/* Successful load - Set carry flag and A to 0 */
	/*TODO*/////			if ((de_reg != 17) || (a_reg))
	/*TODO*/////				data_loaded = 1;		/* Non-header file loaded */
	/*TODO*/////			cpu_set_reg(Z80_AF, (af_reg & 0x00ff) | 0x0001);
	/*TODO*/////			logerror("Loaded %04x bytes from address %04x onwards (type=%02x) using tape block at offset %ld\n", load_length,
	/*TODO*/////					 load_addr, a_reg, TapePosition);
	/*TODO*/////		}
	/*TODO*/////		else
	/*TODO*/////		{
				/* Wrong tape block size - reset carry flag */
	/*TODO*/////			cpu_set_reg(Z80_AF, af_reg & 0xfffe);
	/*TODO*/////			logerror("Bad block length %04x bytes wanted starting at address %04x (type=%02x) , Data length of tape block at offset %ld is %04x bytes\n",
	/*TODO*/////					 de_reg, load_addr, a_reg, TapePosition, tap_block_length - 2);
	/*TODO*/////		}
	/*TODO*/////	}
	/*TODO*/////	else
	/*TODO*/////	{
	/*TODO*/////		/* Wrong flag byte or verify selected so reset carry flag to indicate failure */
	/*TODO*/////		cpu_set_reg(Z80_AF, af_reg & 0xfffe);
	/*TODO*/////		if ((af_reg & 0x0001) != 0)
	/*TODO*/////			logerror("Failed to load tape block at offset %ld - type wanted %02x, got type %02x\n", TapePosition, a_reg,
	/*TODO*/////					 pSnapshotData[TapePosition + 2]);
	/*TODO*/////		else
	/*TODO*/////			logerror("Failed to load tape block at offset %ld - verify selected\n", TapePosition);
	/*TODO*/////	}
	
	/*TODO*/////	TapePosition += (tap_block_length + 2);
	/*TODO*/////	if (TapePosition >= SnapshotDataSize)
	/*TODO*/////	{
			/* End of tape - either rewind or disable op base override */
	/*TODO*/////		if (readinputport(16) & 0x40)
	/*TODO*/////		{
	/*TODO*/////			if (data_loaded != 0)
	/*TODO*/////			{
	/*TODO*/////				TapePosition = 0;
	/*TODO*/////				data_loaded = 0;
	/*TODO*/////				logerror("All tape blocks used! - rewinding tape to start\n");
	/*TODO*/////			}
	/*TODO*/////			else
	/*TODO*/////			{
					/* Disable .TAP support if no files were loaded to avoid getting caught in infinite loop */
	/*TODO*/////				cpu_setOPbaseoverride(0, 0);
	/*TODO*/////				logerror("No valid data loaded! - disabling .TAP support\n");
	/*TODO*/////			}
	/*TODO*/////		}
	/*TODO*/////		else
	/*TODO*/////		{
	/*TODO*/////			cpu_setOPbaseoverride(0, 0);
	/*TODO*/////			logerror("All tape blocks used! - disabling .TAP support\n");
	/*TODO*/////		}
	/*TODO*/////	}
	
		/* Leave the load routine by removing addresses from the stack
		 * until one outside the load routine is found.
		 * eg. SA/LD-RET at address 053f (00e5 on TS2068)
		 */
	/*TODO*/////	do
	/*TODO*/////	{
	/*TODO*/////		return_addr = cpu_geturnpc();
	/*TODO*/////		cpu_set_reg(Z80_PC, (return_addr & 0x0ffff));
	
	/*TODO*/////		sp_reg = cpu_get_reg(Z80_SP);
	/*TODO*/////		sp_reg += 2;
	/*TODO*/////		cpu_set_reg(Z80_SP, (sp_reg & 0x0ffff));
	/*TODO*/////		cpu_set_sp((sp_reg & 0x0ffff));
	/*TODO*/////	}
	/*TODO*/////	while (((return_addr != 0x053f) && (return_addr < 0x0605) && (ts2068_port_f4_data == -1)) ||
	/*TODO*/////		   ((return_addr != 0x00e5) && (return_addr < 0x01aa) && (ts2068_port_f4_data != -1)));
	/*TODO*/////	logerror("Load return address=%04x, SP=%04x\n", return_addr, sp_reg);
	/*TODO*/////	return return_addr;
	/*TODO*/////}
	
	/*******************************************************************
	 *
	 *      Update the memory and paging of the spectrum being emulated
	 *
	 *      if port_7ffd_data is -1 then machine is 48K - no paging
	 *      if port_1ffd_data is -1 then machine is 128K
	 *      if neither port is -1 then machine is +2a/+3
	 *
	 *      Note: the 128K .SNA and .Z80 file formats do not store the
	 *      port 1FFD setting so it is necessary to calculate the appropriate
	 *      value for the ROM paging.
	 *
	 *******************************************************************/
	static void spectrum_update_paging()
	{
		if (spectrum_128_port_7ffd_data == -1)
			return;
		if (spectrum_plus3_port_1ffd_data == -1){
			/*TODO*/////spectrum_128_update_memory();
                }else
		{
			if ((spectrum_128_port_7ffd_data & 0x10) != 0)
				/* Page in Spec 48K basic ROM */
				spectrum_plus3_port_1ffd_data = 0x04;
                        else{
				spectrum_plus3_port_1ffd_data = 0;
                        }
			/*TODO*/////spectrum_plus3_update_memory();
		}
	}
	
	/* Page in the 48K Basic ROM. Used when running 48K snapshots on a 128K machine. */
	static void spectrum_page_basicrom()
	{
		if (spectrum_128_port_7ffd_data == -1)
			return;
		spectrum_128_port_7ffd_data |= 0x10;
		spectrum_update_paging();
	}
	
	/* Dump the state of registers after loading a snapshot to the log file for debugging */
	static void dump_registers()
	{
		logerror("PC   = %04x\n", cpu_get_reg(Z80_PC));
		logerror("SP   = %04x\n", cpu_get_reg(Z80_SP));
		logerror("AF   = %04x\n", cpu_get_reg(Z80_AF));
		logerror("BC   = %04x\n", cpu_get_reg(Z80_BC));
		logerror("DE   = %04x\n", cpu_get_reg(Z80_DE));
		logerror("HL   = %04x\n", cpu_get_reg(Z80_HL));
		logerror("IX   = %04x\n", cpu_get_reg(Z80_IX));
		logerror("IY   = %04x\n", cpu_get_reg(Z80_IY));
		logerror("AF'  = %04x\n", cpu_get_reg(Z80_AF2));
		logerror("BC'  = %04x\n", cpu_get_reg(Z80_BC2));
		logerror("DE'  = %04x\n", cpu_get_reg(Z80_DE2));
		logerror("HL'  = %04x\n", cpu_get_reg(Z80_HL2));
		logerror("I    = %02x\n", cpu_get_reg(Z80_I));
		logerror("R    = %02x\n", cpu_get_reg(Z80_R));
		logerror("IFF1 = %02x\n", cpu_get_reg(Z80_IFF1));
		logerror("IFF2 = %02x\n", cpu_get_reg(Z80_IFF2));
		logerror("IM   = %02x\n", cpu_get_reg(Z80_IM));
		logerror("NMI  = %02x\n", cpu_get_reg(Z80_NMI_STATE));
		logerror("IRQ  = %02x\n", cpu_get_reg(Z80_IRQ_STATE));
	}
	
	/*******************************************************************
	 *
	 *      Load a 48K or 128K .SNA file.
	 *
	 *      48K Format as follows:
	 *      Offset  Size    Description (all registers stored with LSB first)
	 *      0       1       I
	 *      1       18      HL',DE',BC',AF',HL,DE,BC,IY,IX
	 *      19      1       Interrupt (bit 2 contains IFF2 1=EI/0=DI
	 *      20      1       R
	 *      21      4       AF,SP
	 *      25      1       Interrupt Mode (0=IM0/1=IM1/2=IM2)
	 *      26      1       Border Colour (0..7)
	 *      27      48K     RAM dump 0x4000-0xffff
	 *      PC is stored on stack.
	 *
	 *      128K Format as follows:
	 *      Offset  Size    Description
	 *      0       27      Header as 48K
	 *      27      16K     RAM bank 5 (0x4000-0x7fff)
	 *      16411   16K     RAM bank 2 (0x8000-0xbfff)
	 *      32795   16K     RAM bank n (0xc000-0xffff - currently paged bank)
	 *      49179   2       PC
	 *      49181   1       port 7FFD setting
	 *      49182   1       TR-DOS rom paged (1=yes)
	 *      49183   16K     remaining RAM banks in ascending order
	 *
	 *      The bank in 0xc000 is always included even if it is page 2 or 5
	 *      in which case it is included twice.
	 *
	 *******************************************************************/
	void spectrum_setup_sna(UBytePtr pSnapshot, long SnapshotSize)
	{
		int i, j=0;
                int[] usedbanks=new int[8];
		//long bank_offset;
                int bank_offset;
		//char lo, hi, data;
                int lo, hi, data;
		int addr;
	
		if ((SnapshotDataSize != 49179) && (spectrum_128_port_7ffd_data == -1))
		{
			logerror("Can't load 128K .SNA file into 48K Machine\n");
			return;
		}
	
		cpu_set_reg(Z80_I, (pSnapshot.read(0) & 0x0ff));
		//lo = pSnapshot[1] & 0x0ff;
                lo = pSnapshot.read(1) & 0x0ff;
		//hi = pSnapshot[2] & 0x0ff;
                hi = pSnapshot.read(2) & 0x0ff;
		cpu_set_reg(Z80_HL2, (hi << 8) | lo);
		//lo = pSnapshot[3] & 0x0ff;
                lo = pSnapshot.read(3) & 0x0ff;
		//hi = pSnapshot[4] & 0x0ff;
                hi = pSnapshot.read(4) & 0x0ff;
		cpu_set_reg(Z80_DE2, (hi << 8) | lo);
		lo = pSnapshot.read(5) & 0x0ff;
		hi = pSnapshot.read(6) & 0x0ff;
		cpu_set_reg(Z80_BC2, (hi << 8) | lo);
		lo = pSnapshot.read(7) & 0x0ff;
		hi = pSnapshot.read(8) & 0x0ff;
		cpu_set_reg(Z80_AF2, (hi << 8) | lo);
		lo = pSnapshot.read(9) & 0x0ff;
		hi = pSnapshot.read(10) & 0x0ff;
		cpu_set_reg(Z80_HL, (hi << 8) | lo);
		lo = pSnapshot.read(11) & 0x0ff;
		hi = pSnapshot.read(12) & 0x0ff;
		cpu_set_reg(Z80_DE, (hi << 8) | lo);
		lo = pSnapshot.read(13) & 0x0ff;
		hi = pSnapshot.read(14) & 0x0ff;
		cpu_set_reg(Z80_BC, (hi << 8) | lo);
		lo = pSnapshot.read(15) & 0x0ff;
		hi = pSnapshot.read(16) & 0x0ff;
		cpu_set_reg(Z80_IY, (hi << 8) | lo);
		lo = pSnapshot.read(17) & 0x0ff;
		hi = pSnapshot.read(18) & 0x0ff;
		cpu_set_reg(Z80_IX, (hi << 8) | lo);
		data = (pSnapshot.read(19) & 0x04) >> 2;
		cpu_set_reg(Z80_IFF2, data);
		cpu_set_reg(Z80_IFF1, data);
		data = (pSnapshot.read(20) & 0x0ff);
		cpu_set_reg(Z80_R, data);
		lo = pSnapshot.read(21) & 0x0ff;
		hi = pSnapshot.read(22) & 0x0ff;
		cpu_set_reg(Z80_AF, (hi << 8) | lo);
		lo = pSnapshot.read(23) & 0x0ff;
		hi = pSnapshot.read(24) & 0x0ff;
		cpu_set_reg(Z80_SP, (hi << 8) | lo);
		//cpu_set_sp((hi << 8) | lo);
		data = (pSnapshot.read(25) & 0x0ff);
		cpu_set_reg(Z80_IM, data);
	
		/* Set border colour */
		PreviousFE = (PreviousFE & 0xf8) | (pSnapshot.read(26) & 0x07);
		/*TODO*/////EventList_Reset();
		/*TODO*/////set_last_border_color(pSnapshot.read(26) & 0x07);
		/*TODO*/////force_border_redraw();
	
		cpu_set_reg(Z80_NMI_STATE, 0);
		cpu_set_reg(Z80_IRQ_STATE, 0);
		cpu_set_reg(Z80_HALT, 0);
	
		if (SnapshotDataSize == 49179)
			/* 48K Snapshot */
			spectrum_page_basicrom();
		else
		{
			/* 128K Snapshot */
			spectrum_128_port_7ffd_data = (pSnapshot.read(49181) & 0x0ff);
			spectrum_update_paging();
		}
	
		/* memory dump */
		for (i = 0; i < 49152; i++)
		{
			cpu_writemem16(i + 16384, pSnapshot.read(27 + i));
		}
	
		if (SnapshotDataSize == 49179)
		{
			/* get pc from stack */
			//addr = cpu_geturnpc();
                        addr = cpu_get_reg(Z80_PC);
			cpu_set_reg(Z80_PC, (addr & 0x0ffff));
	
			addr = cpu_get_reg(Z80_SP);
			addr += 2;
			cpu_set_reg(Z80_SP, (addr & 0x0ffff));
			//cpu_set_sp((addr & 0x0ffff));
		}
		else
		{
			/* Set up other RAM banks */
			bank_offset = 49183;
			for (i = 0; i < 8; i++)
				usedbanks[i] = 0;
	
			usedbanks[5] = 1;				/* 0x4000-0x7fff */
			usedbanks[2] = 1;				/* 0x8000-0xbfff */
			usedbanks[spectrum_128_port_7ffd_data & 0x07] = 1;	/* Banked memory */
	
			for (i = 0; i < 8; i++)
			{
				//if (!usedbanks[i])
                                if (usedbanks[i]==0)
				{
					logerror("Loading bank %d from offset %ld\n", i, bank_offset);
					spectrum_128_port_7ffd_data &= 0xf8;
					spectrum_128_port_7ffd_data += i;
					spectrum_update_paging();
					for (j = 0; j < 16384; j++)
						cpu_writemem16(j + 49152, pSnapshot.read(bank_offset + j));
					bank_offset += 16384;
				}
			}
	
			/* Reset paging */
			spectrum_128_port_7ffd_data = (pSnapshot.read(49181) & 0x0ff);
			spectrum_update_paging();
	
			/* program counter */
			lo = pSnapshot.read(49179) & 0x0ff;
			hi = pSnapshot.read(49180) & 0x0ff;
			cpu_set_reg(Z80_PC, (hi << 8) | lo);
		}
		dump_registers();
	}
	
	
	static void spectrum_z80_decompress_block(UBytePtr pSource, int Dest, int size)
	{
		char ch;
		int i;
	
		do
		{
			/* get byte */
			ch = pSource.read(0);
	
			/* either start 0f 0x0ed, 0x0ed, xx yy or
			 * single 0x0ed */
			if (ch == (char) 0x0ed)
			{
				if (pSource.read(1) == (char) 0x0ed)
				{
	
					/* 0x0ed, 0x0ed, xx yy */
					/* repetition */
	
					int count;
					int data;
	
					count = (pSource.read(2) & 0x0ff);
	
					if (count == 0)
						return;
	
					data = (pSource.read(3) & 0x0ff);
	
					//pSource += 4;
                                        pSource.offset=(4);
	
					if (count > size)
						count = size;
	
					size -= count;
	
					for (i = 0; i < count; i++)
					{
						cpu_writemem16(Dest, data);
						Dest++;
					}
				}
				else
				{
					/* single 0x0ed */
					cpu_writemem16(Dest, ch);
					Dest++;
					//pSource++;
                                        pSource.inc();
					size--;
	
				}
			}
			else
			{
				/* not 0x0ed */
				cpu_writemem16(Dest, ch);
				Dest++;
				//pSource++;
                                pSource.inc();
				size--;
			}
	
		}
		while (size > 0);
	}
	
	/* now supports 48k & 128k .Z80 files */
	void spectrum_setup_z80(UBytePtr pSnapshot, long SnapshotSize)
	{
		int i, is48ksnap;
		//char lo, hi, data;
                int lo, hi, data;
	
		is48ksnap = is48k_z80snapshot(pSnapshotData, SnapshotDataSize);
		//if ((spectrum_128_port_7ffd_data == -1) && !is48ksnap)
                if ((spectrum_128_port_7ffd_data == -1) && (is48ksnap !=1))
		{
			logerror("Not a 48K .Z80 file\n");
			return;
		}
	
		/* AF */
		hi = pSnapshot.read(0) & 0x0ff;
		lo = pSnapshot.read(1) & 0x0ff;
		cpu_set_reg(Z80_AF, (hi << 8) | lo);
		/* BC */
		lo = pSnapshot.read(2) & 0x0ff;
		hi = pSnapshot.read(3) & 0x0ff;
		cpu_set_reg(Z80_BC, (hi << 8) | lo);
		/* HL */
		lo = pSnapshot.read(4) & 0x0ff;
		hi = pSnapshot.read(5) & 0x0ff;
		cpu_set_reg(Z80_HL, (hi << 8) | lo);
	
		/* program counter - 0 if not version 1.45 */
	
		/* SP */
		lo = pSnapshot.read(8) & 0x0ff;
		hi = pSnapshot.read(9) & 0x0ff;
		cpu_set_reg(Z80_SP, (hi << 8) | lo);
		//cpu_set_sp((hi << 8) | lo);
	
		/* I */
		cpu_set_reg(Z80_I, (pSnapshot.read(10) & 0x0ff));
	
		/* R */
		data = (pSnapshot.read(11) & 0x07f) | ((pSnapshot.read(12) & 0x01) << 7);
		cpu_set_reg(Z80_R, data);
	
		/* Set border colour */
		PreviousFE = (PreviousFE & 0xf8) | ((pSnapshot.read(12) & 0x0e) >> 1);
		/*TODO*/////EventList_Reset();
		/*TODO*/////set_last_border_color((pSnapshot.read(12) & 0x0e) >> 1);
		/*TODO*/////force_border_redraw();
	
		lo = pSnapshot.read(13) & 0x0ff;
		hi = pSnapshot.read(14) & 0x0ff;
		cpu_set_reg(Z80_DE, (hi << 8) | lo);
	
		lo = pSnapshot.read(15) & 0x0ff;
		hi = pSnapshot.read(16) & 0x0ff;
		cpu_set_reg(Z80_BC2, (hi << 8) | lo);
	
		lo = pSnapshot.read(17) & 0x0ff;
		hi = pSnapshot.read(18) & 0x0ff;
		cpu_set_reg(Z80_DE2, (hi << 8) | lo);
	
		lo = pSnapshot.read(19) & 0x0ff;
		hi = pSnapshot.read(20) & 0x0ff;
		cpu_set_reg(Z80_HL2, (hi << 8) | lo);
	
		hi = pSnapshot.read(21) & 0x0ff;
		lo = pSnapshot.read(22) & 0x0ff;
		cpu_set_reg(Z80_AF2, (hi << 8) | lo);
	
		lo = pSnapshot.read(23) & 0x0ff;
		hi = pSnapshot.read(24) & 0x0ff;
		cpu_set_reg(Z80_IY, (hi << 8) | lo);
	
		lo = pSnapshot.read(25) & 0x0ff;
		hi = pSnapshot.read(26) & 0x0ff;
		cpu_set_reg(Z80_IX, (hi << 8) | lo);
	
		/* Interrupt Flip/Flop */
		if (pSnapshot.read(27) == 0)
		{
			cpu_set_reg(Z80_IFF1, 0);
			//cpu_set_reg(Z80_IRQ_STATE, 0);
		}
		else
		{
			cpu_set_reg(Z80_IFF1, 1);
			//cpu_set_reg(Z80_IRQ_STATE, 1);
		}
	
		cpu_set_reg(Z80_NMI_STATE, 0);
		cpu_set_reg(Z80_IRQ_STATE, 0);
		cpu_set_reg(Z80_HALT, 0);
	
	
		/* IFF2 */
		if (pSnapshot.read(28) != 0)
		{
			data = 1;
		}
		else
		{
			data = 0;
		}
		cpu_set_reg(Z80_IFF2, data);
	
		/* Interrupt Mode */
		cpu_set_reg(Z80_IM, (pSnapshot.read(29) & 0x03));
	
		//if ((pSnapshot.read(6) | pSnapshot.read(7) != 0)
                if ((pSnapshot.read(6)!=0) | (pSnapshot.read(7) != 0))
		{
			logerror("Old 1.45 V of Z80 snapshot found!\n");
	
			/* program counter is specified. Old 1.45 */
			lo = pSnapshot.read(6) & 0x0ff;
			hi = pSnapshot.read(7) & 0x0ff;
			cpu_set_reg(Z80_PC, (hi << 8) | lo);
	
			spectrum_page_basicrom();
	
			if ((pSnapshot.read(12) & 0x020) == 0)
			{
				logerror("Not compressed\n");
	
				/* not compressed */
				for (i = 0; i < 49152; i++)
				{
					cpu_writemem16(i + 16384, pSnapshot.read(30 + i));
				}
			}
			else
			{
				logerror("Compressed\n");
	
				/* compressed */
				//spectrum_z80_decompress_block(pSnapshot + 30, 16384, 49152);
                                pSnapshot.offset=(30);
                                spectrum_z80_decompress_block(pSnapshot, 16384, 49152);
			}
		}
		else
		{
			UBytePtr pSource;
			int header_size;
	
			logerror("v2.0+ V of Z80 snapshot found!\n");
	
			header_size = 30 + 2 + ((pSnapshot.read(30) & 0x0ff) | ((pSnapshot.read(31) & 0x0ff) << 8));
	
			lo = pSnapshot.read(32) & 0x0ff;
			hi = pSnapshot.read(33) & 0x0ff;
			cpu_set_reg(Z80_PC, (hi << 8) | lo);
	
			/*TODO*/////if (spectrum_128_port_7ffd_data != -1)
			/*TODO*/////{
			/*TODO*/////	/* Only set up sound registers for 128K machine! */
			/*TODO*/////	for (i = 0; i < 16; i++)
			/*TODO*/////	{
			/*TODO*/////		AY8910_control_port_0_w(0, i);
			/*TODO*/////		AY8910_write_port_0_w(0, pSnapshot.read(39 + i));
			/*TODO*/////	}
			/*TODO*/////	AY8910_control_port_0_w(0, pSnapshot.read(38);
			/*TODO*/////}
	
			//pSource = pSnapshot + header_size;
                        pSource = pSnapshot;
                        pSource.offset=(header_size);
	
			if (is48ksnap != 0)
				/* Ensure 48K Basic ROM is used */
				spectrum_page_basicrom();
	
			do
			{
				//short length;
                                int length;
				//char page;
                                int page;
				int Dest = 0;
	
				length = (pSource.read(0) & 0x0ff) | ((pSource.read(1) & 0x0ff) << 8);
				page = pSource.read(2);
	
				if (is48ksnap != 0)
				{
					switch (page)
					{
					case 4:
						Dest = 0x08000;
						break;
	
					case 5:
						Dest = 0x0c000;
						break;
	
					case 8:
						Dest = 0x04000;
						break;
	
					default:
						Dest = 0;
						break;
					}
				}
				else
				{
					/* 3 = bank 0, 4 = bank 1 ... 10 = bank 7 */
					if ((page >= 3) && (page <= 10))
					{
						/* Page the appropriate bank into 0xc000 - 0xfff */
						spectrum_128_port_7ffd_data = page - 3;
						spectrum_update_paging();
						Dest = 0x0c000;
					}
					else
						/* Other values correspond to ROM pages */
						Dest = 0x0;
				}
	
				if (Dest != 0)
				{
					if (length == 0x0ffff)
					{
						/* block is uncompressed */
						logerror("Not compressed\n");
	
						/* not compressed */
						for (i = 0; i < 16384; i++)
						{
							cpu_writemem16(i + Dest, pSource.read(i));
						}
	
	
					}
					else
					{
						logerror("Compressed\n");
	
						/* block is compressed */
						//spectrum_z80_decompress_block(&pSource[3], Dest, 16384);
                                                pSource.offset=(3);
                                                spectrum_z80_decompress_block(pSource, Dest, 16384);
					}
				}
	
				/* go to next block */
				//pSource += (3 + length);
                                pSource.offset=(3 + length);
			}
			//while (((unsigned long) pSource - (unsigned long) pSnapshot) < SnapshotDataSize);
                        while ((pSource.memory.length - pSnapshot.memory.length) < SnapshotDataSize);
	
			//if ((spectrum_128_port_7ffd_data != -1) && !is48ksnap)
                        if ((spectrum_128_port_7ffd_data != -1) && (is48ksnap==0))
			{
				/* Set up paging */
				spectrum_128_port_7ffd_data = (pSnapshot.read(35) & 0x0ff);
				spectrum_update_paging();
			}
		}
		dump_registers();
	}
	
	/*******************************************************************
	 *
	 *      Returns 1 if the specified z80 snapshot is a 48K snapshot
	 *      and 0 if it is a Spectrum 128K or SamRam snapshot.
	 *
	 *******************************************************************/
	int is48k_z80snapshot(UBytePtr pSnapshot, long SnapshotSize)
	{
		//unsigned char lo, hi, data;
                int lo, hi, data;
	
		if (SnapshotSize < 30)
			return 0;						/* Invalid file */
	
		lo = pSnapshot.read(6) & 0x0ff;
		hi = pSnapshot.read(7) & 0x0ff;
		if ((hi==1) || (lo==1))
			return 1;						/* V1.45 - 48K only */
	
		lo = pSnapshot.read(30) & 0x0ff;
		hi = pSnapshot.read(31) & 0x0ff;
		data = pSnapshot.read(34) & 0x0ff;		/* Hardware mode */
	
		if ((hi == 0) && (lo == 23))
		{									/* V2.01 format */
			if ((data == 0) || (data == 1))
				return 1;
		}
		else
		{									/* V3.0 format */
			if ((data == 0) || (data == 1) || (data == 3))
				return 1;
		}
		return 0;
	}
        
        public static io_idPtr spectrum_rom_id = new io_idPtr() {
            public int handler(int id) {
                return 1;
            }
        };
	
	/*-----------------27/02/00 10:54-------------------
	 SPECTRUM WAVE CASSETTE SUPPORT
	--------------------------------------------------*/
	
	/*TODO*/////int spectrum_cassette_init(int id)
	/*TODO*/////{
	/*TODO*/////	void *file;
	
	/*TODO*/////	if ((device_filename(IO_CASSETTE, id) != null) &&
	/*TODO*/////		!stricmp(device_filename(IO_CASSETTE, id) + strlen(device_filename(IO_CASSETTE, id) ) - 4, ".tap"))
	/*TODO*/////	{
	/*TODO*/////		int datasize;
	/*TODO*/////		UBytePtr data;
	
	/*TODO*/////		file = image_fopen(IO_CASSETTE, id, OSD_FILETYPE_IMAGE_RW, OSD_FOPEN_READ);
	/*TODO*/////		logerror(".TAP file found\n");
	/*TODO*/////		if (file != 0)
	/*TODO*/////			datasize = osd_fsize(file);
	/*TODO*/////		else
	/*TODO*/////			datasize = 0;
	/*TODO*/////		if (datasize != 0)
	/*TODO*/////		{
	/*TODO*/////			data = malloc(datasize);
	/*TODO*/////
	/*TODO*/////			if (data != NULL)
	/*TODO*/////			{
	/*TODO*/////				pSnapshotData = data;
	/*TODO*/////				SnapshotDataSize = datasize;
	/*TODO*/////
	/*TODO*/////				osd_fread(file, data, datasize);
	/*TODO*/////				osd_fclose(file);
	
					/* Always reset tape position when loading new tapes */
	/*TODO*/////				TapePosition = 0;
	/*TODO*/////				cpu_setOPbaseoverride(0, spectrum_tape_opbaseoverride);
	/*TODO*/////				spectrum_snapshot_type = SPECTRUM_TAPEFILE_TAP;
	/*TODO*/////				logerror(".TAP file successfully loaded\n");
	/*TODO*/////				return INIT_OK;
	/*TODO*/////			}
	/*TODO*/////		}
	/*TODO*/////		osd_fclose(file);
	/*TODO*/////		return INIT_FAILED;
	/*TODO*/////	}
	
	/*TODO*/////	file = image_fopen(IO_CASSETTE, id, OSD_FILETYPE_IMAGE_RW, OSD_FOPEN_READ);
	/*TODO*/////	if (file != 0)
	/*TODO*/////	{
	/*TODO*/////		struct wave_args wa =
	/*TODO*/////		{0,};
	
	/*TODO*/////		wa.file = file;
	/*TODO*/////		wa.display = 1;
	
	/*TODO*/////		if (device_open(IO_CASSETTE, id, 0, &wa))
	/*TODO*/////			return INIT_FAILED;
	
	/*TODO*/////		return INIT_OK;
	/*TODO*/////	}
	
		/* HJB 02/18: no file, create a new file instead */
	/*TODO*/////	file = image_fopen(IO_CASSETTE, id, OSD_FILETYPE_IMAGE_RW, OSD_FOPEN_WRITE);
	/*TODO*/////	if (file != 0)
	/*TODO*/////	{
	/*TODO*/////		struct wave_args wa =
	/*TODO*/////		{0,};
	
	/*TODO*/////		wa.file = file;
	/*TODO*/////		wa.display = 1;
	/*TODO*/////		wa.smpfreq = 22050;				/* maybe 11025 Hz would be sufficient? */
			/* open in write mode */
	/*TODO*/////		if (device_open(IO_CASSETTE, id, 1, &wa))
	/*TODO*/////			return INIT_FAILED;
	/*TODO*/////		return INIT_OK;
	/*TODO*/////	}
	
	/*TODO*/////	return INIT_FAILED;
	/*TODO*/////}
	
	void spectrum_cassette_exit(int id)
	{
		/*TODO*/////device_close(IO_CASSETTE, id);
		spectrum_rom_exit(id);
	}
	
	/*************************************
	 *
	 *      Interrupt handlers.
	 *
	 *************************************/
	
	void spectrum_nmi_generate(int param)
	{
		cpu_cause_interrupt(0, Z80_NMI_INT);
	}
	
	int spec_quick_init(int id)
	{
		/*TODO*/////FILE *fp;
		int read;
	
		/*TODO*/////memset(&quick, 0, sizeof (quick));
	
		if (device_filename(IO_QUICKLOAD, id) == null)
			return INIT_OK;
	
	/*	quick.name = name; */
	
		/*TODO*/////fp = image_fopen(IO_QUICKLOAD, id, OSD_FILETYPE_IMAGE_R, 0);
		/*TODO*/////if (!fp)
		/*TODO*/////	return INIT_FAILED;
	
		/*TODO*/////quick.length = osd_fsize(fp);
		/*TODO*/////quick.addr = 0x4000;
	
		/*TODO*/////if ((quick.data = malloc(quick.length)) == NULL)
		/*TODO*/////{
		/*TODO*/////	osd_fclose(fp);
		/*TODO*/////	return INIT_FAILED;
		/*TODO*/////}
		/*TODO*/////read = osd_fread(fp, quick.data, quick.length);
		/*TODO*/////osd_fclose(fp);
		/*TODO*/////return read != quick.length;
                
                // REMEBER REMOVE THIS WHEN FINISH TODOs!!!!!
                return 0;
	}
	
	void spec_quick_exit(int id)
	{
		/*TODO*/////if (quick.data != null)
		/*TODO*/////	free(quick.data);
	}
	
	//static int spec_quick_open(int id, int mode, void arg)
        static int spec_quick_open(int id, int mode)
	{
		/*TODO*/////int i;
	
		/*TODO*/////if (quick.data == null)
		/*TODO*/////	return 1;
	
		/*TODO*/////for (i = 0; i < quick.length; i++)
		/*TODO*/////{
		/*TODO*/////	cpu_writemem16(i + quick.addr, quick.data[i]);
		/*TODO*/////}
		/*TODO*/////logerror("quick loading %s at %.4x size:%.4x\n",
		/*TODO*/////		 device_filename(IO_QUICKLOAD, id), quick.addr, quick.length);
	
		return 0;
	}
}
