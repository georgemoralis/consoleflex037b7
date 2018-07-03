/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
 /*
 *  Ported to 0.37b7
 */
package WIP.mame;

import static WIP.arcadeflex.libc_v2.UBytePtr;

public class osdependH {

    /**
     * ****************************************************************************
     * Display
     *****************************************************************************
     */
    public static class osd_bitmap {

        public osd_bitmap() {
        }

        public int width, height;
        /* width and hegiht of the bitmap */
        public int depth;
        /* bits per pixel*/
        public Object _private;
        /* don't touch! - reserved for osdepend use */
        public UBytePtr[] line;
        /* pointers to the start of each line */
        public int ptrPos = 0;
        /* maybe this is a way to implement that (nick) */
    }

    /**
     * ****************************************************************************
     * Joystick & Mouse/Trackball
     *****************************************************************************
     */

    /* We support 4 players for each analog control */
    public static final int OSD_MAX_JOY_ANALOG = 4;
    public static final int X_AXIS = 1;
    public static final int Y_AXIS = 2;

    /**
     * ****************************************************************************
     * File I/O
     *****************************************************************************
     */

    /* inp header */
 /*TODO*/ //        typedef struct {
    /*TODO*/ //            char name[9];      /* 8 bytes for game->name + NULL */
    /*TODO*/ //            char version[3];   /* byte[0] = 0, byte[1] = version byte[2] = beta_version */
    /*TODO*/ //            char reserved[20]; /* for future use, possible store game options? */
    /*TODO*/ //       } INP_HEADER;
    /* file handling routines */
    public static final int OSD_FILETYPE_ROM = 1;
    public static final int OSD_FILETYPE_SAMPLE = 2;
    public static final int OSD_FILETYPE_NVRAM = 3;
    public static final int OSD_FILETYPE_HIGHSCORE = 4;
    public static final int OSD_FILETYPE_HIGHSCORE_DB = 5;
    public static final int OSD_FILETYPE_CONFIG = 6;
    public static final int OSD_FILETYPE_INPUTLOG = 7;
    public static final int OSD_FILETYPE_STATE = 8;
    public static final int OSD_FILETYPE_ARTWORK = 9;
    public static final int OSD_FILETYPE_MEMCARD = 10;
    public static final int OSD_FILETYPE_SCREENSHOT = 11;
    public static final int OSD_FILETYPE_HISTORY = 12;
    /* LBO 040400 */
    public static final int OSD_FILETYPE_CHEAT = 13;
    /* LBO 040400 */
    public static final int OSD_FILETYPE_LANGUAGE = 14;
    /* LBO 042400 */
    public static final int OSD_FILETYPE_IMAGE_R=15;
    public static final int OSD_FILETYPE_IMAGE_RW=16;
    public static final int OSD_FILETYPE_end = 17;
    /* dummy last entry */

}
