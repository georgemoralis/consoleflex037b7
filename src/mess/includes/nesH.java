/*
 * ported to v0.37b6
 *
 */
package mess.includes;

import WIP.arcadeflex.libc_v2.UBytePtr;

public class nesH {

    /* Uncomment this to see all 4 ppu vram pages at once */
////#define BIG_SCREEN

    /* Uncomment this to do away with any form of mirroring */
////#define NO_MIRRORING
//#define NEW_SPRITE_HIT
    public static final int BOTTOM_VISIBLE_SCANLINE = 239;
    /* The bottommost visible scanline */
    public static final int NMI_SCANLINE = 244;
    /* 244 times Bayou Billy perfectly */
    public static final int NTSC_SCANLINES_PER_FRAME = 262;
    public static final int PAL_SCANLINES_PER_FRAME = 305;
    /* verify - times Elite perfectly */

    public static final int PPU_c0_inc = 0x04;
    public static final int PPU_c0_spr_select = 0x08;
    public static final int PPU_c0_chr_select = 0x10;
    public static final int PPU_c0_sprite_size = 0x20;
    public static final int PPU_c0_NMI = 0x80;

    public static final int PPU_c1_background_L8 = 0x02;
    public static final int PPU_c1_sprites_L8 = 0x04;
    public static final int PPU_c1_background = 0x08;
    public static final int PPU_c1_sprites = 0x10;

    public static final int PPU_status_8sprites = 0x20;
    public static final int PPU_status_sprite0_hit = 0x40;
    public static final int PPU_status_vblank = 0x80;


    /*TODO*///struct ppu_struct {
/*TODO*///	UINT8 control_0;		/* $2000 */
/*TODO*///	UINT8 control_1;		/* $2001 */
/*TODO*///	UINT8 status;			/* $2002 */
/*TODO*///	UINT16 sprite_address;	/* $2003 */

    /*TODO*///	UINT16 refresh_data;	/* $2005 */
/*TODO*///	UINT16 refresh_latch;
/*TODO*///	UINT8 x_fine;
/*TODO*///
/*TODO*///	UINT16 address;			/* $2006 */
/*TODO*///	UINT8 address_latch;
/*TODO*///
/*TODO*///	UINT8 data_latch;		/* $2007 - read */
/*TODO*///
/*TODO*///	UINT16 current_scanline;
/*TODO*///	UINT8 *page[4];
/*TODO*///	UINT16 scanlines_per_frame;
/*TODO*///};
    public static class nes_struct {

        /* load-time cart variables which remain constant */
        public int u8_trainer;
        public int u8_battery;
        public char[] prg_chunks = new char[1];
        public char[] chr_chunks = new char[1];

        /* system variables which don't change at run-time */
        public char mapper;
        public int u8_four_screen_vram;
        public int u8_hard_mirroring;
        public int u8_slow_banking;
        public UBytePtr rom;
        public UBytePtr vrom;
        public UBytePtr vram;
        public UBytePtr wram;

        /* Variables which can change */
        public int u8_mid_ram_enable;
    };

    /*TODO*///extern struct nes_struct nes;

    /*TODO*///struct fds_struct {
/*TODO*///	UINT8 *data;
/*TODO*///	UINT8 sides;
/*TODO*///
/*TODO*///	/* Variables which can change */
/*TODO*///	UINT8 motor_on;
/*TODO*///	UINT8 door_closed;
/*TODO*///	UINT8 current_side;
/*TODO*///	UINT32 head_position;
/*TODO*///	UINT8 status0;
/*TODO*///	UINT8 read_mode;
/*TODO*///	UINT8 write_reg;
/*TODO*///};

    /*TODO*///extern struct fds_struct nes_fds;
}
