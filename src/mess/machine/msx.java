package mess.machine;

/*
** msx.c : MSX1 emulation
**
** Todo:
**
** - memory emulation needs be rewritten
** - add support for printer and serial ports
** - add support for SCC+ and megaRAM
** - add support for diskdrives
**
** Sean Young
*/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 

import static mess.machine.msxH.*;
import static mess.machine._8255ppiH.*;
import static mess.machine._8255ppi.*;

import static mame.commonH.*;
import static old.mame.common.*;
import static old.arcadeflex.fileio.osd_fwrite;

import WIP.arcadeflex.fucPtr.InitMachinePtr;
import WIP.arcadeflex.fucPtr.ReadHandlerPtr;
import WIP.arcadeflex.libc_v2.*;
import static WIP.arcadeflex.fucPtr.*;
import static WIP.mame.mame.Machine;
import static WIP.mame.osdependH.*;
import static WIP.mame.memory.*;
import static WIP.mame.memoryH.*;

import static old.arcadeflex.osdepend.logerror;
import static old.arcadeflex.input.*;
import static old.mame.cpuintrf.*;
import static mess.messH.*;
import static mess.mess.*;
import static mess.osdepend.fileio.*;
import static cpu.z80.z80H.*;

import static old.arcadeflex.libc_old.*;

import static arcadeflex.libc.cstring.*;
import consoleflex.funcPtr.*;
import cpu.z80.z80;
import static mess.machine._8255ppiH.ppi8255_interface;

import static mess.systems.msx.*;
import static old.arcadeflex.osdepend.logerror;
import static old.mame.cpuintrfH.*;
import static old.mame.cpuintrf.*;
import static old.mame.inptport.*;
import static mess.eventlst.*;
import static mess.eventlstH.*;

import static mess.vidhrdw.border.*;
import static mess.vidhrdw.tms9928aH.*;
import static mess.vidhrdw.tms9928a.*;

import static sound.ay8910.*;
import static sound.ay8910H.*;
import sound.waveH.wave_args;
import static sound.ym2413.*;
import static old.sound.dacH.*;
import static old.sound.dac.*;
import static WIP.sound.sccH.*;
import static WIP.sound.scc.*;

import static arcadeflex.libc.cstring.*;

public class msx
{
	
	static MSX msx1=new MSX();
	
	/*
	** The PPI functions
	*/
	
	public static PortReadHandlerPtr msx_ppi_port_b_r  = new PortReadHandlerPtr() { 
		public int handler(int offset){
		    int row;
		
		    row = ppi8255_0_r.handler(2) & 0x0f;
		    if (row <= 8) return readinputport (row);
		    else return 0xff;
	} };
	
	public static PortWriteHandlerPtr msx_ppi_port_a_w = new PortWriteHandlerPtr() {public void handler(int offset, int data)
	{
	    msx_set_all_mem_banks ();
	} };
	
	public static PortWriteHandlerPtr msx_ppi_port_c_w = new PortWriteHandlerPtr() {public void handler(int offset, int data)
	{
	    int old_val = 0xff;
	
	    /* caps lock */
	    if ( ((old_val ^ data) & 0x40) != 0)
	        osd_led_w.handler(0, (((data & 0x40) == 0) ? 1: 0) );
	    /* key click */
	    if ( ((old_val ^ data) & 0x80) != 0)
	        DAC_signed_data_w (0, (((data & 0x80) != 0) ? 0x7f : 0));
	    /* cassette motor on/off */
	    if ( ((old_val ^ data) & 0x10) != 0)
	        device_status (IO_CASSETTE, 0, (((data & 0x10) != 0) ? 0 : 1));
	    /* cassette signal write */
	    if ( ((old_val ^ data) & 0x20) != 0)
	        device_output (IO_CASSETTE, 0, (((data & 0x20) != 0) ? -32768 : 32767));
	
	    old_val = data;
	} };
	
	public static ppi8255_interface msx_ppi8255_interface =
            new ppi8255_interface(
		1,
		null,
                msx_ppi_port_b_r,
                null,
                msx_ppi_port_a_w,
                null,
                msx_ppi_port_c_w
            );
	
	
	//static char PAC_HEADER[] = "PAC2 BACKUP DATA";
	static String PAC_HEADER = "PAC2 BACKUP DATA";
	public static int PAC_HEADER_LEN = (16);
	
	public static io_idPtr msx_id_rom = new io_idPtr() {
        public int handler(int id) {
            System.out.println("msx_id_rom");
		    Object F;
		    char[] magic=new char[2];
		
		    /* read the first two bytes */
		    F = image_fopen (IO_CARTSLOT, id, OSD_FILETYPE_IMAGE_R, 0);
		    if (F==null) return 0;
		    osd_fread (F, magic, 2);
		    osd_fclose (F);
		
		    /* first to bytes must be 'AB' */
		    return ( (magic[0] == 'A') && (magic[1] == 'B') ? 1 : 0 );
	}};
	
	static int msx_probe_type (UBytePtr pmem, int size)
	{
	    int kon4, kon5, asc8, asc16, i;
	
	    if (size <= 0x10000) return 0;
	
	    kon4 = kon5 = asc8 = asc16 = 0;
	
	    for (i=0;i<size-3;i++)
	    {
	        if (pmem.read(i) == 0x32 && pmem.read(i+1) == 0)
	        {
	            switch (pmem.read(i+2)) {
	            case 0x60:
	            case 0x70:
	                asc16++;
	                asc8++;
	                break;
	            case 0x68:
	            case 0x78:
	                asc8++;
	                asc16--;
	            }
	
	            switch (pmem.read(i+2)) {
	            case 0x60:
	            case 0x80:
	            case 0xa0:
	                kon4++;
	                break;
	            case 0x50:
	            case 0x70:
	            case 0x90:
	            case 0xb0:
	                kon5++;
	            }
	        }
	    }
	
	//#define MAX(x, y) ((x) < (y) ? (y) : (x) )
	
	    if (MAX (kon4, kon5) > MAX (asc8, asc16) )
	        return (kon5 > kon4) ? 2 : 3;
	    else
	        return (asc8 > asc16) ? 4 : 5;
	}
	
	static int MAX(int x, int y) {
		return ((x) < (y) ? (y) : (x) );
	}
	
	static String[] mapper_types = { "none", "MSX-DOS 2", "konami5 with SCC",
	        "konami4 without SCC", "ASCII/8kB", "ASCII//16kB",
	        "Konami Game Master 2", "ASCII/8kB with 8kB SRAM",
	        "ASCII/16kB with 2kB SRAM", "R-Type", "Konami Majutsushi",
	        "Panasonic FM-PAC", "ASCII/16kB (bogus; use type 5)",
	        "Konami Synthesizer" };
	
	public static io_initPtr msx_load_rom = new io_initPtr() {
        public int handler(int id) {
            System.out.println("msx_load_rom");
	    Object F;
	    UBytePtr pmem,m;
	    int size,size_aligned,n,p,i;
	    int type=0;
	    //char[] pext=new char[PAC_HEADER_LEN + 2];
	    String pext="";
	    char[] buf=new char[PAC_HEADER_LEN + 2];
	    
	    /* try to load it */
	    F = image_fopen (IO_CARTSLOT, id, OSD_FILETYPE_IMAGE_R, 0);
	    if (F==null) return 1;
	    size = osd_fsize (F);
	    if (size < 0x2000)
	    {
	        logerror("%s: file to small\n",
	            device_filename (IO_CARTSLOT, id));
	        osd_fclose (F);
	        return 1;
	    }
	    /* get mapper type */
	    pext = (device_extrainfo (IO_CARTSLOT, id));
	    //if ( (pext != null) || (1 != sscanf (pext, "%d", type) ) )
	    if ( (pext == null) )
	    {
	        logerror("Cart #%d No extra info found in crc file\n", id);
	        type = -1;
	    }
	    else
	    {
	        if (type < 0 || type > 13)
	        {
	            logerror("Cart #%d Invalid extra info\n", id);
	            type = -1;
	        }
	        else logerror("Cart %d extra info: %s\n", id, pext);
	    }
	
	
	    /* calculate aligned size (8, 16, 32, 64, 128, 256, etc. (kB) ) */
	    size_aligned = 0x2000;
	    while (size_aligned < size) size_aligned *= 2;
	
	    //pmem = (UINT8*)malloc (size_aligned);
	    pmem=new UBytePtr(size_aligned);
	    
	    if (pmem == null)
	    {
	        logerror("malloc () failed\n");
	        osd_fclose (F);
	        return 1;
	    }
	    memset (pmem, 0xff, size_aligned);
	    if (osd_fread (F, pmem, size) != size)
	    {
	        logerror("%s: can't read file\n",
	            device_filename (IO_CARTSLOT, id));
	        osd_fclose (F);
	        //free (msx1.cart[id].mem); 
	        msx1.cart[id].mem = null;
	        return 1;
	    }
	    osd_fclose (F);
	    /* check type */
	    if (type < 0)
	    {
	        type = msx_probe_type (pmem, size);
	
	        if ( !( (pmem.read(0) == 'A') && (pmem.read(1) == 'B') ) )
	        {
	            logerror("%s: May not be a valid ROM file\n",device_filename (IO_CARTSLOT, id) );
	        }
	
	        logerror("Probed cartridge mapper %s\n", mapper_types[type]);
	    }
	
	    /* mapper type 0 always needs 64kB */
	    if (type !=0)
	    {
	        size_aligned = 0x10000;
	        //pmem = realloc (pmem, 0x10000);
	        pmem = new UBytePtr(0x10000);
	        if (pmem == null)
	        {
	            logerror("Realloc failed!\n");
	            //free (msx1.cart[id].mem); 
	            msx1.cart[id].mem = null;
	            return 1;
	        }
	        //if (size < 0x10000) memset (pmem + size, 0xff, 0x10000 - size);
	        if (size < 0x10000) memset (new UBytePtr(pmem, size), 0xff, 0x10000 - size);
	        if (size > 0x10000) size = 0x10000;
	    }
	
	    /* set mapper specific stuff */
	    //msx1.cart[id].mem = pmem.memory;
	    msx1.cart[id].mem = pmem;
	    msx1.cart[id].type = type;
	    msx1.cart[id].bank_mask = (size_aligned / 0x2000) - 1;
	    for (i=0;i<4;i++) msx1.cart[id].banks[i] = (i & msx1.cart[id].bank_mask);
	    logerror("Cart #%d size %d, mask %d, type: %s\n",id, size, msx1.cart[id].bank_mask, mapper_types[type]);
	    /* set filename for sram (memcard) */
	    //msx1.cart[id].sramfile = malloc (strlen (device_filename (IO_CARTSLOT, id)) + 1);
	    msx1.cart[id].sramfile = device_filename (IO_CARTSLOT, id);
	    if (msx1.cart[id].sramfile == null)
	    {
	        logerror("malloc () failed\n");
	        //free (msx1.cart[id].mem); 
	        msx1.cart[id].mem = null;
	        return 1;
	    }
	    //strcpy (msx1.cart[id].sramfile, device_filename (IO_CARTSLOT, id) );
	    msx1.cart[id].sramfile = device_filename (IO_CARTSLOT, id);
	    pext = strrchr (msx1.cart[id].sramfile, '.');
	    if (pext != null) pext = null;
	    /* do some stuff for some types :)) */
	    switch (type) {
	    case 0:
	        /*
	         * mapper-less type; determine what page it should be in .
	         * After the 'AB' there are 4 pointers to somewhere in the
	         * rom itself. null doesn't count, so the first non-zero
	         * pointer determines the page. page 1 is the most common,
	         * so we default to that.
	         */
	
	        p = 1;
	        for (n=2;n<=8;n+=2)
	        {
	            if (( (pmem.READ_WORD(n) != 0) || (pmem.READ_WORD(n+1) != 0)) )
	            {
	                /* this hack works on all byte order systems */
	                p = pmem.read(n+1) / 0x40;
	                break;
	            }
	        }
	        if (size <= 0x4000)
	        {
	            if (p == 1 || p == 2)
	            {
	                /* copy to the respective page */
	                //memcpy (pmem+(p*0x4000), pmem, 0x4000);
	            	memcpy (new UBytePtr(pmem,(p*0x4000)), pmem, 0x4000);
	                memset (pmem, 0xff, 0x4000);
	            } else {
	                /* memory is repeated 4 times */
	                p = -1;
	                //memcpy (pmem + 0x4000, pmem, 0x4000);
	                memcpy (new UBytePtr(pmem, 0x4000), pmem, 0x4000);
	                memcpy (new UBytePtr(pmem, 0x8000), pmem, 0x4000);
	                memcpy (new UBytePtr(pmem, 0xc000), pmem, 0x4000);
	            }
	        }
	        else if (size <= 0xc000)
	        {
	            if (p != 0)
	            {
	            	System.out.println("TODO");
	                /* shift up 16kB; custom memcpy so overlapping memory
	                   isn't corrupted. ROM starts in page 1 (0x4000) */
	            	p = 1;
	                n = 0xc000; 
	                m = new UBytePtr(pmem, 0xffff);
	                /*TODO*///while (n--) { *m = *(m - 0x4000); m--; }
                        while((n--)!=0){
                            m.write(m.read()-0x400); 
                            m.dec();
                        }
	                memset (pmem, 0xff, 0x4000);
	            }
	        }
	
	        {
	            if (p >= 0)
	                logerror("Cart #%d in page %d\n", id, p);
	            else
	                logerror("Cart #%d memory duplicated in all pages\n", id);
	        }
	        break;
	   case 1: /* msx-dos 2: extra blank page for page 2 */
	        //pmem = realloc (msx1.cart[id].mem, 0x12000);
		   pmem = new UBytePtr (msx1.cart[id].mem, 0x12000);
	        if (pmem != null)
	        {
	            //free (msx1.cart[id].mem); 
	            msx1.cart[id].mem = null;
	            return 1;
	        }
	        msx1.cart[id].mem = pmem;
	        msx1.cart[id].banks[2] = 8;
	        msx1.cart[id].banks[3] = 8;
	        break;
	   case 6: /* game master 2; try to load sram */
	        //pmem = realloc (msx1.cart[id].mem, 0x24000);
		   	pmem = new UBytePtr (msx1.cart[id].mem, 0x24000);
	        if (pmem != null)
	        {
	            //free (msx1.cart[id].mem); 
	            msx1.cart[id].mem = null;
	            return 1;
	        }
	        F = osd_fopen (Machine.gamedrv.name, msx1.cart[id].sramfile,
	                OSD_FILETYPE_MEMCARD, 0);
	        if ((F != null) && (osd_fread (F, new UBytePtr(pmem, 0x21000), 0x2000)) == 0x2000) 
	        {
	            memcpy (new UBytePtr(pmem, 0x20000), new UBytePtr(pmem, 0x21000), 0x1000);
	            memcpy (new UBytePtr(pmem, 0x23000), new UBytePtr(pmem, 0x22000), 0x1000);
	            logerror("Cart #%d SRAM loaded\n", id);
	        } else {
	            memset (new UBytePtr(pmem, 0x20000), 0, 0x4000);
	            logerror("Cart #%d Failed to load SRAM\n", id);
	        }
	        if (F != null) osd_fclose (F);
	
	        msx1.cart[id].mem = pmem;
	        break;
	    case 2: /* Konami SCC */
	        /* we want an extra page that looks like the SCC page */
	        //pmem = realloc (pmem, size_aligned + 0x2000);
	    	pmem = new UBytePtr(pmem, size_aligned + 0x2000);
	        if (pmem != null)
	        {
	            //free (msx1.cart[id].mem); 
	            msx1.cart[id].mem = null;
	            return 1;
	        }
	        memcpy (new UBytePtr(pmem, size_aligned), new UBytePtr(pmem, size_aligned - 0x2000), 0x1800);
	        for (i=0;i<8;i++)
	        {
	            memset (new UBytePtr(pmem, size_aligned + i * 0x100 + 0x1800), 0, 0x80);
	            memset (new UBytePtr(pmem, size_aligned + i * 0x100 + 0x1880), 0xff, 0x80);
	        }
	        msx1.cart[id].mem = pmem;
	        break;
	   case 7: /* ASCII/8kB with SRAM */
	        //pmem = realloc (msx1.cart[id].mem, size_aligned + 0x2000);
		   pmem = new UBytePtr(msx1.cart[id].mem, size_aligned + 0x2000);
	        if (pmem != null)
	        {
	            //free (msx1.cart[id].mem); 
	            msx1.cart[id].mem = null;
	            return 1;
	        }
	        F = osd_fopen (Machine.gamedrv.name, msx1.cart[id].sramfile,
	                OSD_FILETYPE_MEMCARD, 0);
	        if ((F != null) && (osd_fread (F, new UBytePtr(pmem, size_aligned), 0x2000) == 0x2000) )
	        {
	            logerror("Cart #%d SRAM loaded\n", id);
	        } else {
	            memset (new UBytePtr(pmem, size_aligned), 0, 0x2000);
	            logerror("Cart #%d Failed to load SRAM\n", id);
	        }
	        if (F != null) osd_fclose (F);
	
	        msx1.cart[id].mem = pmem;
	        break;
	   case 8: /* ASCII/16kB with SRAM */
	        //pmem = realloc (msx1.cart[id].mem, size_aligned + 0x4000);
		   	pmem = new UBytePtr(msx1.cart[id].mem, size_aligned + 0x4000);
	        if (pmem != null)
	        {
	            //free (msx1.cart[id].mem); 
	            msx1.cart[id].mem = null;
	            return 1;
	        }
	        F = osd_fopen (Machine.gamedrv.name, msx1.cart[id].sramfile,
	                OSD_FILETYPE_MEMCARD, 0);
	        if ((F != null) && (osd_fread (F, new UBytePtr(pmem, size_aligned), 0x2000) == 0x2000) )
	        {
	            for (i=1;i<8;i++)
	            {
	                memcpy (new UBytePtr(pmem, size_aligned + i * 0x800),
	                		new UBytePtr(pmem, size_aligned), 0x800);
	            }
	            logerror("Cart #%d SRAM loaded\n", id);
	        } else {
	            memset (new UBytePtr(pmem, size_aligned), 0, 0x4000);
	            logerror("Cart #%d Failed to load SRAM\n", id);
	        }
	        if (F != null) osd_fclose (F);
	
	        msx1.cart[id].mem = pmem;
	        break;
	    case 9: /* R-Type */
	        msx1.cart[id].banks[0] = 0x1e;
	        msx1.cart[id].banks[1] = 0x1f;
	        msx1.cart[id].banks[2] = 0x1e;
	        msx1.cart[id].banks[3] = 0x1f;
	        break;
	    case 11: /* fm-pac */
	        msx1.cart[id].pacsram = !strncmp ((new UBytePtr(msx1.cart[id].mem, (0x18))).memory, "PAC2", 4);
	        //pmem = realloc (msx1.cart[id].mem, 0x18000);
	        pmem = new UBytePtr(msx1.cart[id].mem, 0x18000);
	        if (pmem != null)
	        {
	            //free (msx1.cart[id].mem); 
	        	msx1.cart[id].mem = null;
	            return 1;
	        }
	        memset (new UBytePtr(pmem, size_aligned), 0xff, 0x18000 - size_aligned);
	        pmem.write(0x13ff6, 0);
	        pmem.write(0x13ff7, 0);
	        if (msx1.cart[id].pacsram)
	        {
	            F = osd_fopen (Machine.gamedrv.name, msx1.cart[id].sramfile,
	                OSD_FILETYPE_MEMCARD, 0);
	            if ((F != null) &&
	                (osd_fread (F, buf, PAC_HEADER_LEN) == PAC_HEADER_LEN) &&
	                !strncmp (buf, PAC_HEADER, PAC_HEADER_LEN) &&
	                (osd_fread (F, new UBytePtr(pmem, 0x10000), 0x1ffe) == 0x1ffe) )
	            {
	               logerror("Cart #%d SRAM loaded\n", id);
	            } else {
	               memset ( new UBytePtr(pmem, 0x10000), 0, 0x2000);
	               logerror("Cart #%d Failed to load SRAM\n", id);
	            }
	            if (F != null) osd_fclose (F);
	        }
	        msx1.cart[id].banks[2] = (0x14000/0x2000);
	        msx1.cart[id].banks[3] = (0x16000/0x2000);
	        msx1.cart[id].mem = pmem;
	        break;
	    case 5: /* ASCII 16kb */
	    case 12: /* Gall Force */
	        msx1.cart[id].banks[0] = 0;
	        msx1.cart[id].banks[1] = 1;
	        msx1.cart[id].banks[2] = 0;
	        msx1.cart[id].banks[3] = 1;
	        break;
	    }
	    if (msx1.run != 0) msx_set_all_mem_banks ();
	    return 0;
	}};
	
	static int save_sram (int id, String filename, UBytePtr pmem, int size)
	{
	    System.out.println("save_sram");
            Object F;
	    int res=0;
	
	    F = osd_fopen (Machine.gamedrv.name, filename, OSD_FILETYPE_MEMCARD, 1);
	    
	    res = ((F!=null) && ((osd_fwrite (F, pmem, size)) == size))? 1:0;
	    if (F != null) osd_fclose (F);
	    return res;
	}
	
	public static io_exitPtr msx_exit_rom = new io_exitPtr() {
        public int handler(int id) {
	    Object F;
	    int size,res=0;
	
	    if (msx1.cart[id].mem != null)
	    {
	        /* save sram thingies */
	        switch (msx1.cart[id].type) {
	        case 6:
	            res = save_sram (id, msx1.cart[id].sramfile,
	            		 new UBytePtr(msx1.cart[id].mem, 0x21000), 0x2000);
	            break;
	        case 7:
	            res = save_sram (id, msx1.cart[id].sramfile,
	            		 new UBytePtr(msx1.cart[id].mem, (msx1.cart[id].bank_mask + 1) * 0x2000),
	                0x2000);
	            break;
	        case 8:
	            res = save_sram (id, msx1.cart[id].sramfile,
	            		 new UBytePtr(msx1.cart[id].mem, (msx1.cart[id].bank_mask + 1) * 0x2000),
	                0x800);
	            break;
	        case 11: /* fm-pac */
	            res = 1;
	            F = osd_fopen (Machine.gamedrv.name, msx1.cart[id].sramfile,
	                OSD_FILETYPE_MEMCARD, 1);
	            if (F==null) break;
	            size = strlen (PAC_HEADER);
	            System.out.println("TODO 3");
	            /*TODO*///if (osd_fwrite (F, PAC_HEADER, size) != size)
	            /*TODO*///    { osd_fclose (F); break; }
	            /*TODO*///if (osd_fwrite (F,  new UBytePtr(msx1.cart[id].mem, 0x10000), 0x1ffe) != 0x1ffe)
	            /*TODO*///    { osd_fclose (F); break; }
	            osd_fclose (F);
	            res = 0;
	            break;
	        default:
	            res = -1;
	            break;
	        }
	        if (res == 0) {
	            logerror("Cart %d# SRAM saved\n", id);
	        } else if (res > 0) {
	            logerror("Cart %d# failed to save SRAM\n", id);
	        }
	        //free (msx1.cart[id].mem);
	        msx1.cart[id].mem=null;
	        //free (msx1.cart[id].sramfile);
	        msx1.cart[id].sramfile=null;
	    }
	    
	    return res;
	}};
	
	//INTCallbackPtr
	public static INTCallbackPtr msx_vdp_interrupt = new INTCallbackPtr() { 

		public void handler(int i) {
			cpu_set_irq_line (0, 0, (i != 0 ? HOLD_LINE : CLEAR_LINE));
	
	}};
	
	public static InitMachinePtr msx_ch_reset = new InitMachinePtr() { public void handler() 
	{
	
		TMS9928A_reset ();
	    SCCResetChip (0);
	    /* set interrupt stuff */
	    cpu_irq_line_vector_w(0,0,0xff);
	    /* setup PPI */
	    ppi8255_init (msx_ppi8255_interface);
	
	    /* initialize mem regions */
	    if (msx1.empty==null || msx1.ram==null)
	    {
	        //msx1.empty = (UINT8*)malloc (0x4000);
	    	msx1.empty = new UBytePtr(0x4000);
	        //msx1.ram = (UINT8*)malloc (0x10000);
	    	msx1.ram = new UBytePtr(0x10000);
	        if (msx1.ram==null || msx1.empty==null)
	        {
	            logerror("malloc () in init_msx () failed!\n");
	            return;
	        }
	
	        memset (msx1.empty, 0xff, 0x4000);
	        memset (msx1.ram, 0, 0x10000);
	    }
	    msx1.run = 1;
	
	    return;
	}};
	
	public static InitDriverPtr init_msx = new InitDriverPtr() { public void handler() 
	{
		/* this function is called at a very early stage, and not after a reset. */
	    TMS9928A_int_callback(msx_vdp_interrupt);
	} };
	
	public static StopMachinePtr msx_ch_stop = new StopMachinePtr() { public void handler()
	{
	    //free (msx1.empty); 
	    msx1.empty = null;
	    //free (msx1.ram); 
	    msx1.ram = null;
	    msx1.run = 0;
	}};
	
	/*
	** The I/O funtions
	*/
	
	//READ_HANDLER ( msx_vdp_r )
	public static ReadHandlerPtr msx_vdp_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            System.out.println("msx_vdp_r");
        	if ((offset & 0x01) != 0)
    	        return TMS9928A_register_r();
    	    else
    	        return TMS9928A_vram_r();
        }
    };

	//WRITE_HANDLER ( msx_vdp_w )
    public static WriteHandlerPtr msx_vdp_w = new WriteHandlerPtr() { 
    	public void handler(int offset, int data){
            System.out.println("msx_vdp_w");
    		if ((offset & 0x01) != 0)
		        TMS9928A_register_w(data);
		    else
		        TMS9928A_vram_w(data);
    	}
	};
	
	//READ_HANDLER ( msx_psg_r )
	public static ReadHandlerPtr msx_psg_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            System.out.println("msx_psg_r");
        	return AY8910_read_port_0_r.handler(offset);
	}};
	
	//WRITE_HANDLER ( msx_psg_w )
	public static WriteHandlerPtr msx_psg_w = new WriteHandlerPtr() { 
    	public void handler(int offset, int data){
            System.out.println("msx_psg_w");
    		if ((offset & 0x01) != 0)
		        AY8910_write_port_0_w.handler(offset, data);
		    else
		        AY8910_control_port_0_w.handler(offset, data);
	}};
	
	//READ_HANDLER ( msx_psg_port_a_r )
	public static ReadHandlerPtr msx_psg_port_a_r = new ReadHandlerPtr() {
        public int handler(int offset) {
        	System.out.println("msx_psg_port_a_r");
		    int data;
		
		    data = (device_input (IO_CASSETTE, 0) > 255 ? 0x80 : 0);
		
		    if ((msx1.psg_b & 0x40) != 0)
		        data |= input_port_10_r.handler(0) & 0x7f;
		    else
		        data |= input_port_9_r.handler(0) & 0x7f;
		
		    return data;
	}};
	
	//READ_HANDLER ( msx_psg_port_b_r )
	public static ReadHandlerPtr msx_psg_port_b_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            System.out.println("msx_psg_port_b_r");
        	return msx1.psg_b;
	}};
	
	//WRITE_HANDLER ( msx_psg_port_a_w )
	public static WriteHandlerPtr msx_psg_port_a_w = new WriteHandlerPtr() { 
    	public void handler(int offset, int data){
    		System.out.println("msx_psg_port_a_w");
	}};
	
	//WRITE_HANDLER ( msx_psg_port_b_w )
	public static WriteHandlerPtr msx_psg_port_b_w = new WriteHandlerPtr() {
		public void handler(int offset, int data){
			System.out.println("msx_psg_port_b_w");
		    /* Arabic or kana mode led */
		    //if ( ((data ^ msx1.psg_b) & 0x80)!=0) osd_led_w (1, !(data & 0x80) );
			if ( ((data ^ msx1.psg_b) & 0x80)!=0) osd_led_w.handler(1, ((data & 0x80)) );
		        msx1.psg_b = data;
	}};
	
	//WRITE_HANDLER ( msx_printer_w )
	public static WriteHandlerPtr msx_printer_w = new WriteHandlerPtr() {
		public void handler(int offset, int data){
			System.out.println("msx_printer_w "+offset);
                        
		     if (offset == 1) {
		        /* SIMPL emulation */
		        DAC_signed_data_w (0, data);
		     }
	}};
	
	//READ_HANDLER ( msx_printer_r )
	public static ReadHandlerPtr msx_printer_r = new ReadHandlerPtr() {
        public int handler(int offset) {
        	System.out.println("msx_printer_r");
        	return 0xff;
	}};
	
	//WRITE_HANDLER ( msx_fmpac_w )
	public static WriteHandlerPtr msx_fmpac_w = new WriteHandlerPtr() {
		public void handler(int offset, int data){
			System.out.println("msx_fmpac_w");
		    if ((msx1.opll_active & 1) != 0)
		    {
		        if (offset == 1) YM2413_data_port_0_w.handler(0, data);
		        else YM2413_register_port_0_w.handler(0, data);
		    }
	}};
	
	
	
	/*
	** The memory functions
	*/
	static void msx_set_slot_0 (int page)
	{
	    UBytePtr ROM;
	    ROM = memory_region(REGION_CPU1);
	    if (page < (strcmp (Machine.gamedrv.name, "msxkr")==1 ? 2 : 3) )
	    {
	        //cpu_setbank (1 + page * 2, ROM + page * 0x4000);
	    	cpu_setbank (1 + page * 2, new UBytePtr(ROM, page * 0x4000));
	        //cpu_setbank (2 + page * 2, ROM + page * 0x4000 + 0x2000);
	    	cpu_setbank (2 + page * 2, new UBytePtr(ROM, page * 0x4000 + 0x2000));
	    } else {
	        cpu_setbank (1 + page * 2, msx1.empty);
	        cpu_setbank (2 + page * 2, msx1.empty);
	    }
	}
	
	static void msx_set_slot_1 (int page) {
	    int i,n;
	
	    
	    if ((msx1.cart[0].type == 0) && (msx1.cart[0].mem != null))
	    {
	        //cpu_setbank (1 + page * 2, msx1.cart[0].mem + page * 0x4000);
	    	cpu_setbank (1 + page * 2, new UBytePtr(msx1.cart[0].mem, page * 0x4000));
	        //cpu_setbank (2 + page * 2, msx1.cart[0].mem + page * 0x4000 + 0x2000);
	    	cpu_setbank (2 + page * 2, new UBytePtr(msx1.cart[0].mem, page * 0x4000 + 0x2000));
	    } else {
	        if (page == 0 || page == 3 || msx1.cart[0].mem != null)
	        {
	            cpu_setbank (1 + page * 2, msx1.empty);
	            cpu_setbank (2 + page * 2, msx1.empty);
	            return;
	        }
	        n = (page - 1) * 2;
	        for (i=0;i<2;i++)
	        {
	            cpu_setbank (3 + i + n,
	            //msx1.cart[0].mem + msx1.cart[0].banks[i + n] * 0x2000);
	            new UBytePtr(msx1.cart[0].mem, msx1.cart[0].banks[i + n] * 0x2000));
	        }
	    }
	}
	
	static void msx_set_slot_2 (int page)
	{
	    int i,n;
	
	    if ((msx1.cart[1].type == 0) && (msx1.cart[1].mem != null))
	    {
	        //cpu_setbank (1 + page * 2, msx1.cart[1].mem + page * 0x4000);
	    	cpu_setbank (1 + page * 2, new UBytePtr(msx1.cart[1].mem, page * 0x4000));
	        //cpu_setbank (2 + page * 2, msx1.cart[1].mem + page * 0x4000 + 0x2000);
	    	cpu_setbank (2 + page * 2, new UBytePtr(msx1.cart[1].mem, page * 0x4000 + 0x2000));
	    } else {
	        //if (page == 0 || page == 3 || !msx1.cart[1].mem)
	    	if (page == 0 || page == 3 || (msx1.cart[1].mem != null))
	        {
	            cpu_setbank (1 + page * 2, msx1.empty);
	            cpu_setbank (2 + page * 2, msx1.empty);
	            return;
	        }
	        n = (page - 1) * 2;
	        for (i=0;i<2;i++)
	        {
	            cpu_setbank (3 + i + n,
	            //msx1.cart[1].mem + msx1.cart[1].banks[i + n] * 0x2000);
	            new UBytePtr(msx1.cart[1].mem, msx1.cart[1].banks[i + n] * 0x2000));
	        }
	    }
	}
	
	static void msx_set_slot_3 (int page)
	{
	    //cpu_setbank (1 + page * 2, msx1.ram + page * 0x4000);
		cpu_setbank (1 + page * 2, new UBytePtr(msx1.ram, page * 0x4000));
	    //cpu_setbank (2 + page * 2, msx1.ram + page * 0x4000 + 0x2000);
		cpu_setbank (2 + page * 2, new UBytePtr(msx1.ram, page * 0x4000 + 0x2000));
	}
	
	static void msx_set_slot(int slot, int page) {
		
			switch (slot) {
				case 0:
					msx_set_slot_0(page);
					break;
				case 1:
					msx_set_slot_1(page);
					break;
				case 2:
					msx_set_slot_2(page);
					break;
				case 3:
					msx_set_slot_3(page);
					break;
			}
	
	};
	
	static void msx_set_all_mem_banks ()
	{
	    int i;
	    
	    for (i=0;i<4;i++) {
	    	msx_set_slot((ppi8255_0_r.handler(0)>>(i*2))&3,(i));
	    }
	}
	
	//WRITE_HANDLER ( msx_writemem0 )
	public static WriteHandlerPtr msx_writemem0 = new WriteHandlerPtr() { 
    	public void handler(int offset, int data){
		     if ( (ppi8255_0_r.handler(0) & 0x03) == 0x03 )
		        msx1.ram.write(offset, data);
	}};
	
	static int msx_cart_page_2 (int cart)
	{
	    /* returns non-zero if `cart' is in page 2 */
	    switch (ppi8255_0_r.handler(0) & 0x30)
	    {
	    case 0x10: return (cart == 0 ? 1 :0);
	    case 0x20: return (cart == 1 ? 1 :0);
	    }
	    return 0;
	}
	
	static void msx_cart_write (int cart, int offset, int data)
	{
		System.out.println("msx_cart_write");
	    int n,i;
	    UBytePtr p;
	    
	    System.out.println("Cart: "+cart);
            System.out.println("Offset: "+offset);
            System.out.println("Data: "+data);
	    System.out.println("msx1.cart: "+msx1.cart);
	    
	    if (msx1.cart[cart] == null) {
	    	msx1.cart[cart]=new MSX_CART();
	    	
	    }
            
            System.out.println("msx1.cart[cart].type: "+msx1.cart[cart].type);
	
	    switch (msx1.cart[cart].type)
	    {
	    case 0:
	        break;
	    case 1: /* MSX-DOS 2 cartridge */
	        if (offset == 0x2000)
	        {
	            n  = (data * 2) & 7;
	            msx1.cart[cart].banks[0] = n;
	            msx1.cart[cart].banks[1] = n + 1;
	            //cpu_setbank (3,msx1.cart[cart].mem + n * 0x2000);
	            cpu_setbank (3,new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	            //cpu_setbank (4,msx1.cart[cart].mem + (n + 1) * 0x2000);
	            cpu_setbank (4,new UBytePtr(msx1.cart[cart].mem, (n + 1) * 0x2000));
	        }
	        break;
	    case 2: /* Konami5 with SCC */
	        if ( (offset & 0x1800) == 0x1000)
	        {
	            /* check if SCC should be activated */
	            //if ( ( (offset & 0x7800) == 0x5000) && !(~data & 0x3f) )
	        	if ( ( (offset & 0x7800) == 0x5000) && ((~data & 0x3f) == 0) )
	                n = msx1.cart[cart].bank_mask + 1;
	            else
	                n = data & msx1.cart[cart].bank_mask;
	            msx1.cart[cart].banks[(offset/0x2000)] = n;
	            //cpu_setbank (3+(offset/0x2000),msx1.cart[cart].mem + n * 0x2000);
	            cpu_setbank (3+(offset/0x2000),new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	        }
	        else if ( (msx1.cart[cart].banks[2] > msx1.cart[cart].bank_mask) &&
	                (offset >= 0x5800) && (offset < 0x6000) )
	        {
	            SCCWriteReg (0, offset & 0xff, data, SCC_MEGAROM);
	            if ( (offset & 0x80) == 0)
	            {
	                p = new UBytePtr(msx1.cart[cart].mem,
	                    (msx1.cart[cart].bank_mask + 1) * 0x2000);
	                for (n=0;n<8;n++) 
	                	p.write(n*0x100+0x1800+(offset&0x7f), data);
	            }
	        }
	        break;
	    case 3: /* Konami4 without SCC */
	        if (((offset!= 0) && ((offset & 0x1fff)!=0) ) )
	        {
	            n = data & msx1.cart[cart].bank_mask;
	            msx1.cart[cart].banks[(offset/0x2000)] = n;
	            //cpu_setbank (3+(offset/0x2000),msx1.cart[cart].mem + n * 0x2000);
	            cpu_setbank (3+(offset/0x2000),new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	        }
	        break;
	    case 4: /* ASCII 8kB */
	        if ( (offset >= 0x2000) && (offset < 0x4000) )
	        {
	            offset -= 0x2000;
	            n = data & msx1.cart[cart].bank_mask;
	            msx1.cart[cart].banks[(offset/0x800)] = n;
	            if ((offset/0x800) < 2 || (msx_cart_page_2 (cart)!=0) ) {
	                //cpu_setbank (3+(offset/0x800),msx1.cart[cart].mem + n * 0x2000);
	            	cpu_setbank (3+(offset/0x800), new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	            }
	        }
	        break;
	    case 12: /* Gall Force */
	    case 5: /* ASCII 16kB */
	        if ( (offset & 0x6800) == 0x2000)
	        {
	            n = (data * 2) & msx1.cart[cart].bank_mask;
	
	            if ((offset & 0x1000) != 0)
	            {
	                /* page 2 */
	                msx1.cart[cart].banks[2] = n;
	                msx1.cart[cart].banks[3] = n + 1;
	                if (msx_cart_page_2 (cart) != 0)
	                {
	                    //cpu_setbank (5,msx1.cart[cart].mem + n * 0x2000);
	                	cpu_setbank (5, new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	                    //cpu_setbank (6,msx1.cart[cart].mem + (n + 1) * 0x2000);
	                	cpu_setbank (6, new UBytePtr(msx1.cart[cart].mem, (n + 1) * 0x2000));
	                }
	            } else {
	                /* page 1 */
	                msx1.cart[cart].banks[0] = n;
	                msx1.cart[cart].banks[1] = n + 1;
	                //cpu_setbank (3,msx1.cart[cart].mem + n * 0x2000);
	                cpu_setbank (3, new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	                //cpu_setbank (4,msx1.cart[cart].mem + (n + 1) * 0x2000);
	                cpu_setbank (4, new UBytePtr(msx1.cart[cart].mem, (n + 1) * 0x2000));
	            }
	        }
	        break;
	    case 6: /* Game Master 2 */
	        if (((offset & 0x1000)==0) && (offset >= 0x2000) )
	        {
	            n = (((data & 0x10)!=0) ? (((data & 0x20)!=0) ? 0x11:0x10) : (data & 0x0f));
	            msx1.cart[cart].banks[(offset/0x2000)] = n;
	            //cpu_setbank (3+(offset/0x2000),msx1.cart[cart].mem+n*0x2000);
	            cpu_setbank (3+(offset/0x2000), new UBytePtr(msx1.cart[cart].mem, n*0x2000));
	        }
	        else if (offset >= 0x7000)
	        {
	            switch (msx1.cart[cart].banks[3])
	            {
	            case 0x10:
	                msx1.cart[cart].mem.write(0x20000+(offset&0x0fff), data);
	                msx1.cart[cart].mem.write(0x21000+(offset&0x0fff), data);
	                break;
	            case 0x11:
	                msx1.cart[cart].mem.write(0x22000+(offset&0x0fff), data);
	                msx1.cart[cart].mem.write(0x23000+(offset&0x0fff), data);
	                break;
	            }
	        }
	        break;
	    case 7: /* ASCII 8kB/SRAM */
	        if ( (offset >= 0x2000) && (offset < 0x4000) )
	        {
	            offset -= 0x2000;
	            if (data > msx1.cart[cart].bank_mask)
	                n = msx1.cart[cart].bank_mask + 1;
	            else
	                n = data;
	            msx1.cart[cart].banks[(offset/0x800)] = n;
	            if ((offset/0x800) < 2 || (msx_cart_page_2 (cart)!=0) ) {
	                //cpu_setbank (3+(offset/0x800),msx1.cart[cart].mem + n * 0x2000);
	            	cpu_setbank (3+(offset/0x800), new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	            }
	        }
	        else if (offset >= 0x4000)
	        {
	            n = (offset >= 0x6000 ? 1 : 0);
	            if (msx1.cart[cart].banks[2+n] > msx1.cart[cart].bank_mask)
	                msx1.cart[cart].mem.write((offset&0x1fff)+
	                    (msx1.cart[cart].bank_mask+1)*0x2000, data);
	        }
	        break;
	    case 8: /* ASCII 16kB */
	        if ( (offset & 0x6800) == 0x2000)
	        {
	            if (data > (msx1.cart[cart].bank_mask/2))
	                n = msx1.cart[cart].bank_mask + 1;
	            else
	                n = (data * 2) & msx1.cart[cart].bank_mask;
	
	            if ((offset & 0x1000) != 0)
	            {
	                /* page 2 */
	                msx1.cart[cart].banks[2] = n;
	                msx1.cart[cart].banks[3] = n + 1;
	                if (msx_cart_page_2 (cart) != 0)
	                {
	                    //cpu_setbank (5,msx1.cart[cart].mem + n * 0x2000);
	                	cpu_setbank (5, new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	                    cpu_setbank (6, new UBytePtr(msx1.cart[cart].mem, (n + 1) * 0x2000));
	                }
	            } else {
	                /* page 1 */
	                msx1.cart[cart].banks[0] = n;
	                msx1.cart[cart].banks[1] = n + 1;
	                //cpu_setbank (3,msx1.cart[cart].mem + n * 0x2000);
	                cpu_setbank (3, new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	                //cpu_setbank (4,msx1.cart[cart].mem + (n + 1) * 0x2000);
	                cpu_setbank (4, new UBytePtr(msx1.cart[cart].mem, (n + 1) * 0x2000));
	            }
	        }
	        else if (offset >= 0x4000 &&
	            msx1.cart[cart].banks[2] > msx1.cart[cart].bank_mask)
	        {
	            for (i=0;i<8;i++)
	                msx1.cart[cart].mem.write(i*0x800+(offset&0x7ff)+
	                    (msx1.cart[cart].bank_mask+1)*0x2000, data);
	        }
	        break;
	    case 9: /* R-Type */
	        if (offset >= 0x3000 && offset < 0x4000)
	        {
	            if ((data & 0x10) != 0)
	            {
	                n = (( (data & 0x07) | 0x10) * 2) & msx1.cart[cart].bank_mask;
	            } else {
	                n = ((data & 0x0f) * 2) & msx1.cart[cart].bank_mask;
	            }
	
	            msx1.cart[cart].banks[2] = n;
	            msx1.cart[cart].banks[3] = n + 1;
	            if (msx_cart_page_2 (cart) != 0)
	            {
	                //cpu_setbank (5,msx1.cart[cart].mem + n * 0x2000);
	            	cpu_setbank (5, new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	                cpu_setbank (6, new UBytePtr(msx1.cart[cart].mem, (n + 1) * 0x2000));
	            }
	        }
	        break;
	    case 10: /* Konami majutushi */
	        if (offset >= 0x1000 && offset < 0x2000)
	            DAC_data_w (0, data);
	        else if (offset >= 0x2000)
	        {
	            n = data & msx1.cart[cart].bank_mask;
	            msx1.cart[cart].banks[(offset/0x2000)] = n;
	            //cpu_setbank (3+(offset/0x2000),msx1.cart[cart].mem + n * 0x2000);
	            cpu_setbank (3+(offset/0x2000), new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	        }
	        break;
	    case 11: /* FM-PAC */
	        if (offset < 0x1ffe && (msx1.cart[cart].pacsram ))
	        {
	            if (msx1.cart[cart].banks[1] > 7)
	                msx1.cart[cart].mem.write(0x10000 + offset, data);
	            break;
	        }
	        if (offset == 0x3ff4 && (msx1.opll_active != 0))
	        {
	            YM2413_register_port_0_w.handler(0, data);
	            break;
	        }
	        if (offset == 0x3ff5 && (msx1.opll_active != 0))
	        {
	            YM2413_data_port_0_w.handler(0, data);
	            break;
	        }
	        if (offset == 0x3ff6)
	        {
	            n = data & 0x11;
	            msx1.cart[cart].mem.write(0x3ff6, n);
	            msx1.cart[cart].mem.write(0x7ff6, n);
	            msx1.cart[cart].mem.write(0xbff6, n);
	            msx1.cart[cart].mem.write(0xfff6, n);
	            msx1.cart[cart].mem.write(0x13ff6, n);
	            msx1.opll_active = data & 1;
	            logerror("FM-PAC: OPLL %s\n",((data & 1) != 0) ? "activated" : "deactivated");
	            break;
	        }
	        if ( (offset == 0x1ffe || offset == 0x1fff) && (msx1.cart[cart].pacsram))
	        {
	            msx1.cart[cart].mem.write(0x10000 + offset,  data);
	            if (msx1.cart[cart].mem.read(0x11ffe) == 0x4d &&
	                msx1.cart[cart].mem.read(0x11fff) == 0x69)
	                n = 8;
	            else
	                n = msx1.cart[cart].mem.read(0x13ff7) * 2;
	        }
	        else
	        {
	            if (offset == 0x3ff7)
	            {
	                msx1.cart[cart].mem.write(0x13ff7, data & 3);
	                if (msx1.cart[cart].banks[1] > 7) break;
	                n = ((data & 3) * 2) & msx1.cart[cart].bank_mask;
	            } else break;
	        }
	        msx1.cart[cart].banks[0] = n;
	        msx1.cart[cart].banks[1] = n + 1;
	        //cpu_setbank (3,msx1.cart[cart].mem + n * 0x2000);
	        cpu_setbank (3, new UBytePtr(msx1.cart[cart].mem, n * 0x2000));
	        //cpu_setbank (4,msx1.cart[cart].mem + (n + 1) * 0x2000);
	        cpu_setbank (4, new UBytePtr(msx1.cart[cart].mem, (n + 1) * 0x2000));
	        break;
	    case 13: /* Konami Synthesizer */
	        if (offset==0) DAC_data_w (0, data);
	        break;
	    }
	}
	
	//WRITE_HANDLER ( msx_writemem1 )
	public static WriteHandlerPtr msx_writemem1 = new WriteHandlerPtr() { 
    	public void handler(int offset, int data){
            System.out.println("msx_writemem1");
		    switch (ppi8255_0_r.handler(0) & 0x0c)
		    {
		    case 0x04:
		        msx_cart_write (0, offset, data);
		        break;
		    case 0x08:
		        msx_cart_write (1, offset, data);
		        break;
		    case 0x0c:
		        msx1.ram.write(0x4000+offset, data);
		    }
	}};
	
	//WRITE_HANDLER ( msx_writemem2 )
	public static WriteHandlerPtr msx_writemem2 = new WriteHandlerPtr() { 
    	public void handler(int offset, int data){
            System.out.println("msx_writemem2");
		    switch (ppi8255_0_r.handler(0) & 0x30)
		    {
		    case 0x10:
		        msx_cart_write (0, 0x4000 + offset, data);
		        break;
		    case 0x20:
		        msx_cart_write (1, 0x4000 + offset, data);
		        break;
		    case 0x30:
		        msx1.ram.write(0x8000+offset, data);
		    }
	}};
	
	//WRITE_HANDLER ( msx_writemem3 )
	public static WriteHandlerPtr msx_writemem3 = new WriteHandlerPtr() { 
    	public void handler(int offset, int data){
            int da=ppi8255_0_r.handler(0);
            //System.out.println("msx_writemem3 "+da);
		    if ( (da & 0xc0) == 0xc0){
                        //System.out.println("Escribo en "+0xc000+offset+":"+data);
		        msx1.ram.write(0xc000+offset, data);
                    }
	}};
	
	/*
	** Cassette functions
	*/
	
	public static io_initPtr msx_cassette_init = new io_initPtr() {
        public int handler(int id) {
	    Object file;
	
	    file = image_fopen(IO_CASSETTE, id, OSD_FILETYPE_IMAGE_RW, OSD_FOPEN_READ);
	    if (file != null)
	    {
	        //wave_args wa = {0,};
	    	wave_args wa = new wave_args(file);
	        wa.file = file;
	        wa.display = 1;
	        if( device_open(IO_CASSETTE,id,0,wa) != 0)
	            return INIT_FAILED;
	        return INIT_OK;
	    }
	    file = image_fopen(IO_CASSETTE, id, OSD_FILETYPE_IMAGE_RW,
	        OSD_FOPEN_RW_CREATE);
	    if (file != null)
	    {
	        wave_args wa = new wave_args(file);
	        wa.file = file;
	        wa.display = 1;
	        wa.smpfreq = 44100;
	        if( device_open(IO_CASSETTE,id,1,wa) != 0)
	            return INIT_FAILED;
	        return INIT_OK;
	    }
	    return INIT_FAILED;
	}};
	
	public static io_exitPtr msx_cassette_exit = new io_exitPtr() {
        public int handler(int id) {
		/*TODO*///device_close(IO_CASSETTE,id);
        return 1;
	}};
	
}
