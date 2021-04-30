/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

Amstrad hardware consists of:

- General Instruments AY-3-8912 (audio and keyboard scanning)
- Intel 8255PPI (keyboard, access to AY-3-8912, cassette etc)
- Z80A CPU
- 765 FDC (disc drive interface)
- "Gate Array" (custom chip by Amstrad controlling colour, mode,
rom/ram selection


***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.machine;

import static WIP.arcadeflex.libc_v2.*;
import static WIP.arcadeflex.fucPtr.*;
import static WIP.mame.mame.Machine;
import static WIP.mame.osdependH.*;
import static WIP.mame.memory.*;
import static WIP.mame.memoryH.*;

//import static WIP.arcadeflex.libc.memcpy.*;

import static old.arcadeflex.osdepend.logerror;
import static old.mame.cpuintrf.*;
import static mess.messH.*;
import static mess.mess.*;
import static mess.osdepend.fileio.*;
import static cpu.z80.z80H.*;

//import static old.arcadeflex.libc_old.*;

import static arcadeflex.libc.cstring.*;
import consoleflex.funcPtr.*;
import cpu.z80.z80;

import static sound.ay8910.*;
import static sound.ay8910H.*;

import static mess.systems.amstrad.*;
import static mess.vidhrdw.m6845.*;

import static mess.machine._8255ppiH.*;
import static mess.machine._8255ppi.*;

import static mess.machine.dsk.*;

public class amstrad
{
	//
	/*TODO*/////void AmstradCPC_GA_Write(int);
	/*TODO*/////void AmstradCPC_SetUpperRom(int);
	/*TODO*/////void amstrad_handle_snapshot(UBytePtr );
	
	
	public static UBytePtr snapshot = null;
	
	static int snapshot_loaded=0;
        
        public static io_initPtr amstrad_floppy_init = new io_initPtr() {
            public int handler(int id) {
                if (device_filename(IO_FLOPPY, id)==null)
                    return 1;

        	return dsk_floppy_load.handler(id);
        }};
            
	
	/* used to setup computer if a snapshot was specified */
	//OPBASE_HANDLER( amstrad_opbaseoverride )
	public static opbase_handlerPtr amstrad_opbaseoverride=new opbase_handlerPtr() {
            
            public int handler (int address){
		/* clear op base override */
		cpu_setOPbaseoverride(0,null);
	
		if (snapshot_loaded != 0)
		{
			/* its a snapshot file - setup hardware state */
			amstrad_handle_snapshot(snapshot);
	
			/* free memory */
			//free(snapshot);
			snapshot = null;
	
			snapshot_loaded = 0;
	
		}
	
		return (cpu_get_pc() & 0x0ffff);
	}};
	
	public static void amstrad_setup_machine()
	{
		/* allocate ram - I control how it is accessed so I must
		allocate it somewhere - here will do */
		Amstrad_Memory = new UBytePtr(128*1024);
		//if(Amstrad_Memory==null) return;
	
		if (snapshot_loaded != 0)
		{
			/* setup for snapshot */
			cpu_setOPbaseoverride(0,amstrad_opbaseoverride);
		}
	
	
		if (snapshot_loaded==0)
		{
			Amstrad_Reset();
		}
	}
	
	
	
	//int amstrad_cassette_init(int id)
	public static io_initPtr amstrad_cassette_init = new io_initPtr() {
            public int handler(int id) {
		Object file = null;
	
		file = image_fopen(IO_CASSETTE, id, OSD_FILETYPE_IMAGE_RW, OSD_FOPEN_READ);
		if (file != null)
		{
			throw new UnsupportedOperationException("TODO implement!");
                        /*TODO*/////struct wave_args wa = {0,};
			/*TODO*/////wa.file = file;
			/*TODO*/////wa.display = 1;
	
			/*TODO*/////if (device_open(IO_CASSETTE, id, 0, &wa))
			/*TODO*/////	return INIT_FAILED;
	
			/*TODO*/////return INIT_OK;
		}
	
		/* HJB 02/18: no file, create a new file instead */
		file = image_fopen(IO_CASSETTE, id, OSD_FILETYPE_IMAGE_RW, OSD_FOPEN_WRITE);
		if (file != null)
		{
		/*TODO*/////	struct wave_args wa = {0,};
		/*TODO*/////	wa.file = file;
		/*TODO*/////	wa.display = 1;
		/*TODO*/////	wa.smpfreq = 22050; /* maybe 11025 Hz would be sufficient? */
			/* open in write mode */
	        /*TODO*/////if (device_open(IO_CASSETTE, id, 1, &wa))
	        /*TODO*/////    return INIT_FAILED;
			return INIT_OK;
	    }
	
		return INIT_FAILED;
	}};
	
	//void amstrad_cassette_exit(int id)
	public static io_exitPtr amstrad_cassette_exit = new io_exitPtr() {
            public int handler(int id) {
		/*TODO*/////device_close(IO_CASSETTE, id);
                return INIT_OK;
	}};
	
	
	/* load CPCEMU style snapshots */
	public static void amstrad_handle_snapshot(UBytePtr pSnapshot)
	{
		int RegData;
		int i;
	
                System.out.println("SNA "+pSnapshot);
	
		/* init Z80 */
		RegData = (pSnapshot.read(0x011) & 0x0ff) | ((pSnapshot.read(0x012) & 0x0ff)<<8);
		cpu_set_reg(Z80_AF, RegData);
	
		RegData = (pSnapshot.read(0x013) & 0x0ff) | ((pSnapshot.read(0x014) & 0x0ff)<<8);
		cpu_set_reg(Z80_BC, RegData);
	
		RegData = (pSnapshot.read(0x015) & 0x0ff) | ((pSnapshot.read(0x016) & 0x0ff)<<8);
		cpu_set_reg(Z80_DE, RegData);
	
		RegData = (pSnapshot.read(0x017) & 0x0ff) | ((pSnapshot.read(0x018) & 0x0ff)<<8);
		cpu_set_reg(Z80_HL, RegData);
	
		RegData = (pSnapshot.read(0x019) & 0x0ff) ;
		cpu_set_reg(Z80_R, RegData);
	
		RegData = (pSnapshot.read(0x01a) & 0x0ff);
		cpu_set_reg(Z80_I, RegData);
	
		if ((pSnapshot.read(0x01b) & 1)==1)
		{
			cpu_set_reg(Z80_IFF1, 1);
		}
		else
		{
			cpu_set_reg(Z80_IFF1, 0);
		}
	
		if ((pSnapshot.read(0x01c) & 1)==1)
		{
			cpu_set_reg(Z80_IFF2, 1);
		}
		else
		{
			cpu_set_reg(Z80_IFF2, 0);
		}
	
		RegData = (pSnapshot.read(0x01d) & 0x0ff) | ((pSnapshot.read(0x01e) & 0x0ff)<<8);
		cpu_set_reg(Z80_IX, RegData);
	
		RegData = (pSnapshot.read(0x01f) & 0x0ff) | ((pSnapshot.read(0x020) & 0x0ff)<<8);
		cpu_set_reg(Z80_IY, RegData);
	
		RegData = (pSnapshot.read(0x021) & 0x0ff) | ((pSnapshot.read(0x022) & 0x0ff)<<8);
		cpu_set_reg(Z80_SP, RegData);
		//cpu_set_sp(RegData);
	
		RegData = (pSnapshot.read(0x023) & 0x0ff) | ((pSnapshot.read(0x024) & 0x0ff)<<8);
	
		cpu_set_reg(Z80_PC, RegData);
	//	cpu_set_pc(RegData);
	
		RegData = (pSnapshot.read(0x025) & 0x0ff);
		cpu_set_reg(Z80_IM, RegData);
	
		RegData = (pSnapshot.read(0x026) & 0x0ff) | ((pSnapshot.read(0x027) & 0x0ff)<<8);
		cpu_set_reg(Z80_AF2, RegData);
	
		RegData = (pSnapshot.read(0x028) & 0x0ff) | ((pSnapshot.read(0x029) & 0x0ff)<<8);
		cpu_set_reg(Z80_BC2, RegData);
	
		RegData = (pSnapshot.read(0x02a) & 0x0ff) | ((pSnapshot.read(0x02b) & 0x0ff)<<8);
		cpu_set_reg(Z80_DE2, RegData);
	
		RegData = (pSnapshot.read(0x02c) & 0x0ff) | ((pSnapshot.read(0x02d) & 0x0ff)<<8);
		cpu_set_reg(Z80_HL2, RegData);
	
		/* init GA */
		for (i=0; i<17; i++)
		{
			AmstradCPC_GA_Write(i);
	
			AmstradCPC_GA_Write(((pSnapshot.read(0x02f + i) & 0x01f) | 0x040));
		}
	
		AmstradCPC_GA_Write(pSnapshot.read(0x02e) & 0x01f);
	
		AmstradCPC_GA_Write(((pSnapshot.read(0x040) & 0x03f) | 0x080));
	
		AmstradCPC_GA_Write(((pSnapshot.read(0x041) & 0x03f) | 0x0c0));
	
		/* init CRTC */
		for (i=0; i<18; i++)
		{
	                crtc6845_address_w(0,i);
	                crtc6845_register_w(0, pSnapshot.read(0x043+i) & 0x0ff);
		}
	
	    crtc6845_address_w(0,i);
	
		/* upper rom selection */
		AmstradCPC_SetUpperRom(pSnapshot.read(0x055));
	
		/* PPI */
		ppi8255_w(0,3,pSnapshot.read(0x059) & 0x0ff);
	
		ppi8255_w(0,0,pSnapshot.read(0x056) & 0x0ff);
		ppi8255_w(0,1,pSnapshot.read(0x057) & 0x0ff);
		ppi8255_w(0,2,pSnapshot.read(0x058) & 0x0ff);
	
		/* PSG */
		for (i=0; i<16; i++)
		{
			AY8910_control_port_0_w.handler(0,i);
	
			AY8910_write_port_0_w.handler(0,pSnapshot.read(0x05b + i) & 0x0ff);
		}
	
		AY8910_control_port_0_w.handler(0,pSnapshot.read(0x05a));
	
		{
			int MemSize;
			int MemorySize;
	
			MemSize = (pSnapshot.read(0x06b) & 0x0ff) | ((pSnapshot.read(0x06c) & 0x0ff)<<8);
	
			if (MemSize==128)
			{
				MemorySize = 128*1024;
			}
			else
			{
				MemorySize = 64*1024;
			}
	
			WIP.arcadeflex.libc.memcpy.memcpy(Amstrad_Memory, new UBytePtr(pSnapshot, 0x0100), MemorySize);
                        
	
		}
	
		Amstrad_RethinkMemory();
	
	}
	
	/* load image */
	static int amstrad_load(int type, int id, UBytePtr ptr)
	{
		Object file = null;
                
                //System.out.println("A");
	
		file = image_fopen(type, id, OSD_FILETYPE_IMAGE_R, OSD_FOPEN_READ);
                //System.out.println("B");
                //System.out.println(file);
	
		if (file != null)
		{
			int datasize;
			UBytePtr data;
	
			/* get file size */
			datasize = osd_fsize(file);
	
			if (datasize!=0)
			{
				/* malloc memory for this data */
				data = new UBytePtr(datasize);
	
				if (data!=null)
				{
					/* read whole file */
					osd_fread(file, data, datasize);
	
					ptr = new UBytePtr(data, 0);
                                        snapshot=new UBytePtr(data, 0);
                                        snapshot_loaded=1;
	
					/* close file */
					osd_fclose(file);
	
					logerror("File loaded!\r\n");
	
					/* ok! */
					return 1;
				}
				osd_fclose(file);
	
			}
		}
	
		return 0;
	}
	
	/* load snapshot */
	//int amstrad_snapshot_load(int id)
	public static io_initPtr amstrad_snapshot_load = new io_initPtr() {
            public int handler(int id) {
		/* machine can be started without a snapshot */
	/* if filename not specified, then init is ok */
	if (device_filename(IO_SNAPSHOT, id)==null)
		return 1;

                /* filename specified */
                snapshot_loaded = 0;

                /* load and verify image */
                if (amstrad_load(IO_SNAPSHOT,id,snapshot) != 0)
                {
                        snapshot_loaded = 1;
                        if (memcmp(snapshot.memory, 0, "MV - SNA", 8)==0)
                                return 1;
                        else
                                return 0;
                }

                return 0;
	}};
	
	/* check if a snapshot file is valid to load */
	//public static int amstrad_snapshot_id(int id)
	public static io_idPtr amstrad_snapshot_id = new io_idPtr() {
            public int handler(int id) {
		int valid;
		UBytePtr snapshot_data=new UBytePtr();
                snapshot_data.memory=new char[1024];
	
		valid = ID_FAILED;
	
		/* load snapshot */
		if (amstrad_load(IO_SNAPSHOT, id, snapshot_data) != 0)
		{
			/* snapshot loaded, check it is valid */
	
			if (memcmp(snapshot_data.memory, 0, "MV - SNA", 8)==0)
			{
				valid = ID_OK;
			}
	
			/* free the file */
			//free(snapshot_data);
                        snapshot_data = null;
		}
	
		return valid;
	
	}};
	
	//void amstrad_snapshot_exit(int id)
	public static io_exitPtr amstrad_snapshot_exit = new io_exitPtr() {
            public int handler(int id) {
		if (snapshot!=null)
			snapshot=null;
	
		snapshot_loaded = 0;
                return snapshot_loaded;
	}};
}
