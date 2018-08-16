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

import static old.mame.timer.*;

import static mess.includes.crtc6845H.*;
import static mess.vidhrdw.m6845.*;

public class crtc6845
{
    public static boolean VERBOSE = false;

    /*TODO*////#if VERBOSE
    /*TODO*////#define DBG_LOG(N,M,A)      \
    /*TODO*////    if(VERBOSE>=N){ if( M )logerror("%11.6f: %-24s",timer_get_time(),(char*)M ); logerror A; }
    /*TODO*////#else
    /*TODO*////#define DBG_LOG(N,M,A)
    /*TODO*////#endif

    public static class CRTC6845 {
            CRTC6845_CONFIG config;
            int[] reg= new int[18];
            int index;
            int lightpen_pos;
            int changed;
            double cursor_time;
            int cursor_on;
    };

    public static CRTC6845 crtc6845_static;
    public static CRTC6845 crtc6845=crtc6845_static;

    void crtc6845_init (CRTC6845 crtc, CRTC6845_CONFIG config)
    {
            //memset(crtc, 0, sizeof(*crtc));
        
            crtc.cursor_time=timer_get_time();
            crtc.config=config;           
            
    }

    /*TODO*////static const struct { 
    /*TODO*////        int stored, 
    /*TODO*////                read;
    /*TODO*////} reg_mask[]= { 
    public static int[][] reg_mask = {
            { 0xff, 0 },
            { 0xff, 0 },
            { 0xff, 0 },
            { 0xff, 0 },
            { 0x7f, 0 },
            { 0x1f, 0 },
            { 0x7f, 0 },
            { 0x7f, 0 },
            {  0x3f, 0 },
            {  0x1f, 0 },
            {  0x7f, 0 },
            {  0x1f, 0 },
            {  0x3f, 0x3f },
            {  0xff, 0xff },
            {  0x3f, 0x3f },
            {  0xff, 0xff },
            {  -1, 0x3f },
            {  -1, 0xff },
    };
    
    public static int REG(int x) {
            return (crtc.registers[x]&reg_mask[x][0]);
    }
    
    static int crtc6845_clocks_in_frame(CRTC6845 crtc)
    {
            int clocks=CRTC6845_COLUMNS*CRTC6845_LINES;
            switch (CRTC6845_INTERLACE_MODE) {
            case CRTC6845_INTERLACE_SIGNAL: // interlace generation of video signals only
            case CRTC6845_INTERLACE: // interlace
                    return clocks/2;
            default:
                    return clocks;
            }
    }

    void crtc6845_set_clock(CRTC6845 crtc, int freq)
    {
            crtc.config.freq=freq;
            crtc.changed=1;
    }

    int crtc6845_do_full_refresh(CRTC6845 crtc) 
    {
            int t=crtc.changed;
            crtc.changed=0;
            return t;
    }

    void crtc6845_time(CRTC6845 crtc)
    {
            double neu, ftime;
            CRTC6845_CURSOR cursor=new CRTC6845_CURSOR();

            neu=timer_get_time();

            if (crtc6845_clocks_in_frame(crtc)==0.0) return;
            ftime=crtc6845_clocks_in_frame(crtc)*16.0/crtc6845.config.freq;
            if ( CRTC6845_CURSOR_MODE==CRTC6845_CURSOR_32FRAMES) ftime*=2;
            if (neu-crtc.cursor_time>ftime) {
                    crtc.cursor_time+=ftime;
                    crtc6845_get_cursor(crtc, cursor);
                    //if (crtc.config.cursor_changed != 0) 
                        crtc.config.cursor_changed(cursor);
                    crtc.cursor_on^=1;
            }
    }

    int crtc6845_get_char_columns(CRTC6845 crtc) 
    { 
            return CRTC6845_CHAR_COLUMNS;
    }

    int crtc6845_get_char_height(CRTC6845 crtc) 
    {
            return CRTC6845_CHAR_HEIGHT;
    }

    int crtc6845_get_char_lines(CRTC6845 crtc) 
    { 
            return CRTC6845_CHAR_LINES;
    }

    int crtc6845_get_start(CRTC6845 crtc) 
    {
            return CRTC6845_VIDEO_START;
    }

    public static void crtc6845_get_cursor(CRTC6845 crtc, CRTC6845_CURSOR cursor)
    {
            cursor.pos=CRTC6845_CURSOR_POS;
            switch (CRTC6845_CURSOR_MODE) {
            default: cursor.on=1;break;
            case CRTC6845_CURSOR_OFF: cursor.on=0;break;
            case CRTC6845_CURSOR_16FRAMES:
                    cursor.on=crtc.cursor_on;
            case CRTC6845_CURSOR_32FRAMES:
                    cursor.on=crtc.cursor_on;
                    break;
            }
            cursor.top=CRTC6845_CURSOR_TOP;
            cursor.bottom=CRTC6845_CURSOR_BOTTOM;
    }

    public static void crtc6845_port_w(CRTC6845 crtc, int offset, int data)
    {
            CRTC6845_CURSOR cursor=new CRTC6845_CURSOR();
            
            if ((offset & 1) != 0)
            {
                    if ((crtc.index & 0x1f) < 18) {
                            switch (crtc.index & 0x1f) {
                            case 0xa:case 0xb:
                            case 0xe:case 0xf:
                                    crtc6845_get_cursor(crtc, cursor);
                                    crtc.reg[crtc.index]=data;
                                    //if (crtc.config.cursor_changed != 0) 
                                        crtc.config.cursor_changed(cursor);
                                    break;
                            default:
                                    crtc.changed=1;
                                    crtc.reg[crtc.index]=data;
                            }
                                    //DBG_LOG (2, "crtc_port_w", ("%.2x:%.2x\n", crtc->index, data));
                    } else { 
                            //DBG_LOG (1, "crtc6845_port_w", ("%.2x:%.2x\n", crtc->index, data));
                    }
            }
            else
            {
                    crtc.index = data;
            }
    }

    public static int crtc6845_port_r(CRTC6845 crtc, int offset)
    {
            int val;

            val = 0xff;
            if ((offset & 1) != 0)
            {
                    if ((crtc.index & 0x1f) < 18)
                    {
                            switch (crtc.index & 0x1f)
                            {
                            case 0x10: val=crtc.lightpen_pos>>8;break;
                            case 0x11: val=crtc.lightpen_pos&0xff;break;
                                    
                            default:
                                    val=crtc.reg[crtc.index&0x1f]&reg_mask[crtc.index&0x1f][1];
                            }
                    }
                    //DBG_LOG (1, "crtc6845_port_r", ("%.2x:%.2x\n", crtc->index, val));
            }
            else
            {
                    val = crtc.index;
            }
            return val;
    }

    void crtc6845_state ()
    {
            String text="";

            /*TODO*////snprintf (text, sizeof (text), "crtc6845 %.2x %.2x %.2x %.2x",
            /*TODO*////                  crtc6845->reg[0xc], crtc6845->reg[0xd], crtc6845->reg[0xe],crtc6845->reg[0xf]);

            /*TODO*////state_display_text(text);
    }

    //WRITE_HANDLER ( crtc6845_0_port_w ) { 
    public static WriteHandlerPtr crtc6845_0_port_w = new WriteHandlerPtr() {
            public void handler(int offset, int data){
                crtc6845_port_w(crtc6845, offset, data); 
    }};
    
    //READ_HANDLER ( crtc6845_0_port_r ) { 
    public static ReadHandlerPtr crtc6845_0_port_r = new ReadHandlerPtr() {
            public int handler(int offset) {
                return crtc6845_port_r(crtc6845, offset); 
    }};

}
