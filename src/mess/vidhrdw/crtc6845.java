/***************************************************************************

  motorola cathode ray tube controller 6845

  praster version

  copyright peter.trauner@jk.uni-linz.ac.at

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.vidhrdw;

import static WIP.arcadeflex.fucPtr.*;
import static WIP.arcadeflex.libc_v2.*;
import static mame.commonH.*;
import static mess.includes.prasterH.*;
import static old.mame.common.*;
import static old.arcadeflex.osdepend.logerror;

//import static mess.includes.prasterH.*;
import static mess.vidhrdw.praster.*;

public class crtc6845
{
	
	public static int VERBOSE_DBG = 1;
	
	
	public static class crtc_struct {
        //public static class crtc {
		public int[] reg = new int[18];
		public int index;
		public int cursor_on; /* cursor output used */
	};
        
        public static crtc_struct crtc = new crtc_struct();
	
	public static void crtc6845_init (UBytePtr memory)
	{
		//memset(crtc, sizeof(crtc));
		crtc.cursor_on=1;
	
		praster_2_init();
                                                
		raster2.memory.ram=memory;
		raster2.raytube.screenpos_x=raster2.raytube.screenpos_y=0;
		raster2.mode=mess.includes.prasterH.PRASTER_MODE.PRASTER_GFXTEXT;
	
		raster2.text.charsize_x=raster2.text.charsize_y=8;
		raster2.text.visiblesize_x=raster2.text.visiblesize_y=8;
		raster2.linediff=0;raster2.text.size_x=80;
		raster2.text.size_y=25;
		raster2.cursor.on=0;
		raster2.cursor.pos=0;
		raster2.cursor.blinking=1;
		raster2.cursor.delay=16;
		raster2.cursor.ybegin=0;raster2.cursor.yend=7;
	
		raster2.memory.mask=raster2.memory.videoram_mask=0xffff;
	}
	
	void crtc6845_pet_init (UBytePtr memory)
	{
		UBytePtr gfx = memory_region(REGION_GFX1);
		int i;
	
		for (i=0; i<0x400; i++) {
			gfx.write((0x800+i), gfx.read(0x400+i));
			gfx.write((0xc00+i), gfx.read(0x800+i)^0xff);
			gfx.write((0x400+i), gfx.read(i)^0xff);
		}
	
		crtc6845_init(memory);
		crtc.cursor_on=0;
		raster2.text.fontsize_y=8;
		raster2.memory.mask=raster2.memory.videoram_mask=0x7ff;
		raster2.text.charsize_x=8;
		raster2.text.visiblesize_x=8;
	}
	
	void crtc6845_superpet_init (UBytePtr memory)
	{
		UBytePtr gfx = memory_region(REGION_GFX1);
		int i;
	
		for (i=0; i<0x400; i++) {
			gfx.write((0x1000+i), gfx.read(0x800+i));
			gfx.write((0x1800+i), gfx.read(0xc00+i));
			gfx.write((0x1c00+i), gfx.read(0x1800+i)^0xff);
			gfx.write((0x1400+i), gfx.read(0x1000+i)^0xff);
			gfx.write((0x800+i), gfx.read(0x400+i));
			gfx.write((0xc00+i), gfx.read(0x800+i)^0xff);
			gfx.write((0x400+i), gfx.read(i)^0xff);
		}
	
		crtc6845_init(memory);
		crtc.cursor_on=0;
		raster2.text.fontsize_y=8;
		raster2.memory.mask=raster2.memory.videoram_mask=0x7ff;
		raster2.text.charsize_x=8;
		raster2.text.visiblesize_x=8;
	}
	
	void crtc6845_cbm600_init(UBytePtr memory)
	{
		UBytePtr gfx = memory_region(REGION_GFX1);
		int i;
	
		for (i=0; i<0x800; i++) {
			gfx.write((0x1000+i), gfx.read(0x800+i));
			gfx.write((0x1800+i), gfx.read(0x1000+i)^0xff);
			gfx.write((0x800+i), gfx.read(i)^0xff);
		}
	
		crtc6845_init(memory);
		raster2.text.fontsize_y=16;
		raster2.text.charsize_x=8;
		raster2.text.visiblesize_x=8;
	}
	
	void crtc6845_cbm600pal_init(UBytePtr memory)
	{
		/* no hardware reverse logic, instead double size charrom,
		   and switching between hungarian and ascii charset */
		crtc6845_init(memory);
		raster2.text.fontsize_y=16;
		raster2.text.charsize_x=8;
		raster2.text.visiblesize_x=8;
	}
	
	void crtc6845_cbm700_init(UBytePtr memory)
	{
		UBytePtr gfx = memory_region(REGION_GFX1);
		int i;
	
		for (i=0; i<0x800; i++) {
			gfx.write((0x1000+i), gfx.read(0x800+i));
			gfx.write((0x1800+i), gfx.read(0x1000+i)^0xff);
			gfx.write((0x800+i), gfx.read(i)^0xff);
		}
	
		crtc6845_init(memory);
		raster2.text.fontsize_y=16;
		raster2.text.charsize_x=9;
		raster2.text.charsize_y=14;
		raster2.text.visiblesize_x=8;
		raster2.text.visiblesize_y=14;
	}
	
	void crtc6845_set_rastering(int on)
	{
		raster2.display.no_rastering=((on==0)? 1:0);
	/*	raster2.text.visiblesize.x=raster2.text.visiblesize.y=8; */
	}
	
	public static int COLUMNS = (crtc.reg[0]+1);
	public static int COLUMNS_VISIBLE = (crtc.reg[1]);
	public static int COLUMNS_SYNC_POS = (crtc.reg[2]);
	public static int COLUMNS_SYNC_SIZE = ((crtc.reg[3]&0xf)-1);
	public static int LINES_SYNC_SIZE = (crtc.reg[3]>>4);
	public static int CHARHEIGHT = (crtc.reg[9]%0x1f);
        public static int LINES = (crtc.reg[4]*CHARHEIGHT+crtc.reg[5]&0x1f);
	public static int CHARLINES = (crtc.reg[6]&0x1f);
	public static int LINES_SYNC_POS = (crtc.reg[7]);
	public static int INTERLACE = (crtc.reg[8]&1);
	public static int LINES400 = ((crtc.reg[8]&3)==3 ? 1 : 0);
	
	
	
	//WRITE_HANDLER ( crtc6845_port_w )
	public static WriteHandlerPtr crtc6845_port_w = new WriteHandlerPtr() {
            public void handler(int offset, int data) {
		if ((offset & 1) != 0)
		{
			if ((crtc.index & 0x1f) < 18)
			{
				switch (crtc.index & 0x1f)
				{
				case 1:
					crtc.reg[crtc.index]=data;
					raster2.text.size_x=data;
					raster2.memory.videoram_size=
						raster2.text.size_y*data;
					praster_2_update();
					break;
				case 6:
					crtc.reg[crtc.index]=data;
					raster2.text.size_y=data;
					raster2.memory.videoram_size=
						raster2.text.size_x*data;
					praster_2_update();
					break;
				case 9:
					crtc.reg[crtc.index]=data;
					raster2.text.charsize_y=(data&0x1f)+1;
					praster_2_update();
					break;
				case 0xa:
					crtc.reg[crtc.index]=data;
					raster2.cursor.on=((data&0x60)!=0x20)&&(crtc.cursor_on==1)? 1 : 0;
					raster2.cursor.blinking=(data&0x60)!=0 ? 1:0;
					if ((data&0x60)==0x60) raster2.cursor.delay=32;
					else raster2.cursor.delay=16;
					raster2.cursor.ybegin=data&0x1f;
					praster_2_cursor_update();
					break;
				case 0xb:
					crtc.reg[crtc.index]=data;
					raster2.cursor.yend=data&0x1f;
					praster_2_cursor_update();
					break;
				case 0xc: case 0xd:
					crtc.reg[crtc.index]=data;
					raster2.memory.videoram_offset=
						( (crtc.reg[0xc]<<8)|crtc.reg[0xd])
						&raster2.memory.videoram_mask;
					praster_2_update();
					break;
				case 0xe: case 0xf:
					crtc.reg[crtc.index]=data;
					raster2.cursor.pos=((crtc.reg[0xe]&0xf)<<8)|crtc.reg[0xf];
					praster_2_cursor_update();
					break;
				default:
					crtc.reg[crtc.index]=data;
					//DBG_LOG (2, "crtc_port_w",("%.2x:%.2x\n", crtc.index, data));
					break;
				}
			}
			//DBG_LOG (1, "crtc6845_port_w",("%.2x:%.2x\n", crtc.index, data));
		}
		else
		{
			crtc.index = data;
		}
	}};
	
	/* internal flipflop for doubling horizontal
	   value */
	//WRITE_HANDLER ( crtc6845_pet_port_w )
        public static WriteHandlerPtr crtc6845_pet_port_w = new WriteHandlerPtr() {
            public void handler(int offset, int data) {
		if ((offset & 1) != 0)
		{
			if ((crtc.index & 0x1f) < 18)
			{
				switch (crtc.index & 0x1f)
				{
				case 1:
					crtc.reg[crtc.index]=data;
					raster2.text.size_x=data*2;
					raster2.memory.videoram_size=
						raster2.text.size_y*data*2;
					praster_2_update();
					break;
				default:
					crtc6845_port_w.handler(offset,data);
					break;
				}
			} else
				crtc6845_port_w.handler(offset,data);
		}
		else
		{
			crtc6845_port_w.handler(offset,data);
		}
	}};
	
	//READ_HANDLER ( crtc6845_port_r )
	public static ReadHandlerPtr crtc6845_port_r = new ReadHandlerPtr() {
            public int handler(int offset) {
		int val;
	
		val = 0xff;
		if ((offset & 1) != 0)
		{
			if ((crtc.index & 0x1f) < 18)
			{
				switch (crtc.index & 0x1f)
				{
				case 0x14: val=crtc.reg[offset]&0x3f;break;
				default:
					val=crtc.reg[offset];
				}
			}
			//logerror (1, "crtc6845_port_r", ("%.2x:%.2x\n", crtc.index, val));
		}
		else
		{
			val = crtc.index;
		}
		return val;
	}};
	
	void crtc6845_address_line_11(int level)
	{
		raster2.memory.fontram_offset=(level!=0)?0x800:0;
	}
	
	void crtc6845_address_line_12(int level)
	{
		raster2.memory.fontram_offset=(level!=0)?0x1000:0;
	}
	
	void crtc6845_status (char[] text, int size)
	{
		text[0]=0;
	//#if VERBOSE_DBG
			//snprintf (text, sizeof (text), "crtc6845 %.2x %.2x %.2x %.2x",crtc.reg[0xc], crtc.reg[0xd], crtc.reg[0xe],crtc.reg[0xf]);
	//#endif
	}
}
