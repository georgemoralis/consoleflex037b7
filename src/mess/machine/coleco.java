/*
 * ported to v0.37b6
 * using automatic conversion tool v0.01
 */
package mess.machine;

import static WIP.arcadeflex.libc_v2.*;
import static WIP.arcadeflex.fucPtr.*;
import static consoleflex.funcPtr.*;
import static old.mame.inptport.*;
import static mess.vidhrdw.tms9928a.*;

public class coleco {

    /* local */
    public static UBytePtr coleco_ram = new UBytePtr();
    public static UBytePtr coleco_cartridge_rom;

    static int JoyMode = 0;

    //static UBytePtr ROM;
    public static io_idPtr coleco_id_rom = new io_idPtr() {
        public int handler(int id) {
            throw new UnsupportedOperationException("unimplemented");
            /*TODO*///        FILE * romfile;
/*TODO*///         unsigned char magic[2];
/*TODO*/// 		int retval = ID_FAILED;
/*TODO*/// 
/*TODO*///         logerror("---------coleco_id_rom-----\n");
/*TODO*///         logerror("Gamename is %s\n", device_filename(IO_CARTSLOT, id));
/*TODO*///         logerror("filetype is %d\n", OSD_FILETYPE_IMAGE_R);
/*TODO*/// 
/*TODO*///         /* If no file was specified, don't bother */
/*TODO*///         if (!device_filename(IO_CARTSLOT, id) || !strlen(device_filename(IO_CARTSLOT, id))) {
/*TODO*///             return ID_OK;
/*TODO*///         }
/*TODO*/// 
/*TODO*///         if (!(romfile = image_fopen(IO_CARTSLOT, id, OSD_FILETYPE_IMAGE_R, 0))) {
/*TODO*///             return ID_FAILED;
/*TODO*///         }
/*TODO*///
/*TODO*///         retval = 0;
            /* Verify the file is in Colecovision format */
 /*TODO*///         osd_fread(romfile, magic, 2);
/*TODO*///         if ((magic[0] == 0xAA) && (magic[1] == 0x55)) {
/*TODO*///             retval = ID_OK;
/*TODO*///         }
/*TODO*///         if ((magic[0] == 0x55) && (magic[1] == 0xAA)) {
/*TODO*///             retval = ID_OK;
/*TODO*///         }

            /*TODO*///         osd_fclose(romfile);
/*TODO*///         return retval;
        }
    };

    public static io_initPtr coleco_load_rom = new io_initPtr() {
        public int handler(int id) {
            throw new UnsupportedOperationException("unimplemented");
            /*TODO*///         FILE * cartfile;
/*TODO*/// 
/*TODO*///         UINT8 * ROM = memory_region(REGION_CPU1);
/*TODO*/// 
/*TODO*///         logerror("---------coleco_load_rom-----\n");
/*TODO*///         logerror("filetype is %d  \n", OSD_FILETYPE_IMAGE_R);
/*TODO*///         logerror("Machine.game.name is %s  \n", Machine.gamedrv.name);
/*TODO*///         logerror("romname[0] is %s  \n", device_filename(IO_CARTSLOT, id));

            /* A cartridge isn't strictly mandatory, but it's recommended */
 /*TODO*///         cartfile = NULL;
/*TODO*///         if (!device_filename(IO_CARTSLOT, id) || !strlen(device_filename(IO_CARTSLOT, id))) {
/*TODO*///             logerror("Coleco - warning: no cartridge specified!\n");
/*TODO*///         } else if (!(cartfile = image_fopen(IO_CARTSLOT, id, OSD_FILETYPE_IMAGE_R, 0))) {
/*TODO*///             logerror("Coleco - Unable to locate cartridge: %s\n", device_filename(IO_CARTSLOT, id));
/*TODO*///             return 1;
/*TODO*///         }

            /*TODO*///         coleco_cartridge_rom =  & (ROM[0x8000]);

            /*TODO*///         if (cartfile != NULL) {
/*TODO*///             osd_fread(cartfile, coleco_cartridge_rom, 0x8000);
/*TODO*///             osd_fclose(cartfile);
/*TODO*///         }

            /*TODO*///         return 0;
        }
    };

    public static ReadHandlerPtr coleco_ram_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return coleco_ram.read(offset);
        }
    };

    public static WriteHandlerPtr coleco_ram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            coleco_ram.write(offset, data);
        }
    };

    public static ReadHandlerPtr coleco_paddle_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* Player 1 */
            if ((offset & 0x02) == 0) {
                /* Keypad and fire 1 */
                if (JoyMode == 0) {
                    int inport0, inport1, data;

                    inport0 = input_port_0_r.handler(0);
                    inport1 = input_port_1_r.handler(0);

                    if ((inport0 & 0x01) == 0) /* 0 */ {
                        data = 0x0A;
                    } else if ((inport0 & 0x02) == 0) /* 1 */ {
                        data = 0x0D;
                    } else if ((inport0 & 0x04) == 0) /* 2 */ {
                        data = 0x07;
                    } else if ((inport0 & 0x08) == 0) /* 3 */ {
                        data = 0x0C;
                    } else if ((inport0 & 0x10) == 0) /* 4 */ {
                        data = 0x02;
                    } else if ((inport0 & 0x20) == 0) /* 5 */ {
                        data = 0x03;
                    } else if ((inport0 & 0x40) == 0) /* 6 */ {
                        data = 0x0E;
                    } else if ((inport0 & 0x80) == 0) /* 7 */ {
                        data = 0x05;
                    } else if ((inport1 & 0x01) == 0) /* 8 */ {
                        data = 0x01;
                    } else if ((inport1 & 0x02) == 0) /* 9 */ {
                        data = 0x0B;
                    } else if ((inport1 & 0x04) == 0) /* # */ {
                        data = 0x06;
                    } else if ((inport1 & 0x08) == 0) /* . */ {
                        data = 0x09;
                    } else {
                        data = 0x0F;
                    }

                    return (inport1 & 0xF0) | (data);

                } /* Joystick and fire 2*/ else {
                    return input_port_2_r.handler(0);
                }
            } /* Player 2 */ else {
                /* Keypad and fire 1 */
                if (JoyMode == 0) {
                    int inport3, inport4, data;

                    inport3 = input_port_3_r.handler(0);
                    inport4 = input_port_4_r.handler(0);

                    if ((inport3 & 0x01) == 0) /* 0 */ {
                        data = 0x0A;
                    } else if ((inport3 & 0x02) == 0) /* 1 */ {
                        data = 0x0D;
                    } else if ((inport3 & 0x04) == 0) /* 2 */ {
                        data = 0x07;
                    } else if ((inport3 & 0x08) == 0) /* 3 */ {
                        data = 0x0C;
                    } else if ((inport3 & 0x10) == 0) /* 4 */ {
                        data = 0x02;
                    } else if ((inport3 & 0x20) == 0) /* 5 */ {
                        data = 0x03;
                    } else if ((inport3 & 0x40) == 0) /* 6 */ {
                        data = 0x0E;
                    } else if ((inport3 & 0x80) == 0) /* 7 */ {
                        data = 0x05;
                    } else if ((inport4 & 0x01) == 0) /* 8 */ {
                        data = 0x01;
                    } else if ((inport4 & 0x02) == 0) /* 9 */ {
                        data = 0x0B;
                    } else if ((inport4 & 0x04) == 0) /* # */ {
                        data = 0x06;
                    } else if ((inport4 & 0x08) == 0) /* . */ {
                        data = 0x09;
                    } else {
                        data = 0x0F;
                    }

                    return (inport4 & 0xF0) | (data);

                } /* Joystick and fire 2*/ else {
                    return input_port_5_r.handler(0);
                }
            }

            //return 0x00;
        }
    };

    public static WriteHandlerPtr coleco_paddle_toggle_1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            JoyMode = 0;
        }
    };

    public static WriteHandlerPtr coleco_paddle_toggle_2_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            JoyMode = 1;
        }
    };

    public static ReadHandlerPtr coleco_VDP_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            if ((offset & 0x01) != 0) {
                return TMS9928A_register_r();
            } else {
                return TMS9928A_vram_r();
            }
        }
    };

    public static WriteHandlerPtr coleco_VDP_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if ((offset & 0x01) != 0) {
                TMS9928A_register_w(data);
            } else {
                TMS9928A_vram_w(data);
            }

        }
    };

}
