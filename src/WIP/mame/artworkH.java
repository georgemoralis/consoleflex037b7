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
package WIP.mame;

public class artworkH {

    public static class artwork_info {
        /*TODO*///	/* Publically accessible */
/*TODO*///	struct osd_bitmap *artwork;
/*TODO*///	struct osd_bitmap *artwork1;
/*TODO*///	struct osd_bitmap *alpha;
/*TODO*///	struct osd_bitmap *orig_artwork;   /* needed for palette recalcs */
/*TODO*///	UINT8 *orig_palette;               /* needed for restoring the colors after special effects? */
/*TODO*///	int num_pens_used;
/*TODO*///	UINT8 *transparency;
/*TODO*///	int num_pens_trans;
/*TODO*///	int start_pen;
/*TODO*///	UINT8 *brightness;                 /* brightness of each palette entry */
/*TODO*///	UINT64 *rgb;
/*TODO*///	UINT8 *pTable;                     /* Conversion table usually used for mixing colors */
    }
    /*TODO*///
/*TODO*///
/*TODO*///struct artwork_element
/*TODO*///{
/*TODO*///	struct rectangle box;
/*TODO*///	UINT8 red,green,blue;
/*TODO*///	UINT16 alpha;   /* 0x00-0xff or OVERLAY_DEFAULT_OPACITY */
/*TODO*///};
/*TODO*///
/*TODO*///struct artwork_size_info
/*TODO*///{
/*TODO*///	int width, height;         /* widht and height of the artwork */
/*TODO*///	struct rectangle screen;   /* location of the screen relative to the artwork */
/*TODO*///};
/*TODO*///
/*TODO*///#define OVERLAY_DEFAULT_OPACITY         0xffff
/*TODO*///
/*TODO*///  
}
