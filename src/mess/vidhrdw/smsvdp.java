/*
 * ported to v0.37b6
 * using automatic conversion tool v0.01
 */ 
package mess.vidhrdw;

import static WIP.arcadeflex.fucPtr.*;
import WIP.arcadeflex.libc_v2.UBytePtr;
import WIP.arcadeflex.libc_v2.UShortPtr;
import static WIP.mame.common.*;
import static WIP.mame.mame.Machine;
import static WIP.mame.osdependH.*;
import static arcadeflex.libc.cstring.memset;
import static vidhrdw.generic.tmpbitmap;
import static old.arcadeflex.video.osd_free_bitmap;
import static mess.vidhrdw.smsvdpH.*;
import static old.mame.cpuintrfH.ASSERT_LINE;
import static old.arcadeflex.video.osd_skip_this_frame;
import static old.cpu.z80.z80H.Z80_IGNORE_INT;
import static old.mame.cpuintrfH.CLEAR_LINE;
import static old.mame.drawgfxH.TRANSPARENCY_NONE;
import static old.mame.cpuintrf.*;
import static old.mame.drawgfx.*;
import static old.mame.palette.*;
import static cpu.z80.z80.*;

public class smsvdp
{
	
	static int[]/*UINT8*/ u8_reg=new int[0x10];
	static int[]/*UINT8*/ u8_cram=new int[0x40];
	static UBytePtr vram=new UBytePtr(0x4000);
	static int addr;
	static int code;
	static int pending;
	static int latch;
	static int buffer;
	static int status;
	
	static int is_vram_dirty;
	static int is_cram_dirty;
	static int[]/*UINT8*/ u8_vram_dirty=new int[0x200];
	static int[]/*UINT8*/ u8_cram_dirty=new int[0x20];
	static int[]/*UINT8*/ u8_cache=new int[0x20000];
	
	static int GameGear;
	static int vpstart;
	static int vpend;
	static int ntab;
	static int satb;
	static int curline;
	static int linesleft;
	static int irq_state;      /* The status of the IRQ line, as seen by the VDP */
	
	/*--------------------------------------------------------------------------*/
	
	/* Precalculated return values from the V counter */
	static int vcnt[] =
	{
	    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
	    0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F,
	    0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F,
	    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,
	    0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,
	    0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F,
	    0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,
	    0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F,
	    0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F,
	    0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F,
	    0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF,
	    0xB0, 0xB1, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xBB, 0xBC, 0xBD, 0xBE, 0xBF,
	    0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD, 0xCE, 0xCF,
	    0xD0, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA,
	                                  0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF,
	    0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
	    0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, 0xFC, 0xFD, 0xFE, 0xFF,
	};
	
	/*--------------------------------------------------------------------------*/
	
        public static VhStartPtr sms_vdp_start = new VhStartPtr() {
        public int handler() {
	    return SMSVDP_start(0);
	}};
	public static VhStartPtr gamegear_vdp_start = new VhStartPtr() {
        public int handler() {
	    return SMSVDP_start(1);
	}};
	
	public static ReadHandlerPtr  sms_vdp_curline_r= new ReadHandlerPtr() {
        public int handler(int offset) {
	    return (vcnt[(curline & 0x1FF)]);
	}};
	
	static int SMSVDP_start (int vdp_type)
	{
	    GameGear = vdp_type;
	
	    vpstart = GameGear!=0 ? 24 : 0;
	    vpend   = GameGear!=0 ? 168 : 192;
	
	    /* Clear RAM */
	    memset(u8_reg, 0, 0x10);
	    memset(u8_cram, 0, 0x40);
	    memset(vram, 0, 0x4000);
	
	    /* Initialize VDP state variables */
	    addr = code = pending = latch = buffer = status = 
	    ntab = satb = curline = linesleft = irq_state = 0;
	
	    is_vram_dirty = 1;
	    memset(u8_vram_dirty, 1, 0x200);
	
	    is_cram_dirty = 1;
	    memset(u8_cram_dirty, 1, 0x20);
	
	    /* Clear pattern cache */
	    memset(u8_cache, 0, 0x20000);
	
	    /* Make temp bitmap for rendering */
	    tmpbitmap = bitmap_alloc(256, 224);
	    if(tmpbitmap==null) return (1);
	
	    return (0);
	}
	
	public static VhStopPtr sms_vdp_stop = new VhStopPtr() {
        public void handler() {
	    if (tmpbitmap != null) osd_free_bitmap(tmpbitmap);
	}};
	
	
	public static InterruptPtr sms_vdp_interrupt = new InterruptPtr() { public int handler() 
	{
	    /* Bump scanline counter */
	    curline = (curline + 1) % 262;
	
	    if(curline <= 0xC0)
	    {
	        /* Technically, this happens at cycle 0xF4 of line 0xBF, but
	           this is close enough. */
	        if(curline == 0xC0)
	        {
	            status |= STATUS_VINT;
	        }
	
	        if(curline == 0x00)
	        {
	            linesleft = u8_reg[10];
	        }
	
	        if(linesleft == 0x00)
	        {
	            linesleft = u8_reg[10];
	            status |= STATUS_HINT;
	        }
	        else
	        {
	            linesleft -= 1;
	        }
	
	        if((status & STATUS_HINT)!=0 && (u8_reg[0] & 0x10)!=0)
	        {
	            irq_state = 1;
	            SETIRQLINE(0,0,ASSERT_LINE);//z80_set_irq_line(0, ASSERT_LINE);
	        }
	    }
	    else
	    {
	        linesleft = u8_reg[10];
	
	        if((curline < 0xE0) && (status & STATUS_VINT)!=0 && (u8_reg[1] & 0x20)!=0)
	        {
	            irq_state = 1;
	            SETIRQLINE(0,0,ASSERT_LINE);//z80_set_irq_line(0, ASSERT_LINE);
	        }
	    }
	
	    if( (curline >= vpstart) && (curline < vpend) && (osd_skip_this_frame()==0) )
	    {
	        sms_cache_tiles();
	        sms_refresh_line(tmpbitmap, curline);
	    }
	
	    return (Z80_IGNORE_INT);
	} };
	
	
	
	public static ReadHandlerPtr  sms_vdp_data_r= new ReadHandlerPtr() {
        public int handler(int offset) {
	    int temp = 0;
	
	    /* Clear pending write flag */
	    pending = 0;
	
	    switch(code)
	    {
	        case 0x00: /* VRAM */
	        case 0x01: /* VRAM */
	        case 0x02: /* VRAM */
	
	            /* Return read buffer contents */
	            temp = buffer;
	
	            /* Load read buffer */
/*TODO buggy??*/	            buffer = vram.read((addr & 0xFF));//buffer = vram[(addr & 0xFFFF)];
	            break;
	
	        case 0x03: /* CRAM (invalid) */
	            /* This should never happen; only known use is a
	               dummy read in the GG game 'NBA Action' */
	            break;
	    }
	
	    /* Bump internal address register */
	    addr += 1;
	    return (temp);
	}};
	
	
	public static ReadHandlerPtr  sms_vdp_ctrl_r= new ReadHandlerPtr() {
        public int handler(int offset) {
	    int temp = status;
	
	    pending = 0;
	
	    status &= ~(STATUS_VINT | STATUS_HINT | STATUS_SPRCOL);
	
	    if(irq_state == 1)
	    {
	        irq_state = 0;
	        SETIRQLINE(0,0,CLEAR_LINE);//z80_set_irq_line(0, CLEAR_LINE);
	    }
	
	    return (temp);
	}};
	
	
	public static WriteHandlerPtr sms_vdp_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	    pending = 0;
	
	    switch(code)
	    {
	        case 0x00:
	        case 0x01:
	        case 0x02:
	            {
	                int address = (addr & 0x3FFF);
	                int _index   = (addr & 0x3FFF) >> 5;
	
	                if(data != vram.read(address))
	                {
	                    vram.write(address, data);
	                    u8_vram_dirty[_index] = is_vram_dirty = 1;
	                }
	            }
	            break;
	
	        case 0x03:
	            {
	                int address = GameGear!=0 ? (addr & 0x3F) : (addr & 0x1F);
	                int _index   = GameGear!=0 ? ((addr & 0x3E) >> 1) : (addr & 0x1F);
	
	                if(data != u8_cram[address])
	                {
	                    u8_cram[address] = data&0xFF;
	                    u8_cram_dirty[_index] = is_cram_dirty = 1;
	                }
	            }
	            break;
	    }
	
	    addr += 1;
	}};
	
	
	
	
	public static WriteHandlerPtr sms_vdp_ctrl_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	    if(pending == 0)
	    {
	        pending = 1;
	        latch = data;
	    }
	    else
	    {
	        pending = 0;
	
	        if((data & 0xF0) == 0x80)
	        {
	            u8_reg[(data & 0x0F)] = latch&0xFF;
	            ntab = (u8_reg[2] << 10) & 0x3800;
	            satb = (u8_reg[5] <<  7) & 0x3F00;
	        }
	        else
	        {
	            code = (data >> 6) & 3;
	            addr = (data << 8 | latch);
	
	            if(code == 0x00)
	            {
	                buffer = vram.read((addr & 0x3FFF));
	                addr += 1;
	            }
	        }
	    }
	}};
	
	
	static void sms_refresh_line(osd_bitmap bitmap, int line)
	{
	    int i, x, color;
	    int charindex, palselect;
	    int sy, sx, sn, sl, width = 8, height = ((u8_reg[1] & 0x02)!=0 ? 16 : 8);
	    int v_line = (line + u8_reg[9]) % 224, v_row = v_line & 7;
	    UShortPtr nametable = new UShortPtr(vram,ntab+((v_line >> 3) << 6));
	    UBytePtr objtable = new UBytePtr(vram,satb);
	    int xscroll = (((u8_reg[0] & 0x40)!=0&&(line < 16)) ? 0 : u8_reg[8]);
	
	    /* Check if display is disabled */
	    if((u8_reg[1] & 0x40)==0)
	    {
	        memset(bitmap.line[line],0, Machine.pens[0x10 + u8_reg[7]], 0x100);
	        return;
	    }
	
	
	    for(i=0;i<32;i++)
	    {
	    	char tile = nametable.read(i);
		
	        charindex = (tile & 0x07FF);
	        palselect = (tile >> 7) & 0x10;
	
	        for(x=0;x<8;x++)
	        {
	            color = u8_cache[ (charindex << 6) + (v_row << 3) + (x)];
	            bitmap.line[line].write((xscroll+(i<<3)+x) & 0xFF,Machine.pens[color | palselect]);
	        }
	    }
	
	    for(i=0;(i<64)&&(objtable.read(i)!=208);i++);
	    for(i--;i>=0;i--)
	    {
	        sy = objtable.read(i)+1; /* sprite y position starts at line 1 */
	        if(sy>240) sy-=256; /* wrap from top if y position is > 240 */
	        if((line>=sy)&&(line<(sy+height)))
	        {
	            sx = objtable.read(0x80 + (i << 1));
	            if((u8_reg[0]&0x08)!=0) sx -= 8;   /* sprite shift */
	            sn = objtable.read(0x81 + (i << 1));
	            if((u8_reg[6]&0x04)!=0) sn += 256; /* pattern table select */
	            if((u8_reg[1]&0x02)!=0) sn &= 0x01FE; /* force even index */
	
	            sl = (line - sy);
	
	            for(x=0;x<width;x++)
	            {
	                color = u8_cache[(sn << 6)+(sl << 3) + (x)];
	                if (color != 0) bitmap.line[line].write((sx + x) & 0xFF, Machine.pens[0x10 | color]);
	            }
	        }
	    }
	
	    if((u8_reg[0] & 0x20)!=0)
	    {
	        memset(bitmap.line[line],0, Machine.pens[0x10 + u8_reg[7]], 8);
	    }
	}
	
	
	static void sms_update_palette()
	{
	    int i, r, g, b;
	
	    if(is_cram_dirty == 0) return;
	    is_cram_dirty = 0;
	
	    for(i = 0; i < 0x20; i += 1)
	    {
	        if(u8_cram_dirty[i] == 1)
	        {
	            u8_cram_dirty[i] = 0;
	
	            if(GameGear == 1)
	            {
	                r = ((u8_cram[i * 2 + 0] >> 0) & 0x0F) << 4;
	                g = ((u8_cram[i * 2 + 0] >> 4) & 0x0F) << 4;
	                b = ((u8_cram[i * 2 + 1] >> 0) & 0x0F) << 4;
	            }
	            else
	            {
	                r = ((u8_cram[i] >> 0) & 0x03) << 6;
	                g = ((u8_cram[i] >> 2) & 0x03) << 6;
	                b = ((u8_cram[i] >> 4) & 0x03) << 6;
	            }
	
	            palette_change_color(i, r, g, b);
	        }
	    }
	}
	
	
	static void sms_cache_tiles()
	{
	    int i, x, y, c;
	    int b0, b1, b2, b3;
	    int i0, i1, i2, i3;
	
	    if(is_vram_dirty == 0) return;
	    is_vram_dirty = 0;
	
	    for(i = 0; i < 0x200; i += 1)
	    {
	        if(u8_vram_dirty[i] == 1)
	        {
	            u8_vram_dirty[i] = 0;
	
	            for(y=0;y<8;y++)
	            {
	                b0 = vram.read((i << 5) + (y << 2) + 0);
	                b1 = vram.read((i << 5) + (y << 2) + 1);
	                b2 = vram.read((i << 5) + (y << 2) + 2);
	                b3 = vram.read((i << 5) + (y << 2) + 3);
	
	                for(x=0;x<8;x++)
	                {
	                    i0 = (b0 >> (7-x)) & 1;
	                    i1 = (b1 >> (7-x)) & 1;
	                    i2 = (b2 >> (7-x)) & 1;
	                    i3 = (b3 >> (7-x)) & 1;
	
	                    c = (i3 << 3 | i2 << 2 | i1 << 1 | i0);
	
	                    u8_cache[ (0 << 15) + (i << 6) + ((  y) << 3) + (  x) ] = c&0xFF;
	                    u8_cache[ (1 << 15) + (i << 6) + ((  y) << 3) + (7-x) ] = c&0xFF;
	                    u8_cache[ (2 << 15) + (i << 6) + ((7-y) << 3) + (  x) ] = c&0xFF;
	                    u8_cache[ (3 << 15) + (i << 6) + ((7-y) << 3) + (7-x) ] = c&0xFF;
	                }
	            }
	        }
	    }
	}
	
	public static VhUpdatePtr sms_vdp_refresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
	    sms_update_palette();
		palette_recalc();
	    copybitmap (bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
	}};
	
	
}
