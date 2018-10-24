package mess.machine;

import static WIP.arcadeflex.libc_v2.*;

public class msxH {
	/*
	** msx.h : part of MSX1 emulation.
	**
	** By Sean Young 1999
	*/

	public static int MSX_MAX_ROMSIZE 	= (512*1024);
	public static int MSX_MAX_CARTS   		= (2);

	public static class MSX_CART {
	    public int type=0;
	    public int bank_mask=0; //new int[4];
	    public int[] banks=new int[4];
	    public UBytePtr mem;
	    public String sramfile;
	    public boolean pacsram=false;
	};

	public static class MSX{
	    int run=0; /* set after init_msx () */
	    /* PSG */
	    int psg_b,opll_active;
	    /* memory */
	    UBytePtr empty, ram;
	    /* memory status */
	    public static MSX_CART[] cart=new MSX_CART[MSX_MAX_CARTS];
	    
	    static {
	    	for (int i=0 ; i<MSX_MAX_CARTS ; i++) {
	    		cart[i]=new MSX_CART();
	    	}
	    }
	};

	/* start/stop functions */
	//int msx_load_rom (int id);
	//int msx_id_rom (int id);
	//void msx_exit_rom (int id);

	/* I/O functions */
	//WRITE_HANDLER ( msx_printer_w );
	//READ_HANDLER ( msx_printer_r );
	//WRITE_HANDLER ( msx_vdp_w );
	//READ_HANDLER ( msx_vdp_r );
	//WRITE_HANDLER ( msx_psg_w );
	//READ_HANDLER ( msx_psg_r );
	//WRITE_HANDLER ( msx_psg_port_a_w );
	//READ_HANDLER ( msx_psg_port_a_r );
	//WRITE_HANDLER ( msx_psg_port_b_w );
	//READ_HANDLER ( msx_psg_port_b_r );
	//WRITE_HANDLER ( msx_fmpac_w );

	/* memory functions */
	//WRITE_HANDLER ( msx_writemem0 );
	//WRITE_HANDLER ( msx_writemem1 );
	//WRITE_HANDLER ( msx_writemem2 );
	//WRITE_HANDLER ( msx_writemem3 );

	/* cassette functions */
	//int msx_cassette_init (int id);
	//void msx_cassette_exit (int id);

}
