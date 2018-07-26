/**
 * ported to 0.37b5
 */
package old.mame;

import static arcadeflex.libc.cstring.*;

import static mess.messH.*;
import static mess.system.drivers;
import static old.arcadeflex.libc_old.sprintf;
import static WIP.arcadeflex.libc_v2.UShortArray;
import static WIP.arcadeflex.libc_v2.UBytePtr;
import static old.arcadeflex.sound.*;
import static old.arcadeflex.video.*;
import static old.arcadeflex.video.osd_clearbitmap;
import static WIP.mame.mame.update_video_and_audio;
import static old.mame.common.*;
import static mame.commonH.COIN_COUNTERS;
import static old.mame.cpuintrf.cputype_name;
import static old.mame.cpuintrf.machine_reset;
import static old.mame.drawgfx.*;
import static old.mame.drawgfx.drawgfx;
import static old.mame.drawgfxH.*;
import static old.mame.driverH.*;
import static old.mame.inptport.*;
import static old.mame.inptportH.*;
import static old.mame.input.*;
import static old.mame.inputH.*;
import static WIP.mame.mame.*;
import static mame.mameH.MAX_GFX_ELEMENTS;
import static WIP.mame.osdependH.osd_bitmap;
import static WIP.mame.sndintrf.*;
import static mame.ui_text.ui_getstring;
import static mame.ui_textH.*;
import static old.mame.usrintrfH.*;
import static WIP.mame.version.build_version;
import static mame.cheat.DoCheat;
import static mame.cheat.cheat_menu;
import static mame.datafile.load_driver_history;
import static mame.usrintrf.*;


public class usrintrf {
    



    public static void showcharset(osd_bitmap bitmap) {
        int i;
        String buf = "";
        int bank, color, firstdrawn;
        int palpage;
        int changed;
        int game_is_neogeo = 0;
/*TODO*///	unsigned char *orig_used_colors=0;
/*TODO*///
/*TODO*///
/*TODO*///	if (palette_used_colors)
/*TODO*///	{
/*TODO*///		orig_used_colors = malloc(Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		if (!orig_used_colors) return;
/*TODO*///
/*TODO*///		memcpy(orig_used_colors,palette_used_colors,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->gamedrv->clone_of == &driver_neogeo ||
/*TODO*///			(Machine->gamedrv->clone_of &&
/*TODO*///				Machine->gamedrv->clone_of->clone_of == &driver_neogeo))
/*TODO*///		game_is_neogeo=1;
/*TODO*///
        bank = -1;
        color = 0;
        firstdrawn = 0;
        palpage = 0;

        changed = 1;

        do {
            int cpx, cpy, skip_chars;

            if (bank >= 0) {
                cpx = Machine.uiwidth / Machine.gfx[bank].width;
                cpy = (Machine.uiheight - Machine.uifontheight) / Machine.gfx[bank].height;
                skip_chars = cpx * cpy;
            } else cpx = cpy = skip_chars = 0;

            if (changed != 0) {
                int lastdrawn = 0;

                osd_clearbitmap(bitmap);

			/* validity chack after char bank change */
                if (bank >= 0) {
                    if (firstdrawn >= Machine.gfx[bank].total_elements) {
                        firstdrawn = Machine.gfx[bank].total_elements - skip_chars;
                        if (firstdrawn < 0) firstdrawn = 0;
                    }
                }

                if (bank != 2 || game_is_neogeo == 0) {
                    switch_ui_orientation();

                    if (bank >= 0) {
                        int table_offs;
                        int flipx, flipy;

/*TODO*///					if (palette_used_colors)
/*TODO*///					{
/*TODO*///						memset(palette_used_colors,PALETTE_COLOR_TRANSPARENT,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///						table_offs = Machine->gfx[bank]->colortable - Machine->remapped_colortable
/*TODO*///								+ Machine->gfx[bank]->color_granularity * color;
/*TODO*///						for (i = 0;i < Machine->gfx[bank]->color_granularity;i++)
/*TODO*///							palette_used_colors[Machine->game_colortable[table_offs + i]] = PALETTE_COLOR_USED;
/*TODO*///						palette_recalc();	/* do it twice in case of previous overflow */
/*TODO*///						palette_recalc();	/*(we redraw the screen only when it changes) */
/*TODO*///					}
                        flipx = (Machine.orientation ^ trueorientation) & ORIENTATION_FLIP_X;
                        flipy = (Machine.orientation ^ trueorientation) & ORIENTATION_FLIP_Y;

                        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
                            int t;
                            t = flipx;
                            flipx = flipy;
                            flipy = t;
                        }


                        for (i = 0; i + firstdrawn < Machine.gfx[bank].total_elements && i < cpx * cpy; i++) {
                            drawgfx(bitmap, Machine.gfx[bank],
                                    i + firstdrawn, color,  /*sprite num, color*/
                                    flipx, flipy,
                                    (i % cpx) * Machine.gfx[bank].width + Machine.uixmin,
                                    Machine.uifontheight + (i / cpx) * Machine.gfx[bank].height + Machine.uiymin,
                                    null, TRANSPARENCY_NONE, 0);

                            lastdrawn = i + firstdrawn;
                        }
                    } else {
                        int sx, sy, colors;

                        colors = Machine.drv.total_colors - 256 * palpage;
                        if (colors > 256) colors = 256;
/*TODO*///					if (palette_used_colors)
/*TODO*///					{
/*TODO*///						memset(palette_used_colors,PALETTE_COLOR_UNUSED,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///						memset(palette_used_colors+256*palpage,PALETTE_COLOR_USED,colors * sizeof(unsigned char));
/*TODO*///						palette_recalc();	/* do it twice in case of previous overflow */
/*TODO*///						palette_recalc();	/*(we redraw the screen only when it changes) */
/*TODO*///					}

                        for (i = 0; i < 16; i++) {
                            String bf = "";

                            sx = 3 * Machine.uifontwidth + (Machine.uifontwidth * 4 / 3) * (i % 16);
                            bf = sprintf("%X", i);
                            ui_text(bitmap, bf, sx, 2 * Machine.uifontheight);
                            if (16 * i < colors) {
                                sy = 3 * Machine.uifontheight + (Machine.uifontheight) * (i % 16);
                                bf = sprintf("%3X", i + 16 * palpage);
                                ui_text(bitmap, bf, 0, sy);
                            }
                        }

                        for (i = 0; i < colors; i++) {
                            sx = Machine.uixmin + 3 * Machine.uifontwidth + (Machine.uifontwidth * 4 / 3) * (i % 16);
                            sy = Machine.uiymin + 2 * Machine.uifontheight + (Machine.uifontheight) * (i / 16) + Machine.uifontheight;
                            plot_box.handler(bitmap, sx, sy, Machine.uifontwidth * 4 / 3, Machine.uifontheight, Machine.pens[i + 256 * palpage]);
                        }
                    }

                    switch_true_orientation();
                } else	/* neogeo sprite tiles */ {
                    throw new UnsupportedOperationException("unsupported");
/*TODO*///				struct rectangle clip;
/*TODO*///
/*TODO*///				clip.min_x = Machine->uixmin;
/*TODO*///				clip.max_x = Machine->uixmin + Machine->uiwidth - 1;
/*TODO*///				clip.min_y = Machine->uiymin;
/*TODO*///				clip.max_y = Machine->uiymin + Machine->uiheight - 1;
/*TODO*///
/*TODO*///				if (palette_used_colors)
/*TODO*///				{
/*TODO*///					memset(palette_used_colors,PALETTE_COLOR_TRANSPARENT,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///					memset(palette_used_colors+Machine->gfx[bank]->color_granularity*color,PALETTE_COLOR_USED,Machine->gfx[bank]->color_granularity * sizeof(unsigned char));
/*TODO*///					palette_recalc();	/* do it twice in case of previous overflow */
/*TODO*///					palette_recalc();	/*(we redraw the screen only when it changes) */
/*TODO*///				}
/*TODO*///
/*TODO*///				for (i = 0; i+firstdrawn < no_of_tiles && i<cpx*cpy; i++)
/*TODO*///				{
/*TODO*///					if (bitmap->depth == 16)
/*TODO*///						NeoMVSDrawGfx16(bitmap->line,Machine->gfx[bank],
/*TODO*///							i+firstdrawn,color,  /*sprite num, color*/
/*TODO*///							0,0,
/*TODO*///							(i % cpx) * Machine->gfx[bank]->width + Machine->uixmin,
/*TODO*///							Machine->uifontheight+1 + (i / cpx) * Machine->gfx[bank]->height + Machine->uiymin,
/*TODO*///							16,16,&clip);
/*TODO*///					else
/*TODO*///						NeoMVSDrawGfx(bitmap->line,Machine->gfx[bank],
/*TODO*///							i+firstdrawn,color,  /*sprite num, color*/
/*TODO*///							0,0,
/*TODO*///							(i % cpx) * Machine->gfx[bank]->width + Machine->uixmin,
/*TODO*///							Machine->uifontheight+1 + (i / cpx) * Machine->gfx[bank]->height + Machine->uiymin,
/*TODO*///							16,16,&clip);
/*TODO*///
/*TODO*///					lastdrawn = i+firstdrawn;
/*TODO*///				}
                }
                if (bank >= 0)
                    buf = sprintf("GFXSET %d COLOR %2X CODE %X-%X", bank, color, firstdrawn, lastdrawn);
                else
                    buf = "PALETTE";
                ui_text(bitmap, buf, 0, 0);

                changed = 0;
            }

            update_video_and_audio();

            if (code_pressed(KEYCODE_LCONTROL) != 0 || code_pressed(KEYCODE_RCONTROL) != 0) {
                skip_chars = cpx;
            }
            if (code_pressed(KEYCODE_LSHIFT) != 0 || code_pressed(KEYCODE_RSHIFT) != 0) {
                skip_chars = 1;
            }


            if (input_ui_pressed_repeat(IPT_UI_RIGHT, 8) != 0) {
                if (bank + 1 < MAX_GFX_ELEMENTS && Machine.gfx[bank + 1] != null) {
                    bank++;
//				firstdrawn = 0;
                    changed = 1;
                }
            }

            if (input_ui_pressed_repeat(IPT_UI_LEFT, 8) != 0) {
                if (bank > -1) {
                    bank--;
//				firstdrawn = 0;
                    changed = 1;
                }
            }

            if (code_pressed_memory_repeat(KEYCODE_PGDN, 4) != 0) {
                if (bank >= 0) {
                    if (firstdrawn + skip_chars < Machine.gfx[bank].total_elements) {
                        firstdrawn += skip_chars;
                        changed = 1;
                    }
                } else {
                    if (256 * (palpage + 1) < Machine.drv.total_colors) {
                        palpage++;
                        changed = 1;
                    }
                }
            }

            if (code_pressed_memory_repeat(KEYCODE_PGUP, 4) != 0) {
                if (bank >= 0) {
                    firstdrawn -= skip_chars;
                    if (firstdrawn < 0) firstdrawn = 0;
                    changed = 1;
                } else {
                    if (palpage > 0) {
                        palpage--;
                        changed = 1;
                    }
                }
            }

            if (input_ui_pressed_repeat(IPT_UI_UP, 6) != 0) {
                if (bank >= 0) {
                    if (color < Machine.gfx[bank].total_colors - 1) {
                        color++;
                        changed = 1;
                    }
                }
            }

            if (input_ui_pressed_repeat(IPT_UI_DOWN, 6) != 0) {
                if (color > 0) {
                    color--;
                    changed = 1;
                }
            }

/*TODO*///		if (input_ui_pressed(IPT_UI_SNAPSHOT))
/*TODO*///			osd_save_snapshot(bitmap);
        } while (input_ui_pressed(IPT_UI_SHOW_GFX) == 0 &&
                input_ui_pressed(IPT_UI_CANCEL) == 0);

			/* clear the screen before returning */
        osd_clearbitmap(bitmap);

/*TODO*///	if (palette_used_colors)
/*TODO*///	{
/*TODO*///		/* this should force a full refresh by the video driver */
/*TODO*///		memset(palette_used_colors,PALETTE_COLOR_TRANSPARENT,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		palette_recalc();
/*TODO*///		/* restore the game used colors array */
/*TODO*///		memcpy(palette_used_colors,orig_used_colors,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		free(orig_used_colors);
/*TODO*///	}

        return;
    }


    public static int mame_stats(osd_bitmap bitmap, int selected) {
        String temp = "";
        String buf = "";
        int sel, i;


        sel = selected - 1;

        if (dispensed_tickets != 0) {
            buf += ui_getstring(UI_tickets);
            buf += ": ";
            temp = sprintf("%d\n\n", dispensed_tickets);
            buf += temp;
        }

        for (i = 0; i < COIN_COUNTERS; i++) {
            buf += ui_getstring(UI_coin);
            temp = sprintf(" %c: ", i + 'A');
            buf += temp;
            if (coins[i] == 0)
                buf += ui_getstring(UI_NA);
            else {
                temp = sprintf("%d", coins[i]);
                buf += temp;
            }
            if (coinlockedout[i] != 0) {
                buf += " ";
                buf += ui_getstring(UI_locked);
                buf += "\n";
            } else {
                buf += "\n";
            }
        }

        {
        /* menu system, use the normal menu keys */
            buf += "\n\t";
            buf += ui_getstring(UI_lefthilight);
            buf += " ";
            buf += ui_getstring(UI_returntomain);
            buf += " ";
            buf += ui_getstring(UI_righthilight);

            ui_displaymessagewindow(bitmap, buf);

            if (input_ui_pressed(IPT_UI_SELECT) != 0)
                sel = -1;

            if (input_ui_pressed(IPT_UI_CANCEL) != 0)
                sel = -1;

            if (input_ui_pressed(IPT_UI_CONFIGURE) != 0)
                sel = -2;
        }

        if (sel == -1 || sel == -2) {
        /* tell updatescreen() to clean after us */
            need_to_clear_bitmap = 1;
        }

        return sel + 1;
    }

    public static int showcopyright(osd_bitmap bitmap) {
        int done;
        String buf = "";
        String buf2 = "";

        buf = ui_getstring(UI_copyright1);
        buf += "\n\n";
        buf2 = sprintf(ui_getstring(UI_copyright2), Machine.gamedrv.description);
        buf += buf2;
        buf += "\n\n";
        buf += ui_getstring(UI_copyright3);

        ui_displaymessagewindow(bitmap, buf);

        setup_selected = -1;////
        done = 0;
        do {
            update_video_and_audio();
/*TODO*///		osd_poll_joysticks();
            if (input_ui_pressed(IPT_UI_CANCEL) != 0) {
                setup_selected = 0;////
                return 1;
            }
            if (keyboard_pressed_memory(KEYCODE_O) != 0 ||
                    input_ui_pressed(IPT_UI_LEFT) != 0)
                done = 1;
            if (done == 1 && (keyboard_pressed_memory(KEYCODE_K) != 0 ||
                    input_ui_pressed(IPT_UI_RIGHT) != 0))
                done = 2;
        } while (done < 2);

        setup_selected = 0;////
        osd_clearbitmap(bitmap);
        update_video_and_audio();

        return 0;
    }

    /*TODO*///int memcard_menu(struct osd_bitmap *bitmap, int selection)
/*TODO*///{
/*TODO*///	int sel;
/*TODO*///	int menutotal = 0;
/*TODO*///	const char *menuitem[10];
/*TODO*///	char buf[256];
/*TODO*///	char buf2[256];
/*TODO*///
/*TODO*///	sel = selection - 1 ;
/*TODO*///
/*TODO*///	sprintf(buf, "%s %03d", ui_getstring (UI_loadcard), mcd_number);
/*TODO*///	menuitem[menutotal++] = buf;
/*TODO*///	menuitem[menutotal++] = ui_getstring (UI_ejectcard);
/*TODO*///	menuitem[menutotal++] = ui_getstring (UI_createcard);
/*TODO*///	menuitem[menutotal++] = ui_getstring (UI_resetcard);
/*TODO*///	menuitem[menutotal++] = ui_getstring (UI_returntomain);
/*TODO*///	menuitem[menutotal] = 0;
/*TODO*///
/*TODO*///	if (mcd_action!=0)
/*TODO*///	{
/*TODO*///		strcpy (buf2, "\n");
/*TODO*///
/*TODO*///		switch(mcd_action)
/*TODO*///		{
/*TODO*///			case 1:
/*TODO*///				strcat (buf2, ui_getstring (UI_loadfailed));
/*TODO*///				break;
/*TODO*///			case 2:
/*TODO*///				strcat (buf2, ui_getstring (UI_loadok));
/*TODO*///				break;
/*TODO*///			case 3:
/*TODO*///				strcat (buf2, ui_getstring (UI_cardejected));
/*TODO*///				break;
/*TODO*///			case 4:
/*TODO*///				strcat (buf2, ui_getstring (UI_cardcreated));
/*TODO*///				break;
/*TODO*///			case 5:
/*TODO*///				strcat (buf2, ui_getstring (UI_cardcreatedfailed));
/*TODO*///				strcat (buf2, "\n");
/*TODO*///				strcat (buf2, ui_getstring (UI_cardcreatedfailed2));
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				strcat (buf2, ui_getstring (UI_carderror));
/*TODO*///				break;
/*TODO*///		}
/*TODO*///
/*TODO*///		strcat (buf2, "\n\n");
/*TODO*///		ui_displaymessagewindow(bitmap,buf2);
/*TODO*///		if (input_ui_pressed(IPT_UI_SELECT))
/*TODO*///			mcd_action = 0;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		ui_displaymenu(bitmap,menuitem,0,0,sel,0);
/*TODO*///
/*TODO*///		if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
/*TODO*///			mcd_number = (mcd_number + 1) % 1000;
/*TODO*///
/*TODO*///		if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
/*TODO*///			mcd_number = (mcd_number + 999) % 1000;
/*TODO*///
/*TODO*///		if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
/*TODO*///			sel = (sel + 1) % menutotal;
/*TODO*///
/*TODO*///		if (input_ui_pressed_repeat(IPT_UI_UP,8))
/*TODO*///			sel = (sel + menutotal - 1) % menutotal;
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_SELECT))
/*TODO*///		{
/*TODO*///			switch(sel)
/*TODO*///			{
/*TODO*///			case 0:
/*TODO*///				neogeo_memcard_eject();
/*TODO*///				if (neogeo_memcard_load(mcd_number))
/*TODO*///				{
/*TODO*///					memcard_status=1;
/*TODO*///					memcard_number=mcd_number;
/*TODO*///					mcd_action = 2;
/*TODO*///				}
/*TODO*///				else
/*TODO*///					mcd_action = 1;
/*TODO*///				break;
/*TODO*///			case 1:
/*TODO*///				neogeo_memcard_eject();
/*TODO*///				mcd_action = 3;
/*TODO*///				break;
/*TODO*///			case 2:
/*TODO*///				if (neogeo_memcard_create(mcd_number))
/*TODO*///					mcd_action = 4;
/*TODO*///				else
/*TODO*///					mcd_action = 5;
/*TODO*///				break;
/*TODO*///			case 3:
/*TODO*///				memcard_manager=1;
/*TODO*///				sel=-2;
/*TODO*///				machine_reset();
/*TODO*///				break;
/*TODO*///			case 4:
/*TODO*///				sel=-1;
/*TODO*///				break;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///			sel = -1;
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///			sel = -2;
/*TODO*///
/*TODO*///		if (sel == -1 || sel == -2)
/*TODO*///		{
/*TODO*///			/* tell updatescreen() to clean after us */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	return sel + 1;
/*TODO*///}
/*TODO*///
/*TODO*///


    static int show_total_colors;

    public static int handle_user_interface(osd_bitmap bitmap) {
/*TODO*///	/* if the user pressed F12, save the screen to a file */
/*TODO*///	if (input_ui_pressed(IPT_UI_SNAPSHOT))
/*TODO*///		osd_save_snapshot(bitmap);
/*TODO*///
/*TODO*///	/* This call is for the cheat, it must be called once a frame */
	if (options.cheat!=0) DoCheat(bitmap);
/*TODO*///
    /* if the user pressed ESC, stop the emulation */
    /* but don't quit if the setup menu is on screen */
        if (setup_selected == 0 && input_ui_pressed(IPT_UI_CANCEL) != 0)
            return 1;

        if (setup_selected == 0 && input_ui_pressed(IPT_UI_CONFIGURE) != 0) {
            setup_selected = -1;
            if (osd_selected != 0) {
                osd_selected = 0;	/* disable on screen display */
				/* tell updatescreen() to clean after us */
                need_to_clear_bitmap = 1;
            }
        }
        if (setup_selected != 0) setup_selected = setup_menu(bitmap, setup_selected);

        if (osd_selected == 0 && input_ui_pressed(IPT_UI_ON_SCREEN_DISPLAY) != 0) {
            osd_selected = -1;
            if (setup_selected != 0) {
                setup_selected = 0; /* disable setup menu */
				/* tell updatescreen() to clean after us */
                need_to_clear_bitmap = 1;
            }
        }
        if (osd_selected != 0) osd_selected = on_screen_display(bitmap, osd_selected);

	/* if the user pressed F3, reset the emulation */
        if (input_ui_pressed(IPT_UI_RESET_MACHINE) != 0)
            machine_reset();


        if (single_step != 0 || input_ui_pressed(IPT_UI_PAUSE) != 0) /* pause the game */ {
/*		osd_selected = 0;	   disable on screen display, since we are going   */
                            /* to change parameters affected by it */

            if (single_step == 0) {
                osd_sound_enable(0);
                osd_pause(1);
            }

            while (input_ui_pressed(IPT_UI_PAUSE) == 0) {
                if (osd_skip_this_frame() == 0) {
                    if (need_to_clear_bitmap != 0 || bitmap_dirty != 0) {
                        osd_clearbitmap(bitmap);
                        need_to_clear_bitmap = 0;
                        draw_screen(bitmap_dirty);
                        bitmap_dirty = 0;
                    }
                }

/*TODO*///			if (input_ui_pressed(IPT_UI_SNAPSHOT))
/*TODO*///				osd_save_snapshot(bitmap);

                if (setup_selected == 0 && input_ui_pressed(IPT_UI_CANCEL) != 0)
                    return 1;

                if (setup_selected == 0 && input_ui_pressed(IPT_UI_CONFIGURE) != 0) {
                    setup_selected = -1;
                    if (osd_selected != 0) {
                        osd_selected = 0;	/* disable on screen display */
					/* tell updatescreen() to clean after us */
                        need_to_clear_bitmap = 1;
                    }
                }
                if (setup_selected != 0) setup_selected = setup_menu(bitmap, setup_selected);

                if (osd_selected == 0 && input_ui_pressed(IPT_UI_ON_SCREEN_DISPLAY) != 0) {
                    osd_selected = -1;
                    if (setup_selected != 0) {
                        setup_selected = 0; /* disable setup menu */
					/* tell updatescreen() to clean after us */
                        need_to_clear_bitmap = 1;
                    }
                }
                if (osd_selected != 0) osd_selected = on_screen_display(bitmap, osd_selected);

			/* show popup message if any */
                if (messagecounter > 0) displaymessage(bitmap, messagetext);

                update_video_and_audio();
/*TODO*///			osd_poll_joysticks();
            }

            if (code_pressed(KEYCODE_LSHIFT) != 0 || code_pressed(KEYCODE_RSHIFT) != 0)
                single_step = 1;
            else {
                single_step = 0;
                osd_pause(0);
                osd_sound_enable(1);
            }
        }


	/* show popup message if any */
        if (messagecounter > 0) {
            displaymessage(bitmap, messagetext);

            if (--messagecounter == 0)
			/* tell updatescreen() to clean after us */
                need_to_clear_bitmap = 1;
        }

        if (input_ui_pressed(IPT_UI_SHOW_COLORS) != 0) {
            show_total_colors ^= 1;
            if (show_total_colors == 0)
			/* tell updatescreen() to clean after us */
                need_to_clear_bitmap = 1;
        }
        if (show_total_colors != 0) showtotalcolors(bitmap);


	/* if the user pressed F4, show the character set */
        if (input_ui_pressed(IPT_UI_SHOW_GFX) != 0) {
            osd_sound_enable(0);

            showcharset(bitmap);

            osd_sound_enable(1);
        }

        return 0;
    }


    public static void init_user_interface() {
/*TODO*///	extern int snapno;	/* in common.c */
/*TODO*///
/*TODO*///	snapno = 0; /* reset snapshot counter */
/*TODO*///
        setup_menu_init();
        setup_selected = 0;

        onscrd_init();
        osd_selected = 0;


        single_step = 0;

        orientation_count = 0;
    }

    public static int onscrd_active() {
        return osd_selected;
    }

    public static int setup_active() {
        return setup_selected;
    }
}
