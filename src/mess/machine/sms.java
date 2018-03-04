
/*
 * ported to v0.37b6
 * using automatic conversion tool v0.01
 */ 
package mess.machine;

import static WIP.arcadeflex.libc_v2.*;
import static WIP.arcadeflex.fucPtr.*;
import static WIP.mame.mame.Machine;
import static consoleflex.funcPtr.*;
import static mess.messH.*;
import static mess.mess.*;
import static old.mame.inptport.*;
import static mess.vidhrdw.tms9928a.*;
import static old.arcadeflex.osdepend.*;
import static mess.osdepend.fileio.*;
import static arcadeflex.libc.cstring.*;
import static WIP.mame.osdependH.*;
import static mame.commonH.*;
import static old.mame.common.*;
import static WIP.arcadeflex.libc.memcpy.*;
import static old.arcadeflex.libc_old.SEEK_SET;
import static old.arcadeflex.libc_old.printf;
import static old.arcadeflex.libc_old.strcmp;

public class sms
{
	public static final int SMS_ROM_MAXSIZE =(0x810200);
	static /*unsigned char*/ int u8_sms_page_count;
	static /*unsigned char*/ int u8_sms_fm_detect;
	static /*unsigned char*/ int u8_sms_version;
	
	public static WriteHandlerPtr sms_fm_detect_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	    u8_sms_fm_detect = (data & 1);
	}};
	
	public static ReadHandlerPtr sms_fm_detect_r = new ReadHandlerPtr() {
        public int handler(int offset) {
	    return ( (readinputport(3) & 1)!=0 ? u8_sms_fm_detect : 0x00 );
	}};
	
	public static WriteHandlerPtr sms_version_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	    u8_sms_version = (data & 0xA0);
	}};
	
	public static ReadHandlerPtr sms_version_r = new ReadHandlerPtr() {
        public int handler(int offset) {
	    int/*unsigned char*/ temp;
	
	    /* Move bits 7,5 of port 3F into bits 7, 6 */
	    temp = ((u8_sms_version & 0x80) | (u8_sms_version & 0x20) << 1)&0xFF;
	
	    /* Inverse version detect value for Japanese machines */
	    if((readinputport(3) & 2)!=0) temp = (temp ^ 0xC0)&0xFF;
	
	    /* Merge version data with input port #2 data */
	    temp = (temp & 0xC0) | (readinputport(1) & 0x3F);
	
	    return (temp&0xFF);
	}};
	
	public static WriteHandlerPtr sms_mapper_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	    UBytePtr RAM = memory_region(REGION_CPU1);
	
	    offset &= 3;
	    data %= u8_sms_page_count;
	
	    RAM.write(0xDFFC + offset, data);
	    RAM.write(0xFFFC + offset, data);
	
	    switch(offset)
	    {
	        case 0: /* Control */
	            break;
	
	        case 1: /* Select 16k ROM bank for 0000-3FFF */
	            memcpy(RAM,0x0000, RAM,0x10000 + (data * 0x4000), 0x3C00);
	            break;
	
	        case 2: /* Select 16k ROM bank for 4000-7FFF */
	            memcpy(RAM,0x4000, RAM,0x10000 + (data * 0x4000), 0x4000);
	            break;
	
	        case 3: /* Select 16k ROM bank for 8000-BFFF */
	            memcpy(RAM,0x8000, RAM,0x10000 + (data * 0x4000), 0x4000);
	            break;
	    }
	}};
	
	public static WriteHandlerPtr sms_cartram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	}};
	
	public static WriteHandlerPtr sms_ram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	    UBytePtr RAM = memory_region(REGION_CPU1);
	    RAM.write(0xC000 + (offset & 0x1FFF), data);
	    RAM.write(0xE000 + (offset & 0x1FFF), data);
	}};
	
	public static WriteHandlerPtr gg_sio_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	    logerror("*** write %02X to SIO register #%d\n", data, offset);
	
	    switch(offset & 7)
	    {
	        case 0x00: /* Parallel Data */
	            break;
	
	        case 0x01: /* Data Direction/ NMI Enable */
	            break;
	
	        case 0x02: /* Serial Output */
	            break;
	
	        case 0x03: /* Serial Input */
	            break;
	
	        case 0x04: /* Serial Control / Status */
	            break;
	    }
	}};
	
	public static ReadHandlerPtr gg_sio_r = new ReadHandlerPtr() {
        public int handler(int offset) {
	    logerror("*** read SIO register #%d\n", offset);
	
	    switch(offset & 7)
	    {
	        case 0x00: /* Parallel Data */
	            break;
	
	        case 0x01: /* Data Direction/ NMI Enable */
	            break;
	
	        case 0x02: /* Serial Output */
	            break;
	
	        case 0x03: /* Serial Input */
	            break;
	
	        case 0x04: /* Serial Control / Status */
	            break;
	    }
	
	    return (0x00);
	}};
	
	public static WriteHandlerPtr gg_psg_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	    /* D7 = Noise Left */
	    /* D6 = Tone3 Left */
	    /* D5 = Tone2 Left */
	    /* D4 = Tone1 Left */
	
	    /* D3 = Noise Right */
	    /* D2 = Tone3 Right */
	    /* D1 = Tone2 Right */
	    /* D0 = Tone1 Right */
	}};
	
	
	/****************************************************************************/
	
	public static io_initPtr sms_load_rom = new io_initPtr() {
        public int handler(int id) {
	    int size, ret;
	    Object handle;
	    UBytePtr RAM;
	
	    /* Ensure filename was specified */
	    if(device_filename(IO_CARTSLOT,id) == null)
	    {
	        printf("Cartridge Name Required!\n");
	        return (INIT_FAILED);
		}
	
	    /* Ensure filename was specified */
	    handle = image_fopen(IO_CARTSLOT, id, OSD_FILETYPE_IMAGE_R, 0);
	    if(handle == null)
	    {
			printf("Cartridge Name Required!\n");
	        return (INIT_FAILED);
		}
	
	    /* Get file size */
	    size = osd_fsize(handle);
	
	    /* Check for 512-byte header */
	    if(((size / 512) & 1)!=0)
	    {
	        osd_fseek(handle, 512, SEEK_SET);
	        size -= 512;
	    }
	
	    /* Allocate memory */
	    ret = new_memory_region(REGION_CPU1, size);
	
	    /* Oops.. couldn't do it */
	    if (ret != 0)
	    {
	        printf("Error allocating %d bytes.\n", size);
	        return INIT_FAILED;
	    }
	
	    /* Get base of CPU1 memory region */
	    RAM = memory_region(REGION_CPU1);
	
	    /* Load ROM banks */
	    size = osd_fread(handle, RAM,0x10000, size);
	
	    /* Close file */
	    osd_fclose(handle);
	
	    /* Get 16K page count */
	    u8_sms_page_count = (size / 0x4000)&0xFF;
	
	    /* Load up first 32K of image */
	    memcpy(RAM,0x0000, RAM,0x10000, 0x4000);
	    memcpy(RAM,0x4000, RAM,0x14000, 0x4000);
	    memcpy(RAM,0x8000, RAM,0x10000, 0x4000);
	
	    return (INIT_OK);
	}};
	
	public static InitMachinePtr sms_init_machine = new InitMachinePtr() { public void handler() 
	{
	    u8_sms_fm_detect = 0;
	} };
	
	public static io_idPtr sms_id_rom = new io_idPtr() {
        public int handler(int id) {
		Object romfile;
		char[] magic=new char[9];
		char[] extra=new char[1];
		int retval;
	
		if ((romfile = image_fopen (IO_CARTSLOT, id, OSD_FILETYPE_IMAGE_R, 0))==null) return 0;
	
		retval = 0;
	
		/* Verify the file is a valid image - check $7ff0 for "TMR SEGA" */
		osd_fseek (romfile, 0x7ff0, SEEK_SET);
		osd_fread (romfile, magic, 8);
		magic[8] = 0;
		if (strcmp(new String(magic),"TMR SEGA")==0)
		{
			/* TODO: Is this right? If it's $50 or greater, it is SMS */
			osd_fseek (romfile, 0x7ffd, SEEK_SET);
			osd_fread (romfile, extra, 1);
			if (extra[0] >= 0x50)
				retval = 1;
		}
		/* Check at $81f0 also */
		if (retval==0)
		{
			osd_fseek (romfile, 0x81f0, SEEK_SET);
			osd_fread (romfile, magic, 8);
			magic[8] = 0;
			if (strcmp(new String(magic),"TMR SEGA")==0)
			{
				/* TODO: Is this right? If it's $50 or greater, it is SMS */
				osd_fseek (romfile, 0x81fd, SEEK_SET);
				osd_fread (romfile, extra, 1);
				if (extra[0] >= 0x50)
					retval = 1;
			}
		}
	
		osd_fclose (romfile);
	
		return retval;
	}};
	
	public static io_idPtr gamegear_id_rom = new io_idPtr() {
        public int handler(int id) {
		Object romfile;
		char[] magic=new char[9];
		char[] extra=new char[1];
		int retval;
	
		if ((romfile = image_fopen (IO_CARTSLOT, id, OSD_FILETYPE_IMAGE_R, 0))==null) return 0;
	
		retval = 0;
	
		/* Verify the file is a valid image - check $7ff0 for "TMR SEGA" */
		osd_fseek (romfile, 0x7ff0, SEEK_SET);
		osd_fread (romfile, magic, 8);
		magic[8] = 0;
		if (strcmp(new String(magic),"TMR SEGA")==0)
		{
			/* TODO: Is this right? If it's less than 0x50, it is a GameGear image */
			osd_fseek (romfile, 0x7ffd, SEEK_SET);
			osd_fread (romfile, extra, 1);
			if (extra[0] < 0x50)
				retval = 1;
		}
		/* Check at $81f0 also */
		if (retval==0)
		{
			osd_fseek (romfile, 0x81f0, SEEK_SET);
			osd_fread (romfile, magic, 8);
			magic[8] = 0;
			if (strcmp(new String(magic),"TMR SEGA")==0)
			{
				/* TODO: Is this right? If it's less than 0x50, it is a GameGear image */
				osd_fseek (romfile, 0x81fd, SEEK_SET);
				osd_fread (romfile, extra, 1);
				if (extra[0] < 0x50)
					retval = 1;
			}
		}
	
		osd_fclose (romfile);
	
		return retval;
	}};
	
	
}
