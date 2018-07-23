/**
 * ported to 0.37b7
 */
/**
 * Changelog
 * 23/07/2018 (shadow) - Adjustments for MESS
 */
package mame;

import static WIP.arcadeflex.libc_v2.UBytePtr;

public class ui_textH {

    /* Important: this must match the default_text list in ui_text.c! */
    public static final int UI_first_entry = -1;

    public static final int UI_mame = 0;

    /* copyright stuff */
    public static final int UI_copyright1 = 1;
    public static final int UI_copyright2 = 2;
    public static final int UI_copyright3 = 3;

    /* misc menu stuff */
    public static final int UI_returntomain = 4;
    public static final int UI_returntoprior = 5;
    public static final int UI_anykey = 6;
    public static final int UI_on = 7;
    public static final int UI_off = 8;
    public static final int UI_NA = 9;
    public static final int UI_INVALID = 10;
    public static final int UI_none = 11;
    public static final int UI_cpu = 12;
    public static final int UI_address = 13;
    public static final int UI_value = 14;
    public static final int UI_sound = 15;
    public static final int UI_sound_lc = 16;
    /* lower-case version */
    public static final int UI_stereo = 17;
    public static final int UI_vectorgame = 18;
    public static final int UI_screenres = 19;
    public static final int UI_text = 20;
    public static final int UI_volume = 21;
    public static final int UI_relative = 22;
    public static final int UI_allchannels = 23;
    public static final int UI_brightness = 24;
    public static final int UI_gamma = 25;
    public static final int UI_vectorintensity = 26;
    public static final int UI_overclock = 27;
    public static final int UI_allcpus = 28;
    public static final int UI_historymissing = 29;

    /* special characters */
    public static final int UI_leftarrow = 30;
    public static final int UI_rightarrow = 31;
    public static final int UI_uparrow = 32;
    public static final int UI_downarrow = 33;
    public static final int UI_lefthilight = 34;
    public static final int UI_righthilight = 35;

    /* warnings */
    public static final int UI_knownproblems = 36;
    public static final int UI_imperfectcolors = 37;
    public static final int UI_wrongcolors = 38;
    public static final int UI_imperfectsound = 39;
    public static final int UI_nosound = 40;
    public static final int UI_nococktail = 41;
    public static final int UI_brokengame = 42;
    public static final int UI_brokenprotection = 43;
    public static final int UI_workingclones = 44;
    public static final int UI_typeok = 45;
//#ifdef MESS
    public static final int UI_comp1 = 46;
    public static final int UI_comp2 = 47;
//#endif
    /* main menu */
    public static final int UI_inputgeneral = 48;
    public static final int UI_dipswitches = 49;
    public static final int UI_analogcontrols = 50;
    public static final int UI_calibrate = 51;
    public static final int UI_bookkeeping = 52;
    public static final int UI_inputspecific = 53;
    public static final int UI_gameinfo = 54;
    public static final int UI_history = 55;
    public static final int UI_resetgame = 56;
    public static final int UI_returntogame = 57;
//#ifdef MESS
    public static final int UI_imageinfo = 58;
    public static final int UI_filemanager = 59;
    public static final int UI_tapecontrol = 60;
//#endif
    public static final int UI_cheat = 61;
    public static final int UI_memorycard = 62;

    /* input stuff */
    public static final int UI_keyjoyspeed = 63;
    public static final int UI_reverse = 64;
    public static final int UI_sensitivity = 65;

    /* stats */
    public static final int UI_tickets = 66;
    public static final int UI_coin = 67;
    public static final int UI_locked = 68;

    /* memory card */
    public static final int UI_loadcard = 69;
    public static final int UI_ejectcard = 70;
    public static final int UI_createcard = 71;
    public static final int UI_resetcard = 72;
    public static final int UI_loadfailed = 73;
    public static final int UI_loadok = 74;
    public static final int UI_cardejected = 75;
    public static final int UI_cardcreated = 76;
    public static final int UI_cardcreatedfailed = 77;
    public static final int UI_cardcreatedfailed2 = 78;
    public static final int UI_carderror = 79;

    /* cheat stuff */
    public static final int UI_enablecheat = 80;
    public static final int UI_addeditcheat = 81;
    public static final int UI_startcheat = 82;
    public static final int UI_continuesearch = 83;
    public static final int UI_viewresults = 84;
    public static final int UI_restoreresults = 85;
    public static final int UI_memorywatch = 86;
    public static final int UI_generalhelp = 87;
    public static final int UI_watchpoint = 88;
    public static final int UI_disabled = 89;
    public static final int UI_cheats = 90;
    public static final int UI_watchpoints = 91;
    public static final int UI_moreinfo = 92;
    public static final int UI_moreinfoheader = 93;
    public static final int UI_cheatname = 94;
    public static final int UI_cheatdescription = 95;
    public static final int UI_code = 96;

    /* watchpoint stuff */
    public static final int UI_watchlength = 97;
    public static final int UI_watchlabeltype = 98;
    public static final int UI_watchlabel = 99;
    public static final int UI_watchx = 100;
    public static final int UI_watchy = 101;
    public static final int UI_watch = 102;

    /* search stuff */
    public static final int UI_search_lives = 103;
    public static final int UI_search_timers = 104;
    public static final int UI_search_energy = 105;
    public static final int UI_search_status = 106;
    public static final int UI_search_slow = 107;
    public static final int UI_search_speed = 108;
    public static final int UI_search_matches_found = 109;
    public static final int UI_search_noinit = 110;
    public static final int UI_search_nosave = 111;
    public static final int UI_search_done = 112;
    public static final int UI_search_OK = 113;

    public static final int UI_last_entry = 114;

    public static class lang_struct {

        public int version;
        public int multibyte;
        /* UNUSED: 1 if this is a multibyte font/language */
        public UBytePtr fontdata;
        /* pointer to the raw font data to be decoded */
        public char fontglyphs;
        /* total number of glyps in the external font - 1 */
        public String langname;
        public String fontname;
        public String author;
    }
}
