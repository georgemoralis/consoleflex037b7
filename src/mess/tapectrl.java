/** *
 * Ported to mess 0.37b6
 */
package mess;

import static WIP.arcadeflex.libc_v2.sprintf;
import static WIP.mame.osdependH.*;
import static WIP.mame.mame.*;
import static mame.usrintrf.*;
import static mess.mess.*;
import static mess.messH.*;
import static old.mame.inptportH.*;
import static old.mame.input.*;
import static old.arcadeflex.libc_old.*;

public class tapectrl {

    static int id = 0;

    public static int tapecontrol(osd_bitmap bitmap, int selected) {
        String timepos = "";
        int t0, t1;
        String[] menu_item = new String[40];
        String[] menu_subitem = new String[40];
        char[] flag = new char[40];

        int sel;
        int total;
        int arrowize;
        int status;

        total = 0;
        sel = selected - 1;

        menu_item[total] = device_typename_id(IO_CASSETTE, id);
        menu_subitem[total] = device_filename(IO_CASSETTE, id) != null ? device_filename(IO_CASSETTE, id) : "---";
        flag[total] = 0;
        total++;

        t0 = device_tell(IO_CASSETTE, id);
        /* Using the following trick because device_length() is the file length,
	 * and might not be valid */
        t1 = device_seek(IO_CASSETTE, id, 0, SEEK_END);
        device_seek(IO_CASSETTE, id, t0, SEEK_SET);

        if (t1 != 0) {
            timepos = sprintf("%3d%%", t0 * 100 / t1);
        } else {
            timepos = sprintf("%3d%%", 0);
        }
        menu_item[total] = (device_status(IO_CASSETTE, id, -1) & 1) != 0 ? "playing" : "stopped";
        menu_subitem[total] = timepos;
        flag[total] = 0;
        total++;

        menu_item[total] = "Pause/Stop";
        menu_subitem[total] = null;
        flag[total] = 0;
        total++;

        menu_item[total] = "Play";
        menu_subitem[total] = null;
        flag[total] = 0;
        total++;

        menu_item[total] = "Rewind";
        menu_subitem[total] = null;
        flag[total] = 0;
        total++;

        menu_item[total] = "Fast forward";
        menu_subitem[total] = null;
        flag[total] = 0;
        total++;

        menu_item[total] = "Return to Main Menu";
        menu_subitem[total] = null;
        flag[total] = 0;
        total++;

        menu_item[total] = null;
        /* terminate array */
        menu_subitem[total] = null;
        flag[total] = 0;

        arrowize = 0;
        if (sel < total - 1) {
            arrowize = 2;
        }

        if (sel > 255) /* are we waiting for a new key? */ {
            /* display the menu */
            ui_displaymenu(bitmap, menu_item, menu_subitem, flag, sel & 0xff, 3);
            return sel + 1;
        }

        ui_displaymenu(bitmap, menu_item, menu_subitem, flag, sel, arrowize);

        if (input_ui_pressed_repeat(IPT_UI_DOWN, 8) != 0) {
            if (sel < total - 1) {
                sel++;
            } else {
                sel = 0;
            }
        }

        if (input_ui_pressed_repeat(IPT_UI_UP, 8) != 0) {
            if (sel > 0) {
                sel--;
            } else {
                sel = total - 1;
            }
        }

        if (input_ui_pressed(IPT_UI_LEFT) != 0) {
            switch (sel) {
                case 0:
                    id = --id % device_count(IO_CASSETTE);
                    break;
            }
            /* tell updatescreen() to clean after us (in case the window changes size) */
            need_to_clear_bitmap = 1;
        }

        if (input_ui_pressed(IPT_UI_RIGHT) != 0) {
            switch (sel) {
                case 0:
                    id = ++id % device_count(IO_CASSETTE);
                    break;
            }
            /* tell updatescreen() to clean after us (in case the window changes size) */
            need_to_clear_bitmap = 1;
        }

        if (input_ui_pressed(IPT_UI_SELECT) != 0) {
            if (sel == total - 1) {
                sel = -1;
            } else {
                status = device_status(IO_CASSETTE, id, -1);
                switch (sel) {
                    case 0:
                        id = ++id % device_count(IO_CASSETTE);
                        break;
                    case 2:
                        if ((status & 1) == 0) {
                            device_seek(IO_CASSETTE, id, 0, SEEK_SET);
                        }
                        device_status(IO_CASSETTE, id, status & ~1);
                        break;
                    case 3:
                        device_status(IO_CASSETTE, id, status | 1);
                        break;
                    case 4:
                        device_seek(IO_CASSETTE, id, -11025, SEEK_CUR);
                        break;
                    case 5:
                        device_seek(IO_CASSETTE, id, +11025, SEEK_CUR);
                        break;
                }
                /* tell updatescreen() to clean after us (in case the window changes size) */
                need_to_clear_bitmap = 1;
            }
        }

        if (input_ui_pressed(IPT_UI_CANCEL) != 0) {
            sel = -1;
        }

        if (input_ui_pressed(IPT_UI_CONFIGURE) != 0) {
            sel = -2;
        }

        if (sel == -1 || sel == -2) {
            /* tell updatescreen() to clean after us */
            need_to_clear_bitmap = 1;
        }

        return sel + 1;
    }

}
