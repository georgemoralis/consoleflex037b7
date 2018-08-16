/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mess.includes;

import static mess.vidhrdw.crtc6845.*;

public class crtc6845H {
    	public static CRTC6845 crtc6845;
	// static instance used by standard handlers, use it to call functions
	
        public static class CRTC6845_CURSOR {
		public int on;
		public int pos;
		public int top;
		public int bottom;
	} ;

	public static abstract class CRTC6845_CONFIG {
		public int freq;
		public abstract void cursor_changed(CRTC6845_CURSOR old);
	} ;

	/*TODO*////void crtc6845_init(struct _CRTC6845 *crtc, CRTC6845_CONFIG *config);
	/*TODO*////void crtc6845_set_clock(struct _CRTC6845 *crtc, int freq);

	// to be called before drawing screen
	/*TODO*////void crtc6845_time(struct _CRTC6845 *crtc);

	/*TODO*////int crtc6845_do_full_refresh(struct _CRTC6845 *crtc);

	/*TODO*////int crtc6845_get_char_columns(struct _CRTC6845 *crtc);
	/*TODO*////int crtc6845_get_char_height(struct _CRTC6845 *crtc);
	/*TODO*////int crtc6845_get_char_lines(struct _CRTC6845 *crtc);
	/*TODO*////int crtc6845_get_start(struct _CRTC6845 *crtc);

	/* cursor off, cursor on, cursor 16 frames on/off, cursor 32 frames on/off 
	   start line, end line */
	/*TODO*////void crtc6845_get_cursor(struct _CRTC6845 *crtc, CRTC6845_CURSOR *cursor);

	/*TODO*////data8_t crtc6845_port_r(struct _CRTC6845 *crtc, int offset);
	/*TODO*////void crtc6845_port_w(struct _CRTC6845 *crtc, int offset, data8_t data);

	// functions for 
	// querying more videodata 
	// set lightpen position
	// ...
	// later
	


/* to be called when writting to port */
	/*TODO*////extern WRITE_HANDLER ( crtc6845_0_port_w );

/* to be called when reading from port */
	/*TODO*////extern READ_HANDLER ( crtc6845_0_port_r );
	
	// for displaying of debug info
	/*TODO*////void crtc6845_state(void);


        /* use these only in emulations of 6845 variants */

        public static int CRTC6845_COLUMNS=(REG(0)+1);
        public static int CRTC6845_CHAR_COLUMNS=(REG(1));
        //#define COLUMNS_SYNC_POS (REG(2))
        //#define COLUMNS_SYNC_SIZE ((REG(3)&0xf)-1)
        //#define LINES_SYNC_SIZE (vdc.reg[3]>>4)
        public static final int CRTC6845_CHAR_HEIGHT=((REG(9)&0x1f)+1);
        public static int CRTC6845_LINES=(REG(4)*CRTC6845_CHAR_HEIGHT+REG(5));
        public static int CRTC6845_CHAR_LINES=REG(6);
        //#define LINES_SYNC_POS (vdc.reg[7])
        
        public static int CRTC6845_VIDEO_START=((REG(0xc)<<8)|REG(0xd));

        public static int CRTC6845_INTERLACE_MODE=(REG(8)&3);
        public static final int CRTC6845_INTERLACE_SIGNAL = 1;
        public static final int CRTC6845_INTERLACE = 3;

        public static int CRTC6845_CURSOR_MODE=(REG(0xa)&0x60);
        public static final int CRTC6845_CURSOR_OFF = 0x20;
        public static final int CRTC6845_CURSOR_16FRAMES = 0x40;
        public static final int CRTC6845_CURSOR_32FRAMES = 0x60;

        public static int CRTC6845_CURSOR_POS=((REG(0xe)<<8)|REG(0xf));

        public static int CRTC6845_CURSOR_TOP=(REG(0xa)&0x1f);
        public static int CRTC6845_CURSOR_BOTTOM=REG(0xb);
}
