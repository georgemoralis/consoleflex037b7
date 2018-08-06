package mess.vidhrdw;

/************************************************************************
	crct6845

	MESS Driver By:

 	Gordon Jefferyes
 	mess_bbc@gjeffery.dircon.co.uk

 ************************************************************************/

public class m6845H{
    public static abstract class crtc6845_interface
    {
            public boolean out_MA_func=false;
            public boolean out_RA_func=false;
            public boolean out_HS_func=false;
            public boolean out_VS_func=false;
            public boolean out_DE_func=false;
            public boolean out_CR_func=false;
            
            public abstract void out_MA_func(int offset, int data);
            public abstract void out_RA_func(int offset, int data);
            public abstract void out_HS_func(int offset, int data);
            public abstract void out_VS_func(int offset, int data);
            public abstract void out_DE_func(int offset, int data);
            public abstract void out_CR_func(int offset, int data);
    };

    public static class crtc6845_state
    {
            /* Register Select */
            int address_register; 
            /* register data */
            int[] registers=new int[32];
            /* vertical and horizontal sync widths */
            int vertical_sync_width, horizontal_sync_width;

            int screen_start_address;         /* = R12<<8 + R13 */
            int cursor_address;				  /* = R14<<8 + R15 */
            int light_pen_address;			  /* = R16<<8 + R17 */

            int scan_lines_increment;

            int Horizontal_Counter;
            int Horizontal_Counter_Reset;

            int Scan_Line_Counter;
            int Scan_Line_Counter_Reset;

            int Character_Row_Counter;
            int Character_Row_Counter_Reset;

            int Horizontal_Sync_Width_Counter;
            int Vertical_Sync_Width_Counter;

            int HSYNC;
            int VSYNC;

            int Vertical_Total_Adjust_Active;
            int Vertical_Total_Adjust_Counter;

            int Memory_Address;
            int Memory_Address_of_next_Character_Row;
            int Memory_Address_of_this_Character_Row;

            int Horizontal_Display_Enabled;
            int Vertical_Display_Enabled;
            int Display_Enabled;
            int Display_Delayed_Enabled;

            int Cursor_Delayed_Status;

            int Delay_Flags;
            int Cursor_Start_Delay;
            int Display_Enabled_Delay;
            int Display_Disable_Delay;

            int	Vertical_Adjust_Done;
    //	int cycles_to_vsync_start;
    //	int cycles_to_vsync_end;
    };

    /* set up the local copy of the 6845 external procedure calls */
    /*TODO*/////void crtc6845_config(const struct crtc6845_interface *intf);


    /* functions to set the 6845 registers */
    /*TODO*/////int crtc6845_register_r(int offset);
    /*TODO*/////void crtc6845_address_w(int offset, int data);
    /*TODO*/////void crtc6845_register_w(int offset, int data);


    /* clock the 6845 */


    /* functions to read the 6845 outputs */
    /*TODO*/////int crtc6845_memory_address_r(int offset);
    /*TODO*/////int crtc6845_row_address_r(int offset);
    /*TODO*/////int crtc6845_horizontal_sync_r(int offset);
    /*TODO*/////int crtc6845_vertical_sync_r(int offset);
    /*TODO*/////int crtc6845_display_enabled_r(int offset);
    /*TODO*/////int crtc6845_cursor_enabled_r(int offset);

    /*TODO*/////void crtc6845_recalc(int offset, int cycles);

    /*TODO*/////void crtc6845_set_state(int offset, crtc6845_state *state);
    /*TODO*/////void crtc6845_get_state(int offset, crtc6845_state *state);

    /*TODO*/////void	crtc6845_reset(int which);


}
