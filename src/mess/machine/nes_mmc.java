/*
 * ported to v0.37b6
 * using automatic conversion tool v0.01
 */
package mess.machine;

import static WIP.arcadeflex.libc_v2.*;
import static WIP.arcadeflex.fucPtr.*;
import static WIP.mame.mame.Machine;
import static consoleflex.funcPtr.*;
import static mess.messH.*;
import static mess.mess.*;
import static old.mame.inptport.*;
import static mess.vidhrdw.tms9928a.*;
import static old.arcadeflex.osdepend.*;
import static mess.osdepend.fileio.*;
import static arcadeflex.libc.cstring.*;
import static WIP.mame.osdependH.*;
import static mame.commonH.*;
import static old.mame.common.*;
import static WIP.arcadeflex.libc.memcpy.*;
import static old.arcadeflex.libc_old.SEEK_SET;
import static old.arcadeflex.libc_old.printf;
import static old.arcadeflex.libc_old.strcmp;
import static mess.includes.nesH.*;

public class nes_mmc {

    /*TODO*///	
/*TODO*///	
/*TODO*///	#define LOG_MMC
/*TODO*///	#define LOG_FDS
/*TODO*///	
/*TODO*///	/* Global variables */
/*TODO*///	int prg_mask;
/*TODO*///	
/*TODO*///	int IRQ_enable, IRQ_enable_latch;
/*TODO*///	UINT16 IRQ_count, IRQ_count_latch;
/*TODO*///	UINT8 IRQ_status;
/*TODO*///	UINT8 IRQ_mode_jaleco;
/*TODO*///	
/*TODO*///	int MMC1_extended;	/* 0 = normal MMC1 cart, 1 = 512k MMC1, 2 = 1024k MMC1 */
/*TODO*///	
/*TODO*///	void (*mmc_write_low)(int offset, int data);
/*TODO*///	int (*mmc_read_low)(int offset);
/*TODO*///	void (*mmc_write_mid)(int offset, int data);
/*TODO*///	int (*mmc_read_mid)(int offset);
/*TODO*///	void (*mmc_write)(int offset, int data);
/*TODO*///	void (*ppu_latch)(int offset);
/*TODO*///	int (*mmc_irq)(int scanline);
/*TODO*///	
/*TODO*///	static int vrom_bank[16];
/*TODO*///	static int mult1, mult2;
/*TODO*///	
/*TODO*///	/* Local variables */
/*TODO*///	static int MMC1_Size_16k;
/*TODO*///	static int MMC1_High;
/*TODO*///	static int MMC1_reg;
/*TODO*///	static int MMC1_reg_count;
/*TODO*///	static int MMC1_Switch_Low, MMC1_SizeVrom_4k;
/*TODO*///	static int MMC1_bank1, MMC1_bank2, MMC1_bank3, MMC1_bank4;
/*TODO*///	static int MMC1_extended_bank;
/*TODO*///	static int MMC1_extended_base;
/*TODO*///	static int MMC1_extended_swap;
/*TODO*///	
/*TODO*///	static int MMC2_bank0, MMC2_bank0_hi, MMC2_bank0_latch, MMC2_bank1, MMC2_bank1_hi, MMC2_bank1_latch;
/*TODO*///	
/*TODO*///	static int MMC3_cmd;
/*TODO*///	static int MMC3_prg0, MMC3_prg1;
/*TODO*///	static int MMC3_chr[6];
/*TODO*///	
/*TODO*///	static int MMC5_rom_bank_mode;
/*TODO*///	static int MMC5_vrom_bank_mode;
/*TODO*///	static int MMC5_vram_protect;
/*TODO*///	static int MMC5_scanline;
/*TODO*///	UINT8 MMC5_vram[0x400];
/*TODO*///	int MMC5_vram_control;
/*TODO*///	
/*TODO*///	static int mapper41_chr, mapper41_reg2;
/*TODO*///	
/*TODO*///	static int mapper_warning;
/*TODO*///	
    public static WriteHandlerPtr nes_low_mapper_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///		if (*mmc_write_low) (*mmc_write_low)(offset, data);
/*TODO*///		else
/*TODO*///		{
/*TODO*///			logerror("Unimplemented LOW mapper write, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///			if (! mapper_warning)
/*TODO*///			{
/*TODO*///				printf ("This game is writing to the low mapper area but no mapper is set. You may get better results by switching to a valid mapper.\n");
/*TODO*///				mapper_warning = 1;
/*TODO*///			}
/*TODO*///	#endif
/*TODO*///		}
        }
    };

    /* Handle mapper reads from $4100-$5fff */
    public static ReadHandlerPtr nes_low_mapper_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///		if (*mmc_read_low)
/*TODO*///			return (*mmc_read_low)(offset);
/*TODO*///		else
/*TODO*///			logerror("low mapper area read, addr: %04x\n", offset + 0x4100);
/*TODO*///	
/*TODO*///		return 0;
        }
    };
    /*TODO*///	
/*TODO*///	WRITE_HANDLER ( nes_mid_mapper_w )
/*TODO*///	{
/*TODO*///		if (*mmc_write_mid) (*mmc_write_mid)(offset, data);
/*TODO*///		else if (nes.mid_ram_enable)
/*TODO*///			battery_ram[offset] = data;
/*TODO*///	//	else
/*TODO*///		{
/*TODO*///			logerror("Unimplemented MID mapper write, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///			if (! mapper_warning)
/*TODO*///			{
/*TODO*///				printf ("This game is writing to the MID mapper area but no mapper is set. You may get better results by switching to a valid mapper or changing the battery flag for this ROM.\n");
/*TODO*///				mapper_warning = 1;
/*TODO*///			}
/*TODO*///	#endif
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	int nes_mid_mapper_r (int offset)
/*TODO*///	{
/*TODO*///		if ((nes.mid_ram_enable) || (nes.mapper == 5))
/*TODO*///			return battery_ram[offset];
/*TODO*///		else
/*TODO*///			return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER ( nes_mapper_w )
/*TODO*///	{
/*TODO*///		if (*mmc_write) (*mmc_write)(offset, data);
/*TODO*///		else
/*TODO*///		{
/*TODO*///			logerror("Unimplemented mapper write, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#if 1
/*TODO*///			if (! mapper_warning)
/*TODO*///			{
/*TODO*///				printf ("This game is writing to the mapper area but no mapper is set. You may get better results by switching to a valid mapper.\n");
/*TODO*///				mapper_warning = 1;
/*TODO*///			}
/*TODO*///	
/*TODO*///			switch (offset)
/*TODO*///			{
/*TODO*///				/* Hacked mapper for the 24-in-1 NES_ROM. */
/*TODO*///				/* It's really 35-in-1, and it's mostly different versions of Battle City. Unfortunately, the vrom dumps are bad */
/*TODO*///				case 0x7fde:
/*TODO*///					data &= (nes.prg_chunks - 1);
/*TODO*///					cpu_setbank (3, &nes.rom[data * 0x4000 + 0x10000]);
/*TODO*///					cpu_setbank (4, &nes.rom[data * 0x4000 + 0x12000]);
/*TODO*///					break;
/*TODO*///			}
/*TODO*///	#endif
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 * Some helpful routines used by the mappers
/*TODO*///	 */
/*TODO*///	
/*TODO*///	static void prg8_89 (int bank)
/*TODO*///	{
/*TODO*///		/* assumes that bank references an 8k chunk */
/*TODO*///		bank &= ((nes.prg_chunks << 1) - 1);
/*TODO*///		if (nes.slow_banking)
/*TODO*///			memcpy (&nes.rom[0x8000], &nes.rom[bank * 0x2000 + 0x10000], 0x2000);
/*TODO*///		else
/*TODO*///			cpu_setbank (1, &nes.rom[bank * 0x2000 + 0x10000]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void prg8_ab (int bank)
/*TODO*///	{
/*TODO*///		/* assumes that bank references an 8k chunk */
/*TODO*///		bank &= ((nes.prg_chunks << 1) - 1);
/*TODO*///		if (nes.slow_banking)
/*TODO*///			memcpy (&nes.rom[0xa000], &nes.rom[bank * 0x2000 + 0x10000], 0x2000);
/*TODO*///		else
/*TODO*///			cpu_setbank (2, &nes.rom[bank * 0x2000 + 0x10000]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void prg8_cd (int bank)
/*TODO*///	{
/*TODO*///		/* assumes that bank references an 8k chunk */
/*TODO*///		bank &= ((nes.prg_chunks << 1) - 1);
/*TODO*///		if (nes.slow_banking)
/*TODO*///			memcpy (&nes.rom[0xc000], &nes.rom[bank * 0x2000 + 0x10000], 0x2000);
/*TODO*///		else
/*TODO*///			cpu_setbank (3, &nes.rom[bank * 0x2000 + 0x10000]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void prg8_ef (int bank)
/*TODO*///	{
/*TODO*///		/* assumes that bank references an 8k chunk */
/*TODO*///		bank &= ((nes.prg_chunks << 1) - 1);
/*TODO*///		if (nes.slow_banking)
/*TODO*///			memcpy (&nes.rom[0xe000], &nes.rom[bank * 0x2000 + 0x10000], 0x2000);
/*TODO*///		else
/*TODO*///			cpu_setbank (4, &nes.rom[bank * 0x2000 + 0x10000]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void prg16_89ab (int bank)
/*TODO*///	{
/*TODO*///		/* assumes that bank references a 16k chunk */
/*TODO*///		bank &= (nes.prg_chunks - 1);
/*TODO*///		if (nes.slow_banking)
/*TODO*///			memcpy (&nes.rom[0x8000], &nes.rom[bank * 0x4000 + 0x10000], 0x4000);
/*TODO*///		else
/*TODO*///		{
/*TODO*///			cpu_setbank (1, &nes.rom[bank * 0x4000 + 0x10000]);
/*TODO*///			cpu_setbank (2, &nes.rom[bank * 0x4000 + 0x12000]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void prg16_cdef (int bank)
/*TODO*///	{
/*TODO*///		/* assumes that bank references a 16k chunk */
/*TODO*///		bank &= (nes.prg_chunks - 1);
/*TODO*///		if (nes.slow_banking)
/*TODO*///			memcpy (&nes.rom[0xc000], &nes.rom[bank * 0x4000 + 0x10000], 0x4000);
/*TODO*///		else
/*TODO*///		{
/*TODO*///			cpu_setbank (3, &nes.rom[bank * 0x4000 + 0x10000]);
/*TODO*///			cpu_setbank (4, &nes.rom[bank * 0x4000 + 0x12000]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void prg32 (int bank)
/*TODO*///	{
/*TODO*///		/* assumes that bank references a 32k chunk */
/*TODO*///		bank &= ((nes.prg_chunks >> 1) - 1);
/*TODO*///		if (nes.slow_banking)
/*TODO*///			memcpy (&nes.rom[0x8000], &nes.rom[bank * 0x8000 + 0x10000], 0x8000);
/*TODO*///		else
/*TODO*///		{
/*TODO*///			cpu_setbank (1, &nes.rom[bank * 0x8000 + 0x10000]);
/*TODO*///			cpu_setbank (2, &nes.rom[bank * 0x8000 + 0x12000]);
/*TODO*///			cpu_setbank (3, &nes.rom[bank * 0x8000 + 0x14000]);
/*TODO*///			cpu_setbank (4, &nes.rom[bank * 0x8000 + 0x16000]);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void chr8 (int bank)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		bank &= (nes.chr_chunks - 1);
/*TODO*///		for (i = 0; i < 8; i ++)
/*TODO*///			nes_vram[i] = bank * 512 + 64*i;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void chr4_0 (int bank)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		bank &= ((nes.chr_chunks << 1) - 1);
/*TODO*///		for (i = 0; i < 4; i ++)
/*TODO*///			nes_vram[i] = bank * 256 + 64*i;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void chr4_4 (int bank)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		bank &= ((nes.chr_chunks << 1) - 1);
/*TODO*///		for (i = 4; i < 8; i ++)
/*TODO*///			nes_vram[i] = bank * 256 + 64*(i-4);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void chr2_0 (int bank)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		bank &= ((nes.chr_chunks << 2) - 1);
/*TODO*///		for (i = 0; i < 2; i ++)
/*TODO*///			nes_vram[i] = bank * 128 + 64*i;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void chr2_2 (int bank)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		bank &= ((nes.chr_chunks << 2) - 1);
/*TODO*///		for (i = 2; i < 4; i ++)
/*TODO*///			nes_vram[i] = bank * 128 + 64*(i-2);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void chr2_4 (int bank)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		bank &= ((nes.chr_chunks << 2) - 1);
/*TODO*///		for (i = 4; i < 6; i ++)
/*TODO*///			nes_vram[i] = bank * 128 + 64*(i-4);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void chr2_6 (int bank)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		bank &= ((nes.chr_chunks << 2) - 1);
/*TODO*///		for (i = 6; i < 8; i ++)
/*TODO*///			nes_vram[i] = bank * 128 + 64*(i-6);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper1_w (int offset, int data)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///		int reg;
/*TODO*///	
/*TODO*///		/* Find which register we are dealing with */
/*TODO*///		/* Note that there is only one latch and shift counter, shared amongst the 4 regs */
/*TODO*///		/* Space Shuttle will not work if they have independent variables. */
/*TODO*///		reg = (offset >> 13);
/*TODO*///	
/*TODO*///		if ((data & 0x80) != 0)
/*TODO*///		{
/*TODO*///			MMC1_reg_count = 0;
/*TODO*///			MMC1_reg = 0;
/*TODO*///	
/*TODO*///			/* Set these to their defaults - needed for Robocop 3, Dynowars */
/*TODO*///			MMC1_Size_16k = 1;
/*TODO*///			MMC1_Switch_Low = 1;
/*TODO*///			/* TODO: should we switch banks at this time also? */
/*TODO*///	#ifdef LOG_MMC
/*TODO*///			logerror("=== MMC1 regs reset to default\n");
/*TODO*///	#endif
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (MMC1_reg_count < 5)
/*TODO*///		{
/*TODO*///			if (MMC1_reg_count == 0) MMC1_reg = 0;
/*TODO*///			MMC1_reg >>= 1;
/*TODO*///			MMC1_reg |= (data & 0x01) ? 0x10 : 0x00;
/*TODO*///			MMC1_reg_count ++;
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (MMC1_reg_count == 5)
/*TODO*///		{
/*TODO*///	//		logerror("   MMC1 reg#%02x val:%02x\n", offset, MMC1_reg);
/*TODO*///			switch (reg)
/*TODO*///			{
/*TODO*///				case 0:
/*TODO*///					MMC1_Switch_Low = MMC1_reg & 0x04;
/*TODO*///					MMC1_Size_16k =   MMC1_reg & 0x08;
/*TODO*///	
/*TODO*///					switch (MMC1_reg & 0x03)
/*TODO*///					{
/*TODO*///						case 0: ppu_mirror_low (); break;
/*TODO*///						case 1: ppu_mirror_high (); break;
/*TODO*///						case 2: ppu_mirror_v (); break;
/*TODO*///						case 3: ppu_mirror_h (); break;
/*TODO*///					}
/*TODO*///	
/*TODO*///					MMC1_SizeVrom_4k = (MMC1_reg & 0x10);
/*TODO*///	#ifdef LOG_MMC
/*TODO*///					logerror("   MMC1 reg #1 val:%02x\n", MMC1_reg);
/*TODO*///						logerror("\t\tBank Size: ");
/*TODO*///						if (MMC1_Size_16k != 0)
/*TODO*///							logerror("16k\n");
/*TODO*///						else logerror("32k\n");
/*TODO*///	
/*TODO*///						logerror("\t\tBank Select: ");
/*TODO*///						if (MMC1_Switch_Low != 0)
/*TODO*///							logerror("$8000\n");
/*TODO*///						else logerror("$C000\n");
/*TODO*///	
/*TODO*///						logerror("\t\tVROM Bankswitch Size Select: ");
/*TODO*///						if (MMC1_SizeVrom_4k != 0)
/*TODO*///							logerror("4k\n");
/*TODO*///						else logerror("8k\n");
/*TODO*///	
/*TODO*///						logerror ("\t\tMirroring: %d\n", MMC1_reg & 0x03);
/*TODO*///	#endif
/*TODO*///					break;
/*TODO*///	
/*TODO*///				case 1:
/*TODO*///					MMC1_extended_bank = (MMC1_extended_bank & ~0x01) | ((MMC1_reg & 0x10) >> 4);
/*TODO*///					if (MMC1_extended == 2)
/*TODO*///					{
/*TODO*///						/* MMC1_SizeVrom_4k determines if we use the special 256k bank register */
/*TODO*///					 	if (!MMC1_SizeVrom_4k)
/*TODO*///					 	{
/*TODO*///							/* Pick 1st or 4th 256k bank */
/*TODO*///							MMC1_extended_base = 0xc0000 * (MMC1_extended_bank & 0x01) + 0x10000;
/*TODO*///							cpu_setbank (1, &nes.rom[MMC1_extended_base + MMC1_bank1]);
/*TODO*///							cpu_setbank (2, &nes.rom[MMC1_extended_base + MMC1_bank2]);
/*TODO*///							cpu_setbank (3, &nes.rom[MMC1_extended_base + MMC1_bank3]);
/*TODO*///							cpu_setbank (4, &nes.rom[MMC1_extended_base + MMC1_bank4]);
/*TODO*///	#ifdef LOG_MMC
/*TODO*///							logerror("MMC1_extended 1024k bank (no reg) select: %02x\n", MMC1_extended_bank);
/*TODO*///	#endif
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							/* Set 256k bank based on the 256k bank select register */
/*TODO*///							if (MMC1_extended_swap != 0)
/*TODO*///							{
/*TODO*///								MMC1_extended_base = 0x40000 * MMC1_extended_bank + 0x10000;
/*TODO*///								cpu_setbank (1, &nes.rom[MMC1_extended_base + MMC1_bank1]);
/*TODO*///								cpu_setbank (2, &nes.rom[MMC1_extended_base + MMC1_bank2]);
/*TODO*///								cpu_setbank (3, &nes.rom[MMC1_extended_base + MMC1_bank3]);
/*TODO*///								cpu_setbank (4, &nes.rom[MMC1_extended_base + MMC1_bank4]);
/*TODO*///	#ifdef LOG_MMC
/*TODO*///								logerror("MMC1_extended 1024k bank (reg 1) select: %02x\n", MMC1_extended_bank);
/*TODO*///	#endif
/*TODO*///								MMC1_extended_swap = 0;
/*TODO*///							}
/*TODO*///							else MMC1_extended_swap = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else if (MMC1_extended == 1 && nes.chr_chunks == 0)
/*TODO*///					{
/*TODO*///						/* Pick 1st or 2nd 256k bank */
/*TODO*///						MMC1_extended_base = 0x40000 * (MMC1_extended_bank & 0x01) + 0x10000;
/*TODO*///						cpu_setbank (1, &nes.rom[MMC1_extended_base + MMC1_bank1]);
/*TODO*///						cpu_setbank (2, &nes.rom[MMC1_extended_base + MMC1_bank2]);
/*TODO*///						cpu_setbank (3, &nes.rom[MMC1_extended_base + MMC1_bank3]);
/*TODO*///						cpu_setbank (4, &nes.rom[MMC1_extended_base + MMC1_bank4]);
/*TODO*///	#ifdef LOG_MMC
/*TODO*///						logerror("MMC1_extended 512k bank select: %02x\n", MMC1_extended_bank & 0x01);
/*TODO*///	#endif
/*TODO*///					}
/*TODO*///					else if (nes.chr_chunks > 0)
/*TODO*///					{
/*TODO*///	//					logerror("MMC1_SizeVrom_4k: %02x bank:%02x\n", MMC1_SizeVrom_4k, MMC1_reg);
/*TODO*///	
/*TODO*///						if (!MMC1_SizeVrom_4k)
/*TODO*///						{
/*TODO*///							int bank = MMC1_reg & ((nes.chr_chunks << 1) - 1);
/*TODO*///	
/*TODO*///							for (i = 0; i < 8; i ++)
/*TODO*///								nes_vram[i] = bank * 256 + 64*i;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///							logerror("MMC1 8k VROM switch: %02x\n", MMC1_reg);
/*TODO*///	#endif
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							int bank = MMC1_reg & ((nes.chr_chunks << 1) - 1);
/*TODO*///	
/*TODO*///							for (i = 0; i < 4; i ++)
/*TODO*///								nes_vram[i] = bank * 256 + 64*i;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///							logerror("MMC1 4k VROM switch (low): %02x\n", MMC1_reg);
/*TODO*///	#endif
/*TODO*///						}
/*TODO*///					}
/*TODO*///					break;
/*TODO*///				case 2:
/*TODO*///	//				logerror("MMC1_Reg_2: %02x\n",MMC1_Reg_2);
/*TODO*///					MMC1_extended_bank = (MMC1_extended_bank & ~0x02) | ((MMC1_reg & 0x10) >> 3);
/*TODO*///					if (MMC1_extended == 2 && MMC1_SizeVrom_4k)
/*TODO*///					{
/*TODO*///						if (MMC1_extended_swap != 0)
/*TODO*///						{
/*TODO*///							/* Set 256k bank based on the 256k bank select register */
/*TODO*///							MMC1_extended_base = 0x40000 * MMC1_extended_bank + 0x10000;
/*TODO*///							cpu_setbank (1, &nes.rom[MMC1_extended_base + MMC1_bank1]);
/*TODO*///							cpu_setbank (2, &nes.rom[MMC1_extended_base + MMC1_bank2]);
/*TODO*///							cpu_setbank (3, &nes.rom[MMC1_extended_base + MMC1_bank3]);
/*TODO*///							cpu_setbank (4, &nes.rom[MMC1_extended_base + MMC1_bank4]);
/*TODO*///	#ifdef LOG_MMC
/*TODO*///							logerror("MMC1_extended 1024k bank (reg 2) select: %02x\n", MMC1_extended_bank);
/*TODO*///	#endif
/*TODO*///							MMC1_extended_swap = 0;
/*TODO*///						}
/*TODO*///						else
/*TODO*///							MMC1_extended_swap = 1;
/*TODO*///					}
/*TODO*///					if (MMC1_SizeVrom_4k != 0)
/*TODO*///					{
/*TODO*///						int bank = MMC1_reg & ((nes.chr_chunks << 1) - 1);
/*TODO*///	
/*TODO*///						for (i = 4; i < 8; i ++)
/*TODO*///							nes_vram[i] = bank * 256 + 64*(i-4);
/*TODO*///	#ifdef LOG_MMC
/*TODO*///							logerror("MMC1 4k VROM switch (high): %02x\n", MMC1_reg);
/*TODO*///	#endif
/*TODO*///					}
/*TODO*///					break;
/*TODO*///				case 3:
/*TODO*///					/* Switching 1 32k bank of PRG ROM */
/*TODO*///					MMC1_reg &= 0x0f;
/*TODO*///					if (!MMC1_Size_16k)
/*TODO*///					{
/*TODO*///						int bank = MMC1_reg & (nes.prg_chunks - 1);
/*TODO*///	
/*TODO*///						MMC1_bank1 = bank * 0x4000;
/*TODO*///						MMC1_bank2 = bank * 0x4000 + 0x2000;
/*TODO*///						cpu_setbank (1, &nes.rom[MMC1_extended_base + MMC1_bank1]);
/*TODO*///						cpu_setbank (2, &nes.rom[MMC1_extended_base + MMC1_bank2]);
/*TODO*///						if (!MMC1_extended)
/*TODO*///						{
/*TODO*///							MMC1_bank3 = bank * 0x4000 + 0x4000;
/*TODO*///							MMC1_bank4 = bank * 0x4000 + 0x6000;
/*TODO*///							cpu_setbank (3, &nes.rom[MMC1_extended_base + MMC1_bank3]);
/*TODO*///							cpu_setbank (4, &nes.rom[MMC1_extended_base + MMC1_bank4]);
/*TODO*///						}
/*TODO*///	#ifdef LOG_MMC
/*TODO*///						logerror("MMC1 32k bank select: %02x\n", MMC1_reg);
/*TODO*///	#endif
/*TODO*///					}
/*TODO*///					else
/*TODO*///					/* Switching one 16k bank */
/*TODO*///					{
/*TODO*///						if (MMC1_Switch_Low != 0)
/*TODO*///						{
/*TODO*///							int bank = MMC1_reg & (nes.prg_chunks - 1);
/*TODO*///	
/*TODO*///							MMC1_bank1 = bank * 0x4000;
/*TODO*///							MMC1_bank2 = bank * 0x4000 + 0x2000;
/*TODO*///	
/*TODO*///							cpu_setbank (1, &nes.rom[MMC1_extended_base + MMC1_bank1]);
/*TODO*///							cpu_setbank (2, &nes.rom[MMC1_extended_base + MMC1_bank2]);
/*TODO*///							if (!MMC1_extended)
/*TODO*///							{
/*TODO*///								MMC1_bank3 = MMC1_High;
/*TODO*///								MMC1_bank4 = MMC1_High + 0x2000;
/*TODO*///								cpu_setbank (3, &nes.rom[MMC1_extended_base + MMC1_bank3]);
/*TODO*///								cpu_setbank (4, &nes.rom[MMC1_extended_base + MMC1_bank4]);
/*TODO*///							}
/*TODO*///	#ifdef LOG_MMC
/*TODO*///							logerror("MMC1 16k-low bank select: %02x\n", MMC1_reg);
/*TODO*///	#endif
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							int bank = MMC1_reg & (nes.prg_chunks - 1);
/*TODO*///	
/*TODO*///							if (!MMC1_extended)
/*TODO*///							{
/*TODO*///	
/*TODO*///								MMC1_bank1 = 0;
/*TODO*///								MMC1_bank2 = 0x2000;
/*TODO*///								MMC1_bank3 = bank * 0x4000;
/*TODO*///								MMC1_bank4 = bank * 0x4000 + 0x2000;
/*TODO*///	
/*TODO*///								cpu_setbank (1, &nes.rom[MMC1_extended_base + MMC1_bank1]);
/*TODO*///								cpu_setbank (2, &nes.rom[MMC1_extended_base + MMC1_bank2]);
/*TODO*///								cpu_setbank (3, &nes.rom[MMC1_extended_base + MMC1_bank3]);
/*TODO*///								cpu_setbank (4, &nes.rom[MMC1_extended_base + MMC1_bank4]);
/*TODO*///							}
/*TODO*///	#ifdef LOG_MMC
/*TODO*///							logerror("MMC1 16k-high bank select: %02x\n", MMC1_reg);
/*TODO*///	#endif
/*TODO*///						}
/*TODO*///					}
/*TODO*///	
/*TODO*///	#ifdef LOG_MMC
/*TODO*///					logerror("-- page1: %06x\n", MMC1_bank1);
/*TODO*///					logerror("-- page2: %06x\n", MMC1_bank2);
/*TODO*///					logerror("-- page3: %06x\n", MMC1_bank3);
/*TODO*///					logerror("-- page4: %06x\n", MMC1_bank4);
/*TODO*///	#endif
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			MMC1_reg_count = 0;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper2_w (int offset, int data)
/*TODO*///	{
/*TODO*///		prg16_89ab (data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper3_w (int offset, int data)
/*TODO*///	{
/*TODO*///		chr8 (data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper4_set_prg (void)
/*TODO*///	{
/*TODO*///		MMC3_prg0 &= prg_mask;
/*TODO*///		MMC3_prg1 &= prg_mask;
/*TODO*///	
/*TODO*///		if ((MMC3_cmd & 0x40) != 0)
/*TODO*///		{
/*TODO*///			cpu_setbank (1, &nes.rom[(nes.prg_chunks-1) * 0x4000 + 0x10000]);
/*TODO*///			cpu_setbank (3, &nes.rom[0x2000 * (MMC3_prg0) + 0x10000]);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			cpu_setbank (1, &nes.rom[0x2000 * (MMC3_prg0) + 0x10000]);
/*TODO*///			cpu_setbank (3, &nes.rom[(nes.prg_chunks-1) * 0x4000 + 0x10000]);
/*TODO*///		}
/*TODO*///		cpu_setbank (2, &nes.rom[0x2000 * (MMC3_prg1) + 0x10000]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper4_set_chr (void)
/*TODO*///	{
/*TODO*///		UINT8 chr_page = (MMC3_cmd & 0x80) >> 5;
/*TODO*///	
/*TODO*///		nes_vram [chr_page] = MMC3_chr[0];
/*TODO*///		nes_vram [chr_page+1] = nes_vram [chr_page] + 64;
/*TODO*///		nes_vram [chr_page ^ 2] = MMC3_chr[1];
/*TODO*///		nes_vram [(chr_page ^ 2)+1] = nes_vram [chr_page ^ 2] + 64;
/*TODO*///		nes_vram [chr_page ^ 4] = MMC3_chr[2];
/*TODO*///		nes_vram [chr_page ^ 5] = MMC3_chr[3];
/*TODO*///		nes_vram [chr_page ^ 6] = MMC3_chr[4];
/*TODO*///		nes_vram [chr_page ^ 7] = MMC3_chr[5];
/*TODO*///	}
/*TODO*///	
/*TODO*///	int mapper4_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		if ((scanline < BOTTOM_VISIBLE_SCANLINE) || (scanline == ppu_scanlines_per_frame-1))
/*TODO*///		{
/*TODO*///			if ((IRQ_enable) && (PPU_Control1 & 0x18))
/*TODO*///			{
/*TODO*///				if (IRQ_count == 0)
/*TODO*///				{
/*TODO*///					IRQ_count = IRQ_count_latch;
/*TODO*///					ret = M6502_INT_IRQ;
/*TODO*///				}
/*TODO*///				IRQ_count --;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper4_w (int offset, int data)
/*TODO*///	{
/*TODO*///		static UINT8 last_bank = 0xff;
/*TODO*///	
/*TODO*///	//	logerror("mapper4_w offset: %04x, data: %02x, scanline: %d\n", offset, data, current_scanline);
/*TODO*///	
/*TODO*///		switch (offset & 0x7001)
/*TODO*///		{
/*TODO*///			case 0x0000: /* $8000 */
/*TODO*///				MMC3_cmd = data;
/*TODO*///	
/*TODO*///				/* Toggle between switching $8000 and $c000 */
/*TODO*///				if (last_bank != (data & 0xc0))
/*TODO*///				{
/*TODO*///					/* Reset the banks */
/*TODO*///					mapper4_set_prg ();
/*TODO*///					mapper4_set_chr ();
/*TODO*///				}
/*TODO*///				last_bank = data & 0xc0;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x0001: /* $8001 */
/*TODO*///			{
/*TODO*///				UINT8 cmd = MMC3_cmd & 0x07;
/*TODO*///				switch (cmd)
/*TODO*///				{
/*TODO*///					case 0: case 1:
/*TODO*///						data &= 0xfe;
/*TODO*///						MMC3_chr[cmd] = data * 64;
/*TODO*///						mapper4_set_chr ();
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 2: case 3: case 4: case 5:
/*TODO*///						MMC3_chr[cmd] = data * 64;
/*TODO*///						mapper4_set_chr ();
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 6:
/*TODO*///						MMC3_prg0 = data;
/*TODO*///						mapper4_set_prg ();
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 7:
/*TODO*///						MMC3_prg1 = data;
/*TODO*///						mapper4_set_prg ();
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			}
/*TODO*///			case 0x2000: /* $a000 */
/*TODO*///				if ((data & 0x40) != 0)
/*TODO*///					ppu_mirror_high();
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if ((data & 0x01) != 0)
/*TODO*///						ppu_mirror_h();
/*TODO*///					else
/*TODO*///						ppu_mirror_v();
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x2001: /* $a001 - extra RAM enable/disable */
/*TODO*///				nes.mid_ram_enable = data;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 mid_ram enable: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x4000: /* $c000 - IRQ scanline counter */
/*TODO*///				IRQ_count = data;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 set irq count: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x4001: /* $c001 - IRQ scanline latch */
/*TODO*///				IRQ_count_latch = data;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 set irq count latch: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x6000: /* $e000 - Disable IRQs */
/*TODO*///				IRQ_enable = 0;
/*TODO*///				IRQ_count = IRQ_count_latch; /* TODO: verify this */
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 disable irqs: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x6001: /* $e001 - Enable IRQs */
/*TODO*///				IRQ_enable = 1;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 enable irqs: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("mapper4_w uncaught: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper118_w (int offset, int data)
/*TODO*///	{
/*TODO*///		static UINT8 last_bank = 0xff;
/*TODO*///	
/*TODO*///	//	logerror("mapper4_w offset: %04x, data: %02x, scanline: %d\n", offset, data, current_scanline);
/*TODO*///	
/*TODO*///		switch (offset & 0x7001)
/*TODO*///		{
/*TODO*///			case 0x0000: /* $8000 */
/*TODO*///				MMC3_cmd = data;
/*TODO*///	
/*TODO*///				/* Toggle between switching $8000 and $c000 */
/*TODO*///				if (last_bank != (data & 0xc0))
/*TODO*///				{
/*TODO*///					/* Reset the banks */
/*TODO*///					mapper4_set_prg ();
/*TODO*///					mapper4_set_chr ();
/*TODO*///				}
/*TODO*///				last_bank = data & 0xc0;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x0001: /* $8001 */
/*TODO*///			{
/*TODO*///				UINT8 cmd = MMC3_cmd & 0x07;
/*TODO*///				switch (cmd)
/*TODO*///				{
/*TODO*///					case 0: case 1:
/*TODO*///						data &= 0xfe;
/*TODO*///						MMC3_chr[cmd] = data * 64;
/*TODO*///						mapper4_set_chr ();
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 2: case 3: case 4: case 5:
/*TODO*///						MMC3_chr[cmd] = data * 64;
/*TODO*///						mapper4_set_chr ();
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 6:
/*TODO*///						MMC3_prg0 = data;
/*TODO*///						mapper4_set_prg ();
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 7:
/*TODO*///						MMC3_prg1 = data;
/*TODO*///						mapper4_set_prg ();
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			}
/*TODO*///			case 0x2000: /* $a000 */
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     mapper 118 mirroring: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				switch (data & 0x02)
/*TODO*///				{
/*TODO*///					case 0x00: ppu_mirror_low (); break;
/*TODO*///					case 0x01: ppu_mirror_low(); break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x2001: /* $a001 - extra RAM enable/disable */
/*TODO*///				nes.mid_ram_enable = data;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 mid_ram enable: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x4000: /* $c000 - IRQ scanline counter */
/*TODO*///				IRQ_count = data;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 set irq count: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x4001: /* $c001 - IRQ scanline latch */
/*TODO*///				IRQ_count_latch = data;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 set irq count latch: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x6000: /* $e000 - Disable IRQs */
/*TODO*///				IRQ_enable = 0;
/*TODO*///				IRQ_count = IRQ_count_latch; /* TODO: verify this */
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 disable irqs: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x6001: /* $e001 - Enable IRQs */
/*TODO*///				IRQ_enable = 1;
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     MMC3 enable irqs: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("mapper4_w uncaught: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	int mapper5_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///	#if 1
/*TODO*///		if (scanline == 0)
/*TODO*///			IRQ_status |= 0x40;
/*TODO*///		else if (scanline > BOTTOM_VISIBLE_SCANLINE)
/*TODO*///			IRQ_status &= ~0x40;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		if (scanline == IRQ_count)
/*TODO*///		{
/*TODO*///			if (IRQ_enable != 0)
/*TODO*///				ret = M6502_INT_IRQ;
/*TODO*///	
/*TODO*///			IRQ_status = 0xff;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	int mapper5_l_r (int offset)
/*TODO*///	{
/*TODO*///		int retVal;
/*TODO*///	
/*TODO*///	#ifdef MMC5_VRAM
/*TODO*///		/* $5c00 - $5fff: extended videoram attributes */
/*TODO*///		if ((offset >= 0x1b00) && (offset <= 0x1eff))
/*TODO*///		{
/*TODO*///			return MMC5_vram[offset - 0x1b00];
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x1104: /* $5204 */
/*TODO*///	#if 0
/*TODO*///				if (current_scanline == MMC5_scanline) return 0x80;
/*TODO*///				else return 0x00;
/*TODO*///	#else
/*TODO*///				retVal = IRQ_status;
/*TODO*///				IRQ_status &= ~0x80;
/*TODO*///				return retVal;
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x1105: /* $5205 */
/*TODO*///				return (mult1 * mult2) & 0xff;
/*TODO*///				break;
/*TODO*///			case 0x1106: /* $5206 */
/*TODO*///				return ((mult1 * mult2) & 0xff00) >> 8;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("** MMC5 uncaught read, offset: %04x\n", offset + 0x4100);
/*TODO*///				return 0x00;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper5_sync_vrom (int mode)
/*TODO*///	{
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		for (i = 0; i < 8; i ++)
/*TODO*///			nes_vram[i] = vrom_bank[0 + (mode * 8)] * 64;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void mapper5_l_w (int offset, int data)
/*TODO*///	{
/*TODO*///	//	static int vrom_next[4];
/*TODO*///		static int vrom_page_a;
/*TODO*///		static int vrom_page_b;
/*TODO*///	
/*TODO*///	//	logerror("Mapper 5 write, offset: %04x, data: %02x\n", offset + 0x4100, data);
/*TODO*///		/* Send $5000-$5015 to the sound chip */
/*TODO*///		if ((offset >= 0xf00) && (offset <= 0xf15))
/*TODO*///		{
/*TODO*///			NESPSG_0_w (offset & 0x1f, data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///	#ifdef MMC5_VRAM
/*TODO*///		/* $5c00 - $5fff: extended videoram attributes */
/*TODO*///		if ((offset >= 0x1b00) && (offset <= 0x1eff))
/*TODO*///		{
/*TODO*///			if (MMC5_vram_protect == 0x03)
/*TODO*///				MMC5_vram[offset - 0x1b00] = data;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x1000: /* $5100 */
/*TODO*///				MMC5_rom_bank_mode = data & 0x03;
/*TODO*///				logerror ("MMC5 rom bank mode: %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x1001: /* $5101 */
/*TODO*///				MMC5_vrom_bank_mode = data & 0x03;
/*TODO*///				logerror ("MMC5 vrom bank mode: %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x1002: /* $5102 */
/*TODO*///				if (data == 0x02)
/*TODO*///					MMC5_vram_protect |= 1;
/*TODO*///				else
/*TODO*///					MMC5_vram_protect = 0;
/*TODO*///				logerror ("MMC5 vram protect 1: %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x1003: /* 5103 */
/*TODO*///				if (data == 0x01)
/*TODO*///					MMC5_vram_protect |= 2;
/*TODO*///				else
/*TODO*///					MMC5_vram_protect = 0;
/*TODO*///				logerror ("MMC5 vram protect 2: %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x1004: /* $5104 - Extra VRAM (EXRAM) control */
/*TODO*///				MMC5_vram_control = data;
/*TODO*///				logerror ("MMC5 exram control: %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x1005: /* $5105 */
/*TODO*///				ppu_mirror_custom (0, data & 0x03);
/*TODO*///				ppu_mirror_custom (1, (data & 0x0c) >> 2);
/*TODO*///				ppu_mirror_custom (2, (data & 0x30) >> 4);
/*TODO*///				ppu_mirror_custom (3, (data & 0xc0) >> 6);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* $5106 - ?? cv3 sets to 0 */
/*TODO*///			/* $5107 - ?? cv3 sets to 0 */
/*TODO*///	
/*TODO*///			case 0x1013: /* $5113 */
/*TODO*///				logerror ("MMC5 mid RAM bank select: %02x\n", data & 0x07);
/*TODO*///				cpu_setbank (5, &nes.wram[data * 0x2000]);
/*TODO*///				/* The & 4 is a hack that'll tide us over for now */
/*TODO*///				battery_ram = &nes.wram[(data & 4) * 0x2000];
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x1014: /* $5114 */
/*TODO*///				logerror ("MMC5 $5114 bank select: %02x (mode: %d)\n", data, MMC5_rom_bank_mode);
/*TODO*///				switch (MMC5_rom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x03:
/*TODO*///						/* 8k switch */
/*TODO*///						if ((data & 0x80) != 0)
/*TODO*///						{
/*TODO*///							/* ROM */
/*TODO*///							logerror ("\tROM bank select (8k, $8000): %02x\n", data);
/*TODO*///							data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///							cpu_setbank (1, &nes.rom[data * 0x2000 + 0x10000]);
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							/* RAM */
/*TODO*///							logerror ("\tRAM bank select (8k, $8000): %02x\n", data & 0x07);
/*TODO*///							/* The & 4 is a hack that'll tide us over for now */
/*TODO*///							cpu_setbank (1, &nes.wram[(data & 4) * 0x2000]);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1015: /* $5115 */
/*TODO*///				logerror ("MMC5 $5115 bank select: %02x (mode: %d)\n", data, MMC5_rom_bank_mode);
/*TODO*///				switch (MMC5_rom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x01:
/*TODO*///					case 0x02:
/*TODO*///						if ((data & 0x80) != 0)
/*TODO*///						{
/*TODO*///							/* 16k switch - ROM only */
/*TODO*///							prg16_89ab ((data & 0x7f) >> 1);
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							/* RAM */
/*TODO*///							logerror ("\tRAM bank select (16k, $8000): %02x\n", data & 0x07);
/*TODO*///							/* The & 4 is a hack that'll tide us over for now */
/*TODO*///							cpu_setbank (1, &nes.wram[((data & 4) >> 1) * 0x4000]);
/*TODO*///							cpu_setbank (2, &nes.wram[((data & 4) >> 1) * 0x4000 + 0x2000]);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///					case 0x03:
/*TODO*///						/* 8k switch */
/*TODO*///						if ((data & 0x80) != 0)
/*TODO*///						{
/*TODO*///							/* ROM */
/*TODO*///							data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///							cpu_setbank (2, &nes.rom[data * 0x2000 + 0x10000]);
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							/* RAM */
/*TODO*///							logerror ("\tRAM bank select (8k, $a000): %02x\n", data & 0x07);
/*TODO*///							/* The & 4 is a hack that'll tide us over for now */
/*TODO*///							cpu_setbank (2, &nes.wram[(data & 4) * 0x2000]);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1016: /* $5116 */
/*TODO*///				logerror ("MMC5 $5116 bank select: %02x (mode: %d)\n", data, MMC5_rom_bank_mode);
/*TODO*///				switch (MMC5_rom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x02:
/*TODO*///					case 0x03:
/*TODO*///						/* 8k switch */
/*TODO*///						if ((data & 0x80) != 0)
/*TODO*///						{
/*TODO*///							/* ROM */
/*TODO*///							data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///							cpu_setbank (3, &nes.rom[data * 0x2000 + 0x10000]);
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							/* RAM */
/*TODO*///							logerror ("\tRAM bank select (8k, $c000): %02x\n", data & 0x07);
/*TODO*///							/* The & 4 is a hack that'll tide us over for now */
/*TODO*///							cpu_setbank (3, &nes.wram[(data & 4)* 0x2000]);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1017: /* $5117 */
/*TODO*///				logerror ("MMC5 $5117 bank select: %02x (mode: %d)\n", data, MMC5_rom_bank_mode);
/*TODO*///				switch (MMC5_rom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x00:
/*TODO*///						/* 32k switch - ROM only */
/*TODO*///						prg32 (data >> 2);
/*TODO*///						break;
/*TODO*///					case 0x01:
/*TODO*///						/* 16k switch - ROM only */
/*TODO*///						prg16_cdef (data >> 1);
/*TODO*///						break;
/*TODO*///					case 0x02:
/*TODO*///					case 0x03:
/*TODO*///						/* 8k switch */
/*TODO*///						data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///						cpu_setbank (4, &nes.rom[data * 0x2000 + 0x10000]);
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1020: /* $5120 */
/*TODO*///				logerror ("MMC5 $5120 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[0] = data;
/*TODO*///	//					mapper5_sync_vrom(0);
/*TODO*///						nes_vram[0] = vrom_bank[0] * 64;
/*TODO*///	//					nes_vram_sprite[0] = vrom_bank[0] * 64;
/*TODO*///	//					vrom_next[0] = 4;
/*TODO*///	//					vrom_page_a = 1;
/*TODO*///	//					vrom_page_b = 0;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1021: /* $5121 */
/*TODO*///				logerror ("MMC5 $5121 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x02:
/*TODO*///						/* 2k switch */
/*TODO*///						chr2_0 (data);
/*TODO*///						break;
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[1] = data;
/*TODO*///	//					mapper5_sync_vrom(0);
/*TODO*///						nes_vram[1] = vrom_bank[1] * 64;
/*TODO*///	//					nes_vram_sprite[1] = vrom_bank[0] * 64;
/*TODO*///	//					vrom_next[1] = 5;
/*TODO*///	//					vrom_page_a = 1;
/*TODO*///	//					vrom_page_b = 0;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1022: /* $5122 */
/*TODO*///				logerror ("MMC5 $5122 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[2] = data;
/*TODO*///	//					mapper5_sync_vrom(0);
/*TODO*///						nes_vram[2] = vrom_bank[2] * 64;
/*TODO*///	//					nes_vram_sprite[2] = vrom_bank[0] * 64;
/*TODO*///	//					vrom_next[2] = 6;
/*TODO*///	//					vrom_page_a = 1;
/*TODO*///	//					vrom_page_b = 0;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1023: /* $5123 */
/*TODO*///				logerror ("MMC5 $5123 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x01:
/*TODO*///						chr4_0 (data);
/*TODO*///						break;
/*TODO*///					case 0x02:
/*TODO*///						/* 2k switch */
/*TODO*///						chr2_2 (data);
/*TODO*///						break;
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[3] = data;
/*TODO*///	//					mapper5_sync_vrom(0);
/*TODO*///						nes_vram[3] = vrom_bank[3] * 64;
/*TODO*///	//					nes_vram_sprite[3] = vrom_bank[0] * 64;
/*TODO*///	//					vrom_next[3] = 7;
/*TODO*///	//					vrom_page_a = 1;
/*TODO*///	//					vrom_page_b = 0;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1024: /* $5124 */
/*TODO*///				logerror ("MMC5 $5124 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[4] = data;
/*TODO*///	//					mapper5_sync_vrom(0);
/*TODO*///						nes_vram[4] = vrom_bank[4] * 64;
/*TODO*///	//					nes_vram_sprite[4] = vrom_bank[0] * 64;
/*TODO*///	//					vrom_next[0] = 0;
/*TODO*///	//					vrom_page_a = 0;
/*TODO*///	//					vrom_page_b = 0;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1025: /* $5125 */
/*TODO*///				logerror ("MMC5 $5125 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x02:
/*TODO*///						/* 2k switch */
/*TODO*///						chr2_4 (data);
/*TODO*///						break;
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[5] = data;
/*TODO*///	//					mapper5_sync_vrom(0);
/*TODO*///						nes_vram[5] = vrom_bank[5] * 64;
/*TODO*///	//					nes_vram_sprite[5] = vrom_bank[0] * 64;
/*TODO*///	//					vrom_next[1] = 1;
/*TODO*///	//					vrom_page_a = 0;
/*TODO*///	//					vrom_page_b = 0;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1026: /* $5126 */
/*TODO*///				logerror ("MMC5 $5126 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[6] = data;
/*TODO*///	//					mapper5_sync_vrom(0);
/*TODO*///						nes_vram[6] = vrom_bank[6] * 64;
/*TODO*///	//					nes_vram_sprite[6] = vrom_bank[0] * 64;
/*TODO*///	//					vrom_next[2] = 2;
/*TODO*///	//					vrom_page_a = 0;
/*TODO*///	//					vrom_page_b = 0;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1027: /* $5127 */
/*TODO*///				logerror ("MMC5 $5127 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x00:
/*TODO*///						/* 8k switch */
/*TODO*///						chr8 (data);
/*TODO*///						break;
/*TODO*///					case 0x01:
/*TODO*///						/* 4k switch */
/*TODO*///						chr4_4 (data);
/*TODO*///						break;
/*TODO*///					case 0x02:
/*TODO*///						/* 2k switch */
/*TODO*///						chr2_6 (data);
/*TODO*///						break;
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[7] = data;
/*TODO*///	//					mapper5_sync_vrom(0);
/*TODO*///						nes_vram[7] = vrom_bank[7] * 64;
/*TODO*///	//					nes_vram_sprite[7] = vrom_bank[0] * 64;
/*TODO*///	//					vrom_next[3] = 3;
/*TODO*///	//					vrom_page_a = 0;
/*TODO*///	//					vrom_page_b = 0;
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1028: /* $5128 */
/*TODO*///				logerror ("MMC5 $5128 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[8] = vrom_bank[12] = data;
/*TODO*///	//					nes_vram[vrom_next[0]] = data * 64;
/*TODO*///	//					nes_vram[0 + (vrom_page_a*4)] = data * 64;
/*TODO*///	//					nes_vram[0] = data * 64;
/*TODO*///						nes_vram[4] = data * 64;
/*TODO*///	//					mapper5_sync_vrom(1);
/*TODO*///						if (!vrom_page_b)
/*TODO*///						{
/*TODO*///							vrom_page_a ^= 0x01;
/*TODO*///							vrom_page_b = 1;
/*TODO*///						}
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1029: /* $5129 */
/*TODO*///				logerror ("MMC5 $5129 vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x02:
/*TODO*///						/* 2k switch */
/*TODO*///						chr2_0 (data);
/*TODO*///						chr2_4 (data);
/*TODO*///						break;
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[9] = vrom_bank[13] = data;
/*TODO*///	//					nes_vram[vrom_next[1]] = data * 64;
/*TODO*///	//					nes_vram[1 + (vrom_page_a*4)] = data * 64;
/*TODO*///	//					nes_vram[1] = data * 64;
/*TODO*///						nes_vram[5] = data * 64;
/*TODO*///	//					mapper5_sync_vrom(1);
/*TODO*///						if (!vrom_page_b)
/*TODO*///						{
/*TODO*///							vrom_page_a ^= 0x01;
/*TODO*///							vrom_page_b = 1;
/*TODO*///						}
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x102a: /* $512a */
/*TODO*///				logerror ("MMC5 $512a vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[10] = vrom_bank[14] = data;
/*TODO*///	//					nes_vram[vrom_next[2]] = data * 64;
/*TODO*///	//					nes_vram[2 + (vrom_page_a*4)] = data * 64;
/*TODO*///	//					nes_vram[2] = data * 64;
/*TODO*///						nes_vram[6] = data * 64;
/*TODO*///	//					mapper5_sync_vrom(1);
/*TODO*///						if (!vrom_page_b)
/*TODO*///						{
/*TODO*///							vrom_page_a ^= 0x01;
/*TODO*///							vrom_page_b = 1;
/*TODO*///						}
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x102b: /* $512b */
/*TODO*///				logerror ("MMC5 $512b vrom select: %02x (mode: %d)\n", data, MMC5_vrom_bank_mode);
/*TODO*///				switch (MMC5_vrom_bank_mode)
/*TODO*///				{
/*TODO*///					case 0x00:
/*TODO*///						/* 8k switch */
/*TODO*///						chr8 (data);
/*TODO*///						break;
/*TODO*///					case 0x01:
/*TODO*///						/* 4k switch */
/*TODO*///						chr4_0 (data);
/*TODO*///						chr4_4 (data);
/*TODO*///						break;
/*TODO*///					case 0x02:
/*TODO*///						/* 2k switch */
/*TODO*///						chr2_2 (data);
/*TODO*///						chr2_6 (data);
/*TODO*///						break;
/*TODO*///					case 0x03:
/*TODO*///						/* 1k switch */
/*TODO*///						vrom_bank[11] = vrom_bank[15] = data;
/*TODO*///	//					nes_vram[vrom_next[3]] = data * 64;
/*TODO*///	//					nes_vram[3 + (vrom_page_a*4)] = data * 64;
/*TODO*///	//					nes_vram[3] = data * 64;
/*TODO*///						nes_vram[7] = data * 64;
/*TODO*///	//					mapper5_sync_vrom(1);
/*TODO*///						if (!vrom_page_b)
/*TODO*///						{
/*TODO*///							vrom_page_a ^= 0x01;
/*TODO*///							vrom_page_b = 1;
/*TODO*///						}
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x1103: /* $5203 */
/*TODO*///				IRQ_count = data;
/*TODO*///				MMC5_scanline = data;
/*TODO*///				logerror ("MMC5 irq scanline: %d\n", IRQ_count);
/*TODO*///				break;
/*TODO*///			case 0x1104: /* $5204 */
/*TODO*///				IRQ_enable = data & 0x80;
/*TODO*///				logerror ("MMC5 irq enable: %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x1105: /* $5205 */
/*TODO*///				mult1 = data;
/*TODO*///				break;
/*TODO*///			case 0x1106: /* $5206 */
/*TODO*///				mult2 = data;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("** MMC5 uncaught write, offset: %04x, data: %02x\n", offset + 0x4100, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void mapper5_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("MMC5 uncaught high mapper w, %04x: %02x\n", offset, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	void mapper7_w (int offset, int data)
/*TODO*///	{
/*TODO*///		if ((data & 0x10) != 0)
/*TODO*///			ppu_mirror_high ();
/*TODO*///		else
/*TODO*///			ppu_mirror_low ();
/*TODO*///	
/*TODO*///		prg32 (data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper8_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("* Mapper 8 switch, vrom: %02x, rom: %02x\n", data & 0x07, (data >> 3));
/*TODO*///	#endif
/*TODO*///		/* Switch 8k VROM bank */
/*TODO*///		chr8 (data & 0x07);
/*TODO*///	
/*TODO*///		/* Switch 16k PRG bank */
/*TODO*///		data = (data >> 3) & (nes.prg_chunks - 1);
/*TODO*///		cpu_setbank (1, &nes.rom[data * 0x4000 + 0x10000]);
/*TODO*///		cpu_setbank (2, &nes.rom[data * 0x4000 + 0x12000]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	#if 0
/*TODO*///	void mapper9_latch (int offset)
/*TODO*///	{
/*TODO*///		if (((offset & 0x3ff0) == 0x0fd0) || ((offset & 0x3ff0) == 0x1fd0))
/*TODO*///		{
/*TODO*///			logerror ("mapper9 vrom switch (latch low): %02x\n", MMC2_bank1);
/*TODO*///			MMC2_bank1_latch = 0xfd;
/*TODO*///			chr4_4 (MMC2_bank1);
/*TODO*///		}
/*TODO*///		else if (((offset & 0x3ff0) == 0x0fe0) || ((offset & 0x3ff0) == 0x1fe0))
/*TODO*///		{
/*TODO*///			logerror ("mapper9 vrom switch (latch high): %02x\n", MMC2_bank1_hi);
/*TODO*///			MMC2_bank1_latch = 0xfe;
/*TODO*///			chr4_4 (MMC2_bank1_hi);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper9_w (int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset & 0x7000)
/*TODO*///		{
/*TODO*///			case 0x2000:
/*TODO*///				cpu_setbank (1, &nes.rom[data * 0x2000 + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x3000:
/*TODO*///			case 0x4000:
/*TODO*///				MMC2_bank0 = data;
/*TODO*///				chr4_0 (MMC2_bank0);
/*TODO*///				logerror("MMC2 VROM switch #1 (low): %02x\n", MMC2_bank0);
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				MMC2_bank1 = data;
/*TODO*///				if (MMC2_bank1_latch == 0xfd)
/*TODO*///					chr4_4 (MMC2_bank1);
/*TODO*///				logerror("MMC2 VROM switch #2 (low): %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				MMC2_bank1_hi = data;
/*TODO*///				if (MMC2_bank1_latch == 0xfe)
/*TODO*///					chr4_4 (MMC2_bank1_hi);
/*TODO*///				logerror("MMC2 VROM switch #2 (high): %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				if (data != 0)
/*TODO*///					ppu_mirror_h ();
/*TODO*///				else
/*TODO*///					ppu_mirror_v ();
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("MMC2 uncaught w: %04x:%02x\n", offset, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	void mapper10_latch (int offset)
/*TODO*///	{
/*TODO*///		if ((offset & 0x3ff0) == 0x0fd0)
/*TODO*///		{
/*TODO*///			logerror ("mapper10 vrom latch switch (bank 0 low): %02x\n", MMC2_bank0);
/*TODO*///			MMC2_bank0_latch = 0xfd;
/*TODO*///			chr4_0 (MMC2_bank0);
/*TODO*///		}
/*TODO*///		else if ((offset & 0x3ff0) == 0x0fe0)
/*TODO*///		{
/*TODO*///			logerror ("mapper10 vrom latch switch (bank 0 high): %02x\n", MMC2_bank0_hi);
/*TODO*///			MMC2_bank0_latch = 0xfe;
/*TODO*///			chr4_0 (MMC2_bank0_hi);
/*TODO*///		}
/*TODO*///		else if ((offset & 0x3ff0) == 0x1fd0)
/*TODO*///		{
/*TODO*///			logerror ("mapper10 vrom latch switch (bank 1 low): %02x\n", MMC2_bank1);
/*TODO*///			MMC2_bank1_latch = 0xfd;
/*TODO*///			chr4_4 (MMC2_bank1);
/*TODO*///		}
/*TODO*///		else if ((offset & 0x3ff0) == 0x1fe0)
/*TODO*///		{
/*TODO*///			logerror ("mapper10 vrom latch switch (bank 0 high): %02x\n", MMC2_bank1_hi);
/*TODO*///			MMC2_bank1_latch = 0xfe;
/*TODO*///			chr4_4 (MMC2_bank1_hi);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	void mapper10_w (int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset & 0x7000)
/*TODO*///		{
/*TODO*///			case 0x2000:
/*TODO*///				/* Switch the first 8k prg bank */
/*TODO*///				cpu_setbank (1, &nes.rom[data * 0x2000 + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x3000:
/*TODO*///				MMC2_bank0 = data;
/*TODO*///				if (MMC2_bank0_latch == 0xfd)
/*TODO*///					chr4_0 (MMC2_bank0);
/*TODO*///				logerror("MMC2 VROM switch #1 (low): %02x\n", MMC2_bank0);
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				MMC2_bank0_hi = data;
/*TODO*///				if (MMC2_bank0_latch == 0xfe)
/*TODO*///					chr4_0 (MMC2_bank0_hi);
/*TODO*///				logerror("MMC2 VROM switch #1 (high): %02x\n", MMC2_bank0_hi);
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				MMC2_bank1 = data;
/*TODO*///				if (MMC2_bank1_latch == 0xfd)
/*TODO*///					chr4_4 (MMC2_bank1);
/*TODO*///				logerror("MMC2 VROM switch #2 (low): %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				MMC2_bank1_hi = data;
/*TODO*///				if (MMC2_bank1_latch == 0xfe)
/*TODO*///					chr4_4 (MMC2_bank1_hi);
/*TODO*///				logerror("MMC2 VROM switch #2 (high): %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				if (data != 0)
/*TODO*///					ppu_mirror_h ();
/*TODO*///				else
/*TODO*///					ppu_mirror_v ();
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("MMC4 uncaught w: %04x:%02x\n", offset, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper11_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("* Mapper 11 switch, data: %02x\n", data);
/*TODO*///	#endif
/*TODO*///		/* Switch 8k VROM bank */
/*TODO*///		chr8 (data >> 4);
/*TODO*///	
/*TODO*///		/* Switch 32k prg bank */
/*TODO*///		prg32 (data & 0x0f);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper15_w (int offset, int data)
/*TODO*///	{
/*TODO*///		int bank = (data & (nes.prg_chunks - 1)) * 0x4000;
/*TODO*///		int base = data & 0x80 ? 0x12000 : 0x10000;
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				if ((data & 0x40) != 0)
/*TODO*///					ppu_mirror_h ();
/*TODO*///				else
/*TODO*///					ppu_mirror_v ();
/*TODO*///				cpu_setbank (1, &nes.rom[bank + base]);
/*TODO*///				cpu_setbank (2, &nes.rom[bank + (base ^ 0x2000)]);
/*TODO*///				cpu_setbank (3, &nes.rom[bank + (base ^ 0x4000)]);
/*TODO*///				cpu_setbank (4, &nes.rom[bank + (base ^ 0x6000)]);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x0001:
/*TODO*///				cpu_setbank (3, &nes.rom[bank + base]);
/*TODO*///				cpu_setbank (4, &nes.rom[bank + (base ^ 0x2000)]);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x0002:
/*TODO*///				cpu_setbank (1, &nes.rom[bank + base]);
/*TODO*///				cpu_setbank (2, &nes.rom[bank + base]);
/*TODO*///				cpu_setbank (3, &nes.rom[bank + base]);
/*TODO*///				cpu_setbank (4, &nes.rom[bank + base]);
/*TODO*///	        	break;
/*TODO*///	
/*TODO*///			case 0x0003:
/*TODO*///				if ((data & 0x40) != 0)
/*TODO*///					ppu_mirror_h ();
/*TODO*///				else
/*TODO*///					ppu_mirror_v ();
/*TODO*///				cpu_setbank (3, &nes.rom[bank + base]);
/*TODO*///				cpu_setbank (4, &nes.rom[bank + (base ^ 0x2000)]);
/*TODO*///	        	break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	int bandai_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		/* 114 is the number of cycles per scanline */
/*TODO*///		/* TODO: change to reflect the actual number of cycles spent */
/*TODO*///	
/*TODO*///		if (IRQ_enable != 0)
/*TODO*///		{
/*TODO*///			if (IRQ_count <= 114)
/*TODO*///			{
/*TODO*///				ret = M6502_INT_IRQ;
/*TODO*///			}
/*TODO*///			IRQ_count -= 114;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper16_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror ("mapper16 (mid and high) w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		switch (offset & 0x000f)
/*TODO*///		{
/*TODO*///			case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
/*TODO*///				/* Switch 1k VROM at $0000 - $1fff */
/*TODO*///				nes_vram [offset & 0x0f] = data * 64;
/*TODO*///				break;
/*TODO*///			case 8:
/*TODO*///				/* Switch 16k bank at $8000 */
/*TODO*///				prg16_89ab (data);
/*TODO*///				break;
/*TODO*///			case 9:
/*TODO*///				switch (data & 0x03)
/*TODO*///				{
/*TODO*///					case 0: ppu_mirror_h (); break;
/*TODO*///					case 1: ppu_mirror_v (); break;
/*TODO*///					case 2: ppu_mirror_low (); break;
/*TODO*///					case 3: ppu_mirror_high (); break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x0a:
/*TODO*///				IRQ_enable = data & 0x01;
/*TODO*///				break;
/*TODO*///			case 0x0b:
/*TODO*///				IRQ_count &= 0xff00;
/*TODO*///				IRQ_count |= data;
/*TODO*///				break;
/*TODO*///			case 0x0c:
/*TODO*///				IRQ_count &= 0x00ff;
/*TODO*///				IRQ_count |= (data << 8);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("** uncaught mapper 16 write, offset: %04x, data: %02x\n", offset, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper17_l_w (int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			/* $42fe - mirroring */
/*TODO*///			case 0x1fe:
/*TODO*///				if ((data & 0x10) != 0)
/*TODO*///					ppu_mirror_low ();
/*TODO*///				else
/*TODO*///					ppu_mirror_high ();
/*TODO*///				break;
/*TODO*///			/* $42ff - mirroring */
/*TODO*///			case 0x1ff:
/*TODO*///				if ((data & 0x10) != 0)
/*TODO*///					ppu_mirror_h ();
/*TODO*///				else
/*TODO*///					ppu_mirror_v ();
/*TODO*///				break;
/*TODO*///			/* $4501 - $4503 */
/*TODO*///	//		case 0x401:
/*TODO*///	//		case 0x402:
/*TODO*///	//		case 0x403:
/*TODO*///				/* IRQ control */
/*TODO*///	//			break;
/*TODO*///			/* $4504 - $4507 : 8k PRG-Rom switch */
/*TODO*///			case 0x404:
/*TODO*///			case 0x405:
/*TODO*///			case 0x406:
/*TODO*///			case 0x407:
/*TODO*///				data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///	//			logerror("Mapper 17 bank switch, bank: %02x, data: %02x\n", offset & 0x03, data);
/*TODO*///				cpu_setbank ((offset & 0x03) + 1, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///				break;
/*TODO*///			/* $4510 - $4517 : 1k CHR-Rom switch */
/*TODO*///			case 0x410:
/*TODO*///			case 0x411:
/*TODO*///			case 0x412:
/*TODO*///			case 0x413:
/*TODO*///			case 0x414:
/*TODO*///			case 0x415:
/*TODO*///			case 0x416:
/*TODO*///			case 0x417:
/*TODO*///				data &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[offset & 0x07] = data * 64;
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("** uncaught mapper 17 write, offset: %04x, data: %02x\n", offset, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	int jaleco_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		if (scanline <= BOTTOM_VISIBLE_SCANLINE)
/*TODO*///		{
/*TODO*///			/* Increment & check the IRQ scanline counter */
/*TODO*///			if (IRQ_enable != 0)
/*TODO*///			{
/*TODO*///				IRQ_count -= 0x100;
/*TODO*///	
/*TODO*///				logerror ("scanline: %d, irq count: %04x\n", scanline, IRQ_count);
/*TODO*///				if ((IRQ_mode_jaleco & 0x08) != 0)
/*TODO*///				{
/*TODO*///					if ((IRQ_count & 0x0f) == 0x00)
/*TODO*///						/* rollover every 0x10 */
/*TODO*///						ret = M6502_INT_IRQ;
/*TODO*///				}
/*TODO*///				else if ((IRQ_mode_jaleco & 0x04) != 0)
/*TODO*///				{
/*TODO*///					if ((IRQ_count & 0x0ff) == 0x00)
/*TODO*///						/* rollover every 0x100 */
/*TODO*///						ret = M6502_INT_IRQ;
/*TODO*///				}
/*TODO*///				else if ((IRQ_mode_jaleco & 0x02) != 0)
/*TODO*///				{
/*TODO*///					if ((IRQ_count & 0x0fff) == 0x000)
/*TODO*///						/* rollover every 0x1000 */
/*TODO*///						ret = M6502_INT_IRQ;
/*TODO*///				}
/*TODO*///				else if (IRQ_count == 0)
/*TODO*///					/* rollover at 0x10000 */
/*TODO*///					ret = M6502_INT_IRQ;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			IRQ_count = IRQ_count_latch;
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void mapper18_w (int offset, int data)
/*TODO*///	{
/*TODO*///	//	static int irq;
/*TODO*///		static int bank_8000 = 0;
/*TODO*///		static int bank_a000 = 0;
/*TODO*///		static int bank_c000 = 0;
/*TODO*///	
/*TODO*///		switch (offset & 0x7003)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				/* Switch 8k bank at $8000 - low 4 bits */
/*TODO*///				bank_8000 = (bank_8000 & 0xf0) | (data & 0x0f);
/*TODO*///				bank_8000 &= prg_mask;
/*TODO*///				cpu_setbank (1, &nes.rom[0x2000 * (bank_8000) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x0001:
/*TODO*///				/* Switch 8k bank at $8000 - high 4 bits */
/*TODO*///				bank_8000 = (bank_8000 & 0x0f) | (data << 4);
/*TODO*///				bank_8000 &= prg_mask;
/*TODO*///				cpu_setbank (1, &nes.rom[0x2000 * (bank_8000) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x0002:
/*TODO*///				/* Switch 8k bank at $a000 - low 4 bits */
/*TODO*///				bank_a000 = (bank_a000 & 0xf0) | (data & 0x0f);
/*TODO*///				bank_a000 &= prg_mask;
/*TODO*///				cpu_setbank (2, &nes.rom[0x2000 * (bank_a000) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x0003:
/*TODO*///				/* Switch 8k bank at $a000 - high 4 bits */
/*TODO*///				bank_a000 = (bank_a000 & 0x0f) | (data << 4);
/*TODO*///				bank_a000 &= prg_mask;
/*TODO*///				cpu_setbank (2, &nes.rom[0x2000 * (bank_a000) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x1000:
/*TODO*///				/* Switch 8k bank at $c000 - low 4 bits */
/*TODO*///				bank_c000 = (bank_c000 & 0xf0) | (data & 0x0f);
/*TODO*///				bank_c000 &= prg_mask;
/*TODO*///				cpu_setbank (3, &nes.rom[0x2000 * (bank_c000) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x1001:
/*TODO*///				/* Switch 8k bank at $c000 - high 4 bits */
/*TODO*///				bank_c000 = (bank_c000 & 0x0f) | (data << 4);
/*TODO*///				bank_c000 &= prg_mask;
/*TODO*///				cpu_setbank (3, &nes.rom[0x2000 * (bank_c000) + 0x10000]);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* $9002, 3 (1002, 3) uncaught = Jaleco Baseball writes 0 */
/*TODO*///			/* believe it's related to battery-backed ram enable/disable */
/*TODO*///	
/*TODO*///			case 0x2000:
/*TODO*///				/* Switch 1k vrom at $0000 - low 4 bits */
/*TODO*///				vrom_bank[0] = (vrom_bank[0] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[0] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[0] = vrom_bank[0] * 64;
/*TODO*///				break;
/*TODO*///			case 0x2001:
/*TODO*///				/* Switch 1k vrom at $0000 - high 4 bits */
/*TODO*///				vrom_bank[0] = (vrom_bank[0] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[0] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[0] = vrom_bank[0] * 64;
/*TODO*///				break;
/*TODO*///			case 0x2002:
/*TODO*///				/* Switch 1k vrom at $0400 - low 4 bits */
/*TODO*///				vrom_bank[1] = (vrom_bank[1] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[1] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[1] = vrom_bank[1] * 64;
/*TODO*///				break;
/*TODO*///			case 0x2003:
/*TODO*///				/* Switch 1k vrom at $0400 - high 4 bits */
/*TODO*///				vrom_bank[1] = (vrom_bank[1] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[1] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[1] = vrom_bank[1] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3000:
/*TODO*///				/* Switch 1k vrom at $0800 - low 4 bits */
/*TODO*///				vrom_bank[2] = (vrom_bank[2] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[2] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[2] = vrom_bank[2] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3001:
/*TODO*///				/* Switch 1k vrom at $0800 - high 4 bits */
/*TODO*///				vrom_bank[2] = (vrom_bank[2] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[2] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[2] = vrom_bank[2] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3002:
/*TODO*///				/* Switch 1k vrom at $0c00 - low 4 bits */
/*TODO*///				vrom_bank[3] = (vrom_bank[3] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[3] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[3] = vrom_bank[3] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3003:
/*TODO*///				/* Switch 1k vrom at $0c00 - high 4 bits */
/*TODO*///				vrom_bank[3] = (vrom_bank[3] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[3] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[3] = vrom_bank[3] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				/* Switch 1k vrom at $1000 - low 4 bits */
/*TODO*///				vrom_bank[4] = (vrom_bank[4] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[4] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[4] = vrom_bank[4] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4001:
/*TODO*///				/* Switch 1k vrom at $1000 - high 4 bits */
/*TODO*///				vrom_bank[4] = (vrom_bank[4] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[4] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[4] = vrom_bank[4] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4002:
/*TODO*///				/* Switch 1k vrom at $1400 - low 4 bits */
/*TODO*///				vrom_bank[5] = (vrom_bank[5] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[5] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[5] = vrom_bank[5] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4003:
/*TODO*///				/* Switch 1k vrom at $1400 - high 4 bits */
/*TODO*///				vrom_bank[5] = (vrom_bank[5] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[5] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[5] = vrom_bank[5] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				/* Switch 1k vrom at $1800 - low 4 bits */
/*TODO*///				vrom_bank[6] = (vrom_bank[6] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[6] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[6] = vrom_bank[6] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5001:
/*TODO*///				/* Switch 1k vrom at $1800 - high 4 bits */
/*TODO*///				vrom_bank[6] = (vrom_bank[6] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[6] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[6] = vrom_bank[6] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5002:
/*TODO*///				/* Switch 1k vrom at $1c00 - low 4 bits */
/*TODO*///				vrom_bank[7] = (vrom_bank[7] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[7] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[7] = vrom_bank[7] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5003:
/*TODO*///				/* Switch 1k vrom at $1c00 - high 4 bits */
/*TODO*///				vrom_bank[7] = (vrom_bank[7] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[7] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[7] = vrom_bank[7] * 64;
/*TODO*///				break;
/*TODO*///	
/*TODO*///	/* LBO - these are unverified */
/*TODO*///	
/*TODO*///			case 0x6000: /* IRQ scanline counter - low byte, low nibble */
/*TODO*///				IRQ_count_latch = (IRQ_count_latch & 0xfff0) | (data & 0x0f);
/*TODO*///				logerror("     Mapper 18 copy/set irq latch (l-l): %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x6001: /* IRQ scanline counter - low byte, high nibble */
/*TODO*///				IRQ_count_latch = (IRQ_count_latch & 0xff0f) | ((data & 0x0f) << 4);
/*TODO*///				logerror("     Mapper 18 copy/set irq latch (l-h): %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x6002:
/*TODO*///				IRQ_count_latch = (IRQ_count_latch & 0xf0ff) | ((data & 0x0f) << 9);
/*TODO*///				logerror("     Mapper 18 copy/set irq latch (h-l): %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x6003:
/*TODO*///				IRQ_count_latch = (IRQ_count_latch & 0x0fff) | ((data & 0x0f) << 13);
/*TODO*///				logerror("     Mapper 18 copy/set irq latch (h-h): %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///	/* LBO - these 2 are likely wrong */
/*TODO*///	
/*TODO*///			case 0x7000: /* IRQ Control 0 */
/*TODO*///				if ((data & 0x01) != 0)
/*TODO*///				{
/*TODO*///	//				IRQ_enable = 1;
/*TODO*///					IRQ_count = IRQ_count_latch;
/*TODO*///				}
/*TODO*///				logerror("     Mapper 18 IRQ Control 0: %02x\n", data);
/*TODO*///				break;
/*TODO*///			case 0x7001: /* IRQ Control 1 */
/*TODO*///				IRQ_enable = data & 0x01;
/*TODO*///				IRQ_mode_jaleco = data & 0x0e;
/*TODO*///				logerror("     Mapper 18 IRQ Control 1: %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x7002: /* Misc */
/*TODO*///				switch (data & 0x03)
/*TODO*///				{
/*TODO*///					case 0: ppu_mirror_low (); break;
/*TODO*///					case 1: ppu_mirror_high (); break;
/*TODO*///					case 2: ppu_mirror_v (); break;
/*TODO*///					case 3: ppu_mirror_h (); break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///	/* $f003 uncaught, writes 0(start) & various(ingame) */
/*TODO*///	//		case 0x7003:
/*TODO*///	//			IRQ_count = data;
/*TODO*///	//			break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("Mapper 18 uncaught addr: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	int namcot_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		IRQ_count ++;
/*TODO*///		/* Increment & check the IRQ scanline counter */
/*TODO*///		if (IRQ_enable && (IRQ_count == 0x7fff))
/*TODO*///		{
/*TODO*///			ret = M6502_INT_IRQ;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper19_l_w (int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset & 0x1800)
/*TODO*///		{
/*TODO*///			case 0x1000:
/*TODO*///				/* low byte of IRQ */
/*TODO*///				IRQ_count = (IRQ_count & 0x7f00) | data;
/*TODO*///				break;
/*TODO*///			case 0x1800:
/*TODO*///				/* high byte of IRQ, IRQ enable in high bit */
/*TODO*///				IRQ_count = (IRQ_count & 0xff) | ((data & 0x7f) << 8);
/*TODO*///				IRQ_enable = data & 0x80;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper19_w (int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset & 0x7800)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				/* Switch 1k VROM at $0000 */
/*TODO*///				nes_vram [0] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x0800:
/*TODO*///				/* Switch 1k VROM at $0400 */
/*TODO*///				nes_vram [1] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1000:
/*TODO*///				/* Switch 1k VROM at $0800 */
/*TODO*///				nes_vram [2] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1800:
/*TODO*///				/* Switch 1k VROM at $0c00 */
/*TODO*///				nes_vram [3] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x2000:
/*TODO*///				/* Switch 1k VROM at $1000 */
/*TODO*///				nes_vram [4] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x2800:
/*TODO*///				/* Switch 1k VROM at $1400 */
/*TODO*///				nes_vram [5] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3000:
/*TODO*///				/* Switch 1k VROM at $1800 */
/*TODO*///				nes_vram [6] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3800:
/*TODO*///				/* Switch 1k VROM at $1c00 */
/*TODO*///				nes_vram [7] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				/* Switch 8k bank at $8000 */
/*TODO*///				data &= prg_mask;
/*TODO*///				cpu_setbank (1, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x6800:
/*TODO*///				/* Switch 8k bank at $a000 */
/*TODO*///				data &= prg_mask;
/*TODO*///				cpu_setbank (2, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				/* Switch 8k bank at $c000 */
/*TODO*///				data &= prg_mask;
/*TODO*///				cpu_setbank (3, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	int fds_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		if (IRQ_enable_latch != 0)
/*TODO*///			ret = M6502_INT_IRQ;
/*TODO*///	
/*TODO*///		/* Increment & check the IRQ scanline counter */
/*TODO*///		if (IRQ_enable != 0)
/*TODO*///		{
/*TODO*///			if (IRQ_count <= 114)
/*TODO*///			{
/*TODO*///				ret = M6502_INT_IRQ;
/*TODO*///				IRQ_enable = 0;
/*TODO*///				nes_fds.status0 |= 0x01;
/*TODO*///			}
/*TODO*///			else
/*TODO*///				IRQ_count -= 114;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	READ_HANDLER ( fds_r )
/*TODO*///	{
/*TODO*///		data_t ret = 0x00;
/*TODO*///		static int last_side = 0;
/*TODO*///		static int count = 0;
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x00: /* $4030 - disk status 0 */
/*TODO*///				ret = nes_fds.status0;
/*TODO*///				/* clear the disk IRQ detect flag */
/*TODO*///				nes_fds.status0 &= ~0x01;
/*TODO*///				break;
/*TODO*///			case 0x01: /* $4031 - data latch */
/*TODO*///				if (nes_fds.current_side)
/*TODO*///					ret = nes_fds.data[(nes_fds.current_side-1) * 65500 + nes_fds.head_position++];
/*TODO*///				else
/*TODO*///					ret = 0;
/*TODO*///				break;
/*TODO*///			case 0x02: /* $4032 - disk status 1 */
/*TODO*///				/* If we've switched disks, report "no disk" for a few reads */
/*TODO*///				if (last_side != nes_fds.current_side)
/*TODO*///				{
/*TODO*///					ret = 1;
/*TODO*///					count ++;
/*TODO*///					if (count == 50)
/*TODO*///					{
/*TODO*///						last_side = nes_fds.current_side;
/*TODO*///						count = 0;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///					ret = (nes_fds.current_side == 0); /* 0 if a disk is inserted */
/*TODO*///				break;
/*TODO*///			case 0x03: /* $4033 */
/*TODO*///				ret = 0x80;
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				ret = 0x00;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///	#ifdef LOG_FDS
/*TODO*///		logerror ("fds_r, address: %04x, data: %02x\n", offset + 0x4030, ret);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	WRITE_HANDLER ( fds_w )
/*TODO*///	{
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x00: /* $4020 */
/*TODO*///				IRQ_count_latch &= ~0xff;
/*TODO*///				IRQ_count_latch |= data;
/*TODO*///				break;
/*TODO*///			case 0x01: /* $4021 */
/*TODO*///				IRQ_count_latch &= ~0xff00;
/*TODO*///				IRQ_count_latch |= (data << 8);
/*TODO*///				break;
/*TODO*///			case 0x02: /* $4022 */
/*TODO*///				IRQ_count = IRQ_count_latch;
/*TODO*///				IRQ_enable = data;
/*TODO*///				break;
/*TODO*///			case 0x03: /* $4023 */
/*TODO*///				// d0 = sound io (1 = enable)
/*TODO*///				// d1 = disk io (1 = enable)
/*TODO*///				break;
/*TODO*///			case 0x04: /* $4024 */
/*TODO*///				/* write data out to disk */
/*TODO*///				break;
/*TODO*///			case 0x05: /* $4025 */
/*TODO*///				nes_fds.motor_on = data & 0x01;
/*TODO*///				if ((data & 0x02) != 0) nes_fds.head_position = 0;
/*TODO*///				nes_fds.read_mode = data & 0x04;
/*TODO*///				if ((data & 0x08) != 0)
/*TODO*///					ppu_mirror_h ();
/*TODO*///				else
/*TODO*///					ppu_mirror_v ();
/*TODO*///				if ((!(data & 0x40)) && (nes_fds.write_reg & 0x40))
/*TODO*///					nes_fds.head_position -= 2; // ???
/*TODO*///				IRQ_enable_latch = data & 0x80;
/*TODO*///	
/*TODO*///				nes_fds.write_reg = data;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///	#ifdef LOG_FDS
/*TODO*///		logerror ("fds_w, address: %04x, data: %02x\n", offset + 0x4020, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	}
/*TODO*///	
/*TODO*///	int konami_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		/* Increment & check the IRQ scanline counter */
/*TODO*///		if (IRQ_enable && (++IRQ_count == 0x100))
/*TODO*///		{
/*TODO*///			IRQ_count = IRQ_count_latch;
/*TODO*///			IRQ_enable = IRQ_enable_latch;
/*TODO*///			ret = M6502_INT_IRQ;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void konami_vrc2a_w (int offset, int data)
/*TODO*///	{
/*TODO*///		if (offset < 0x3000)
/*TODO*///		{
/*TODO*///			switch (offset & 0x3000)
/*TODO*///			{
/*TODO*///				case 0:
/*TODO*///					/* Switch 8k bank at $8000 */
/*TODO*///					data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///					cpu_setbank (1, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///					break;
/*TODO*///				case 0x1000:
/*TODO*///					switch (data & 0x03)
/*TODO*///					{
/*TODO*///						case 0x00: ppu_mirror_v (); break;
/*TODO*///						case 0x01: ppu_mirror_h (); break;
/*TODO*///						case 0x02: ppu_mirror_low (); break;
/*TODO*///						case 0x03: ppu_mirror_high (); break;
/*TODO*///					}
/*TODO*///					break;
/*TODO*///	
/*TODO*///				case 0x2000:
/*TODO*///					/* Switch 8k bank at $a000 */
/*TODO*///					data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///					cpu_setbank (2, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///					break;
/*TODO*///				default:
/*TODO*///					logerror("konami_vrc2a_w uncaught offset: %04x value: %02x\n", offset, data);
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		switch (offset & 0x7000)
/*TODO*///		{
/*TODO*///			case 0x3000:
/*TODO*///				/* Switch 1k vrom at $0000 */
/*TODO*///				vrom_bank[0] = data >> 1;
/*TODO*///				vrom_bank[0] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[0] = vrom_bank[0] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3001:
/*TODO*///				/* Switch 1k vrom at $0400 */
/*TODO*///				vrom_bank[1] = data >> 1;
/*TODO*///				vrom_bank[1] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[1] = vrom_bank[1] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				/* Switch 1k vrom at $0800 */
/*TODO*///				vrom_bank[2] = data >> 1;
/*TODO*///				vrom_bank[2] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[2] = vrom_bank[2] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4001:
/*TODO*///				/* Switch 1k vrom at $0c00 */
/*TODO*///				vrom_bank[3] = data >> 1;
/*TODO*///				vrom_bank[3] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[3] = vrom_bank[3] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				/* Switch 1k vrom at $1000 */
/*TODO*///				vrom_bank[4] = data >> 1;
/*TODO*///				vrom_bank[4] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[4] = vrom_bank[4] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5001:
/*TODO*///				/* Switch 1k vrom at $1400 */
/*TODO*///				vrom_bank[5] = data >> 1;
/*TODO*///				vrom_bank[5] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[5] = vrom_bank[5] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				/* Switch 1k vrom at $1800 */
/*TODO*///				vrom_bank[6] = data >> 1;
/*TODO*///				vrom_bank[6] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[6] = vrom_bank[6] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6001:
/*TODO*///				/* Switch 1k vrom at $1c00 */
/*TODO*///				vrom_bank[7] = data >> 1;
/*TODO*///				vrom_bank[7] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[7] = vrom_bank[7] * 64;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x7000:
/*TODO*///				IRQ_count_latch = data;
/*TODO*///				break;
/*TODO*///			case 0x7001:
/*TODO*///				IRQ_enable = IRQ_enable_latch;
/*TODO*///				break;
/*TODO*///			case 0x7002:
/*TODO*///				IRQ_count = IRQ_count_latch;
/*TODO*///				IRQ_enable = data & 0x02;
/*TODO*///				IRQ_enable_latch = data & 0x01;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("konami_vrc2a_w uncaught offset: %04x value: %02x\n", offset, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void konami_vrc2b_w (int offset, int data)
/*TODO*///	{
/*TODO*///		UINT16 select;
/*TODO*///	
/*TODO*///	//	logerror("konami_vrc2b_w offset: %04x value: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		if (offset < 0x3000)
/*TODO*///		{
/*TODO*///			switch (offset & 0x3000)
/*TODO*///			{
/*TODO*///				case 0:
/*TODO*///					/* Switch 8k bank at $8000 */
/*TODO*///					data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///					cpu_setbank (1, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///					break;
/*TODO*///				case 0x1000:
/*TODO*///					switch (data & 0x03)
/*TODO*///					{
/*TODO*///						case 0x00: ppu_mirror_v (); break;
/*TODO*///						case 0x01: ppu_mirror_h (); break;
/*TODO*///						case 0x02: ppu_mirror_low (); break;
/*TODO*///						case 0x03: ppu_mirror_high (); break;
/*TODO*///					}
/*TODO*///					break;
/*TODO*///	
/*TODO*///				case 0x2000:
/*TODO*///					/* Switch 8k bank at $a000 */
/*TODO*///					data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///					cpu_setbank (2, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///					break;
/*TODO*///				default:
/*TODO*///					logerror("konami_vrc2b_w offset: %04x value: %02x\n", offset, data);
/*TODO*///					break;
/*TODO*///			}
/*TODO*///			return;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* The low 2 select bits vary from cart to cart */
/*TODO*///		select = (offset & 0x7000) |
/*TODO*///			(offset & 0x03) |
/*TODO*///			((offset & 0x0c) >> 2);
/*TODO*///	
/*TODO*///		switch (select)
/*TODO*///		{
/*TODO*///			case 0x3000:
/*TODO*///				/* Switch 1k vrom at $0000 - low 4 bits */
/*TODO*///				vrom_bank[0] = (vrom_bank[0] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[0] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[0] = vrom_bank[0] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3001:
/*TODO*///				/* Switch 1k vrom at $0000 - high 4 bits */
/*TODO*///				vrom_bank[0] = (vrom_bank[0] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[0] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[0] = vrom_bank[0] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3002:
/*TODO*///				/* Switch 1k vrom at $0400 - low 4 bits */
/*TODO*///				vrom_bank[1] = (vrom_bank[1] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[1] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[1] = vrom_bank[1] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3003:
/*TODO*///				/* Switch 1k vrom at $0400 - high 4 bits */
/*TODO*///				vrom_bank[1] = (vrom_bank[1] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[1] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[1] = vrom_bank[1] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				/* Switch 1k vrom at $0800 - low 4 bits */
/*TODO*///				vrom_bank[2] = (vrom_bank[2] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[2] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[2] = vrom_bank[2] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4001:
/*TODO*///				/* Switch 1k vrom at $0800 - high 4 bits */
/*TODO*///				vrom_bank[2] = (vrom_bank[2] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[2] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[2] = vrom_bank[2] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4002:
/*TODO*///				/* Switch 1k vrom at $0c00 - low 4 bits */
/*TODO*///				vrom_bank[3] = (vrom_bank[3] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[3] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[3] = vrom_bank[3] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4003:
/*TODO*///				/* Switch 1k vrom at $0c00 - high 4 bits */
/*TODO*///				vrom_bank[3] = (vrom_bank[3] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[3] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[3] = vrom_bank[3] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				/* Switch 1k vrom at $1000 - low 4 bits */
/*TODO*///				vrom_bank[4] = (vrom_bank[4] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[4] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[4] = vrom_bank[4] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5001:
/*TODO*///				/* Switch 1k vrom at $1000 - high 4 bits */
/*TODO*///				vrom_bank[4] = (vrom_bank[4] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[4] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[4] = vrom_bank[4] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5002:
/*TODO*///				/* Switch 1k vrom at $1400 - low 4 bits */
/*TODO*///				vrom_bank[5] = (vrom_bank[5] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[5] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[5] = vrom_bank[5] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5003:
/*TODO*///				/* Switch 1k vrom at $1400 - high 4 bits */
/*TODO*///				vrom_bank[5] = (vrom_bank[5] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[5] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[5] = vrom_bank[5] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				/* Switch 1k vrom at $1800 - low 4 bits */
/*TODO*///				vrom_bank[6] = (vrom_bank[6] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[6] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[6] = vrom_bank[6] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6001:
/*TODO*///				/* Switch 1k vrom at $1800 - high 4 bits */
/*TODO*///				vrom_bank[6] = (vrom_bank[6] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[6] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[6] = vrom_bank[6] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6002:
/*TODO*///				/* Switch 1k vrom at $1c00 - low 4 bits */
/*TODO*///				vrom_bank[7] = (vrom_bank[7] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[7] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[7] = vrom_bank[7] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6003:
/*TODO*///				/* Switch 1k vrom at $1c00 - high 4 bits */
/*TODO*///				vrom_bank[7] = (vrom_bank[7] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[7] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[7] = vrom_bank[7] * 64;
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				IRQ_count_latch = data;
/*TODO*///				break;
/*TODO*///			case 0x7001:
/*TODO*///				IRQ_enable = IRQ_enable_latch;
/*TODO*///				break;
/*TODO*///			case 0x7002:
/*TODO*///				IRQ_count = IRQ_count_latch;
/*TODO*///				IRQ_enable = data & 0x02;
/*TODO*///				IRQ_enable_latch = data & 0x01;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("konami_vrc2b_w uncaught offset: %04x value: %02x\n", offset, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void konami_vrc4_w (int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset & 0x7007)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				/* Switch 8k bank at $8000 */
/*TODO*///				data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///				cpu_setbank (1, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x1000:
/*TODO*///				switch (data & 0x03)
/*TODO*///				{
/*TODO*///					case 0x00: ppu_mirror_v (); break;
/*TODO*///					case 0x01: ppu_mirror_h (); break;
/*TODO*///					case 0x02: ppu_mirror_low (); break;
/*TODO*///					case 0x03: ppu_mirror_high (); break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			/* $1001 is uncaught */
/*TODO*///	
/*TODO*///			case 0x2000:
/*TODO*///				/* Switch 8k bank at $a000 */
/*TODO*///				data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///				cpu_setbank (2, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x3000:
/*TODO*///				/* Switch 1k vrom at $0000 - low 4 bits */
/*TODO*///				vrom_bank[0] = (vrom_bank[0] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[0] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[0] = vrom_bank[0] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3002:
/*TODO*///				/* Switch 1k vrom at $0000 - high 4 bits */
/*TODO*///				vrom_bank[0] = (vrom_bank[0] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[0] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[0] = vrom_bank[0] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3001:
/*TODO*///			case 0x3004:
/*TODO*///				/* Switch 1k vrom at $0400 - low 4 bits */
/*TODO*///				vrom_bank[1] = (vrom_bank[1] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[1] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[1] = vrom_bank[1] * 64;
/*TODO*///				break;
/*TODO*///			case 0x3003:
/*TODO*///			case 0x3006:
/*TODO*///				/* Switch 1k vrom at $0400 - high 4 bits */
/*TODO*///				vrom_bank[1] = (vrom_bank[1] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[1] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[1] = vrom_bank[1] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				/* Switch 1k vrom at $0800 - low 4 bits */
/*TODO*///				vrom_bank[2] = (vrom_bank[2] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[2] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[2] = vrom_bank[2] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4002:
/*TODO*///				/* Switch 1k vrom at $0800 - high 4 bits */
/*TODO*///				vrom_bank[2] = (vrom_bank[2] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[2] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[2] = vrom_bank[2] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4001:
/*TODO*///			case 0x4004:
/*TODO*///				/* Switch 1k vrom at $0c00 - low 4 bits */
/*TODO*///				vrom_bank[3] = (vrom_bank[3] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[3] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[3] = vrom_bank[3] * 64;
/*TODO*///				break;
/*TODO*///			case 0x4003:
/*TODO*///			case 0x4006:
/*TODO*///				/* Switch 1k vrom at $0c00 - high 4 bits */
/*TODO*///				vrom_bank[3] = (vrom_bank[3] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[3] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[3] = vrom_bank[3] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				/* Switch 1k vrom at $1000 - low 4 bits */
/*TODO*///				vrom_bank[4] = (vrom_bank[4] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[4] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[4] = vrom_bank[4] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5002:
/*TODO*///				/* Switch 1k vrom at $1000 - high 4 bits */
/*TODO*///				vrom_bank[4] = (vrom_bank[4] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[4] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[4] = vrom_bank[4] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5001:
/*TODO*///			case 0x5004:
/*TODO*///				/* Switch 1k vrom at $1400 - low 4 bits */
/*TODO*///				vrom_bank[5] = (vrom_bank[5] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[5] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[5] = vrom_bank[5] * 64;
/*TODO*///				break;
/*TODO*///			case 0x5003:
/*TODO*///			case 0x5006:
/*TODO*///				/* Switch 1k vrom at $1400 - high 4 bits */
/*TODO*///				vrom_bank[5] = (vrom_bank[5] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[5] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[5] = vrom_bank[5] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				/* Switch 1k vrom at $1800 - low 4 bits */
/*TODO*///				vrom_bank[6] = (vrom_bank[6] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[6] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[6] = vrom_bank[6] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6002:
/*TODO*///				/* Switch 1k vrom at $1800 - high 4 bits */
/*TODO*///				vrom_bank[6] = (vrom_bank[6] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[6] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[6] = vrom_bank[6] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6001:
/*TODO*///			case 0x6004:
/*TODO*///				/* Switch 1k vrom at $1c00 - low 4 bits */
/*TODO*///				vrom_bank[7] = (vrom_bank[7] & 0xf0) | (data & 0x0f);
/*TODO*///				vrom_bank[7] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[7] = vrom_bank[7] * 64;
/*TODO*///				break;
/*TODO*///			case 0x6003:
/*TODO*///			case 0x6006:
/*TODO*///				/* Switch 1k vrom at $1c00 - high 4 bits */
/*TODO*///				vrom_bank[7] = (vrom_bank[7] & 0x0f) | (data << 4);
/*TODO*///				vrom_bank[7] &= ((nes.chr_chunks << 3) - 1);
/*TODO*///				nes_vram[7] = vrom_bank[7] * 64;
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				/* Set low 4 bits of latch */
/*TODO*///				IRQ_count_latch &= ~0x0f;
/*TODO*///				IRQ_count_latch |= data & 0x0f;
/*TODO*///	//			logerror("konami_vrc4 irq_latch low: %02x\n", IRQ_count_latch);
/*TODO*///				break;
/*TODO*///			case 0x7002:
/*TODO*///			case 0x7040:
/*TODO*///				/* Set high 4 bits of latch */
/*TODO*///				IRQ_count_latch &= ~0xf0;
/*TODO*///				IRQ_count_latch |= (data << 4) & 0xf0;
/*TODO*///	//			logerror("konami_vrc4 irq_latch high: %02x\n", IRQ_count_latch);
/*TODO*///				break;
/*TODO*///			case 0x7004:
/*TODO*///			case 0x7001:
/*TODO*///			case 0x7080:
/*TODO*///				IRQ_count = IRQ_count_latch;
/*TODO*///				IRQ_enable = data & 0x02;
/*TODO*///				IRQ_enable_latch = data & 0x01;
/*TODO*///	//			logerror("konami_vrc4 irq_count set: %02x\n", IRQ_count);
/*TODO*///	//			logerror("konami_vrc4 enable: %02x\n", IRQ_enable);
/*TODO*///	//			logerror("konami_vrc4 enable latch: %02x\n", IRQ_enable_latch);
/*TODO*///				break;
/*TODO*///			case 0x7006:
/*TODO*///			case 0x7003:
/*TODO*///			case 0x70c0:
/*TODO*///				IRQ_enable = IRQ_enable_latch;
/*TODO*///	//			logerror("konami_vrc4 enable copy: %02x\n", IRQ_enable);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("konami_vrc4_w uncaught offset: %04x value: %02x\n", offset, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void konami_vrc6a_w (int offset, int data)
/*TODO*///	{
/*TODO*///	//	logerror("konami_vrc6_w offset: %04x, data: %02x, scanline: %d\n", offset, data, current_scanline);
/*TODO*///	
/*TODO*///		switch (offset & 0x7003)
/*TODO*///		{
/*TODO*///			case 0: case 1: case 2: case 3:
/*TODO*///				/* Switch 16k bank at $8000 */
/*TODO*///				prg16_89ab (data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///	/* $1000-$1002 = sound regs */
/*TODO*///	/* $2000-$2002 = sound regs */
/*TODO*///	/* $3000-$3002 = sound regs */
/*TODO*///	
/*TODO*///			case 0x3003:
/*TODO*///				switch (data & 0x0c)
/*TODO*///				{
/*TODO*///					case 0x00: ppu_mirror_v (); break;
/*TODO*///					case 0x04: ppu_mirror_h (); break;
/*TODO*///					case 0x08: ppu_mirror_low (); break;
/*TODO*///					case 0x0c: ppu_mirror_high (); break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x4000: case 0x4001: case 0x4002: case 0x4003:
/*TODO*///				/* Switch 8k bank at $c000 */
/*TODO*///				prg8_cd (data);
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				/* Switch 1k VROM at $0000 */
/*TODO*///				nes_vram [0] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x5001:
/*TODO*///				/* Switch 1k VROM at $0400 */
/*TODO*///				nes_vram [1] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x5002:
/*TODO*///				/* Switch 1k VROM at $0800 */
/*TODO*///				nes_vram [2] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x5003:
/*TODO*///				/* Switch 1k VROM at $0c00 */
/*TODO*///				nes_vram [3] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				/* Switch 1k VROM at $1000 */
/*TODO*///				nes_vram [4] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x6001:
/*TODO*///				/* Switch 1k VROM at $1400 */
/*TODO*///				nes_vram [5] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x6002:
/*TODO*///				/* Switch 1k VROM at $1800 */
/*TODO*///				nes_vram [6] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x6003:
/*TODO*///				/* Switch 1k VROM at $1c00 */
/*TODO*///				nes_vram [7] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				IRQ_count_latch = data;
/*TODO*///				break;
/*TODO*///			case 0x7001:
/*TODO*///				IRQ_count = IRQ_count_latch;
/*TODO*///				IRQ_enable = data & 0x02;
/*TODO*///				IRQ_enable_latch = data & 0x01;
/*TODO*///				break;
/*TODO*///			case 0x7002:
/*TODO*///				IRQ_enable = IRQ_enable_latch;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("konami_vrc6_w uncaught addr: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void konami_vrc6b_w (int offset, int data)
/*TODO*///	{
/*TODO*///	//	logerror("konami_vrc6_w offset: %04x, data: %02x, scanline: %d\n", offset, data, current_scanline);
/*TODO*///	
/*TODO*///		switch (offset & 0x7003)
/*TODO*///		{
/*TODO*///			case 0: case 1: case 2: case 3:
/*TODO*///				/* Switch 16k bank at $8000 */
/*TODO*///				prg16_89ab (data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///	/* $1000-$1002 = sound regs */
/*TODO*///	/* $2000-$2002 = sound regs */
/*TODO*///	/* $3000-$3002 = sound regs */
/*TODO*///	
/*TODO*///			case 0x3003:
/*TODO*///				switch (data & 0x0c)
/*TODO*///				{
/*TODO*///					case 0x00: ppu_mirror_v (); break;
/*TODO*///					case 0x04: ppu_mirror_h (); break;
/*TODO*///					case 0x08: ppu_mirror_low (); break;
/*TODO*///					case 0x0c: ppu_mirror_high (); break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x4000: case 0x4001: case 0x4002: case 0x4003:
/*TODO*///				/* Switch 8k bank at $c000 */
/*TODO*///				prg8_cd (data);
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				/* Switch 1k VROM at $0000 */
/*TODO*///				nes_vram [0] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x5002:
/*TODO*///				/* Switch 1k VROM at $0400 */
/*TODO*///				nes_vram [1] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x5001:
/*TODO*///				/* Switch 1k VROM at $0800 */
/*TODO*///				nes_vram [2] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x5003:
/*TODO*///				/* Switch 1k VROM at $0c00 */
/*TODO*///				nes_vram [3] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				/* Switch 1k VROM at $1000 */
/*TODO*///				nes_vram [4] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x6002:
/*TODO*///				/* Switch 1k VROM at $1400 */
/*TODO*///				nes_vram [5] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x6001:
/*TODO*///				/* Switch 1k VROM at $1800 */
/*TODO*///				nes_vram [6] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x6003:
/*TODO*///				/* Switch 1k VROM at $1c00 */
/*TODO*///				nes_vram [7] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				IRQ_count_latch = data;
/*TODO*///				break;
/*TODO*///			case 0x7001:
/*TODO*///				IRQ_enable = IRQ_enable_latch;
/*TODO*///				break;
/*TODO*///			case 0x7002:
/*TODO*///				IRQ_count = IRQ_count_latch;
/*TODO*///				IRQ_enable = data & 0x02;
/*TODO*///				IRQ_enable_latch = data & 0x01;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("konami_vrc6_w uncaught addr: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper32_w (int offset, int data)
/*TODO*///	{
/*TODO*///		static int bankSel;
/*TODO*///	
/*TODO*///	//	logerror("mapper32_w offset: %04x, data: %02x, scanline: %d\n", offset, data, current_scanline);
/*TODO*///	
/*TODO*///		switch (offset & 0x7000)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				/* Switch 8k bank at $8000 or $c000 */
/*TODO*///				if (bankSel != 0)
/*TODO*///					prg8_cd (data);
/*TODO*///				else
/*TODO*///					prg8_89 (data);
/*TODO*///				break;
/*TODO*///			case 0x1000:
/*TODO*///				bankSel = data & 0x02;
/*TODO*///				if ((data & 0x01) != 0)
/*TODO*///					ppu_mirror_h ();
/*TODO*///				else
/*TODO*///					ppu_mirror_v ();
/*TODO*///				break;
/*TODO*///			case 0x2000:
/*TODO*///				/* Switch 8k bank at $A000 */
/*TODO*///				prg8_ab (data);
/*TODO*///				break;
/*TODO*///			case 0x3000:
/*TODO*///				/* Switch 1k VROM at $1000 */
/*TODO*///				nes_vram[offset & 0x07] = data * 64;
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("Uncaught mapper 32 write, addr: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper33_w (int offset, int data)
/*TODO*///	{
/*TODO*///	
/*TODO*///	//	logerror("mapper33_w offset: %04x, data: %02x, scanline: %d\n", offset, data, current_scanline);
/*TODO*///	
/*TODO*///		switch (offset & 0x7003)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				/* Switch 8k bank at $8000 */
/*TODO*///				data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///				cpu_setbank (1, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x0001:
/*TODO*///				/* Switch 8k bank at $A000 */
/*TODO*///				data &= ((nes.prg_chunks << 1) - 1);
/*TODO*///				cpu_setbank (2, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///				break;
/*TODO*///			case 0x0002:
/*TODO*///				/* Switch 2k VROM at $0000 */
/*TODO*///				nes_vram [0] = data * 128;
/*TODO*///				nes_vram [1] = nes_vram [0] + 64;
/*TODO*///				break;
/*TODO*///			case 0x0003:
/*TODO*///				/* Switch 2k VROM at $0800 */
/*TODO*///				nes_vram [2] = data * 128;
/*TODO*///				nes_vram [3] = nes_vram [2] + 64;
/*TODO*///				break;
/*TODO*///			case 0x2000:
/*TODO*///				/* Switch 1k VROM at $1000 */
/*TODO*///				nes_vram [4] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x2001:
/*TODO*///				/* Switch 1k VROM at $1400 */
/*TODO*///				nes_vram [5] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x2002:
/*TODO*///				/* Switch 1k VROM at $1800 */
/*TODO*///				nes_vram [6] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x2003:
/*TODO*///				/* Switch 1k VROM at $1c00 */
/*TODO*///				nes_vram [7] = data * 64;
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("Uncaught mapper 33 write, addr: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper34_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper34_m_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x1ffd:
/*TODO*///				/* Switch 32k prg banks */
/*TODO*///				prg32 (data);
/*TODO*///				break;
/*TODO*///			case 0x1ffe:
/*TODO*///				/* Switch 4k VNES_ROM at 0x0000 */
/*TODO*///				chr4_0 (data);
/*TODO*///				break;
/*TODO*///			case 0x1fff:
/*TODO*///				/* Switch 4k VNES_ROM at 0x1000 */
/*TODO*///				chr4_4 (data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void mapper34_w (int offset, int data)
/*TODO*///	{
/*TODO*///		/* This portion of the mapper is nearly identical to Mapper 7, except no one-screen mirroring */
/*TODO*///		/* Deadly Towers is really a Mapper 34 game - the demo screens look wrong using mapper 7. */
/*TODO*///		logerror("Mapper 34 w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		prg32 (data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	int mapper40_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		/* Decrement & check the IRQ scanline counter */
/*TODO*///		if (IRQ_enable != 0)
/*TODO*///		{
/*TODO*///			if (--IRQ_count == 0)
/*TODO*///			{
/*TODO*///				ret = M6502_INT_IRQ;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper40_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper40_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		switch (offset & 0x6000)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				IRQ_enable = 0;
/*TODO*///				IRQ_count = 37; /* Hardcoded scanline scroll */
/*TODO*///				break;
/*TODO*///			case 0x2000:
/*TODO*///				IRQ_enable = 1;
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				/* Game runs code between banks, use slow but sure method */
/*TODO*///				prg8_cd (data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper41_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("mapper41_m_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///		if ((offset & 0x20) != 0)
/*TODO*///			ppu_mirror_h();
/*TODO*///		else
/*TODO*///			ppu_mirror_v();
/*TODO*///	
/*TODO*///		mapper41_reg2 = offset & 0x04;
/*TODO*///		mapper41_chr &= ~0x0c;
/*TODO*///		mapper41_chr |= (offset & 0x18) >> 1;
/*TODO*///		prg32 (offset & 0x07);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper41_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("mapper41_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		if (mapper41_reg2 != 0)
/*TODO*///		{
/*TODO*///			mapper41_chr &= ~0x03;
/*TODO*///			mapper41_chr |= data & 0x03;
/*TODO*///			chr8(mapper41_chr);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper64_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper64_m_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper64_w (int offset, int data)
/*TODO*///	{
/*TODO*///		static int cmd = 0;
/*TODO*///		static int chr = 0;
/*TODO*///		static int select_high;
/*TODO*///		static int page;
/*TODO*///	
/*TODO*///	/* TODO: something in the IRQ handling hoses Skull & Crossbones */
/*TODO*///	
/*TODO*///	//	logerror("mapper64_w offset: %04x, data: %02x, scanline: %d\n", offset, data, current_scanline);
/*TODO*///	
/*TODO*///		switch (offset & 0x7001)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///	//			logerror("Mapper 64 0x8000 write value: %02x\n",data);
/*TODO*///				cmd = data & 0x0f;
/*TODO*///				if ((data & 0x80) != 0)
/*TODO*///					chr = 0x1000;
/*TODO*///				else
/*TODO*///					chr = 0x0000;
/*TODO*///	
/*TODO*///				if ((data & 0x10) != 0)
/*TODO*///				{
/*TODO*///					nes_vram[1] = nes_vram[3] = 0;
/*TODO*///				}
/*TODO*///	
/*TODO*///				page = chr >> 10;
/*TODO*///				/* Toggle switching between $8000/$A000/$C000 and $A000/$C000/$8000 */
/*TODO*///				if (select_high != (data & 0x40))
/*TODO*///				{
/*TODO*///					if ((data & 0x40) != 0)
/*TODO*///					{
/*TODO*///						cpu_setbank (1, &nes.rom[(nes.prg_chunks-1) * 0x4000 + 0x10000]);
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						cpu_setbank (3, &nes.rom[(nes.prg_chunks-1) * 0x4000 + 0x10000]);
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///				select_high = data & 0x40;
/*TODO*///	//			logerror("   Mapper 64 select_high: %02x\n", select_high);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x0001:
/*TODO*///				switch (cmd)
/*TODO*///				{
/*TODO*///					case 0:
/*TODO*///						nes_vram [page] = data * 64;
/*TODO*///						nes_vram [page+1] = nes_vram [page] + 64;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 1:
/*TODO*///						nes_vram [page ^ 2] = data * 64;
/*TODO*///						nes_vram [(page ^ 2)+1] = nes_vram [page ^ 2] + 64;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 2:
/*TODO*///						nes_vram [page ^ 4] = data * 64;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 3:
/*TODO*///						nes_vram [page ^ 5] = data * 64;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 4:
/*TODO*///						nes_vram [page ^ 6] = data * 64;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 5:
/*TODO*///						nes_vram [page ^ 7] = data * 64;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 6:
/*TODO*///						/* These damn games will go to great lengths to switch to banks which are outside the valid range */
/*TODO*///						data &= prg_mask;
/*TODO*///						if (select_high != 0)
/*TODO*///						{
/*TODO*///							cpu_setbank (2, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///	//						logerror("     Mapper 64 switch ($A000) cmd 6 value: %02x\n", data);
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							cpu_setbank (1, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///	//						logerror("     Mapper 64 switch ($8000) cmd 6 value: %02x\n", data);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///					case 7:
/*TODO*///						data &= prg_mask;
/*TODO*///						if (select_high != 0)
/*TODO*///						{
/*TODO*///							cpu_setbank (3, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///	//						logerror("     Mapper 64 switch ($C000) cmd 7 value: %02x\n", data);
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							cpu_setbank (2, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///	//						logerror("     Mapper 64 switch ($A000) cmd 7 value: %02x\n", data);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///					case 8:
/*TODO*///						/* Switch 1k VROM at $0400 */
/*TODO*///						nes_vram [1] = data * 64;
/*TODO*///						break;
/*TODO*///					case 9:
/*TODO*///						/* Switch 1k VROM at $0C00 */
/*TODO*///						nes_vram [3] = data * 64;
/*TODO*///						break;
/*TODO*///					case 15:
/*TODO*///						data &= prg_mask;
/*TODO*///						if (select_high != 0)
/*TODO*///						{
/*TODO*///							cpu_setbank (1, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///	//						logerror("     Mapper 64 switch ($C000) cmd 15 value: %02x\n", data);
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							cpu_setbank (3, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///	//						logerror("     Mapper 64 switch ($A000) cmd 15 value: %02x\n", data);
/*TODO*///						}
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				cmd = 16;
/*TODO*///				break;
/*TODO*///			case 0x2000:
/*TODO*///				/* Not sure if the one-screen mirroring applies to this mapper */
/*TODO*///				if ((data & 0x40) != 0)
/*TODO*///					ppu_mirror_high();
/*TODO*///				else
/*TODO*///				{
/*TODO*///					if ((data & 0x01) != 0)
/*TODO*///						ppu_mirror_h();
/*TODO*///					else
/*TODO*///						ppu_mirror_v();
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x4000: /* $c000 - IRQ scanline counter */
/*TODO*///				IRQ_count = data;
/*TODO*///				logerror("     MMC3 copy/set irq latch: %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x4001: /* $c001 - IRQ scanline latch */
/*TODO*///				IRQ_count_latch = data;
/*TODO*///				logerror("     MMC3 set latch: %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x6000: /* $e000 - Disable IRQs */
/*TODO*///				IRQ_enable = 0;
/*TODO*///				logerror("     MMC3 disable irqs: %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x6001: /* $e001 - Enable IRQs */
/*TODO*///				IRQ_enable = 1;
/*TODO*///				logerror("     MMC3 enable irqs: %02x\n", data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("Mapper 64 addr: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	int irem_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		/* Increment & check the IRQ scanline counter */
/*TODO*///		if (IRQ_enable != 0)
/*TODO*///		{
/*TODO*///			if (--IRQ_count == 0)
/*TODO*///				ret = M6502_INT_IRQ;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	static void mapper65_w (int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset & 0x7007)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				/* Switch 8k bank at $8000 */
/*TODO*///				data &= prg_mask;
/*TODO*///				cpu_setbank (1, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     Mapper 65 switch ($8000) value: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///			case 0x1001:
/*TODO*///				if ((data & 0x80) != 0)
/*TODO*///					ppu_mirror_h();
/*TODO*///				else
/*TODO*///					ppu_mirror_v();
/*TODO*///				break;
/*TODO*///			case 0x1005:
/*TODO*///				IRQ_count = data << 1;
/*TODO*///				break;
/*TODO*///			case 0x1006:
/*TODO*///				IRQ_enable = IRQ_count;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x2000:
/*TODO*///				/* Switch 8k bank at $a000 */
/*TODO*///				data &= prg_mask;
/*TODO*///				cpu_setbank (2, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     Mapper 65 switch ($a000) value: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x3000:
/*TODO*///				/* Switch 1k VROM at $0000 */
/*TODO*///				nes_vram [0] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3001:
/*TODO*///				/* Switch 1k VROM at $0400 */
/*TODO*///				nes_vram [1] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3002:
/*TODO*///				/* Switch 1k VROM at $0800 */
/*TODO*///				nes_vram [2] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3003:
/*TODO*///				/* Switch 1k VROM at $0c00 */
/*TODO*///				nes_vram [3] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3004:
/*TODO*///				/* Switch 1k VROM at $1000 */
/*TODO*///				nes_vram [4] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3005:
/*TODO*///				/* Switch 1k VROM at $1400 */
/*TODO*///				nes_vram [5] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3006:
/*TODO*///				/* Switch 1k VROM at $1800 */
/*TODO*///				nes_vram [6] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3007:
/*TODO*///				/* Switch 1k VROM at $1c00 */
/*TODO*///				nes_vram [7] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				/* Switch 8k bank at $c000 */
/*TODO*///				data &= prg_mask;
/*TODO*///				cpu_setbank (3, &nes.rom[0x2000 * (data) + 0x10000]);
/*TODO*///	#ifdef LOG_MMC
/*TODO*///				logerror("     Mapper 65 switch ($c000) value: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("Mapper 65 addr: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper66_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("* Mapper 66 switch, offset %04x, data: %02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		prg32 ((data & 0x30) >> 4);
/*TODO*///		chr8 (data & 0x03);
/*TODO*///	}
/*TODO*///	
/*TODO*///	int sunsoft_irq (int scanline)
/*TODO*///	{
/*TODO*///		int ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		/* 114 is the number of cycles per scanline */
/*TODO*///		/* TODO: change to reflect the actual number of cycles spent */
/*TODO*///	
/*TODO*///		if (IRQ_enable != 0)
/*TODO*///		{
/*TODO*///			if (IRQ_count <= 114)
/*TODO*///			{
/*TODO*///				ret = M6502_INT_IRQ;
/*TODO*///			}
/*TODO*///			IRQ_count -= 114;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return ret;
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void mapper67_w (int offset, int data)
/*TODO*///	{
/*TODO*///	//	logerror("mapper67_w, offset %04x, data: %02x\n", offset, data);
/*TODO*///		switch (offset & 0x7801)
/*TODO*///		{
/*TODO*///	//		case 0x0000: /* IRQ disable? */
/*TODO*///	//			IRQ_enable = 0;
/*TODO*///	//			break;
/*TODO*///			case 0x0800:
/*TODO*///				chr2_0 (data);
/*TODO*///				break;
/*TODO*///			case 0x1800:
/*TODO*///				chr2_2 (data);
/*TODO*///				break;
/*TODO*///			case 0x2800:
/*TODO*///				chr2_4 (data);
/*TODO*///				break;
/*TODO*///			case 0x3800:
/*TODO*///				chr2_6 (data);
/*TODO*///				break;
/*TODO*///	//		case 0x4800:
/*TODO*///	//			nes_vram[5] = data * 64;
/*TODO*///	//			break;
/*TODO*///			case 0x4801:
/*TODO*///				/* IRQ count? */
/*TODO*///				IRQ_count = IRQ_count_latch;
/*TODO*///				IRQ_count_latch = data;
/*TODO*///				break;
/*TODO*///	//		case 0x5800:
/*TODO*///	//			chr4_0 (data);
/*TODO*///	//			break;
/*TODO*///			case 0x5801:
/*TODO*///				IRQ_enable = data;
/*TODO*///				break;
/*TODO*///	//		case 0x6800:
/*TODO*///	//			chr4_4 (data);
/*TODO*///	//			break;
/*TODO*///			case 0x7800:
/*TODO*///				prg16_89ab (data);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("mapper67_w uncaught offset: %04x, data: %02x\n", offset, data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper68_mirror (int m68_mirror, int m0, int m1)
/*TODO*///	{
/*TODO*///		/* The 0x20000 (128k) offset is a magic number */
/*TODO*///		#define M68_OFFSET 0x20000
/*TODO*///	
/*TODO*///		switch (m68_mirror)
/*TODO*///		{
/*TODO*///			case 0x00: ppu_mirror_h (); break;
/*TODO*///			case 0x01: ppu_mirror_v (); break;
/*TODO*///			case 0x02: ppu_mirror_low (); break;
/*TODO*///			case 0x03: ppu_mirror_high (); break;
/*TODO*///			case 0x10:
/*TODO*///				ppu_mirror_custom_vrom (0, (m0 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (1, (m1 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (2, (m0 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (3, (m1 << 10) + M68_OFFSET);
/*TODO*///				break;
/*TODO*///			case 0x11:
/*TODO*///				ppu_mirror_custom_vrom (0, (m0 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (1, (m0 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (2, (m1 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (3, (m1 << 10) + M68_OFFSET);
/*TODO*///				break;
/*TODO*///			case 0x12:
/*TODO*///				ppu_mirror_custom_vrom (0, (m0 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (1, (m0 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (2, (m0 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (3, (m0 << 10) + M68_OFFSET);
/*TODO*///				break;
/*TODO*///			case 0x13:
/*TODO*///				ppu_mirror_custom_vrom (0, (m1 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (1, (m1 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (2, (m1 << 10) + M68_OFFSET);
/*TODO*///				ppu_mirror_custom_vrom (3, (m1 << 10) + M68_OFFSET);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper68_w (int offset, int data)
/*TODO*///	{
/*TODO*///		static int m68_mirror;
/*TODO*///		static int m0, m1;
/*TODO*///	
/*TODO*///		switch (offset & 0x7000)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				/* Switch 2k VROM at $0000 */
/*TODO*///				chr2_0 (data);
/*TODO*///				break;
/*TODO*///			case 0x1000:
/*TODO*///				/* Switch 2k VROM at $0800 */
/*TODO*///				chr2_2 (data);
/*TODO*///				break;
/*TODO*///			case 0x2000:
/*TODO*///				/* Switch 2k VROM at $1000 */
/*TODO*///				chr2_4 (data);
/*TODO*///				break;
/*TODO*///			case 0x3000:
/*TODO*///				/* Switch 2k VROM at $1800 */
/*TODO*///				chr2_6 (data);
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				m0 = data;
/*TODO*///				mapper68_mirror (m68_mirror, m0, m1);
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				m1 = data;
/*TODO*///				mapper68_mirror (m68_mirror, m0, m1);
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				m68_mirror = data & 0x13;
/*TODO*///				mapper68_mirror (m68_mirror, m0, m1);
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				prg16_89ab (data);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("mapper68_w uncaught offset: %04x, data: %02x\n", offset, data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper69_w (int offset, int data)
/*TODO*///	{
/*TODO*///		static int cmd = 0;
/*TODO*///	
/*TODO*///	//	logerror("mapper69_w offset: %04x, data: %02x, scanline: %d\n", offset, data, current_scanline);
/*TODO*///	
/*TODO*///		switch (offset & 0x7000)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				cmd = data & 0x0f;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x2000:
/*TODO*///				switch (cmd)
/*TODO*///				{
/*TODO*///					case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
/*TODO*///						nes_vram [cmd] = data * 64;
/*TODO*///						break;
/*TODO*///	
/*TODO*///					/* TODO: deal with bankswitching/write-protecting the mid-mapper area */
/*TODO*///					case 8:
/*TODO*///	#if 0
/*TODO*///						if (!(data & 0x40))
/*TODO*///						{
/*TODO*///							cpu_setbank (5, &nes.rom[(data & 0x3f) * 0x2000 + 0x10000]);
/*TODO*///						}
/*TODO*///						else
/*TODO*///	#endif
/*TODO*///							logerror ("mapper69_w, cmd 8, data: %02x\n", data);
/*TODO*///						break;
/*TODO*///	
/*TODO*///					case 9:
/*TODO*///						prg8_89 (data);
/*TODO*///						break;
/*TODO*///					case 10:
/*TODO*///						prg8_ab (data);
/*TODO*///						break;
/*TODO*///					case 11:
/*TODO*///						prg8_cd (data);
/*TODO*///						break;
/*TODO*///					case 12:
/*TODO*///						switch (data & 0x03)
/*TODO*///						{
/*TODO*///							case 0x00: ppu_mirror_v (); break;
/*TODO*///							case 0x01: ppu_mirror_h (); break;
/*TODO*///							case 0x02: ppu_mirror_low (); break;
/*TODO*///							case 0x03: ppu_mirror_high (); break;
/*TODO*///						}
/*TODO*///						break;
/*TODO*///					case 13:
/*TODO*///						IRQ_enable = data;
/*TODO*///						break;
/*TODO*///					case 14:
/*TODO*///						IRQ_count = (IRQ_count & 0xff00) | data;
/*TODO*///						break;
/*TODO*///					case 15:
/*TODO*///						IRQ_count = (IRQ_count & 0x00ff) | (data << 8);
/*TODO*///						break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("mapper69_w uncaught %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper70_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper70_w offset %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		/* TODO: is the data being written irrelevant? */
/*TODO*///	
/*TODO*///		prg16_89ab ((data & 0xf0) >> 4);
/*TODO*///	
/*TODO*///		chr8 (data & 0x0f);
/*TODO*///	
/*TODO*///	#if 1
/*TODO*///		if ((data & 0x80) != 0)
/*TODO*///	//		ppu_mirror_h();
/*TODO*///			ppu_mirror_high();
/*TODO*///		else
/*TODO*///	//		ppu_mirror_v();
/*TODO*///			ppu_mirror_low();
/*TODO*///	#endif
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper71_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper71_m_w offset: %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		prg16_89ab (data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper71_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper71_w offset: %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		if (offset >= 0x4000)
/*TODO*///			prg16_89ab (data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper72_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper72_w, offset %04x, data: %02x\n", offset, data);
/*TODO*///		/* This routine is busted */
/*TODO*///	
/*TODO*///	//	prg32 ((data & 0xf0) >> 4);
/*TODO*///	//	prg16_89ab (data & 0x0f);
/*TODO*///	//	prg16_89ab ((data & 0xf0) >> 4);
/*TODO*///	//	chr8 (data & 0x0f);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper73_w (int offset, int data)
/*TODO*///	{
/*TODO*///		switch (offset & 0x7000)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///			case 0x1000:
/*TODO*///				/* dunno which address controls these */
/*TODO*///				IRQ_count_latch = data;
/*TODO*///				IRQ_enable_latch = data;
/*TODO*///				break;
/*TODO*///			case 0x2000:
/*TODO*///				IRQ_enable = data;
/*TODO*///				break;
/*TODO*///			case 0x3000:
/*TODO*///				IRQ_count &= ~0x0f;
/*TODO*///				IRQ_count |= data & 0x0f;
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				IRQ_count &= ~0xf0;
/*TODO*///				IRQ_count |= (data & 0x0f) << 4;
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				prg16_89ab (data);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("mapper73_w uncaught, offset %04x, data: %02x\n", offset, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void mapper75_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper75_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///		switch (offset & 0x7000)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				prg8_89 (data);
/*TODO*///				break;
/*TODO*///			case 0x1000: /* TODO: verify */
/*TODO*///				if ((data & 0x01) != 0)
/*TODO*///					ppu_mirror_h();
/*TODO*///				else
/*TODO*///					ppu_mirror_v();
/*TODO*///				/* vrom banking as well? */
/*TODO*///				break;
/*TODO*///			case 0x2000:
/*TODO*///				prg8_ab (data);
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				prg8_cd (data);
/*TODO*///				break;
/*TODO*///			case 0x6000:
/*TODO*///				chr4_0 (data);
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				chr4_4 (data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper77_w (int offset, int data)
/*TODO*///	{
/*TODO*///	
/*TODO*///	/* Mapper is busted */
/*TODO*///	
/*TODO*///		logerror("mapper77_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x7c84:
/*TODO*///	//			prg8_89 (data);
/*TODO*///				break;
/*TODO*///			case 0x7c86:
/*TODO*///	//			prg8_89 (data);
/*TODO*///	//			prg8_ab (data);
/*TODO*///	//			prg8_cd (data);
/*TODO*///	//			prg16_89ab (data); /* red screen */
/*TODO*///	//			prg16_cdef (data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper78_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper78_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///		/* Switch 8k VROM bank */
/*TODO*///		chr8 ((data & 0xf0) >> 4);
/*TODO*///	
/*TODO*///		/* Switch 16k ROM bank */
/*TODO*///		prg16_89ab (data & 0x0f);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper79_l_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper79_l_w: %04x:%02x\n", offset, data);
/*TODO*///	
/*TODO*///		if ((offset-0x100) & 0x0100)
/*TODO*///		{
/*TODO*///			/* Select 8k VROM bank */
/*TODO*///			chr8 (data & 0x07);
/*TODO*///	
/*TODO*///			/* Select 32k ROM bank? */
/*TODO*///			prg32 ((data & 0x08) >> 3);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper79_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper79_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper80_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper80_m_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x1ef0:
/*TODO*///				/* Switch 2k VROM at $0000 */
/*TODO*///				chr2_0 ((data & 0x7f) >> 1);
/*TODO*///				if ((data & 0x80) != 0)
/*TODO*///				{
/*TODO*///					/* Horizontal, $2000-$27ff */
/*TODO*///					ppu_mirror_custom (0, 0);
/*TODO*///					ppu_mirror_custom (1, 0);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					/* Vertical, $2000-$27ff */
/*TODO*///					ppu_mirror_custom (0, 0);
/*TODO*///					ppu_mirror_custom (1, 1);
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1ef1:
/*TODO*///				/* Switch 2k VROM at $0000 */
/*TODO*///				chr2_2 ((data & 0x7f) >> 1);
/*TODO*///				if ((data & 0x80) != 0)
/*TODO*///				{
/*TODO*///					/* Horizontal, $2800-$2fff */
/*TODO*///					ppu_mirror_custom (2, 0);
/*TODO*///					ppu_mirror_custom (3, 0);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					/* Vertical, $2800-$2fff */
/*TODO*///					ppu_mirror_custom (2, 0);
/*TODO*///					ppu_mirror_custom (3, 1);
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1ef2:
/*TODO*///				/* Switch 1k VROM at $1000 */
/*TODO*///				nes_vram [4] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1ef3:
/*TODO*///				/* Switch 1k VROM at $1400 */
/*TODO*///				nes_vram [5] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1ef4:
/*TODO*///				/* Switch 1k VROM at $1800 */
/*TODO*///				nes_vram [6] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1ef5:
/*TODO*///				/* Switch 1k VROM at $1c00 */
/*TODO*///				nes_vram [7] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1efa: case 0x1efb:
/*TODO*///				/* Switch 8k ROM at $8000 */
/*TODO*///				prg8_89 (data);
/*TODO*///				break;
/*TODO*///			case 0x1efc: case 0x1efd:
/*TODO*///				/* Switch 8k ROM at $a000 */
/*TODO*///				prg8_ab (data);
/*TODO*///				break;
/*TODO*///			case 0x1efe: case 0x1eff:
/*TODO*///				/* Switch 8k ROM at $c000 */
/*TODO*///				prg8_cd (data);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("mapper80_m_w uncaught addr: %04x, value: %02x\n", offset + 0x6000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper82_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///		static int vrom_switch;
/*TODO*///	
/*TODO*///		/* This mapper has problems */
/*TODO*///	
/*TODO*///		logerror("mapper82_m_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x1ef0:
/*TODO*///				/* Switch 2k VROM at $0000 or $1000 */
/*TODO*///				if (vrom_switch != 0)
/*TODO*///					chr2_4 (data);
/*TODO*///				else
/*TODO*///					chr2_0 (data);
/*TODO*///				break;
/*TODO*///			case 0x1ef1:
/*TODO*///				/* Switch 2k VROM at $0800 or $1800 */
/*TODO*///				if (vrom_switch != 0)
/*TODO*///					chr2_6 (data);
/*TODO*///				else
/*TODO*///					chr2_2 (data);
/*TODO*///				break;
/*TODO*///			case 0x1ef2:
/*TODO*///				/* Switch 1k VROM at $1000 */
/*TODO*///				nes_vram [4 ^ vrom_switch] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1ef3:
/*TODO*///				/* Switch 1k VROM at $1400 */
/*TODO*///				nes_vram [5 ^ vrom_switch] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1ef4:
/*TODO*///				/* Switch 1k VROM at $1800 */
/*TODO*///				nes_vram [6 ^ vrom_switch] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1ef5:
/*TODO*///				/* Switch 1k VROM at $1c00 */
/*TODO*///				nes_vram [7 ^ vrom_switch] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x1ef6:
/*TODO*///				vrom_switch = !((data & 0x02) << 1);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x1efa:
/*TODO*///				/* Switch 8k ROM at $8000 */
/*TODO*///				prg8_89 (data >> 2);
/*TODO*///				break;
/*TODO*///			case 0x1efb:
/*TODO*///				/* Switch 8k ROM at $a000 */
/*TODO*///				prg8_ab (data >> 2);
/*TODO*///				break;
/*TODO*///			case 0x1efc:
/*TODO*///				/* Switch 8k ROM at $c000 */
/*TODO*///				prg8_cd (data >> 2);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("mapper82_m_w uncaught addr: %04x, value: %02x\n", offset + 0x6000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void konami_vrc7_w (int offset, int data)
/*TODO*///	{
/*TODO*///	//	logerror("konami_vrc7_w offset: %04x, data: %02x, scanline: %d\n", offset, data, current_scanline);
/*TODO*///	
/*TODO*///		switch (offset & 0x7018)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				/* Switch 8k bank at $8000 */
/*TODO*///				prg8_89 (data);
/*TODO*///				break;
/*TODO*///			case 0x0008: case 0x0010: case 0x0018:
/*TODO*///				/* Switch 8k bank at $a000 */
/*TODO*///				prg8_ab (data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x1000:
/*TODO*///				/* Switch 8k bank at $c000 */
/*TODO*///				prg8_cd (data);
/*TODO*///				break;
/*TODO*///	
/*TODO*///	/* TODO: there are sound regs in here */
/*TODO*///	
/*TODO*///			case 0x2000:
/*TODO*///				/* Switch 1k VROM at $0000 */
/*TODO*///				nes_vram [0] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x2008: case 0x2010: case 0x2018:
/*TODO*///				/* Switch 1k VROM at $0400 */
/*TODO*///				nes_vram [1] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3000:
/*TODO*///				/* Switch 1k VROM at $0800 */
/*TODO*///				nes_vram [2] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x3008: case 0x3010: case 0x3018:
/*TODO*///				/* Switch 1k VROM at $0c00 */
/*TODO*///				nes_vram [3] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x4000:
/*TODO*///				/* Switch 1k VROM at $1000 */
/*TODO*///				nes_vram [4] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x4008: case 0x4010: case 0x4018:
/*TODO*///				/* Switch 1k VROM at $1400 */
/*TODO*///				nes_vram [5] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x5000:
/*TODO*///				/* Switch 1k VROM at $1800 */
/*TODO*///				nes_vram [6] = data * 64;
/*TODO*///				break;
/*TODO*///			case 0x5008: case 0x5010: case 0x5018:
/*TODO*///				/* Switch 1k VROM at $1c00 */
/*TODO*///				nes_vram [7] = data * 64;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 0x6000:
/*TODO*///				switch (data & 0x03)
/*TODO*///				{
/*TODO*///					case 0x00: ppu_mirror_v (); break;
/*TODO*///					case 0x01: ppu_mirror_h (); break;
/*TODO*///					case 0x02: ppu_mirror_low (); break;
/*TODO*///					case 0x03: ppu_mirror_high (); break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x6008: case 0x6010: case 0x6018:
/*TODO*///				IRQ_count_latch = data;
/*TODO*///				break;
/*TODO*///			case 0x7000:
/*TODO*///				IRQ_count = IRQ_count_latch;
/*TODO*///				IRQ_enable = data & 0x02;
/*TODO*///				IRQ_enable_latch = data & 0x01;
/*TODO*///				break;
/*TODO*///			case 0x7008: case 0x7010: case 0x7018:
/*TODO*///				IRQ_enable = IRQ_enable_latch;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			default:
/*TODO*///				logerror("konami_vrc7_w uncaught addr: %04x value: %02x\n", offset + 0x8000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper86_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper86_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x7541:
/*TODO*///				prg16_89ab (data >> 4);
/*TODO*///	//			prg16_cdef (data >> 4);
/*TODO*///				break;
/*TODO*///			case 0x7d41:
/*TODO*///	//			prg16_89ab (data >> 4);
/*TODO*///	//			prg16_cdef (data >> 4);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper87_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror("mapper87_m_w %04x:%02x\n", offset, data);
/*TODO*///	
/*TODO*///		/* TODO: verify */
/*TODO*///		chr8 (data >> 1);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper91_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///		logerror ("mapper91_m_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	
/*TODO*///		switch (offset & 0x7000)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				switch (offset & 0x03)
/*TODO*///				{
/*TODO*///					case 0x00: chr2_0 (data); break;
/*TODO*///					case 0x01: chr2_2 (data); break;
/*TODO*///					case 0x02: chr2_4 (data); break;
/*TODO*///					case 0x03: chr2_6 (data); break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			case 0x1000:
/*TODO*///				switch (offset & 0x01)
/*TODO*///				{
/*TODO*///					case 0x00: prg8_89 (data); break;
/*TODO*///					case 0x01: prg8_ab (data); break;
/*TODO*///				}
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				logerror("mapper91_m_w uncaught addr: %04x value: %02x\n", offset + 0x6000, data);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper93_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("mapper93_m_w %04x:%02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		prg16_89ab (data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper93_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("mapper93_w %04x:%02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///		/* The high nibble appears to be the same prg bank as */
/*TODO*///		/* was written to the mid-area mapper */
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper94_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("mapper94_w %04x:%02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		prg16_89ab (data >> 2);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper95_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("mapper95_w %04x:%02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		switch (offset)
/*TODO*///		{
/*TODO*///			case 0x0000:
/*TODO*///				/* Switch 8k bank at $8000 */
/*TODO*///	//			prg8_89 (data);
/*TODO*///	//			prg8_ab (data);
/*TODO*///	//			prg16_89ab (data);
/*TODO*///				break;
/*TODO*///			case 0x0001:
/*TODO*///				/* Switch 8k bank at $a000 */
/*TODO*///				prg8_ab (data >> 1);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper101_m_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("mapper101_m_w %04x:%02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		chr8 (data);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper101_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("mapper101_w %04x:%02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* ??? */
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper225_w (int offset, int data)
/*TODO*///	{
/*TODO*///		int hi_bank;
/*TODO*///		int size_16;
/*TODO*///		int bank;
/*TODO*///	
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror ("mapper225_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		chr8 (offset & 0x3f);
/*TODO*///		hi_bank = offset & 0x40;
/*TODO*///		size_16 = offset & 0x1000;
/*TODO*///		bank = (offset & 0xf80) >> 7;
/*TODO*///		if (size_16 != 0)
/*TODO*///		{
/*TODO*///			bank <<= 1;
/*TODO*///			if (hi_bank != 0)
/*TODO*///				bank ++;
/*TODO*///	
/*TODO*///			prg16_89ab (bank);
/*TODO*///			prg16_cdef (bank);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			prg32 (bank);
/*TODO*///	
/*TODO*///		if ((offset & 0x2000) != 0)
/*TODO*///			ppu_mirror_h();
/*TODO*///		else
/*TODO*///			ppu_mirror_v();
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper226_w (int offset, int data)
/*TODO*///	{
/*TODO*///		int hi_bank;
/*TODO*///		int size_16;
/*TODO*///		int bank;
/*TODO*///		static int reg0, reg1;
/*TODO*///	
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror ("mapper226_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		if ((offset & 0x01) != 0)
/*TODO*///		{
/*TODO*///			reg1 = data;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			reg0 = data;
/*TODO*///		}
/*TODO*///	
/*TODO*///		hi_bank = reg0 & 0x01;
/*TODO*///		size_16 = reg0 & 0x20;
/*TODO*///		if ((reg0 & 0x40) != 0)
/*TODO*///			ppu_mirror_h();
/*TODO*///		else
/*TODO*///			ppu_mirror_v();
/*TODO*///	
/*TODO*///		bank = ((reg0 & 0x1e) >> 1) | ((reg1 & 0x01) << 4);
/*TODO*///	
/*TODO*///		if (size_16 != 0)
/*TODO*///		{
/*TODO*///			bank <<= 1;
/*TODO*///			if (hi_bank != 0)
/*TODO*///				bank ++;
/*TODO*///	
/*TODO*///			prg16_89ab (bank);
/*TODO*///			prg16_cdef (bank);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			prg32 (bank);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper227_w (int offset, int data)
/*TODO*///	{
/*TODO*///		int hi_bank;
/*TODO*///		int size_32;
/*TODO*///		int bank;
/*TODO*///	
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror ("mapper227_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		hi_bank = offset & 0x04;
/*TODO*///		size_32 = offset & 0x01;
/*TODO*///		bank = ((offset & 0x78) >> 3) | ((offset & 0x0100) >> 4);
/*TODO*///		if (!size_32)
/*TODO*///		{
/*TODO*///			bank <<= 1;
/*TODO*///			if (hi_bank != 0)
/*TODO*///				bank ++;
/*TODO*///	
/*TODO*///			prg16_89ab (bank);
/*TODO*///			prg16_cdef (bank);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			prg32 (bank);
/*TODO*///	
/*TODO*///		if (!(offset & 0x80))
/*TODO*///		{
/*TODO*///			if ((offset & 0x200) != 0)
/*TODO*///				prg16_cdef ((bank >> 2) + 7);
/*TODO*///			else
/*TODO*///				prg16_cdef (bank >> 2);
/*TODO*///		}
/*TODO*///	
/*TODO*///		if ((offset & 0x02) != 0)
/*TODO*///			ppu_mirror_h();
/*TODO*///		else
/*TODO*///			ppu_mirror_v();
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper228_w (int offset, int data)
/*TODO*///	{
/*TODO*///		/* The address lines double as data */
/*TODO*///		/* --mPPppppPP-cccc */
/*TODO*///		int bank;
/*TODO*///		int chr;
/*TODO*///	
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror ("mapper228_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* Determine low 4 bits of program bank */
/*TODO*///		bank = (offset & 0x780) >> 7;
/*TODO*///	
/*TODO*///	#if 1
/*TODO*///		/* Determine high 2 bits of program bank */
/*TODO*///		switch (offset & 0x1800)
/*TODO*///		{
/*TODO*///			case 0x0800:
/*TODO*///				bank |= 0x10;
/*TODO*///				break;
/*TODO*///			case 0x1800:
/*TODO*///				bank |= 0x20;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	#else
/*TODO*///		bank |= (offset & 0x1800) >> 7;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* see if the bank value is 16k or 32k */
/*TODO*///		if ((offset & 0x20) != 0)
/*TODO*///		{
/*TODO*///			/* 16k bank value, adjust */
/*TODO*///			bank <<= 1;
/*TODO*///			if ((offset & 0x40) != 0)
/*TODO*///				bank ++;
/*TODO*///	
/*TODO*///			prg16_89ab (bank);
/*TODO*///			prg16_cdef (bank);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			prg32 (bank);
/*TODO*///		}
/*TODO*///	
/*TODO*///		if ((offset & 0x2000) != 0)
/*TODO*///			ppu_mirror_h();
/*TODO*///		else
/*TODO*///			ppu_mirror_v();
/*TODO*///	
/*TODO*///		/* Now handle vrom banking */
/*TODO*///		chr = (data & 0x03) + ((offset & 0x0f) << 2);
/*TODO*///		chr8 (chr);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper229_w (int offset, int data)
/*TODO*///	{
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror ("mapper229_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		if ((offset & 0x20) != 0)
/*TODO*///			ppu_mirror_h();
/*TODO*///		else
/*TODO*///			ppu_mirror_v();
/*TODO*///	
/*TODO*///		if ((offset & 0x1e) == 0)
/*TODO*///		{
/*TODO*///			prg32 (0);
/*TODO*///			chr8 (0);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			prg16_89ab (offset & 0x1f);
/*TODO*///			prg16_89ab (offset & 0x1f);
/*TODO*///			chr8 (offset);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void mapper231_w (int offset, int data)
/*TODO*///	{
/*TODO*///		int bank;
/*TODO*///	
/*TODO*///	#ifdef LOG_MMC
/*TODO*///		logerror("mapper231_w, offset: %04x, data: %02x\n", offset, data);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		bank = (data & 0x03) | ((data & 0x80) >> 5);
/*TODO*///		prg32 (bank);
/*TODO*///		chr8 ((data & 0x70) >> 4);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/*
/*TODO*///	// mapper_reset
/*TODO*///	//
/*TODO*///	// Resets the mapper bankswitch areas to their defaults. It returns a value "err"
/*TODO*///	// that indicates if it was successful. Possible values for err are:
/*TODO*///	//
/*TODO*///	// 0 = success
/*TODO*///	// 1 = no mapper found
/*TODO*///	// 2 = mapper not supported
/*TODO*///	*/
/*TODO*///	int mapper_reset (int mapperNum)
/*TODO*///	{
/*TODO*///		int err = 0;
/*TODO*///		int i;
/*TODO*///	
/*TODO*///		/* Set the vram bank-switch values to the default */
/*TODO*///		for (i = 0; i < 8; i ++)
/*TODO*///			nes_vram[i] = i * 64;
/*TODO*///	
/*TODO*///		mapper_warning = 0;
/*TODO*///		/* 8k mask */
/*TODO*///		prg_mask = ((nes.prg_chunks << 1) - 1);
/*TODO*///	
/*TODO*///		MMC5_vram_control = 0;
/*TODO*///	
/*TODO*///		/* Point the WRAM/battery area to the first RAM bank */
/*TODO*///		cpu_setbank (5, &nes.wram[0x0000]);
/*TODO*///	
/*TODO*///		switch (mapperNum)
/*TODO*///		{
/*TODO*///			case 0:
/*TODO*///				err = 1; /* No mapper found */
/*TODO*///				prg32(0);
/*TODO*///				break;
/*TODO*///			case 1:
/*TODO*///				/* Reset the latch */
/*TODO*///				MMC1_reg = 0;
/*TODO*///				MMC1_reg_count = 0;
/*TODO*///	
/*TODO*///				MMC1_Size_16k = 1;
/*TODO*///				MMC1_Switch_Low = 1;
/*TODO*///				MMC1_SizeVrom_4k = 0;
/*TODO*///				MMC1_extended_bank = 0;
/*TODO*///				MMC1_extended_swap = 0;
/*TODO*///				MMC1_extended_base = 0x10000;
/*TODO*///				MMC1_extended = ((nes.prg_chunks << 4) + nes.chr_chunks * 8) >> 9;
/*TODO*///	
/*TODO*///				if (!MMC1_extended)
/*TODO*///					/* Set it to the end of the prg rom */
/*TODO*///					MMC1_High = (nes.prg_chunks - 1) * 0x4000;
/*TODO*///				else
/*TODO*///					/* Set it to the end of the first 256k bank */
/*TODO*///					MMC1_High = 15 * 0x4000;
/*TODO*///	
/*TODO*///				MMC1_bank1 = 0;
/*TODO*///				MMC1_bank2 = 0x2000;
/*TODO*///				MMC1_bank3 = MMC1_High;
/*TODO*///				MMC1_bank4 = MMC1_High + 0x2000;
/*TODO*///	
/*TODO*///				cpu_setbank (1, &nes.rom[MMC1_extended_base + MMC1_bank1]);
/*TODO*///				cpu_setbank (2, &nes.rom[MMC1_extended_base + MMC1_bank2]);
/*TODO*///				cpu_setbank (3, &nes.rom[MMC1_extended_base + MMC1_bank3]);
/*TODO*///				cpu_setbank (4, &nes.rom[MMC1_extended_base + MMC1_bank4]);
/*TODO*///				logerror("-- page1: %06x\n", MMC1_bank1);
/*TODO*///				logerror("-- page2: %06x\n", MMC1_bank2);
/*TODO*///				logerror("-- page3: %06x\n", MMC1_bank3);
/*TODO*///				logerror("-- page4: %06x\n", MMC1_bank4);
/*TODO*///				break;
/*TODO*///			case 2:
/*TODO*///				/* These games don't switch VROM, but some ROMs incorrectly have CHR chunks */
/*TODO*///				nes.chr_chunks = 0;
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 3:
/*TODO*///				/* Doesn't bank-switch */
/*TODO*///				prg32(0);
/*TODO*///				break;
/*TODO*///			case 4:
/*TODO*///			case 118:
/*TODO*///				/* Can switch 8k prg banks */
/*TODO*///				IRQ_enable = 0;
/*TODO*///				IRQ_count = IRQ_count_latch = 0;
/*TODO*///				MMC3_prg0 = 0xfe;
/*TODO*///				MMC3_prg1 = 0xff;
/*TODO*///				MMC3_cmd = 0;
/*TODO*///				prg16_89ab (nes.prg_chunks-1);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 5:
/*TODO*///				/* Can switch 8k prg banks, but they are saved as 16k in size */
/*TODO*///				MMC5_rom_bank_mode = 3;
/*TODO*///				MMC5_vrom_bank_mode = 0;
/*TODO*///				MMC5_vram_protect = 0;
/*TODO*///				IRQ_enable = 0;
/*TODO*///				IRQ_count = 0;
/*TODO*///				nes.mid_ram_enable = 0;
/*TODO*///				prg16_89ab (nes.prg_chunks-2);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 7:
/*TODO*///				/* Bankswitches 32k at a time */
/*TODO*///				ppu_mirror_low ();
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 8:
/*TODO*///				/* Switches 16k banks at $8000, 1st 2 16k banks loaded on reset */
/*TODO*///				prg32(0);
/*TODO*///				break;
/*TODO*///			case 9:
/*TODO*///				/* Can switch 8k prg banks */
/*TODO*///				/* Note that the iNES header defines the number of banks as 8k in size, rather than 16k */
/*TODO*///				/* Reset VROM latches */
/*TODO*///				MMC2_bank0 = MMC2_bank1 = 0;
/*TODO*///				MMC2_bank0_hi = MMC2_bank1_hi = 0;
/*TODO*///				MMC2_bank0_latch = MMC2_bank1_latch = 0xfe;
/*TODO*///				cpu_setbank (1, &nes.rom[0x10000]);
/*TODO*///				cpu_setbank (2, &nes.rom[(nes.prg_chunks-2) * 0x4000 + 0x12000]);
/*TODO*///				cpu_setbank (3, &nes.rom[(nes.prg_chunks-1) * 0x4000 + 0x10000]);
/*TODO*///				cpu_setbank (4, &nes.rom[(nes.prg_chunks-1) * 0x4000 + 0x12000]);
/*TODO*///				break;
/*TODO*///			case 10:
/*TODO*///				/* Reset VROM latches */
/*TODO*///				MMC2_bank0 = MMC2_bank1 = 0;
/*TODO*///				MMC2_bank0_hi = MMC2_bank1_hi = 0;
/*TODO*///				MMC2_bank0_latch = MMC2_bank1_latch = 0xfe;
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 11:
/*TODO*///				/* Switches 32k banks, 1st 32k bank loaded on reset (?) May be more like mapper 7... */
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 15:
/*TODO*///				/* Can switch 8k prg banks */
/*TODO*///				prg32(0);
/*TODO*///				break;
/*TODO*///			case 16:
/*TODO*///			case 17:
/*TODO*///			case 18:
/*TODO*///			case 19:
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 20:
/*TODO*///				IRQ_enable = IRQ_enable_latch = 0;
/*TODO*///				IRQ_count = IRQ_count_latch = 0;
/*TODO*///				nes_fds.motor_on = 0;
/*TODO*///				nes_fds.door_closed = 0;
/*TODO*///				nes_fds.current_side = 1;
/*TODO*///				nes_fds.head_position = 0;
/*TODO*///				nes_fds.status0 = 0;
/*TODO*///				nes_fds.read_mode = nes_fds.write_reg = 0;
/*TODO*///				break;
/*TODO*///			case 21:
/*TODO*///			case 25:
/*TODO*///				IRQ_enable = IRQ_enable_latch = 0;
/*TODO*///				IRQ_count = IRQ_count_latch = 0;
/*TODO*///				/* fall through */
/*TODO*///			case 22:
/*TODO*///			case 23:
/*TODO*///			case 32:
/*TODO*///			case 33:
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 24:
/*TODO*///			case 26:
/*TODO*///			case 73:
/*TODO*///			case 75:
/*TODO*///				IRQ_enable = IRQ_enable_latch = 0;
/*TODO*///				IRQ_count = IRQ_count_latch = 0;
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 34:
/*TODO*///				/* Can switch 32k prg banks */
/*TODO*///				prg32(0);
/*TODO*///				break;
/*TODO*///			case 40:
/*TODO*///				IRQ_enable = 0;
/*TODO*///				IRQ_count = 0;
/*TODO*///				/* Who's your daddy? */
/*TODO*///				memcpy (&nes.rom[0x6000], &nes.rom[6 * 0x2000 + 0x10000], 0x2000);
/*TODO*///				prg8_89 (4);
/*TODO*///				prg8_ab (5);
/*TODO*///				prg8_cd (6);
/*TODO*///				prg8_ef (7);
/*TODO*///				break;
/*TODO*///			case 64:
/*TODO*///				/* Can switch 3 8k prg banks */
/*TODO*///				prg16_89ab (nes.prg_chunks-1);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 65:
/*TODO*///				IRQ_enable = 0;
/*TODO*///				IRQ_count = 0;
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 41:
/*TODO*///			case 66:
/*TODO*///				/* Can switch 32k prgm banks */
/*TODO*///				prg32(0);
/*TODO*///				break;
/*TODO*///			case 70:
/*TODO*///	//		case 86:
/*TODO*///				prg16_89ab (nes.prg_chunks-2);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 67:
/*TODO*///			case 68:
/*TODO*///			case 69:
/*TODO*///			case 71:
/*TODO*///			case 72:
/*TODO*///			case 77:
/*TODO*///			case 78:
/*TODO*///				IRQ_enable = IRQ_enable_latch = 0;
/*TODO*///				IRQ_count = IRQ_count_latch = 0;
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 79:
/*TODO*///				/* Mirroring always horizontal...? */
/*TODO*///	//			Mirroring = 1;
/*TODO*///				prg32(0);
/*TODO*///				break;
/*TODO*///			case 80:
/*TODO*///			case 82:
/*TODO*///			case 85:
/*TODO*///			case 86:
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///	//		case 70:
/*TODO*///			case 87:
/*TODO*///			case 228:
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 91:
/*TODO*///				ppu_mirror_v();
/*TODO*///	//			cpu_setbank (1, &nes.rom[0x10000]);
/*TODO*///	//			cpu_setbank (2, &nes.rom[0x12000]);
/*TODO*///				prg16_89ab (nes.prg_chunks-1);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 93:
/*TODO*///			case 94:
/*TODO*///			case 95:
/*TODO*///			case 96:
/*TODO*///			case 101:
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 97:
/*TODO*///	//			cpu_setbank (1, &nes.rom[0x10000]);
/*TODO*///	//			cpu_setbank (2, &nes.rom[0x12000]);
/*TODO*///	//			cpu_setbank (3, &nes.rom[0x14000]);
/*TODO*///	//			cpu_setbank (4, &nes.rom[0x16000]);
/*TODO*///				prg16_89ab (nes.prg_chunks-2);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			case 225:
/*TODO*///			case 226:
/*TODO*///			case 227:
/*TODO*///			case 229:
/*TODO*///				prg16_89ab (0);
/*TODO*///				prg16_cdef (0);
/*TODO*///				break;
/*TODO*///			case 231:
/*TODO*///				prg16_89ab (nes.prg_chunks-2);
/*TODO*///				prg16_cdef (nes.prg_chunks-1);
/*TODO*///				break;
/*TODO*///			default:
/*TODO*///				/* Mapper not supported */
/*TODO*///				err = 2;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (nes.chr_chunks)
/*TODO*///			memcpy (videoram, nes.vrom, 0x2000);
/*TODO*///	
/*TODO*///		return err;
/*TODO*///	}
/*TODO*///	
/*TODO*///	mmc mmc_list[] = {
/*TODO*///	/*	INES   DESC						LOW_W, LOW_R, MED_W, HIGH_W, PPU_latch, IRQ */
/*TODO*///		{ 0, "No Mapper",				NULL, NULL, NULL, NULL, NULL, NULL },
/*TODO*///		{ 1, "MMC1",					NULL, NULL, NULL, mapper1_w, NULL, NULL },
/*TODO*///		{ 2, "74161/32 ROM sw",			NULL, NULL, NULL, mapper2_w, NULL, NULL },
/*TODO*///		{ 3, "74161/32 VROM sw",		NULL, NULL, NULL, mapper3_w, NULL, NULL },
/*TODO*///		{ 4, "MMC3",					NULL, NULL, NULL, mapper4_w, NULL, mapper4_irq },
/*TODO*///		{ 5, "MMC5",					mapper5_l_w, mapper5_l_r, NULL, mapper5_w, NULL, mapper5_irq },
/*TODO*///		{ 7, "ROM Switch",				NULL, NULL, NULL, mapper7_w, NULL, NULL },
/*TODO*///		{ 8, "FFE F3xxxx",				NULL, NULL, NULL, mapper8_w, NULL, NULL },
/*TODO*///	//	{ 9, "MMC2",					NULL, NULL, NULL, mapper9_w, mapper9_latch, NULL},
/*TODO*///		{ 9, "MMC2",					NULL, NULL, NULL, mapper10_w, mapper10_latch, NULL},
/*TODO*///		{ 10, "MMC4",					NULL, NULL, NULL, mapper10_w, mapper10_latch, NULL },
/*TODO*///		{ 11, "Color Dreams Mapper",	NULL, NULL, NULL, mapper11_w, NULL, NULL },
/*TODO*///		{ 15, "100-in-1",				NULL, NULL, NULL, mapper15_w, NULL, NULL },
/*TODO*///		{ 16, "Bandai",					NULL, NULL, mapper16_w, mapper16_w, NULL, bandai_irq },
/*TODO*///		{ 17, "FFE F8xxx",				mapper17_l_w, NULL, NULL, NULL, NULL, mapper4_irq },
/*TODO*///		{ 18, "Jaleco",					NULL, NULL, NULL, mapper18_w, NULL, jaleco_irq },
/*TODO*///		{ 19, "Namco 106",				mapper19_l_w, NULL, NULL, mapper19_w, NULL, namcot_irq },
/*TODO*///		{ 20, "Famicom Disk System",	NULL, NULL, NULL, NULL, NULL, fds_irq },
/*TODO*///		{ 21, "Konami VRC 4",			NULL, NULL, NULL, konami_vrc4_w, NULL, konami_irq },
/*TODO*///		{ 22, "Konami VRC 2a",			NULL, NULL, NULL, konami_vrc2a_w, NULL, konami_irq },
/*TODO*///		{ 23, "Konami VRC 2b",			NULL, NULL, NULL, konami_vrc2b_w, NULL, konami_irq },
/*TODO*///		{ 24, "Konami VRC 6a",			NULL, NULL, NULL, konami_vrc6a_w, NULL, konami_irq },
/*TODO*///		{ 25, "Konami VRC 4",			NULL, NULL, NULL, konami_vrc4_w, NULL, konami_irq },
/*TODO*///		{ 26, "Konami VRC 6b",			NULL, NULL, NULL, konami_vrc6b_w, NULL, konami_irq },
/*TODO*///		{ 32, "Irem G-101",				NULL, NULL, NULL, mapper32_w, NULL, NULL },
/*TODO*///		{ 33, "Taito TC0190",			NULL, NULL, NULL, mapper33_w, NULL, NULL },
/*TODO*///		{ 34, "Nina-1",					NULL, NULL, mapper34_m_w, mapper34_w, NULL, NULL },
/*TODO*///		{ 40, "SMB2j (bootleg)",		NULL, NULL, NULL, mapper40_w, NULL, mapper40_irq },
/*TODO*///		{ 41, "Caltron 6-in-1",			NULL, NULL, mapper41_m_w, mapper41_w, NULL, NULL },
/*TODO*///	// 42 - "Mario Baby" pirate cart
/*TODO*///		{ 64, "Tengen",					NULL, NULL, mapper64_m_w, mapper64_w, NULL, mapper4_irq },
/*TODO*///		{ 65, "Irem H3001",				NULL, NULL, NULL, mapper65_w, NULL, irem_irq },
/*TODO*///		{ 66, "74161/32 Jaleco",		NULL, NULL, NULL, mapper66_w, NULL, NULL },
/*TODO*///		{ 67, "SunSoft 3",				NULL, NULL, NULL, mapper67_w, NULL, mapper4_irq },
/*TODO*///		{ 68, "SunSoft 4",				NULL, NULL, NULL, mapper68_w, NULL, NULL },
/*TODO*///		{ 69, "SunSoft 5",				NULL, NULL, NULL, mapper69_w, NULL, sunsoft_irq },
/*TODO*///		{ 70, "74161/32 Bandai",		NULL, NULL, NULL, mapper70_w, NULL, NULL },
/*TODO*///		{ 71, "Camerica",				NULL, NULL, mapper71_m_w, mapper71_w, NULL, NULL },
/*TODO*///		{ 72, "74161/32 Jaleco",		NULL, NULL, NULL, mapper72_w, NULL, NULL },
/*TODO*///		{ 73, "Konami VRC 3",			NULL, NULL, NULL, mapper73_w, NULL, konami_irq },
/*TODO*///		{ 75, "Konami VRC 1",			NULL, NULL, NULL, mapper75_w, NULL, NULL },
/*TODO*///		{ 77, "74161/32 ?",				NULL, NULL, NULL, mapper77_w, NULL, NULL },
/*TODO*///		{ 78, "74161/32 Irem",			NULL, NULL, NULL, mapper78_w, NULL, NULL },
/*TODO*///		{ 79, "Nina-3 (AVE)",			mapper79_l_w, NULL, mapper79_w, mapper79_w, NULL, NULL },
/*TODO*///		{ 80, "Taito X1-005",			NULL, NULL, mapper80_m_w, NULL, NULL, NULL },
/*TODO*///		{ 82, "Taito C075",				NULL, NULL, mapper82_m_w, NULL, NULL, NULL },
/*TODO*///	// 83
/*TODO*///		{ 84, "Pasofami",				NULL, NULL, NULL, NULL, NULL, NULL },
/*TODO*///		{ 85, "Konami VRC 7",			NULL, NULL, NULL, konami_vrc7_w, NULL, konami_irq },
/*TODO*///		{ 86, "?",				NULL, NULL, NULL, mapper86_w, NULL, NULL },
/*TODO*///		{ 87, "74161/32 VROM sw-a",		NULL, NULL, mapper87_m_w, NULL, NULL, NULL },
/*TODO*///		{ 88, "Namco 118",				NULL, NULL, NULL, mapper4_w, NULL, mapper4_irq },
/*TODO*///	// 90 - pirate mapper
/*TODO*///		{ 91, "HK-SF3 (bootleg)",		NULL, NULL, mapper91_m_w, NULL, NULL, NULL },
/*TODO*///		{ 93, "Sunsoft LS161",			NULL, NULL, mapper93_m_w, mapper93_w, NULL, NULL },
/*TODO*///		{ 94, "Capcom LS161",			NULL, NULL, NULL, mapper94_w, NULL, NULL },
/*TODO*///		{ 95, "Namco ??",				NULL, NULL, NULL, mapper95_w, NULL, NULL },
/*TODO*///		{ 96, "??",			NULL, NULL, NULL, NULL, NULL, NULL },
/*TODO*///		{ 97, "??",			NULL, NULL, NULL, NULL, NULL, NULL },
/*TODO*///	// 99 - vs. system
/*TODO*///	// 100 - images hacked to work with nesticle
/*TODO*///		{ 101, "?? LS161",				NULL, NULL, mapper101_m_w, mapper101_w, NULL, NULL },
/*TODO*///		{ 118, "MMC3?",					NULL, NULL, NULL, mapper118_w, NULL, mapper4_irq },
/*TODO*///	// 119 - Pinbot
/*TODO*///		{ 225, "72-in-1 bootleg",		NULL, NULL, NULL, mapper225_w, NULL, NULL },
/*TODO*///		{ 226, "76-in-1 bootleg",		NULL, NULL, NULL, mapper226_w, NULL, NULL },
/*TODO*///		{ 227, "1200-in-1 bootleg",		NULL, NULL, NULL, mapper227_w, NULL, NULL },
/*TODO*///		{ 228, "Action 52",				NULL, NULL, NULL, mapper228_w, NULL, NULL },
/*TODO*///		{ 229, "31-in-1",				NULL, NULL, NULL, mapper229_w, NULL, NULL },
/*TODO*///	//	{ 230, "22-in-1",				NULL, NULL, NULL, mapper230_w, NULL, NULL },
/*TODO*///		{ 231, "Nina-7 (AVE)",			NULL, NULL, NULL, mapper231_w, NULL, NULL },
/*TODO*///	// 234 - maxi-15
/*TODO*///		{ -1, "Not Supported",			NULL, NULL, NULL, NULL, NULL, NULL },
/*TODO*///	};
}
