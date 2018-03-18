/*
 * ported to v0.37b6
 * using automatic conversion tool v0.01
 */
package mess.machine;


public class nes_mmcH {
/*TODO*///typedef struct __mmc
/*TODO*///{
/*TODO*///	int iNesMapper; /* iNES Mapper # */
/*TODO*///	char *desc;     /* Mapper description */
/*TODO*///	void (*mmc_write_low)(int offset, int data); /* $4100-$5fff write routine */
/*TODO*///	int (*mmc_read_low)(int offset); /* $4100-$5fff read routine */
/*TODO*///	void (*mmc_write_mid)(int offset, int data); /* $6000-$7fff write routine */
/*TODO*///	void (*mmc_write)(int offset, int data); /* $8000-$ffff write routine */
/*TODO*///	void (*ppu_latch)(int offset);
/*TODO*///	int (*mmc_irq)(int scanline);
/*TODO*///} mmc;
/*TODO*///
/*TODO*///    
}
