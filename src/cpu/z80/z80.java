/*
 * ported to v0.37b7
 *
 */
package cpu.z80;

import static cpu.z80.z80H.*;
import static old.mame.cpuintrfH.*;
import static old.mame.driverH.*;
import static WIP.mame.memoryH.*;
import static WIP.mame.memory.*;
import static cpu.z80.z80daaH.DAATable;

public class z80 extends cpu_interface {

    static int[] z80_ICount = new int[1];

    public z80() {
        cpu_num = CPU_Z80;
        num_irqs = 1;
        default_vector = 255;
        overclock = 1.0;
        no_int = Z80_IGNORE_INT;
        irq_int = Z80_IRQ_INT;
        nmi_int = Z80_NMI_INT;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 4;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = z80_ICount;
        //intialize interfaces
        burn = burn_function;
    }

    /*TODO*///#define VERBOSE 0
/*TODO*///
/*TODO*///#if VERBOSE
/*TODO*///#define LOG(x)	logerror x
/*TODO*///#else
/*TODO*///#define LOG(x)
/*TODO*///#endif
/*TODO*///
/*TODO*////* big flags array for ADD/ADC/SUB/SBC/CP results */
/*TODO*///#define BIG_FLAGS_ARRAY     1
/*TODO*///
/*TODO*////* Set to 1 for a more exact (but somewhat slower) Z80 emulation */
/*TODO*///#define Z80_EXACT			1
/*TODO*///
/*TODO*////* on JP and JR opcodes check for tight loops */
/*TODO*///#define BUSY_LOOP_HACKS 	1
/*TODO*///
/*TODO*////* check for delay loops counting down BC */
/*TODO*///#define TIME_LOOP_HACKS 	1
/*TODO*///
/*TODO*///#ifdef X86_ASM
/*TODO*///#undef	BIG_FLAGS_ARRAY
/*TODO*///#define BIG_FLAGS_ARRAY 	0
/*TODO*///#endif
/*TODO*///
/*TODO*///static UINT8 z80_reg_layout[] = {
/*TODO*///    Z80_PC, Z80_SP, Z80_AF, Z80_BC, Z80_DE, Z80_HL, -1,
/*TODO*///    Z80_IX, Z80_IY, Z80_AF2,Z80_BC2,Z80_DE2,Z80_HL2,-1,
/*TODO*///    Z80_R,  Z80_I,  Z80_IM, Z80_IFF1,Z80_IFF2, -1,
/*TODO*///	Z80_NMI_STATE,Z80_IRQ_STATE,Z80_DC0,Z80_DC1,Z80_DC2,Z80_DC3, 0
/*TODO*///};
/*TODO*///
/*TODO*///static UINT8 z80_win_layout[] = {
/*TODO*///	27, 0,53, 4,	/* register window (top rows) */
/*TODO*///	 0, 0,26,22,	/* disassembler window (left colums) */
/*TODO*///	27, 5,53, 8,	/* memory #1 window (right, upper middle) */
/*TODO*///	27,14,53, 8,	/* memory #2 window (right, lower middle) */
/*TODO*///	 0,23,80, 1,	/* command line window (bottom rows) */
/*TODO*///};

    /****************************************************************************/
    /* The Z80 registers. HALT is set to 1 when the CPU is halted, the refresh  */
    /* register is calculated as follows: refresh=(Regs.R&127)|(Regs.R2&128)    */
    /****************************************************************************/
    public static class Z80_Regs
    {
        public int PREPC,PC,SP,A,F,B,C,D,E,H,L,IX,IY;/*TODO*////* 00 */    PAIR    PREPC,PC,SP,AF,BC,DE,HL,IX,IY;
        public int A2,F2,B2,C2,D2,E2,H2,L2;/*TODO*////* 24 */    PAIR    AF2,BC2,DE2,HL2;
        public int R,R2,IFF1,IFF2,HALT,IM,I;/*TODO*////* 34 */    UINT8   R,R2,IFF1,IFF2,HALT,IM,I;
        public int irq_max;            /* number of daisy chain devices        */
        public int request_irq;		/* daisy chain next request device		*/
        public int service_irq;		/* daisy chain next reti handling device */
        public int nmi_state;			/* nmi line state */
        public int irq_state;			/* irq line state */
/*TODO*////* 40 */    UINT8   int_state[Z80_MAXDAISY];
/*TODO*////* 44 */    Z80_DaisyChain irq[Z80_MAXDAISY];
        public irqcallbacksPtr irq_callback;
        public int extra_cycles;       /* extra cycles for interrupts */
    }

    private static int AF() {
        return ((Z80.A << 8) | Z80.F) & 0xFFFF;
    }

    public static int BC() {
        return ((Z80.B << 8) | Z80.C) & 0xFFFF;
    }

    public static int DE() {
        return ((Z80.D << 8) | Z80.E) & 0xFFFF;
    }

    public static int HL() {
        return ((Z80.H << 8) | Z80.L) & 0xFFFF;
    }

    private static void AF(int nn) {
        Z80.A = (nn >> 8) & 0xff;
        Z80.F = nn & 0xff;
    }

    private static void BC(int nn) {
        Z80.B = (nn >> 8) & 0xff;
        Z80.C = nn & 0xff;
    }

    private static void DE(int nn) {
        Z80.D = (nn >> 8) & 0xff;
        Z80.E = nn & 0xff;
    }

    private static void HL(int nn) {
        Z80.H = (nn >> 8) & 0xff;
        Z80.L = nn & 0xff;
    }
    
    public static final int  CF = 0x01;
    public static final int  NF = 0x02;
    public static final int  PF = 0x04;
    public static final int  VF = PF;
    public static final int  XF = 0x08;
    public static final int  HF = 0x10;
    public static final int  YF = 0x20;
    public static final int  ZF = 0x40;
    public static final int  SF = 0x80;

    public static final int  INT_IRQ = 0x01;
    public static final int  NMI_IRQ = 0x02;

/*TODO*///#define	_PPC	Z80.PREPC.d		/* previous program counter */
/*TODO*///
/*TODO*///#define _PCD	Z80.PC.d
/*TODO*///#define _PC 	Z80.PC.w.l
/*TODO*///
/*TODO*///#define _SPD	Z80.SP.d
/*TODO*///#define _SP 	Z80.SP.w.l
/*TODO*///
/*TODO*///#define _AFD	Z80.AF.d
/*TODO*///#define _AF 	Z80.AF.w.l
/*TODO*///#define _A		Z80.AF.b.h
/*TODO*///#define _F		Z80.AF.b.l
/*TODO*///
/*TODO*///#define _BCD	Z80.BC.d
/*TODO*///#define _BC 	Z80.BC.w.l
/*TODO*///#define _B		Z80.BC.b.h
/*TODO*///#define _C		Z80.BC.b.l
/*TODO*///
/*TODO*///#define _DED	Z80.DE.d
/*TODO*///#define _DE 	Z80.DE.w.l
/*TODO*///#define _D		Z80.DE.b.h
/*TODO*///#define _E		Z80.DE.b.l
/*TODO*///
/*TODO*///#define _HLD	Z80.HL.d
/*TODO*///#define _HL 	Z80.HL.w.l
/*TODO*///#define _H		Z80.HL.b.h
/*TODO*///#define _L		Z80.HL.b.l
/*TODO*///
/*TODO*///#define _IXD	Z80.IX.d
/*TODO*///#define _IX 	Z80.IX.w.l
/*TODO*///#define _HX 	Z80.IX.b.h
/*TODO*///#define _LX 	Z80.IX.b.l
/*TODO*///
/*TODO*///#define _IYD	Z80.IY.d
/*TODO*///#define _IY 	Z80.IY.w.l
/*TODO*///#define _HY 	Z80.IY.b.h
/*TODO*///#define _LY 	Z80.IY.b.l
/*TODO*///
/*TODO*///#define _I      Z80.I
/*TODO*///#define _R      Z80.R
/*TODO*///#define _R2     Z80.R2
/*TODO*///#define _IM     Z80.IM
/*TODO*///#define _IFF1	Z80.IFF1
/*TODO*///#define _IFF2	Z80.IFF2
/*TODO*///#define _HALT	Z80.HALT
    static Z80_Regs Z80 = new Z80_Regs();
    static int /*UINT32*/ EA;
    static int after_EI = 0;

    private static int SZ[] = new int[256];		/* zero and sign flags */
    private static int SZ_BIT[] = new int[256];	/* zero, sign and parity/overflow (=zero) flags for BIT opcode */
    private static int SZP[] = new int[256];		/* zero, sign and parity flags */
    private static int SZHV_inc[] = new int[256]; /* zero, sign, half carry and overflow flags INC r8 */
    private static int SZHV_dec[] = new int[256]; /* zero, sign, half carry and overflow flags DEC r8 */

    private static int SZHVC_Add[] = new int[2 * 256 * 256];
    private static int SZHVC_sub[] = new int[2 * 256 * 256];

    /* tmp1 value for ini/inir/outi/otir for [C.1-0][io.1-0] */
    static int irep_tmp1[][] = {
    	{0,0,1,0},{0,1,0,1},{1,0,1,1},{0,1,1,0}
    };

    /* tmp1 value for ind/indr/outd/otdr for [C.1-0][io.1-0] */
    static int drep_tmp1[][] = {
            {0,1,0,0},{1,0,0,1},{0,0,1,0},{0,1,0,1}
    };

    /* tmp2 value for all in/out repeated opcodes for B.7-0 */
    static int breg_tmp2[] = {
            0,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
            0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
            1,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
            1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
            0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
            1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
            0,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
            0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
            1,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
            1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
            0,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
            0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
            1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1,
            0,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
            1,1,0,0,1,0,1,1,0,0,1,1,0,1,0,0,
            1,0,1,1,0,1,0,0,1,1,0,0,1,0,1,1
    };

    static int cc_op[] = {
        4, 10, 7, 6, 4, 4, 7, 4, 4, 11, 7, 6, 4, 4, 7, 4,
        8, 10, 7, 6, 4, 4, 7, 4, 12, 11, 7, 6, 4, 4, 7, 4,
        7, 10, 16, 6, 4, 4, 7, 4, 7, 11, 16, 6, 4, 4, 7, 4,
        7, 10, 13, 6, 11, 11, 10, 4, 7, 11, 13, 6, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        7, 7, 7, 7, 7, 7, 4, 7, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4, 4, 7, 4,
        5, 10, 10, 10, 10, 11, 7, 11, 5, 10, 10, 0, 10, 17, 7, 11,
        5, 10, 10, 11, 10, 11, 7, 11, 5, 4, 10, 11, 10, 0, 7, 11,
        5, 10, 10, 19, 10, 11, 7, 11, 5, 4, 10, 4, 10, 0, 7, 11,
        5, 10, 10, 4, 10, 11, 7, 11, 5, 6, 10, 4, 10, 0, 7, 11};

    static int cc_cb[] = {
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
        8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
        8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
        8, 8, 8, 8, 8, 8, 12, 8, 8, 8, 8, 8, 8, 8, 12, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8,
        8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8, 8, 15, 8};

    static int cc_ed[] = {
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        12, 12, 15, 20, 8, 8, 8, 9, 12, 12, 15, 20, 8, 8, 8, 9,
        12, 12, 15, 20, 8, 8, 8, 9, 12, 12, 15, 20, 8, 8, 8, 9,
        12, 12, 15, 20, 8, 8, 8, 18, 12, 12, 15, 20, 8, 8, 8, 18,
        12, 12, 15, 20, 8, 8, 8, 8, 12, 12, 15, 20, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        16, 16, 16, 16, 8, 8, 8, 8, 16, 16, 16, 16, 8, 8, 8, 8,
        16, 16, 16, 16, 8, 8, 8, 8, 16, 16, 16, 16, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
        8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};

    static int cc_xy[] = {
        4, 4, 4, 4, 4, 4, 4, 4, 4, 15, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 15, 4, 4, 4, 4, 4, 4,
        4, 14, 20, 10, 9, 9, 9, 4, 4, 15, 20, 10, 9, 9, 9, 4,
        4, 4, 4, 4, 23, 23, 19, 4, 4, 15, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        9, 9, 9, 9, 9, 9, 19, 9, 9, 9, 9, 9, 9, 9, 19, 9,
        19, 19, 19, 19, 19, 19, 4, 19, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 9, 9, 19, 4, 4, 4, 4, 4, 9, 9, 19, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 14, 4, 23, 4, 15, 4, 4, 4, 8, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 10, 4, 4, 4, 4, 4, 4};

    static int cc_xycb[] = {
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
        23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23};

    /* extra cycles if jr/jp/call taken and 'interrupt latency' on rst 0-7 */
    static int cc_ex[] = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, /* DJNZ */
        5, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, /* JR NZ/JR Z */
        5, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, /* JR NC/JR C */
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        5, 5, 5, 5, 0, 0, 0, 0, 5, 5, 5, 5, 0, 0, 0, 0, /* LDIR/CPIR/INIR/OTIR LDDR/CPDR/INDR/OTDR */
        6, 0, 0, 0, 7, 0, 0, 2, 6, 0, 0, 0, 7, 0, 0, 2,
        6, 0, 0, 0, 7, 0, 0, 2, 6, 0, 0, 0, 7, 0, 0, 2,
        6, 0, 0, 0, 7, 0, 0, 2, 6, 0, 0, 0, 7, 0, 0, 2,
        6, 0, 0, 0, 7, 0, 0, 2, 6, 0, 0, 0, 7, 0, 0, 2};

    static int cc[][] = {cc_op, cc_cb, cc_ed, cc_xy, cc_xycb, cc_ex};
    
/*TODO*///static void take_interrupt(void);
/*TODO*///
/*TODO*///#define PROTOTYPES(tablename,prefix) \
/*TODO*///	INLINE void prefix##_00(void); INLINE void prefix##_01(void); INLINE void prefix##_02(void); INLINE void prefix##_03(void); \
/*TODO*///	INLINE void prefix##_04(void); INLINE void prefix##_05(void); INLINE void prefix##_06(void); INLINE void prefix##_07(void); \
/*TODO*///	INLINE void prefix##_08(void); INLINE void prefix##_09(void); INLINE void prefix##_0a(void); INLINE void prefix##_0b(void); \
/*TODO*///	INLINE void prefix##_0c(void); INLINE void prefix##_0d(void); INLINE void prefix##_0e(void); INLINE void prefix##_0f(void); \
/*TODO*///	INLINE void prefix##_10(void); INLINE void prefix##_11(void); INLINE void prefix##_12(void); INLINE void prefix##_13(void); \
/*TODO*///	INLINE void prefix##_14(void); INLINE void prefix##_15(void); INLINE void prefix##_16(void); INLINE void prefix##_17(void); \
/*TODO*///	INLINE void prefix##_18(void); INLINE void prefix##_19(void); INLINE void prefix##_1a(void); INLINE void prefix##_1b(void); \
/*TODO*///	INLINE void prefix##_1c(void); INLINE void prefix##_1d(void); INLINE void prefix##_1e(void); INLINE void prefix##_1f(void); \
/*TODO*///	INLINE void prefix##_20(void); INLINE void prefix##_21(void); INLINE void prefix##_22(void); INLINE void prefix##_23(void); \
/*TODO*///	INLINE void prefix##_24(void); INLINE void prefix##_25(void); INLINE void prefix##_26(void); INLINE void prefix##_27(void); \
/*TODO*///	INLINE void prefix##_28(void); INLINE void prefix##_29(void); INLINE void prefix##_2a(void); INLINE void prefix##_2b(void); \
/*TODO*///	INLINE void prefix##_2c(void); INLINE void prefix##_2d(void); INLINE void prefix##_2e(void); INLINE void prefix##_2f(void); \
/*TODO*///	INLINE void prefix##_30(void); INLINE void prefix##_31(void); INLINE void prefix##_32(void); INLINE void prefix##_33(void); \
/*TODO*///	INLINE void prefix##_34(void); INLINE void prefix##_35(void); INLINE void prefix##_36(void); INLINE void prefix##_37(void); \
/*TODO*///	INLINE void prefix##_38(void); INLINE void prefix##_39(void); INLINE void prefix##_3a(void); INLINE void prefix##_3b(void); \
/*TODO*///	INLINE void prefix##_3c(void); INLINE void prefix##_3d(void); INLINE void prefix##_3e(void); INLINE void prefix##_3f(void); \
/*TODO*///	INLINE void prefix##_40(void); INLINE void prefix##_41(void); INLINE void prefix##_42(void); INLINE void prefix##_43(void); \
/*TODO*///	INLINE void prefix##_44(void); INLINE void prefix##_45(void); INLINE void prefix##_46(void); INLINE void prefix##_47(void); \
/*TODO*///	INLINE void prefix##_48(void); INLINE void prefix##_49(void); INLINE void prefix##_4a(void); INLINE void prefix##_4b(void); \
/*TODO*///	INLINE void prefix##_4c(void); INLINE void prefix##_4d(void); INLINE void prefix##_4e(void); INLINE void prefix##_4f(void); \
/*TODO*///	INLINE void prefix##_50(void); INLINE void prefix##_51(void); INLINE void prefix##_52(void); INLINE void prefix##_53(void); \
/*TODO*///	INLINE void prefix##_54(void); INLINE void prefix##_55(void); INLINE void prefix##_56(void); INLINE void prefix##_57(void); \
/*TODO*///	INLINE void prefix##_58(void); INLINE void prefix##_59(void); INLINE void prefix##_5a(void); INLINE void prefix##_5b(void); \
/*TODO*///	INLINE void prefix##_5c(void); INLINE void prefix##_5d(void); INLINE void prefix##_5e(void); INLINE void prefix##_5f(void); \
/*TODO*///	INLINE void prefix##_60(void); INLINE void prefix##_61(void); INLINE void prefix##_62(void); INLINE void prefix##_63(void); \
/*TODO*///	INLINE void prefix##_64(void); INLINE void prefix##_65(void); INLINE void prefix##_66(void); INLINE void prefix##_67(void); \
/*TODO*///	INLINE void prefix##_68(void); INLINE void prefix##_69(void); INLINE void prefix##_6a(void); INLINE void prefix##_6b(void); \
/*TODO*///	INLINE void prefix##_6c(void); INLINE void prefix##_6d(void); INLINE void prefix##_6e(void); INLINE void prefix##_6f(void); \
/*TODO*///	INLINE void prefix##_70(void); INLINE void prefix##_71(void); INLINE void prefix##_72(void); INLINE void prefix##_73(void); \
/*TODO*///	INLINE void prefix##_74(void); INLINE void prefix##_75(void); INLINE void prefix##_76(void); INLINE void prefix##_77(void); \
/*TODO*///	INLINE void prefix##_78(void); INLINE void prefix##_79(void); INLINE void prefix##_7a(void); INLINE void prefix##_7b(void); \
/*TODO*///	INLINE void prefix##_7c(void); INLINE void prefix##_7d(void); INLINE void prefix##_7e(void); INLINE void prefix##_7f(void); \
/*TODO*///	INLINE void prefix##_80(void); INLINE void prefix##_81(void); INLINE void prefix##_82(void); INLINE void prefix##_83(void); \
/*TODO*///	INLINE void prefix##_84(void); INLINE void prefix##_85(void); INLINE void prefix##_86(void); INLINE void prefix##_87(void); \
/*TODO*///	INLINE void prefix##_88(void); INLINE void prefix##_89(void); INLINE void prefix##_8a(void); INLINE void prefix##_8b(void); \
/*TODO*///	INLINE void prefix##_8c(void); INLINE void prefix##_8d(void); INLINE void prefix##_8e(void); INLINE void prefix##_8f(void); \
/*TODO*///	INLINE void prefix##_90(void); INLINE void prefix##_91(void); INLINE void prefix##_92(void); INLINE void prefix##_93(void); \
/*TODO*///	INLINE void prefix##_94(void); INLINE void prefix##_95(void); INLINE void prefix##_96(void); INLINE void prefix##_97(void); \
/*TODO*///	INLINE void prefix##_98(void); INLINE void prefix##_99(void); INLINE void prefix##_9a(void); INLINE void prefix##_9b(void); \
/*TODO*///	INLINE void prefix##_9c(void); INLINE void prefix##_9d(void); INLINE void prefix##_9e(void); INLINE void prefix##_9f(void); \
/*TODO*///	INLINE void prefix##_a0(void); INLINE void prefix##_a1(void); INLINE void prefix##_a2(void); INLINE void prefix##_a3(void); \
/*TODO*///	INLINE void prefix##_a4(void); INLINE void prefix##_a5(void); INLINE void prefix##_a6(void); INLINE void prefix##_a7(void); \
/*TODO*///	INLINE void prefix##_a8(void); INLINE void prefix##_a9(void); INLINE void prefix##_aa(void); INLINE void prefix##_ab(void); \
/*TODO*///	INLINE void prefix##_ac(void); INLINE void prefix##_ad(void); INLINE void prefix##_ae(void); INLINE void prefix##_af(void); \
/*TODO*///	INLINE void prefix##_b0(void); INLINE void prefix##_b1(void); INLINE void prefix##_b2(void); INLINE void prefix##_b3(void); \
/*TODO*///	INLINE void prefix##_b4(void); INLINE void prefix##_b5(void); INLINE void prefix##_b6(void); INLINE void prefix##_b7(void); \
/*TODO*///	INLINE void prefix##_b8(void); INLINE void prefix##_b9(void); INLINE void prefix##_ba(void); INLINE void prefix##_bb(void); \
/*TODO*///	INLINE void prefix##_bc(void); INLINE void prefix##_bd(void); INLINE void prefix##_be(void); INLINE void prefix##_bf(void); \
/*TODO*///	INLINE void prefix##_c0(void); INLINE void prefix##_c1(void); INLINE void prefix##_c2(void); INLINE void prefix##_c3(void); \
/*TODO*///	INLINE void prefix##_c4(void); INLINE void prefix##_c5(void); INLINE void prefix##_c6(void); INLINE void prefix##_c7(void); \
/*TODO*///	INLINE void prefix##_c8(void); INLINE void prefix##_c9(void); INLINE void prefix##_ca(void); INLINE void prefix##_cb(void); \
/*TODO*///	INLINE void prefix##_cc(void); INLINE void prefix##_cd(void); INLINE void prefix##_ce(void); INLINE void prefix##_cf(void); \
/*TODO*///	INLINE void prefix##_d0(void); INLINE void prefix##_d1(void); INLINE void prefix##_d2(void); INLINE void prefix##_d3(void); \
/*TODO*///	INLINE void prefix##_d4(void); INLINE void prefix##_d5(void); INLINE void prefix##_d6(void); INLINE void prefix##_d7(void); \
/*TODO*///	INLINE void prefix##_d8(void); INLINE void prefix##_d9(void); INLINE void prefix##_da(void); INLINE void prefix##_db(void); \
/*TODO*///	INLINE void prefix##_dc(void); INLINE void prefix##_dd(void); INLINE void prefix##_de(void); INLINE void prefix##_df(void); \
/*TODO*///	INLINE void prefix##_e0(void); INLINE void prefix##_e1(void); INLINE void prefix##_e2(void); INLINE void prefix##_e3(void); \
/*TODO*///	INLINE void prefix##_e4(void); INLINE void prefix##_e5(void); INLINE void prefix##_e6(void); INLINE void prefix##_e7(void); \
/*TODO*///	INLINE void prefix##_e8(void); INLINE void prefix##_e9(void); INLINE void prefix##_ea(void); INLINE void prefix##_eb(void); \
/*TODO*///	INLINE void prefix##_ec(void); INLINE void prefix##_ed(void); INLINE void prefix##_ee(void); INLINE void prefix##_ef(void); \
/*TODO*///	INLINE void prefix##_f0(void); INLINE void prefix##_f1(void); INLINE void prefix##_f2(void); INLINE void prefix##_f3(void); \
/*TODO*///	INLINE void prefix##_f4(void); INLINE void prefix##_f5(void); INLINE void prefix##_f6(void); INLINE void prefix##_f7(void); \
/*TODO*///	INLINE void prefix##_f8(void); INLINE void prefix##_f9(void); INLINE void prefix##_fa(void); INLINE void prefix##_fb(void); \
/*TODO*///	INLINE void prefix##_fc(void); INLINE void prefix##_fd(void); INLINE void prefix##_fe(void); INLINE void prefix##_ff(void); \
/*TODO*///static void (*tablename[0x100])(void) = {	\
/*TODO*///    prefix##_00,prefix##_01,prefix##_02,prefix##_03,prefix##_04,prefix##_05,prefix##_06,prefix##_07, \
/*TODO*///    prefix##_08,prefix##_09,prefix##_0a,prefix##_0b,prefix##_0c,prefix##_0d,prefix##_0e,prefix##_0f, \
/*TODO*///    prefix##_10,prefix##_11,prefix##_12,prefix##_13,prefix##_14,prefix##_15,prefix##_16,prefix##_17, \
/*TODO*///    prefix##_18,prefix##_19,prefix##_1a,prefix##_1b,prefix##_1c,prefix##_1d,prefix##_1e,prefix##_1f, \
/*TODO*///    prefix##_20,prefix##_21,prefix##_22,prefix##_23,prefix##_24,prefix##_25,prefix##_26,prefix##_27, \
/*TODO*///    prefix##_28,prefix##_29,prefix##_2a,prefix##_2b,prefix##_2c,prefix##_2d,prefix##_2e,prefix##_2f, \
/*TODO*///    prefix##_30,prefix##_31,prefix##_32,prefix##_33,prefix##_34,prefix##_35,prefix##_36,prefix##_37, \
/*TODO*///    prefix##_38,prefix##_39,prefix##_3a,prefix##_3b,prefix##_3c,prefix##_3d,prefix##_3e,prefix##_3f, \
/*TODO*///    prefix##_40,prefix##_41,prefix##_42,prefix##_43,prefix##_44,prefix##_45,prefix##_46,prefix##_47, \
/*TODO*///    prefix##_48,prefix##_49,prefix##_4a,prefix##_4b,prefix##_4c,prefix##_4d,prefix##_4e,prefix##_4f, \
/*TODO*///    prefix##_50,prefix##_51,prefix##_52,prefix##_53,prefix##_54,prefix##_55,prefix##_56,prefix##_57, \
/*TODO*///    prefix##_58,prefix##_59,prefix##_5a,prefix##_5b,prefix##_5c,prefix##_5d,prefix##_5e,prefix##_5f, \
/*TODO*///    prefix##_60,prefix##_61,prefix##_62,prefix##_63,prefix##_64,prefix##_65,prefix##_66,prefix##_67, \
/*TODO*///    prefix##_68,prefix##_69,prefix##_6a,prefix##_6b,prefix##_6c,prefix##_6d,prefix##_6e,prefix##_6f, \
/*TODO*///    prefix##_70,prefix##_71,prefix##_72,prefix##_73,prefix##_74,prefix##_75,prefix##_76,prefix##_77, \
/*TODO*///    prefix##_78,prefix##_79,prefix##_7a,prefix##_7b,prefix##_7c,prefix##_7d,prefix##_7e,prefix##_7f, \
/*TODO*///    prefix##_80,prefix##_81,prefix##_82,prefix##_83,prefix##_84,prefix##_85,prefix##_86,prefix##_87, \
/*TODO*///    prefix##_88,prefix##_89,prefix##_8a,prefix##_8b,prefix##_8c,prefix##_8d,prefix##_8e,prefix##_8f, \
/*TODO*///    prefix##_90,prefix##_91,prefix##_92,prefix##_93,prefix##_94,prefix##_95,prefix##_96,prefix##_97, \
/*TODO*///    prefix##_98,prefix##_99,prefix##_9a,prefix##_9b,prefix##_9c,prefix##_9d,prefix##_9e,prefix##_9f, \
/*TODO*///    prefix##_a0,prefix##_a1,prefix##_a2,prefix##_a3,prefix##_a4,prefix##_a5,prefix##_a6,prefix##_a7, \
/*TODO*///    prefix##_a8,prefix##_a9,prefix##_aa,prefix##_ab,prefix##_ac,prefix##_ad,prefix##_ae,prefix##_af, \
/*TODO*///    prefix##_b0,prefix##_b1,prefix##_b2,prefix##_b3,prefix##_b4,prefix##_b5,prefix##_b6,prefix##_b7, \
/*TODO*///    prefix##_b8,prefix##_b9,prefix##_ba,prefix##_bb,prefix##_bc,prefix##_bd,prefix##_be,prefix##_bf, \
/*TODO*///    prefix##_c0,prefix##_c1,prefix##_c2,prefix##_c3,prefix##_c4,prefix##_c5,prefix##_c6,prefix##_c7, \
/*TODO*///    prefix##_c8,prefix##_c9,prefix##_ca,prefix##_cb,prefix##_cc,prefix##_cd,prefix##_ce,prefix##_cf, \
/*TODO*///    prefix##_d0,prefix##_d1,prefix##_d2,prefix##_d3,prefix##_d4,prefix##_d5,prefix##_d6,prefix##_d7, \
/*TODO*///    prefix##_d8,prefix##_d9,prefix##_da,prefix##_db,prefix##_dc,prefix##_dd,prefix##_de,prefix##_df, \
/*TODO*///    prefix##_e0,prefix##_e1,prefix##_e2,prefix##_e3,prefix##_e4,prefix##_e5,prefix##_e6,prefix##_e7, \
/*TODO*///    prefix##_e8,prefix##_e9,prefix##_ea,prefix##_eb,prefix##_ec,prefix##_ed,prefix##_ee,prefix##_ef, \
/*TODO*///    prefix##_f0,prefix##_f1,prefix##_f2,prefix##_f3,prefix##_f4,prefix##_f5,prefix##_f6,prefix##_f7, \
/*TODO*///	prefix##_f8,prefix##_f9,prefix##_fa,prefix##_fb,prefix##_fc,prefix##_fd,prefix##_fe,prefix##_ff  \
/*TODO*///}
/*TODO*///
/*TODO*///PROTOTYPES(Z80op,op);
/*TODO*///PROTOTYPES(Z80cb,cb);
/*TODO*///PROTOTYPES(Z80dd,dd);
/*TODO*///PROTOTYPES(Z80ed,ed);
/*TODO*///PROTOTYPES(Z80fd,fd);
/*TODO*///PROTOTYPES(Z80xycb,xycb);
/*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* Burn an odd amount of cycles, that is instructions taking something      */
/*TODO*////* different from 4 T-states per opcode (and R increment)                   */
/*TODO*////****************************************************************************/
/*TODO*///INLINE void BURNODD(int cycles, int opcodes, int cyclesum)
/*TODO*///{
/*TODO*///    if( cycles > 0 )
/*TODO*///    {
/*TODO*///		_R += (cycles / cyclesum) * opcodes;
/*TODO*///		z80_ICount -= (cycles / cyclesum) * cyclesum;
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * define an opcode function
/*TODO*/// ***************************************************************/
/*TODO*///#define OP(prefix,opcode)  INLINE void prefix##_##opcode(void)
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * adjust cycle count by n T-states
/*TODO*/// ***************************************************************/
/*TODO*///#define CC(prefix,opcode) z80_ICount -= cc[Z80_TABLE_##prefix][opcode]
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * execute an opcode
/*TODO*/// ***************************************************************/
/*TODO*///#define EXEC(prefix,opcode) 									\
/*TODO*///{																\
/*TODO*///	unsigned op = opcode;										\
/*TODO*///	CC(prefix,op);												\
/*TODO*///	(*Z80##prefix[op])();										\
/*TODO*///}

    /***************************************************************
     * Enter HALT state; write 1 to fake port on first execution
     ***************************************************************/
    public void ENTER_HALT() {											
        Z80.PC= (Z80.PC - 1 ) & 0xFFFF;//_PC--;                                                      
        Z80.HALT = 1;                                                  
            if( after_EI==0 ) 											
                    burn.handler(z80_ICount[0] ); 								
    }

    /***************************************************************
     * Leave HALT state; write 0 to fake port
     ***************************************************************/
    public static void LEAVE_HALT() {                                            
        if(Z80.HALT!=0 ) 												
        {															
            Z80.HALT = 0;												
            Z80.PC= (Z80.PC+1) & 0xFFFF;													
        }															
    }

    /***************************************************************
     * Input a byte from given I/O port
     ***************************************************************/
    public static int IN(int port)
    {
        return cpu_readport(port)& 0xff;
    }

    /***************************************************************
     * Output a byte to given I/O port
     ***************************************************************/
    public static void OUT(int port,int value)
    {
        cpu_writeport(port,value&0xFF);
    }

    /***************************************************************
     * Read a byte from given memory location
     ***************************************************************/
    public static int RM(int addr)
    {
        return cpu_readmem16(addr)&0xFF;
    }

/*TODO*////***************************************************************
/*TODO*/// * Read a word from given memory location
/*TODO*/// ***************************************************************/
/*TODO*///INLINE void RM16( UINT32 addr, PAIR *r )
/*TODO*///{
/*TODO*///	r->b.l = RM(addr);
/*TODO*///	r->b.h = RM((addr+1)&0xffff);
/*TODO*///}
    public static int RM16(int addr)//TODO recheck
    {
        return (RM(addr) | (RM((addr + 1) & 0xffff) << 8)) & 0xFFFF;
    }

    /***************************************************************
     * Write a byte to given memory location
     ***************************************************************/
    public static void WM(int addr,int value)
    {
        cpu_writemem16(addr,value&0xFF);
    }

    /***************************************************************
     * Write a word to given memory location
     ***************************************************************/
    public static void WM16(int address, int data) {
        WM(address, data & 0xFF);
        WM((address + 1) & 0xffff, data >> 8);
    }

    /***************************************************************
     * ROP() is identical to RM() except it is used for
     * reading opcodes. In case of system with memory mapped I/O,
     * this function can be used to greatly speed up emulation
     ***************************************************************/
    public static /*UINT8*/ int ROP() {
        int pc = Z80.PC & 0xFFFF;
        Z80.PC = (Z80.PC + 1) & 0xFFFF;
        return cpu_readop(pc) & 0xFF;
    }

    /****************************************************************
     * ARG() is identical to ROP() except it is used
     * for reading opcode arguments. This difference can be used to
     * support systems that use different encoding mechanisms for
     * opcodes and opcode arguments
     ***************************************************************/
    public static /*UINT8*/ int ARG() {
        int pc = Z80.PC & 0xFFFF;
        Z80.PC = (Z80.PC + 1 & 0xFFFF);
        return cpu_readop_arg(pc) & 0xFF;
    }
    
    public static int /*UINT32*/ ARG16() {
        int pc = Z80.PC & 0xFFFF;
        Z80.PC = (Z80.PC + 2) & 0xFFFF;
        //return cpu_readop_arg(pc) | (cpu_readop_arg((pc+1)&0xffff) << 8);
        return (cpu_readop_arg(pc) | (cpu_readop_arg((pc + 1) & 0xffff) << 8)) & 0xFFFF;
    }

    /***************************************************************
     * Calculate the effective address EA of an opcode using
     * IX+offset resp. IY+offset addressing.
     ***************************************************************/
    public static void EAX() {
        EA= (Z80.IX + (byte) ARG()) & 0xffff;
    }

    public static void EAY() {
        EA= (Z80.IY + (byte) ARG()) & 0xffff;
    }

    /***************************************************************
     * POP
     ***************************************************************/
    public static int POP() {
        int nn = RM16(Z80.SP);//RM16( _SPD, &Z80.DR );
        Z80.SP = (Z80.SP + 2) & 0xffff;
        return nn;
    }

    /***************************************************************
     * PUSH
     ***************************************************************/
    public static void PUSH(int nn) {
        Z80.SP = (Z80.SP - 2) & 0xffff;
        WM16(Z80.SP, nn);
    }

    /***************************************************************
     * JP
     ***************************************************************/
    public static void JP()
    {
        Z80.PC= ARG16();
        change_pc16(Z80.PC);
    }

    /***************************************************************
     * JP_COND
     ***************************************************************/
    public static void JP_COND(boolean cond)
    {
            if( cond )													
            {															
                    Z80.PC = ARG16(); 										
                    change_pc16(Z80.PC);										
            }															
            else														
            {															
                    Z80.PC = (Z80.PC + 2) & 0xFFFF;												
        }
    }
    
/*TODO*////***************************************************************
/*TODO*/// * JR
/*TODO*/// ***************************************************************/
    public static void JR()
    {
        byte arg = (byte)ARG();
        Z80.PC = (Z80.PC+arg)&0xFFFF;
        change_pc16(Z80.PC);
    }
/*TODO*///#define JR()													
/*TODO*///{																\
/*TODO*///	unsigned oldpc = _PCD-1;									\
/*TODO*///	INT8 arg = (INT8)ARG(); /* ARG() also increments _PC */ 	\
/*TODO*///	_PC += arg; 			/* so don't do _PC += ARG() */      \
/*TODO*///	change_pc16(_PCD);											\
/*TODO*///    /* speed up busy loop */                                    \
/*TODO*///	if( _PCD == oldpc ) 										\
/*TODO*///	{															\
/*TODO*///		if( !after_EI ) 										\
/*TODO*///			BURNODD( z80_ICount, 1, cc[Z80_TABLE_op][0x18] );	\
/*TODO*///	}															\
/*TODO*///	else														\
/*TODO*///	{															\
/*TODO*///		UINT8 op = cpu_readop(_PCD);							\
/*TODO*///		if( _PCD == oldpc-1 )									\
/*TODO*///		{														\
/*TODO*///			/* NOP - JR $-1 or EI - JR $-1 */					\
/*TODO*///			if ( op == 0x00 || op == 0xfb ) 					\
/*TODO*///			{													\
/*TODO*///				if( !after_EI ) 								\
/*TODO*///				   BURNODD( z80_ICount-cc[Z80_TABLE_op][0x00],	\
/*TODO*///					   2, cc[Z80_TABLE_op][0x00]+cc[Z80_TABLE_op][0x18]); \
/*TODO*///			}													\
/*TODO*///		}														\
/*TODO*///		else													\
/*TODO*///		/* LD SP,#xxxx - JR $-3 */								\
/*TODO*///		if( _PCD == oldpc-3 && op == 0x31 ) 					\
/*TODO*///		{														\
/*TODO*///			if( !after_EI ) 									\
/*TODO*///			   BURNODD( z80_ICount-cc[Z80_TABLE_op][0x31],		\
/*TODO*///				   2, cc[Z80_TABLE_op][0x31]+cc[Z80_TABLE_op][0x18]); \
/*TODO*///		}														\
/*TODO*///    }                                                           \
/*TODO*///}

    /***************************************************************
     * JR_COND
     ***************************************************************/
    public static void JR_COND(boolean cond,int opcode)
    {
        if(cond)
        {
            byte arg = (byte)ARG();
            Z80.PC = (Z80.PC+arg)&0xFFFF;
            z80_ICount[0] -= cc[Z80_TABLE_ex][opcode];//CC(ex,opcode);											
            change_pc16(Z80.PC);
        }
        else
        {
            Z80.PC = (Z80.PC + 1) & 0xFFFF;
        }
    }

    /***************************************************************
     * CALL
     ***************************************************************/
    public static void CALL()
    {
            EA = ARG16();												
            PUSH( Z80.PC ); 												
            Z80.PC = EA;													
            change_pc16(Z80.PC);
    }

    /***************************************************************
     * CALL_COND
     ***************************************************************/
    public static void CALL_COND(boolean cond,int opcode)	
    {
            if( cond )													
            {															
                    EA = ARG16();											
                    PUSH( Z80.PC ); 												
                    Z80.PC = EA;												
                    z80_ICount[0] -= cc[Z80_TABLE_ex][opcode];											
                    change_pc16(Z80.PC);										
            }															
            else														
            {															
                    Z80.PC = (Z80.PC + 2) & 0xFFFF; 												
            }
    }

    /***************************************************************
     * RET_COND
     ***************************************************************/
    public static void RET_COND(boolean cond,int opcode)
    {
            if( cond )													
            {															
                    Z80.PC=POP();												
                    change_pc16(Z80.PC);										
                    z80_ICount[0] -= cc[Z80_TABLE_ex][opcode];											
            }
    }
    
    /***************************************************************
     * RETN
     ***************************************************************/
    public static void RETN()	
    {												
        //LOG(("Z80 #%d RETN IFF1:%d IFF2:%d\n", cpu_getactivecpu(), _IFF1, _IFF2)); 
        Z80.PC=POP();													
        change_pc16(Z80.PC);											
        if( Z80.IFF1 == 0 && Z80.IFF2 == 1 )                              
        {															
            Z80.IFF1 = 1;												
            if( Z80.irq_state != CLEAR_LINE || Z80.request_irq >= 0 )								
            {														
                //LOG(("Z80 #%d RETN takes IRQ\n",cpu_getactivecpu()));							
                take_interrupt();									
            }                                                       
        }															
        else 
        {
            Z80.IFF1 = Z80.IFF2;
        } 										
    }

    /***************************************************************
     * RETI
     ***************************************************************/
    public static void RETI()	
    {												
        int device = Z80.service_irq;								
        Z80.PC=POP();													
        change_pc16(Z80.PC);                                          
        /* according to http://www.msxnet.org/tech/Z80/z80undoc.txt */  
        /*	_IFF1 = _IFF2;	*/											
        if( device >= 0 )											
        {	
            throw new UnsupportedOperationException("unimplemented");
                    //LOG(("Z80 #%d RETI device %d: $%02x\n",                 
    /*TODO*///			cpu_getactivecpu(), device, Z80.irq[device].irq_param)); 
    /*TODO*///		Z80.irq[device].interrupt_reti(Z80.irq[device].irq_param); 
        }															
    }

    //
/*TODO*////***************************************************************
/*TODO*/// * LD	R,A
/*TODO*/// ***************************************************************/
/*TODO*///#define LD_R_A {												\
/*TODO*///	_R = _A;													\
/*TODO*///	_R2 = _A & 0x80;				/* keep bit 7 of R */		\
/*TODO*///}
/*TODO*///
    /***************************************************************
     * LD	A,R
     ***************************************************************/
    public static void LD_A_R() 
    {												
            Z80.A = ((Z80.R & 0x7f) | Z80.R2)&0xFF; 									
            Z80.F = (Z80.F & CF) | SZ[Z80.A] | ( Z80.IFF2 << 2 );					
    }

    /***************************************************************
     * LD	I,A
     ***************************************************************/
    public static void LD_I_A() {												
            Z80.I = Z80.A & 0xFF;													
    }

    /***************************************************************
     * LD	A,I
     ***************************************************************/
    public static void LD_A_I() {
        Z80.A = Z80.I & 0xFF;
        Z80.F = (Z80.F & CF) | SZ[Z80.A] | (Z80.IFF2 << 2);
    }

    /***************************************************************
     * RST
     ***************************************************************/
    public static void RST(int addr)
    {
        PUSH(Z80.PC);
        Z80.PC = addr & 0xFFFF;
        change_pc16(Z80.PC);
    }
    /***************************************************************
     * INC	r8
     ***************************************************************/
    public static int INC(int value) {
        value = (value + 1) & 0xFF;
        Z80.F = (Z80.F & CF | SZHV_inc[value]);
        return value;
    }

    /***************************************************************
     * DEC	r8
     ***************************************************************/
    public static int DEC(int value) {
        value = (value - 1) & 0xFF;
        Z80.F = (Z80.F & CF | SZHV_dec[value]);
        return value;
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * RLCA
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define RLCA													\
/*TODO*///	_A = (_A << 1) | (_A >> 7); 								\
/*TODO*///	_F = (_F & (SF | ZF | PF)) | (_A & (YF | XF | CF))
/*TODO*///#else
/*TODO*///#define RLCA                                                    \
/*TODO*///	_A = (_A << 1) | (_A >> 7); 								\
/*TODO*///	_F = (_F & (SF | ZF | YF | XF | PF)) | (_A & CF)
/*TODO*///#endif
/*TODO*///
    public static void RLCA()
    {
        Z80.A = ((Z80.A << 1) | (Z80.A >> 7))&0xFF; 								
        Z80.F = (Z80.F & (SF | ZF | PF)) | (Z80.A & (YF | XF | CF)); 
    }
/*TODO*////***************************************************************
/*TODO*/// * RRCA
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define RRCA                                                    \
/*TODO*///	_F = (_F & (SF | ZF | PF)) | (_A & (YF | XF | CF)); 		\
/*TODO*///    _A = (_A >> 1) | (_A << 7)
/*TODO*///#else
/*TODO*///#define RRCA                                                    \
/*TODO*///	_F = (_F & (SF | ZF | YF | XF | PF)) | (_A & CF);			\
/*TODO*///	_A = (_A >> 1) | (_A << 7)
/*TODO*///#endif
/*TODO*///
    public static void RRCA()
    {
        Z80.F = (Z80.F & (SF | ZF | PF)) | (Z80.A & (YF | XF | CF)); 		
        Z80.A = ((Z80.A >> 1) | (Z80.A << 7))&0xFF;
    }
/*TODO*////***************************************************************
/*TODO*/// * RLA
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define RLA {													\
/*TODO*///	UINT8 res = (_A << 1) | (_F & CF);							\
/*TODO*///	UINT8 c = (_A & 0x80) ? CF : 0; 							\
/*TODO*///	_F = (_F & (SF | ZF | PF)) | c | (res & (YF | XF)); 		\
/*TODO*///	_A = res;													\
/*TODO*///}
    public static void RLA() {
        int res = (Z80.A << 1 | Z80.F & CF) & 0xFF;
        int c = (Z80.A & 0x80) != 0 ? CF : 0;
        Z80.F = (Z80.F & (SF | ZF | PF)) | c | (res & (YF | XF)); 
        Z80.A = res;
    }
/*TODO*///#else
/*TODO*///#define RLA {                                                   \
/*TODO*///	UINT8 res = (_A << 1) | (_F & CF);							\
/*TODO*///	UINT8 c = (_A & 0x80) ? CF : 0; 							\
/*TODO*///	_F = (_F & (SF | ZF | YF | XF | PF)) | c;					\
/*TODO*///	_A = res;													\
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * RRA
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define RRA {                                                   \
/*TODO*///	UINT8 res = (_A >> 1) | (_F << 7);							\
/*TODO*///	UINT8 c = (_A & 0x01) ? CF : 0; 							\
/*TODO*///	_F = (_F & (SF | ZF | PF)) | c | (res & (YF | XF)); 		\
/*TODO*///	_A = res;													\
/*TODO*///}
    public static void RRA() {
        int res = (Z80.A >> 1 | Z80.F << 7) & 0xFF;
        int c = (Z80.A & 0x1) != 0 ? CF : 0;
        Z80.F = (Z80.F  & (SF | ZF | PF)) | c | (res & (YF | XF)); 
        Z80.A = res;
    }

/*TODO*////***************************************************************
/*TODO*/// * RRD
/*TODO*/// ***************************************************************/
/*TODO*///#define RRD {													
/*TODO*///	UINT8 n = RM(_HL);											
/*TODO*///	WM( _HL, (n >> 4) | (_A << 4) );							
/*TODO*///	_A = (_A & 0xf0) | (n & 0x0f);								
/*TODO*///	_F = (_F & CF) | SZP[_A];									
/*TODO*///}
/*TODO*///
    public static void RRD()
    {
        int n = RM(HL());											
	WM( HL(), ((n >> 4) | (Z80.A << 4))&0xFF );							
	Z80.A = ((Z80.A & 0xf0) | (n & 0x0f))&0xFF;								
	Z80.F = (Z80.F & CF) | SZP[Z80.A];
    }
    
/*TODO*////***************************************************************
/*TODO*/// * RLD
/*TODO*/// ***************************************************************/
/*TODO*///#define RLD {                                                   
/*TODO*///    UINT8 n = RM(_HL);                                          
/*TODO*///	WM( _HL, (n << 4) | (_A & 0x0f) );							
/*TODO*///    _A = (_A & 0xf0) | (n >> 4);                                
/*TODO*///	_F = (_F & CF) | SZP[_A];									
/*TODO*///}
    public static void RLD()
    {
        int n = RM(HL());                                          
        WM( HL(), ((n << 4) | (Z80.A & 0x0f))&0xFF );							
        Z80.A = ((Z80.A & 0xf0) | (n >> 4))&0xFF;                                
        Z80.F = (Z80.F & CF) | SZP[Z80.A];
    }

    /***************************************************************
     * ADD	A,n
     ***************************************************************/
    public static void ADD(int value) {
        int res = (Z80.A + value) & 0xFF;
        Z80.F = SZHVC_Add[(Z80.A << 8 | res)];
        Z80.A = res;
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * ADC	A,n
/*TODO*/// ***************************************************************/
/*TODO*///#ifdef X86_ASM
/*TODO*///#if Z80_EXACT
/*TODO*///#define ADC(value)												\
/*TODO*/// asm (															\
/*TODO*/// " shrb $1,%1           \n"                                     \
/*TODO*/// " adcb %2,%0           \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
/*TODO*/// " addb %1,%1           \n" /* shift to P/V bit position */     \
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
/*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
/*TODO*/// " movb %0,%%ah         \n" /* get result */                    \
/*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
/*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
/*TODO*/// :"=r" (_A), "=r" (_F)                                          \
/*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
/*TODO*/// )
/*TODO*///#else
/*TODO*///#define ADC(value)                                              \
/*TODO*/// asm (															\
/*TODO*/// " shrb $1,%1           \n"                                     \
/*TODO*/// " adcb %2,%0           \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
/*TODO*/// " addb %1,%1           \n" /* shift to P/V bit position */     \
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
/*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
/*TODO*/// :"=r" (_A), "=r" (_F)                                          \
/*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
/*TODO*/// )
/*TODO*///#endif
/*TODO*///#else
/*TODO*///#if BIG_FLAGS_ARRAY
/*TODO*///#define ADC(value)												\
/*TODO*///{																\
/*TODO*///	UINT32 ah = _AFD & 0xff00, c = _AFD & 1;					\
/*TODO*///	UINT32 res = (UINT8)((ah >> 8) + value + c);				\
/*TODO*///	_F = SZHVC_add[(c << 16) | ah | res];						\
/*TODO*///    _A = res;                                                   \
/*TODO*///}
/*TODO*///#else
/*TODO*///#define ADC(value)												\
/*TODO*///{																\
/*TODO*///	unsigned val = value;										\
/*TODO*///	unsigned res = _A + val + (_F & CF);						\
/*TODO*///	_F = SZ[res & 0xff] | ((res >> 8) & CF) |					\
/*TODO*///		((_A ^ res ^ val) & HF) |								\
/*TODO*///		(((val ^ _A ^ 0x80) & (val ^ res) & 0x80) >> 5);		\
/*TODO*///	_A = res;													\
/*TODO*///}
/*TODO*///#endif
/*TODO*///#endif
    public static void ADC(int value)
    {
         int c = Z80.F & 0x1;
         int result = (Z80.A + value + c) & 0xFF;
         Z80.F = SZHVC_Add[(c << 16 | Z80.A << 8 | result)];
         Z80.A= result;
    }

    /***************************************************************
     * SUB	n
     ***************************************************************/
    public static void SUB(int value) {
        int result = (Z80.A - value) & 0xFF;
        Z80.F = SZHVC_sub[(Z80.A << 8 | result)];
        Z80.A = result;
    }

/*TODO*////***************************************************************
/*TODO*/// * SBC	A,n
/*TODO*/// ***************************************************************/
/*TODO*///#ifdef X86_ASM
/*TODO*///#if Z80_EXACT
/*TODO*///#define SBC(value)												\
/*TODO*/// asm (															\
/*TODO*/// " shrb $1,%1           \n"                                     \
/*TODO*/// " sbbb %2,%0           \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
/*TODO*/// " stc                  \n" /* prepare to set N flag */         \
/*TODO*/// " adcb %1,%1           \n" /* shift to P/V bit position */     \
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
/*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
/*TODO*/// " movb %0,%%ah         \n" /* get result */                    \
/*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
/*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
/*TODO*/// :"=r" (_A), "=r" (_F)                                          \
/*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
/*TODO*/// )
/*TODO*///#else
/*TODO*///#define SBC(value)                                              \
/*TODO*/// asm (															\
/*TODO*/// " shrb $1,%1           \n"                                     \
/*TODO*/// " sbbb %2,%0           \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " setob %1             \n" /* al = 1 if overflow */            \
/*TODO*/// " stc                  \n" /* prepare to set N flag */         \
/*TODO*/// " adcb %1,%1           \n" /* shift to P/V bit position */     \
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " andb $0xd1,%%ah      \n" /* sign, zero, half carry, carry */ \
/*TODO*/// " orb %%ah,%1          \n" /* combine with P/V */              \
/*TODO*/// :"=r" (_A), "=r" (_F)                                          \
/*TODO*/// :"r" (value), "1" (_F), "0" (_A)                               \
/*TODO*/// )
/*TODO*///#endif
/*TODO*///#else
/*TODO*///#if BIG_FLAGS_ARRAY
/*TODO*///#define SBC(value)												\
/*TODO*///{																\
/*TODO*///	UINT32 ah = _AFD & 0xff00, c = _AFD & 1;					\
/*TODO*///	UINT32 res = (UINT8)((ah >> 8) - value - c);				\
/*TODO*///	_F = SZHVC_sub[(c<<16) | ah | res]; 						\
/*TODO*///    _A = res;                                                   \
/*TODO*///}
    public static void SBC(int value) {
        int c = Z80.F & 1;
        int result = (Z80.A - value - c) & 0xff;
        Z80.F = SZHVC_sub[(c << 16) | (Z80.A << 8) | result];
        Z80.A = result;
    }
/*TODO*///#else
/*TODO*///#define SBC(value)												\
/*TODO*///{																\
/*TODO*///	unsigned val = value;										\
/*TODO*///	unsigned res = _A - val - (_F & CF);						\
/*TODO*///	_F = SZ[res & 0xff] | ((res >> 8) & CF) | NF |				\
/*TODO*///		((_A ^ res ^ val) & HF) |								\
/*TODO*///		(((val ^ _A) & (_A ^ res) & 0x80) >> 5);				\
/*TODO*///	_A = res;													\
/*TODO*///}
/*TODO*///#endif
/*TODO*///#endif
/*TODO*///
    /***************************************************************
     * NEG
     ***************************************************************/
    public static void NEG()
    {
        int value = Z80.A & 0xFF;
        Z80.A=0;
        SUB(value);
    }

    /***************************************************************
     * DAA
     ***************************************************************/
    public static void DAA() 
    {													
            int idx = Z80.A &0xFF;												
            if(( Z80.F & CF )!=0) idx |= 0x100; 								
            if(( Z80.F & HF )!=0) idx |= 0x200; 								
            if(( Z80.F & NF )!=0) idx |= 0x400; 								
            AF(DAATable[idx]);										
    }

    /***************************************************************
     * AND	n
     ***************************************************************/
    public static void AND(int value) {
        Z80.A = (Z80.A & value) & 0xff;
        Z80.F = SZP[Z80.A] | HF;
    }
    
    /***************************************************************
     * OR	n
     ***************************************************************/
    public static void OR(int value) {
        Z80.A = (Z80.A | value) & 0xff;
        Z80.F = SZP[Z80.A];
    }
    
    /***************************************************************
     * XOR	n
     ***************************************************************/
    public static void XOR(int value) {
        Z80.A = (Z80.A ^ value) & 0xff;
        Z80.F = SZP[Z80.A];
    }
    
    /***************************************************************
     * CP	n
     ***************************************************************/
    public static void CP(int value) {
        int result = (Z80.A - value) & 0xFF;
        Z80.F = SZHVC_sub[(Z80.A << 8 | result)];
    }

    /***************************************************************
     * EX   AF,AF'
     ***************************************************************/
    public static void EX_AF() {
        int tmp = Z80.A;
        Z80.A = Z80.A2;
        Z80.A2 = tmp;
        tmp = Z80.F;
        Z80.F = Z80.F2;
        Z80.F2 = tmp;
    }

    /***************************************************************
     * EX   DE,HL
     ***************************************************************/
    public static void EX_DE_HL()
    {
        int tmp = Z80.D;
        Z80.D =  Z80.H;
        Z80.H = tmp;
        tmp =  Z80.E;
        Z80.E =  Z80.L;
        Z80.L = tmp;
    }

    /***************************************************************
     * EXX
     ***************************************************************/
    public static void EXX() {
        int tmp = Z80.B;
        Z80.B = Z80.B2;
        Z80.B2 = tmp;
        tmp = Z80.C;
        Z80.C = Z80.C2;
        Z80.C2 = tmp;
        tmp = Z80.D;
        Z80.D = Z80.D2;
        Z80.D2 = tmp;
        tmp = Z80.E;
        Z80.E = Z80.E2;
        Z80.E2 = tmp;
        tmp = Z80.H;
        Z80.H = Z80.H2;
        Z80.H2 = tmp;
        tmp = Z80.L;
        Z80.L = Z80.L2;
        Z80.L2 = tmp;
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * EX   (SP),r16
/*TODO*/// ***************************************************************/
/*TODO*///#define EXSP(DR)												\
/*TODO*///{																\
/*TODO*///	PAIR tmp = { { 0, 0, 0, 0 } };								\
/*TODO*///	RM16( _SPD, &tmp ); 										\
/*TODO*///	WM16( _SPD, &Z80.DR );										\
/*TODO*///	Z80.DR = tmp;												\
/*TODO*///}
    public static int EXSP(int DR)//to recheck
    {
        int tmp = RM16(Z80.SP);
        WM16(Z80.SP,DR);
        return tmp;
    }
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * ADD16
/*TODO*/// ***************************************************************/
/*TODO*///#ifdef	X86_ASM
/*TODO*///#if Z80_EXACT
/*TODO*///#define ADD16(DR,SR)											\
/*TODO*/// asm (															\
/*TODO*/// " andb $0xc4,%1        \n"                                     \
/*TODO*/// " addb %%dl,%%cl       \n"                                     \
/*TODO*/// " adcb %%dh,%%ch       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " andb $0x11,%%ah      \n"                                     \
/*TODO*/// " orb %%ah,%1          \n"                                     \
/*TODO*/// " movb %%ch,%%ah       \n" /* get result MSB */                \
/*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
/*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
/*TODO*/// :"=c" (Z80.DR.d), "=r" (_F)                                    \
/*TODO*/// :"0" (Z80.DR.d), "1" (_F), "d" (Z80.SR.d)                      \
/*TODO*/// )
/*TODO*///#else
/*TODO*///#define ADD16(DR,SR)                                            \
/*TODO*/// asm (															\
/*TODO*/// " andb $0xc4,%1        \n"                                     \
/*TODO*/// " addb %%dl,%%cl       \n"                                     \
/*TODO*/// " adcb %%dh,%%ch       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " andb $0x11,%%ah      \n"                                     \
/*TODO*/// " orb %%ah,%1          \n"                                     \
/*TODO*/// :"=c" (Z80.DR.d), "=r" (_F)                                    \
/*TODO*/// :"0" (Z80.DR.d), "1" (_F), "d" (Z80.SR.d)                      \
/*TODO*/// )
/*TODO*///#endif
/*TODO*///#else
/*TODO*///#define ADD16(DR,SR)											\
/*TODO*///{																\
/*TODO*///	UINT32 res = Z80.DR.d + Z80.SR.d;							\
/*TODO*///	_F = (_F & (SF | ZF | VF)) |								\
/*TODO*///		(((Z80.DR.d ^ res ^ Z80.SR.d) >> 8) & HF) | 			\
/*TODO*///		((res >> 16) & CF); 									\
/*TODO*///	Z80.DR.w.l = (UINT16)res;									\
/*TODO*///}
/*TODO*///#endif
    public static int ADD16(int a, int b) {
        int result = a + b;
        Z80.F = (Z80.F & (SF | ZF | VF)) | (((a ^ result ^ b) >> 8) & HF) | ((result >> 16) & CF);
        return (result & 0xffff);
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * ADC	r16,r16
/*TODO*/// ***************************************************************/
/*TODO*///#ifdef	X86_ASM
/*TODO*///#if Z80_EXACT
/*TODO*///#define ADC16(Reg)												\
/*TODO*/// asm (                                                          \
/*TODO*/// " shrb $1,%1           \n"                                     \
/*TODO*/// " adcb %%dl,%%cl       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " movb %%ah,%%dl       \n"                                     \
/*TODO*/// " adcb %%dh,%%ch       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " setob %1             \n"                                     \
/*TODO*/// " orb $0xbf,%%dl       \n" /* set all but zero */              \
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " andb $0xd1,%%ah      \n" /* sign,zero,half carry and carry */\
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " orb %%ah,%1          \n" /* overflow into P/V */             \
/*TODO*/// " andb %%dl,%1         \n" /* mask zero */                     \
/*TODO*/// " movb %%ch,%%ah       \n" /* get result MSB */                \
/*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
/*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
/*TODO*/// :"=c" (_HLD), "=r" (_F)                                        \
/*TODO*/// :"0" (_HLD), "1" (_F), "d" (Z80.Reg.d)                         \
/*TODO*/// )
/*TODO*///#else
/*TODO*///#define ADC16(Reg)                                              \
/*TODO*/// asm (                                                          \
/*TODO*/// " shrb $1,%1           \n"                                     \
/*TODO*/// " adcb %%dl,%%cl       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " movb %%ah,%%dl       \n"                                     \
/*TODO*/// " adcb %%dh,%%ch       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " setob %1             \n"                                     \
/*TODO*/// " orb $0xbf,%%dl       \n" /* set all but zero */              \
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " andb $0xd1,%%ah      \n" /* sign,zero,half carry and carry */\
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " orb %%ah,%1          \n" /* overflow into P/V */             \
/*TODO*/// " andb %%dl,%1         \n" /* mask zero */                     \
/*TODO*/// :"=c" (_HLD), "=r" (_F)                                        \
/*TODO*/// :"0" (_HLD), "1" (_F), "d" (Z80.Reg.d)                         \
/*TODO*/// )
/*TODO*///#endif
/*TODO*///#else
/*TODO*///#define ADC16(Reg)												\
/*TODO*///{																\
/*TODO*///	UINT32 res = _HLD + Z80.Reg.d + (_F & CF);					\
/*TODO*///	_F = (((_HLD ^ res ^ Z80.Reg.d) >> 8) & HF) |				\
/*TODO*///		((res >> 16) & CF) |									\
/*TODO*///		((res >> 8) & SF) | 									\
/*TODO*///		((res & 0xffff) ? 0 : ZF) | 							\
/*TODO*///		(((Z80.Reg.d ^ _HLD ^ 0x8000) & (Z80.Reg.d ^ res) & 0x8000) >> 13); \
/*TODO*///	_HL = (UINT16)res;											\
/*TODO*///}
/*TODO*///#endif
    public static void ADC16(int value) {
        int _HLD = HL();
        int result = _HLD + value + (Z80.F & CF);
        Z80.F = (((_HLD ^ result ^ value) >> 8) & 0x10) | ((result >> 16) & 1) | ((result >> 8) & 0x80)
                | (((result & 0xffff) != 0) ? 0 : 0x40) | (((value ^ _HLD ^ 0x8000) & (value ^ result) & 0x8000) >> 13);
        Z80.H = (result >> 8) & 0xff;
        Z80.L = result & 0xff;
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * SBC	r16,r16
/*TODO*/// ***************************************************************/
/*TODO*///#ifdef	X86_ASM
/*TODO*///#if Z80_EXACT
/*TODO*///#define SBC16(Reg)												\
/*TODO*///asm (															\
/*TODO*/// " shrb $1,%1           \n"                                     \
/*TODO*/// " sbbb %%dl,%%cl       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " movb %%ah,%%dl       \n"                                     \
/*TODO*/// " sbbb %%dh,%%ch       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " setob %1             \n"                                     \
/*TODO*/// " orb $0xbf,%%dl       \n" /* set all but zero */              \
/*TODO*/// " stc                  \n"                                     \
/*TODO*/// " adcb %1,%1           \n"                                     \
/*TODO*/// " andb $0xd1,%%ah      \n" /* sign,zero,half carry and carry */\
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " orb %%ah,%1          \n" /* overflow into P/V */             \
/*TODO*/// " andb %%dl,%1         \n" /* mask zero */                     \
/*TODO*/// " movb %%ch,%%ah       \n" /* get result MSB */                \
/*TODO*/// " andb $0x28,%%ah      \n" /* maks flags 5+3 */                \
/*TODO*/// " orb %%ah,%1          \n" /* put them into flags */           \
/*TODO*/// :"=c" (_HLD), "=r" (_F)                                        \
/*TODO*/// :"0" (_HLD), "1" (_F), "d" (Z80.Reg.d)                         \
/*TODO*/// )
/*TODO*///#else
/*TODO*///#define SBC16(Reg)                                              \
/*TODO*///asm (															\
/*TODO*/// " shrb $1,%1           \n"                                     \
/*TODO*/// " sbbb %%dl,%%cl       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " movb %%ah,%%dl       \n"                                     \
/*TODO*/// " sbbb %%dh,%%ch       \n"                                     \
/*TODO*/// " lahf                 \n"                                     \
/*TODO*/// " setob %1             \n"                                     \
/*TODO*/// " orb $0xbf,%%dl       \n" /* set all but zero */              \
/*TODO*/// " stc                  \n"                                     \
/*TODO*/// " adcb %1,%1           \n"                                     \
/*TODO*/// " andb $0xd1,%%ah      \n" /* sign,zero,half carry and carry */\
/*TODO*/// " addb %1,%1           \n"                                     \
/*TODO*/// " orb %%ah,%1          \n" /* overflow into P/V */             \
/*TODO*/// " andb %%dl,%1         \n" /* mask zero */                     \
/*TODO*/// :"=c" (_HLD), "=r" (_F)                                        \
/*TODO*/// :"0" (_HLD), "1" (_F), "d" (Z80.Reg.d)                         \
/*TODO*/// )
/*TODO*///#endif
/*TODO*///#else
/*TODO*///#define SBC16(Reg)												\
/*TODO*///{																\
/*TODO*///	UINT32 res = _HLD - Z80.Reg.d - (_F & CF);					\
/*TODO*///	_F = (((_HLD ^ res ^ Z80.Reg.d) >> 8) & HF) | NF |			\
/*TODO*///		((res >> 16) & CF) |									\
/*TODO*///		((res >> 8) & SF) | 									\
/*TODO*///		((res & 0xffff) ? 0 : ZF) | 							\
/*TODO*///		(((Z80.Reg.d ^ _HLD) & (_HLD ^ res) &0x8000) >> 13);	\
/*TODO*///	_HL = (UINT16)res;											\
/*TODO*///}
/*TODO*///#endif
    public static void SBC16(int value) {
        int _HLD = HL();
        int result = _HLD - value - (Z80.F & CF);
        Z80.F = (((_HLD ^ result ^ value) >> 8) & 0x10) | 0x02 | ((result >> 16) & 1) | ((result >> 8) & 0x80)
                | (((result & 0xffff) != 0) ? 0 : 0x40) | (((value ^ _HLD) & (_HLD ^ result) & 0x8000) >> 13);
        Z80.H = (result >> 8) & 0xff;
        Z80.L = result & 0xff;
    }

    /***************************************************************
     * RLC	r8
     ***************************************************************/
    public static int RLC(int value) {
        int c = (value & 0x80) != 0 ? CF : 0;
        value = (value << 1 | value >> 7) & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /***************************************************************
     * RRC	r8
     ***************************************************************/
    public static int RRC(int value) {
        int res = value & 0xFF;
        int c = (res & 0x1) != 0 ? CF : 0;
        res = (res >> 1 | res << 7) & 0xFF;
        Z80.F = (SZP[res] | c);
        return res;
    }

    /***************************************************************
     * RL	r8
     ***************************************************************/
    public static int RL(int value) {
        int c = (value & 0x80) != 0 ? CF : 0;
        value = (value << 1 | Z80.F & CF) & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /***************************************************************
     * RR	r8
     ***************************************************************/
    public static int RR(int value) {
        int c = (value & 0x1) != 0 ? CF : 0;
        value = (value >> 1 | Z80.F << 7) & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /***************************************************************
     * SLA	r8
     ***************************************************************/
    public static int SLA(int value)
    {
        int c = (value & 0x80)!=0 ? CF : 0;
        value = (value << 1) & 0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

    /***************************************************************
     * SRA	r8
     ***************************************************************/
    private final int SRA(int value)
    {
        int c = (value & 0x01)!=0 ? CF : 0;
        value = ((value >> 1) | (value & 0x80))&0xFF;
        Z80.F = (SZP[value] | c);
        return value;
    }

/*TODO*////***************************************************************
/*TODO*/// * SLL	r8
/*TODO*/// ***************************************************************/
/*TODO*///INLINE UINT8 SLL(UINT8 value)
/*TODO*///{
/*TODO*///	unsigned res = value;
/*TODO*///	unsigned c = (res & 0x80) ? CF : 0;
/*TODO*///	res = ((res << 1) | 0x01) & 0xff;
/*TODO*///	_F = SZP[res] | c;
/*TODO*///	return res;
/*TODO*///}

    /***************************************************************
     * SRL	r8
     ***************************************************************/
    private static int SRL(int value)
    {
        int res = value & 0xFF;
        int c = (res & 0x01)!=0 ? CF : 0;
        res = (res >> 1) & 0xFF;
        Z80.F = (SZP[value] | c);
        return res;
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * BIT  bit,r8
/*TODO*/// ***************************************************************/
/*TODO*///#define BIT(bit,reg)                                            \
/*TODO*///	_F = (_F & CF) | HF | SZ_BIT[reg & (1<<bit)]
/*TODO*///
    private static final int[] bitSet = {1, 2, 4, 8, 16, 32, 64, 128};           // lookup table for setting a bit of an 8-bit value using OR
    private static void BIT(int bitNumber, int value) {
        Z80.F = (Z80.F & 1) | 0x10 | SZ_BIT[value & bitSet[bitNumber]];
    }
/*TODO*////***************************************************************
/*TODO*/// * BIT	bit,(IX/Y+o)
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define BIT_XY(bit,reg)                                         \
/*TODO*///    _F = (_F & CF) | HF | (SZ_BIT[reg & (1<<bit)] & ~(YF|XF)) | ((EA>>8) & (YF|XF))
/*TODO*///#else
/*TODO*///#define BIT_XY	BIT
/*TODO*///#endif
    public static void BIT_XY(int bitNumber,int value)
    {
         Z80.F = (Z80.F & CF) | HF | (SZ_BIT[value & bitSet[bitNumber]] & ~(YF|XF)) | ((EA>>8) & (YF|XF));
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * RES	bit,r8
/*TODO*/// ***************************************************************/
/*TODO*///INLINE UINT8 RES(UINT8 bit, UINT8 value)
/*TODO*///{
/*TODO*///	return value & ~(1<<bit);
/*TODO*///}
    private static final int[]   bitRes          = { 254, 253, 251, 247, 239, 223, 191, 127 }; // lookup table for resetting a bit of an 8-bit value using AND
    public static int RES(int bitNumber, int value) {
        value = value & bitRes[bitNumber];
        return value;
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * SET  bit,r8
/*TODO*/// ***************************************************************/
/*TODO*///INLINE UINT8 SET(UINT8 bit, UINT8 value)
/*TODO*///{
/*TODO*///	return value | (1<<bit);
/*TODO*///}
    public static int SET(int bitNumber, int value) {
        value = value | bitSet[bitNumber];
        return value;
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * LDI
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define LDI {													
/*TODO*///	UINT8 io = RM(_HL); 										
/*TODO*///	WM( _DE, io );												
/*TODO*///	_F &= SF | ZF | CF; 										
/*TODO*///	if( (_A + io) & 0x02 ) _F |= YF; /* bit 1 -> flag 5 */		
/*TODO*///    if( (_A + io) & 0x08 ) _F |= XF; /* bit 3 -> flag 3 */      
/*TODO*///    _HL++; _DE++; _BC--;                                        
/*TODO*///	if( _BC ) _F |= VF; 										
/*TODO*///}
/*TODO*///#else
/*TODO*///#define LDI {                                                   \
/*TODO*///	WM( _DE, RM(_HL) ); 										\
/*TODO*///    _F &= SF | ZF | YF | XF | CF;                               \
/*TODO*///	_HL++; _DE++; _BC--;										\
/*TODO*///	if( _BC ) _F |= VF; 										\
/*TODO*///}
/*TODO*///#endif
/*TODO*///
    public static void LDI() 
    {													
	int io = RM(HL()); 										
	WM( DE(), io );												
	Z80.F &= SF | ZF | CF; 										
	if(( (Z80.A + io) & 0x02 )!=0) Z80.F |= YF; /* bit 1 -> flag 5 */		
        if(( (Z80.A + io) & 0x08 )!=0) Z80.F |= XF; /* bit 3 -> flag 3 */      
        HL((HL()+1)&0xFFFF);
        DE((DE()+1)&0xFFFF); 
        BC((BC()-1)&0xFFFF);                                       
	if( BC()!=0 ) Z80.F |= VF; 										
    }
/*TODO*////***************************************************************
/*TODO*/// * CPI
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define CPI {													
/*TODO*///	UINT8 val = RM(_HL);										
/*TODO*///	UINT8 res = _A - val;										
/*TODO*///	_HL++; _BC--;												
/*TODO*///	_F = (_F & CF) | (SZ[res] & ~(YF|XF)) | ((_A ^ val ^ res) & HF) | NF;  
/*TODO*///	if( _F & HF ) res -= 1; 									
/*TODO*///	if( res & 0x02 ) _F |= YF; /* bit 1 -> flag 5 */			
/*TODO*///	if( res & 0x08 ) _F |= XF; /* bit 3 -> flag 3 */			
/*TODO*///    if( _BC ) _F |= VF;                                         
/*TODO*///}
    
    public static void CPI() 
    {													
        int val = RM(HL());										
        int res = (Z80.A - val)&0xFF;										
        HL((HL()+1)&0xFFFF);
        BC((BC()-1)&0xFFFF);
        Z80.F = (Z80.F & CF) | (SZ[res] & ~(YF|XF)) | ((Z80.A ^ val ^ res) & HF) | NF;  
        if(( Z80.F & HF )!=0) res= (res - 1)& 0xff; 									
        if(( res & 0x02 )!=0) Z80.F |= YF; /* bit 1 -> flag 5 */			
        if(( res & 0x08 )!=0) Z80.F |= XF; /* bit 3 -> flag 3 */			
        if( BC()!=0 ) Z80.F |= VF;                                         
    }
/*TODO*///#else
/*TODO*///#define CPI {                                                   \
/*TODO*///	UINT8 val = RM(_HL);										\
/*TODO*///	UINT8 res = _A - val;										\
/*TODO*///	_HL++; _BC--;												\
/*TODO*///	_F = (_F & CF) | SZ[res] | ((_A ^ val ^ res) & HF) | NF;	\
/*TODO*///	if( _BC ) _F |= VF; 										\
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * INI
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define INI {													
/*TODO*///	UINT8 io = IN(_BC); 										
/*TODO*///    _B--;                                                       
/*TODO*///	WM( _HL, io );												
/*TODO*///	_HL++;														
/*TODO*///	_F = SZ[_B];												
/*TODO*///	if( io & SF ) _F |= NF; 									
/*TODO*///	if( (_C + io + 1) & 0x100 ) _F |= HF | CF;					
/*TODO*///    if( (irep_tmp1[_C & 3][io & 3] ^                            
/*TODO*///		 breg_tmp2[_B] ^										
/*TODO*///		 (_C >> 2) ^											
/*TODO*///		 (io >> 2)) & 1 )										
/*TODO*///		_F |= PF;	
    public static void INI()
    {
        int io = IN(BC()); 										
        Z80.B = (Z80.B-1)&0xFF;                                                     
	WM( HL(), io );												
	HL((HL()+1)&0xFFFF);														
	Z80.F = SZ[Z80.B];												
	if(( io & SF )!=0) Z80.F |= NF; 									
	if( ((Z80.C + io + 1) & 0x100 )!=0) Z80.F |= HF | CF;					
        if(( (irep_tmp1[Z80.C & 3][io & 3] ^                            
		 breg_tmp2[Z80.B] ^										
		 (Z80.C >> 2) ^											
		 (io >> 2)) & 1 )!=0)										
		Z80.F |= PF;
    }
/*TODO*///}
/*TODO*///#else
/*TODO*///#define INI {													\
/*TODO*///    _B--;                                                       \
/*TODO*///	WM( _HL, IN(_BC) ); 										\
/*TODO*///	_HL++;														\
/*TODO*///	_F = (_B) ? NF : NF | ZF;									\
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * OUTI
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define OUTI {													
/*TODO*///	UINT8 io = RM(_HL); 										
/*TODO*///    _B--;                                                       
/*TODO*///	OUT( _BC, io ); 											
/*TODO*///	_HL++;														
/*TODO*///	_F = SZ[_B];												
/*TODO*///	if( io & SF ) _F |= NF; 									
/*TODO*///	if( (_C + io + 1) & 0x100 ) _F |= HF | CF;					
/*TODO*///    if( (irep_tmp1[_C & 3][io & 3] ^                            
/*TODO*///		 breg_tmp2[_B] ^										
/*TODO*///		 (_C >> 2) ^											
/*TODO*///		 (io >> 2)) & 1 )										
/*TODO*///        _F |= PF;                                               
/*TODO*///}
    public static void OUTI() 
    {													
        int io = RM(HL()); 										
        Z80.B=(Z80.B-1)&0xFF;                                                       
        OUT( BC(), io ); 											
        HL((HL()+1)&0xFFFF);														
        Z80.F = SZ[Z80.B];												
        if(( io & SF )!=0) Z80.F |= NF; 									
        if( ((Z80.C + io + 1) & 0x100 )!=0) Z80.F |= HF | CF;					
        if(( (irep_tmp1[Z80.C & 3][io & 3] ^                            
                     breg_tmp2[Z80.B] ^										
                     (Z80.C >> 2) ^											
                     (io >> 2)) & 1 )!=0)										
            Z80.F |= PF;                                               
    }
/*TODO*///#else
/*TODO*///#define OUTI {													\
/*TODO*///    _B--;                                                       \
/*TODO*///    OUT( _BC, RM(_HL) );                                        \
/*TODO*///    _HL++;                                                      \
/*TODO*///    _F = (_B) ? NF : NF | ZF;                                   \
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * LDD
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define LDD {													
/*TODO*///	UINT8 io = RM(_HL); 										
/*TODO*///	WM( _DE, io );												
/*TODO*///	_F &= SF | ZF | CF; 										
/*TODO*///	if( (_A + io) & 0x02 ) _F |= YF; /* bit 1 -> flag 5 */		
/*TODO*///	if( (_A + io) & 0x08 ) _F |= XF; /* bit 3 -> flag 3 */		
/*TODO*///	_HL--; _DE--; _BC--;										
/*TODO*///	if( _BC ) _F |= VF; 										
/*TODO*///}
    public static void LDD() 
    {													
        int io = RM(HL()); 										
        WM( DE(), io );												
        Z80.F &= SF | ZF | CF; 										
        if(( (Z80.A + io) & 0x02 )!=0) Z80.F |= YF; /* bit 1 -> flag 5 */		
        if(( (Z80.A + io) & 0x08 )!=0) Z80.F |= XF; /* bit 3 -> flag 3 */		
        HL((HL()-1)&0xFFFF);
        DE((DE()-1)&0xFFFF); 
        BC((BC()-1)&0xFFFF);										
        if( BC()!=0 ) Z80.F |= VF; 										
    }
/*TODO*///#else
/*TODO*///#define LDD {                                                   \
/*TODO*///	WM( _DE, RM(_HL) ); 										\
/*TODO*///    _F &= SF | ZF | YF | XF | CF;                               \
/*TODO*///	_HL--; _DE--; _BC--;										\
/*TODO*///	if( _BC ) _F |= VF; 										\
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * CPD
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define CPD {													\
/*TODO*///	UINT8 val = RM(_HL);										\
/*TODO*///	UINT8 res = _A - val;										\
/*TODO*///	_HL--; _BC--;												\
/*TODO*///	_F = (_F & CF) | (SZ[res] & ~(YF|XF)) | ((_A ^ val ^ res) & HF) | NF;  \
/*TODO*///	if( _F & HF ) res -= 1; 									\
/*TODO*///	if( res & 0x02 ) _F |= YF; /* bit 1 -> flag 5 */			\
/*TODO*///	if( res & 0x08 ) _F |= XF; /* bit 3 -> flag 3 */			\
/*TODO*///    if( _BC ) _F |= VF;                                         \
/*TODO*///}
/*TODO*///#else
/*TODO*///#define CPD {                                                   \
/*TODO*///	UINT8 val = RM(_HL);										\
/*TODO*///	UINT8 res = _A - val;										\
/*TODO*///	_HL--; _BC--;												\
/*TODO*///	_F = (_F & CF) | SZ[res] | ((_A ^ val ^ res) & HF) | NF;	\
/*TODO*///	if( _BC ) _F |= VF; 										\
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * IND
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define IND {													\
/*TODO*///    UINT8 io = IN(_BC);                                         
/*TODO*///	_B--;														
/*TODO*///	WM( _HL, io );												
/*TODO*///	_HL--;														
/*TODO*///	_F = SZ[_B];												
/*TODO*///    if( io & SF ) _F |= NF;                                     
/*TODO*///	if( (_C + io - 1) & 0x100 ) _F |= HF | CF;					
/*TODO*///	if( (drep_tmp1[_C & 3][io & 3] ^							
/*TODO*///		 breg_tmp2[_B] ^										
/*TODO*///		 (_C >> 2) ^											
/*TODO*///		 (io >> 2)) & 1 )										
/*TODO*///        _F |= PF;                                               
/*TODO*///}
    public static void IND()
    {
        int io = IN(BC());                                         
	Z80.B = (Z80.B-1) & 0xFF;														
	WM( HL(), io );												
	HL((HL()-1)&0xFFFF);														
	Z80.F = SZ[Z80.B];												
        if(( io & SF )!=0) Z80.F |= NF;                                     
	if( ((Z80.C + io - 1) & 0x100 )!=0) Z80.F |= HF | CF;					
	if(( (drep_tmp1[Z80.C & 3][io & 3] ^							
		 breg_tmp2[Z80.B] ^										
		 (Z80.C >> 2) ^											
		 (io >> 2)) & 1 )!=0)										
        Z80.F |= PF; 
    }
/*TODO*///#else
/*TODO*///#define IND {                                                   \
/*TODO*///	_B--;														\
/*TODO*///	WM( _HL, IN(_BC) ); 										\
/*TODO*///	_HL--;														\
/*TODO*///	_F = (_B) ? NF : NF | ZF;									\
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * OUTD
/*TODO*/// ***************************************************************/
/*TODO*///#if Z80_EXACT
/*TODO*///#define OUTD {													\
/*TODO*///	UINT8 io = RM(_HL); 										\
/*TODO*///    _B--;                                                       \
/*TODO*///	OUT( _BC, io ); 											\
/*TODO*///	_HL--;														\
/*TODO*///	_F = SZ[_B];												\
/*TODO*///    if( io & SF ) _F |= NF;                                     \
/*TODO*///	if( (_C + io - 1) & 0x100 ) _F |= HF | CF;					\
/*TODO*///	if( (drep_tmp1[_C & 3][io & 3] ^							\
/*TODO*///		 breg_tmp2[_B] ^										\
/*TODO*///		 (_C >> 2) ^											\
/*TODO*///		 (io >> 2)) & 1 )										\
/*TODO*///        _F |= PF;                                               \
/*TODO*///}
/*TODO*///#else
/*TODO*///#define OUTD {                                                  \
/*TODO*///    _B--;                                                       \
/*TODO*///    OUT( _BC, RM(_HL) );                                        \
/*TODO*///	_HL--;														\
/*TODO*///	_F = (_B) ? NF : NF | ZF;									\
/*TODO*///}
/*TODO*///#endif

    /***************************************************************
     * LDIR
     ***************************************************************/
    public static void LDIR()
    {
        LDI();														
        if( BC()!=0 )													
        {															
            Z80.PC = (Z80.PC -2)&0xFFFF;												
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xb0];//CC(ex,0xb0);											
        }
    }
    /***************************************************************
     * CPIR
     ***************************************************************/
    public static void CPIR()
    {
            CPI();														
            if( BC()!=0 && (Z80.F & ZF)==0 ) 									
            {															
                    Z80.PC = (Z80.PC -2)&0xFFFF;												
                    z80_ICount[0] -= cc[Z80_TABLE_ex][0xb1];//CC(ex,0xb1);											
            }
    }
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * INIR
/*TODO*/// ***************************************************************/
/*TODO*///#define INIR													\
/*TODO*///	INI;														\
/*TODO*///	if( _B )													\
/*TODO*///	{															\
/*TODO*///		_PC -= 2;												\
/*TODO*///		CC(ex,0xb2);											\
/*TODO*///	}
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * OTIR
/*TODO*/// ***************************************************************/
/*TODO*///#define OTIR													\
/*TODO*///	OUTI;														\
/*TODO*///	if( _B )													\
/*TODO*///	{															\
/*TODO*///		_PC -= 2;												\
/*TODO*///		CC(ex,0xb3);											\
/*TODO*///	}

    /***************************************************************
     * LDDR
     ***************************************************************/
    public static void LDDR()
    {
        LDD();														
        if( BC()!=0 )													
        {	
            Z80.PC = (Z80.PC -2)&0xFFFF;												
            z80_ICount[0] -= cc[Z80_TABLE_ex][0xb8];//CC(ex,0xb8);										
        }
    }

/*TODO*////***************************************************************
/*TODO*/// * CPDR
/*TODO*/// ***************************************************************/
/*TODO*///#define CPDR													
/*TODO*///	CPD;														
/*TODO*///	if( _BC && !(_F & ZF) ) 									\
/*TODO*///	{															\
/*TODO*///		_PC -= 2;												\
/*TODO*///		CC(ex,0xb9);											\
/*TODO*///	}
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * INDR
/*TODO*/// ***************************************************************/
/*TODO*///#define INDR													\
/*TODO*///	IND;														\
/*TODO*///	if( _B )													\
/*TODO*///	{															\
/*TODO*///		_PC -= 2;												\
/*TODO*///		CC(ex,0xba);											\
/*TODO*///	}
/*TODO*///
/*TODO*////***************************************************************
/*TODO*/// * OTDR
/*TODO*/// ***************************************************************/
/*TODO*///#define OTDR													\
/*TODO*///	OUTD;														\
/*TODO*///	if( _B )													\
/*TODO*///	{															\
/*TODO*///		_PC -= 2;												\
/*TODO*///		CC(ex,0xbb);											\
/*TODO*///	}

    /***************************************************************
     * EI
     ***************************************************************/
    public void EI() 
    {													
            /* If interrupts were disabled, execute one more			
         * instruction and check the IRQ line.                      
         * If not, simply set interrupt flip-flop 2                 
         */                                                         
            if( Z80.IFF1 == 0 )											
            {															
                Z80.IFF1 = Z80.IFF2 = 1;                                      
                Z80.PREPC = Z80.PC & 0xFFFF;																				
                Z80.R = (Z80.R+1)&0xFF;													
                while( cpu_readop(Z80.PC) == 0xfb ) /* more EIs? */       
                {														
                    //LOG(("Z80 #%d multiple EI opcodes at %04X\n",cpu_getactivecpu(), _PC));						
                    z80_ICount[0] -= cc[Z80_TABLE_op][0xfb];//CC(op,0xfb);										
                    Z80.PREPC = Z80.PC & 0xFFFF;                                         									
                    Z80.PC = (Z80.PC + 1) & 0xFFFF;                                              
                    Z80.R = (Z80.R+1)&0xFF;												
                }														
                if( Z80.irq_state != CLEAR_LINE || Z80.request_irq >= 0 )								
                {														
                    after_EI = 1;	/* avoid cycle skip hacks */		
                    int op = ROP();
                    z80_ICount[0] -= cc[Z80_TABLE_op][op];
                    Z80op[op].handler();//EXEC(op,ROP()); 									
                    after_EI = 0;										
                    //LOG(("Z80 #%d EI takes irq\n", cpu_getactivecpu())); 
                    take_interrupt();                                   
                } 
                else 
                {
                    int op = ROP();
                    z80_ICount[0] -= cc[Z80_TABLE_op][op];
                    Z80op[op].handler();//EXEC(op,ROP());
                }									
            } 
            else 
            {
                Z80.IFF2 = 1;
            }                                           
    }

    /**********************************************************
     * opcodes with CB prefix
     * rotate, shift and bit operations
     **********************************************************/
    opcode cb_00= () -> { Z80.B = RLC(Z80.B);											}; /* RLC  B 		  */
    opcode cb_01= () -> { Z80.C = RLC(Z80.C);											}; /* RLC  C 		  */
    opcode cb_02= () -> { Z80.D = RLC(Z80.D);											}; /* RLC  D 		  */
    opcode cb_03= () -> { Z80.E = RLC(Z80.E);											}; /* RLC  E 		  */
    opcode cb_04= () -> { Z80.H = RLC(Z80.H);											}; /* RLC  H 		  */
    opcode cb_05= () -> { Z80.L = RLC(Z80.L);											}; /* RLC  L 		  */
    opcode cb_06= () -> { WM( HL(), RLC(RM(HL())) );								}; /* RLC  (HL)		  */
    opcode cb_07= () -> { Z80.A = RLC(Z80.A);											}; /* RLC  A 		  */

    opcode cb_08= () -> { Z80.B = RRC(Z80.B);											}; /* RRC  B 		  */
    opcode cb_09= () -> { Z80.C = RRC(Z80.C);											}; /* RRC  C 		  */
    opcode cb_0a= () -> { Z80.D = RRC(Z80.D);											}; /* RRC  D 		  */
    opcode cb_0b= () -> { Z80.E = RRC(Z80.E);											}; /* RRC  E 		  */
    opcode cb_0c= () -> { Z80.H = RRC(Z80.H);											}; /* RRC  H 		  */
    opcode cb_0d= () -> { Z80.L = RRC(Z80.L);											}; /* RRC  L 		  */
    opcode cb_0e= () -> { WM( HL(), RRC(RM(HL())) );								}; /* RRC  (HL)		  */
    opcode cb_0f= () -> { Z80.A = RRC(Z80.A);											}; /* RRC  A 		  */

    opcode cb_10= () -> { Z80.B = RL(Z80.B);											}; /* RL   B 		  */
    opcode cb_11= () -> { Z80.C = RL(Z80.C);											}; /* RL   C 		  */
    opcode cb_12= () -> { Z80.D = RL(Z80.D);											}; /* RL   D 		  */
    opcode cb_13= () -> { Z80.E = RL(Z80.E);											}; /* RL   E 		  */
    opcode cb_14= () -> { Z80.H = RL(Z80.H);											}; /* RL   H 		  */
    opcode cb_15= () -> { Z80.L = RL(Z80.L);											}; /* RL   L 		  */
    opcode cb_16= () -> { WM( HL(), RL(RM(HL())) ); 								}; /* RL   (HL)		  */
    opcode cb_17= () -> { Z80.A = RL(Z80.A);											}; /* RL   A 		  */

    opcode cb_18= () -> { Z80.B = RR(Z80.B);											}; /* RR   B 		  */
    opcode cb_19= () -> { Z80.C = RR(Z80.C);											}; /* RR   C 		  */
    opcode cb_1a= () -> { Z80.D = RR(Z80.D);											}; /* RR   D 		  */
    opcode cb_1b= () -> { Z80.E = RR(Z80.E);											}; /* RR   E 		  */
    opcode cb_1c= () -> { Z80.H = RR(Z80.H);											}; /* RR   H 		  */
    opcode cb_1d= () -> { Z80.L = RR(Z80.L);											}; /* RR   L 		  */
    opcode cb_1e= () -> { WM( HL(), RR(RM(HL())) ); 								}; /* RR   (HL)		  */
    opcode cb_1f= () -> { Z80.A = RR(Z80.A);											}; /* RR   A 		  */

    opcode cb_20= () -> { Z80.B = SLA(Z80.B);											}; /* SLA  B 		  */
    opcode cb_21= () -> { Z80.C = SLA(Z80.C);											}; /* SLA  C 		  */
    opcode cb_22= () -> { Z80.D = SLA(Z80.D);											}; /* SLA  D 		  */
    opcode cb_23= () -> { Z80.E = SLA(Z80.E);											}; /* SLA  E 		  */
    opcode cb_24= () -> { Z80.H = SLA(Z80.H);											}; /* SLA  H 		  */
    opcode cb_25= () -> { Z80.L = SLA(Z80.L);											}; /* SLA  L 		  */
    opcode cb_26= () -> { WM( HL(), SLA(RM(HL())) );								}; /* SLA  (HL)		  */
    opcode cb_27= () -> { Z80.A = SLA(Z80.A);											}; /* SLA  A 		  */

    opcode cb_28= () -> { Z80.B = SRA(Z80.B);											}; /* SRA  B 		  */
    opcode cb_29= () -> { Z80.C = SRA(Z80.C);											}; /* SRA  C 		  */
    opcode cb_2a= () -> { Z80.D = SRA(Z80.D);											}; /* SRA  D 		  */
    opcode cb_2b= () -> { Z80.E = SRA(Z80.E);											}; /* SRA  E 		  */
    opcode cb_2c= () -> { Z80.H = SRA(Z80.H);											}; /* SRA  H 		  */
    opcode cb_2d= () -> { Z80.L = SRA(Z80.L);											}; /* SRA  L 		  */
    opcode cb_2e= () -> { WM( HL(), SRA(RM(HL())) );								}; /* SRA  (HL)		  */
    opcode cb_2f= () -> { Z80.A = SRA(Z80.A);											}; /* SRA  A 		  */

/*TODO*///opcode cb_30= () -> { Z80.B = SLL(Z80.B);											}; /* SLL  B 		  */
/*TODO*///opcode cb_31= () -> { Z80.C = SLL(Z80.C);											}; /* SLL  C 		  */
/*TODO*///opcode cb_32= () -> { Z80.D = SLL(Z80.D);											}; /* SLL  D 		  */
/*TODO*///opcode cb_33= () -> { Z80.E = SLL(Z80.E);											}; /* SLL  E 		  */
/*TODO*///opcode cb_34= () -> { Z80.H = SLL(Z80.H);											}; /* SLL  H 		  */
/*TODO*///opcode cb_35= () -> { Z80.L = SLL(Z80.L);											}; /* SLL  L 		  */
/*TODO*///opcode cb_36= () -> { WM( HL(), SLL(RM(HL())) );								}; /* SLL  (HL)		  */
/*TODO*///opcode cb_37= () -> { Z80.A = SLL(Z80.A);											}; /* SLL  A 		  */

    opcode cb_38= () -> { Z80.B = SRL(Z80.B);											}; /* SRL  B 		  */
    opcode cb_39= () -> { Z80.C = SRL(Z80.C);											}; /* SRL  C 		  */
    opcode cb_3a= () -> { Z80.D = SRL(Z80.D);											}; /* SRL  D 		  */
    opcode cb_3b= () -> { Z80.E = SRL(Z80.E);											}; /* SRL  E 		  */
    opcode cb_3c= () -> { Z80.H = SRL(Z80.H);											}; /* SRL  H 		  */
    opcode cb_3d= () -> { Z80.L = SRL(Z80.L);											}; /* SRL  L 		  */
    opcode cb_3e= () -> { WM( HL(), SRL(RM(HL())) );								}; /* SRL  (HL)		  */
    opcode cb_3f= () -> { Z80.A = SRL(Z80.A);											}; /* SRL  A 		  */

    opcode cb_40= () -> { BIT(0,Z80.B);												}; /* BIT  0,B		  */
    opcode cb_41= () -> { BIT(0,Z80.C);												}; /* BIT  0,C		  */
    opcode cb_42= () -> { BIT(0,Z80.D);												}; /* BIT  0,D		  */
    opcode cb_43= () -> { BIT(0,Z80.E);												}; /* BIT  0,E		  */
    opcode cb_44= () -> { BIT(0,Z80.H);												}; /* BIT  0,H		  */
    opcode cb_45= () -> { BIT(0,Z80.L);												}; /* BIT  0,L		  */
    opcode cb_46= () -> { BIT(0,RM(HL())); 										}; /* BIT  0,(HL)	  */
    opcode cb_47= () -> { BIT(0,Z80.A);												}; /* BIT  0,A		  */

    opcode cb_48= () -> { BIT(1,Z80.B);												}; /* BIT  1,B		  */
    opcode cb_49= () -> { BIT(1,Z80.C);												}; /* BIT  1,C		  */
    opcode cb_4a= () -> { BIT(1,Z80.D);												}; /* BIT  1,D		  */
    opcode cb_4b= () -> { BIT(1,Z80.E);												}; /* BIT  1,E		  */
    opcode cb_4c= () -> { BIT(1,Z80.H);												}; /* BIT  1,H		  */
    opcode cb_4d= () -> { BIT(1,Z80.L);												}; /* BIT  1,L		  */
    opcode cb_4e= () -> { BIT(1,RM(HL())); 										}; /* BIT  1,(HL)	  */
    opcode cb_4f= () -> { BIT(1,Z80.A);												}; /* BIT  1,A		  */

    opcode cb_50= () -> { BIT(2,Z80.B);												}; /* BIT  2,B		  */
    opcode cb_51= () -> { BIT(2,Z80.C);												}; /* BIT  2,C		  */
    opcode cb_52= () -> { BIT(2,Z80.D);												}; /* BIT  2,D		  */
    opcode cb_53= () -> { BIT(2,Z80.E);												}; /* BIT  2,E		  */
    opcode cb_54= () -> { BIT(2,Z80.H);												}; /* BIT  2,H		  */
    opcode cb_55= () -> { BIT(2,Z80.L);												}; /* BIT  2,L		  */
    opcode cb_56= () -> { BIT(2,RM(HL())); 										}; /* BIT  2,(HL)	  */
    opcode cb_57= () -> { BIT(2,Z80.A);												}; /* BIT  2,A		  */

    opcode cb_58= () -> { BIT(3,Z80.B);												}; /* BIT  3,B		  */
    opcode cb_59= () -> { BIT(3,Z80.C);												}; /* BIT  3,C		  */
    opcode cb_5a= () -> { BIT(3,Z80.D);												}; /* BIT  3,D		  */
    opcode cb_5b= () -> { BIT(3,Z80.E);												}; /* BIT  3,E		  */
    opcode cb_5c= () -> { BIT(3,Z80.H);												}; /* BIT  3,H		  */
    opcode cb_5d= () -> { BIT(3,Z80.L);												}; /* BIT  3,L		  */
    opcode cb_5e= () -> { BIT(3,RM(HL())); 										}; /* BIT  3,(HL)	  */
    opcode cb_5f= () -> { BIT(3,Z80.A);												}; /* BIT  3,A		  */

    opcode cb_60= () -> { BIT(4,Z80.B);												}; /* BIT  4,B		  */
    opcode cb_61= () -> { BIT(4,Z80.C);												}; /* BIT  4,C		  */
    opcode cb_62= () -> { BIT(4,Z80.D);												}; /* BIT  4,D		  */
    opcode cb_63= () -> { BIT(4,Z80.E);												}; /* BIT  4,E		  */
    opcode cb_64= () -> { BIT(4,Z80.H);												}; /* BIT  4,H		  */
    opcode cb_65= () -> { BIT(4,Z80.L);												}; /* BIT  4,L		  */
    opcode cb_66= () -> { BIT(4,RM(HL())); 										}; /* BIT  4,(HL)	  */
    opcode cb_67= () -> { BIT(4,Z80.A);												}; /* BIT  4,A		  */

    opcode cb_68= () -> { BIT(5,Z80.B);												}; /* BIT  5,B		  */
    opcode cb_69= () -> { BIT(5,Z80.C);												}; /* BIT  5,C		  */
    opcode cb_6a= () -> { BIT(5,Z80.D);												}; /* BIT  5,D		  */
    opcode cb_6b= () -> { BIT(5,Z80.E);												}; /* BIT  5,E		  */
    opcode cb_6c= () -> { BIT(5,Z80.H);												}; /* BIT  5,H		  */
    opcode cb_6d= () -> { BIT(5,Z80.L);												}; /* BIT  5,L		  */
    opcode cb_6e= () -> { BIT(5,RM(HL())); 										}; /* BIT  5,(HL)	  */
    opcode cb_6f= () -> { BIT(5,Z80.A);												}; /* BIT  5,A		  */

    opcode cb_70= () -> { BIT(6,Z80.B);												}; /* BIT  6,B		  */
    opcode cb_71= () -> { BIT(6,Z80.C);												}; /* BIT  6,C		  */
    opcode cb_72= () -> { BIT(6,Z80.D);												}; /* BIT  6,D		  */
    opcode cb_73= () -> { BIT(6,Z80.E);												}; /* BIT  6,E		  */
    opcode cb_74= () -> { BIT(6,Z80.H);												}; /* BIT  6,H		  */
    opcode cb_75= () -> { BIT(6,Z80.L);												}; /* BIT  6,L		  */
    opcode cb_76= () -> { BIT(6,RM(HL())); 										}; /* BIT  6,(HL)	  */
    opcode cb_77= () -> { BIT(6,Z80.A);												}; /* BIT  6,A		  */

    opcode cb_78= () -> { BIT(7,Z80.B);												}; /* BIT  7,B		  */
    opcode cb_79= () -> { BIT(7,Z80.C);												}; /* BIT  7,C		  */
    opcode cb_7a= () -> { BIT(7,Z80.D);												}; /* BIT  7,D		  */
    opcode cb_7b= () -> { BIT(7,Z80.E);												}; /* BIT  7,E		  */
    opcode cb_7c= () -> { BIT(7,Z80.H);												}; /* BIT  7,H		  */
    opcode cb_7d= () -> { BIT(7,Z80.L);												}; /* BIT  7,L		  */
    opcode cb_7e= () -> { BIT(7,RM(HL())); 										}; /* BIT  7,(HL)	  */
    opcode cb_7f= () -> { BIT(7,Z80.A);												}; /* BIT  7,A		  */

    opcode cb_80= () -> { Z80.B = RES(0,Z80.B); 										}; /* RES  0,B		  */
    opcode cb_81= () -> { Z80.C = RES(0,Z80.C); 										}; /* RES  0,C		  */
    opcode cb_82= () -> { Z80.D = RES(0,Z80.D); 										}; /* RES  0,D		  */
    opcode cb_83= () -> { Z80.E = RES(0,Z80.E); 										}; /* RES  0,E		  */
    opcode cb_84= () -> { Z80.H = RES(0,Z80.H); 										}; /* RES  0,H		  */
    opcode cb_85= () -> { Z80.L = RES(0,Z80.L); 										}; /* RES  0,L		  */
    opcode cb_86= () -> { WM( HL(), RES(0,RM(HL())) );								}; /* RES  0,(HL)	  */
    opcode cb_87= () -> { Z80.A = RES(0,Z80.A); 										}; /* RES  0,A		  */

    opcode cb_88= () -> { Z80.B = RES(1,Z80.B); 										}; /* RES  1,B		  */
    opcode cb_89= () -> { Z80.C = RES(1,Z80.C); 										}; /* RES  1,C		  */
    opcode cb_8a= () -> { Z80.D = RES(1,Z80.D); 										}; /* RES  1,D		  */
    opcode cb_8b= () -> { Z80.E = RES(1,Z80.E); 										}; /* RES  1,E		  */
    opcode cb_8c= () -> { Z80.H = RES(1,Z80.H); 										}; /* RES  1,H		  */
    opcode cb_8d= () -> { Z80.L = RES(1,Z80.L); 										}; /* RES  1,L		  */
    opcode cb_8e= () -> { WM( HL(), RES(1,RM(HL())) );								}; /* RES  1,(HL)	  */
    opcode cb_8f= () -> { Z80.A = RES(1,Z80.A); 										}; /* RES  1,A		  */

    opcode cb_90= () -> { Z80.B = RES(2,Z80.B); 										}; /* RES  2,B		  */
    opcode cb_91= () -> { Z80.C = RES(2,Z80.C); 										}; /* RES  2,C		  */
    opcode cb_92= () -> { Z80.D = RES(2,Z80.D); 										}; /* RES  2,D		  */
    opcode cb_93= () -> { Z80.E = RES(2,Z80.E); 										}; /* RES  2,E		  */
    opcode cb_94= () -> { Z80.H = RES(2,Z80.H); 										}; /* RES  2,H		  */
    opcode cb_95= () -> { Z80.L = RES(2,Z80.L); 										}; /* RES  2,L		  */
    opcode cb_96= () -> { WM( HL(), RES(2,RM(HL())) );								}; /* RES  2,(HL)	  */
    opcode cb_97= () -> { Z80.A = RES(2,Z80.A); 										}; /* RES  2,A		  */

    opcode cb_98= () -> { Z80.B = RES(3,Z80.B); 										}; /* RES  3,B		  */
    opcode cb_99= () -> { Z80.C = RES(3,Z80.C); 										}; /* RES  3,C		  */
    opcode cb_9a= () -> { Z80.D = RES(3,Z80.D); 										}; /* RES  3,D		  */
    opcode cb_9b= () -> { Z80.E = RES(3,Z80.E); 										}; /* RES  3,E		  */
    opcode cb_9c= () -> { Z80.H = RES(3,Z80.H); 										}; /* RES  3,H		  */
    opcode cb_9d= () -> { Z80.L = RES(3,Z80.L); 										}; /* RES  3,L		  */
    opcode cb_9e= () -> { WM( HL(), RES(3,RM(HL())) );								}; /* RES  3,(HL)	  */
    opcode cb_9f= () -> { Z80.A = RES(3,Z80.A); 										}; /* RES  3,A		  */

    opcode cb_a0= () -> { Z80.B = RES(4,Z80.B); 										}; /* RES  4,B		  */
    opcode cb_a1= () -> { Z80.C = RES(4,Z80.C); 										}; /* RES  4,C		  */
    opcode cb_a2= () -> { Z80.D = RES(4,Z80.D); 										}; /* RES  4,D		  */
    opcode cb_a3= () -> { Z80.E = RES(4,Z80.E); 										}; /* RES  4,E		  */
    opcode cb_a4= () -> { Z80.H = RES(4,Z80.H); 										}; /* RES  4,H		  */
    opcode cb_a5= () -> { Z80.L = RES(4,Z80.L); 										}; /* RES  4,L		  */
    opcode cb_a6= () -> { WM( HL(), RES(4,RM(HL())) );								}; /* RES  4,(HL)	  */
    opcode cb_a7= () -> { Z80.A = RES(4,Z80.A); 										}; /* RES  4,A		  */

    opcode cb_a8= () -> { Z80.B = RES(5,Z80.B); 										}; /* RES  5,B		  */
    opcode cb_a9= () -> { Z80.C = RES(5,Z80.C); 										}; /* RES  5,C		  */
    opcode cb_aa= () -> { Z80.D = RES(5,Z80.D); 										}; /* RES  5,D		  */
    opcode cb_ab= () -> { Z80.E = RES(5,Z80.E); 										}; /* RES  5,E		  */
    opcode cb_ac= () -> { Z80.H = RES(5,Z80.H); 										}; /* RES  5,H		  */
    opcode cb_ad= () -> { Z80.L = RES(5,Z80.L); 										}; /* RES  5,L		  */
    opcode cb_ae= () -> { WM( HL(), RES(5,RM(HL())) );								}; /* RES  5,(HL)	  */
    opcode cb_af= () -> { Z80.A = RES(5,Z80.A); 										}; /* RES  5,A		  */

    opcode cb_b0= () -> { Z80.B = RES(6,Z80.B); 										}; /* RES  6,B		  */
    opcode cb_b1= () -> { Z80.C = RES(6,Z80.C); 										}; /* RES  6,C		  */
    opcode cb_b2= () -> { Z80.D = RES(6,Z80.D); 										}; /* RES  6,D		  */
    opcode cb_b3= () -> { Z80.E = RES(6,Z80.E); 										}; /* RES  6,E		  */
    opcode cb_b4= () -> { Z80.H = RES(6,Z80.H); 										}; /* RES  6,H		  */
    opcode cb_b5= () -> { Z80.L = RES(6,Z80.L); 										}; /* RES  6,L		  */
    opcode cb_b6= () -> { WM( HL(), RES(6,RM(HL())) );								}; /* RES  6,(HL)	  */
    opcode cb_b7= () -> { Z80.A = RES(6,Z80.A); 										}; /* RES  6,A		  */

    opcode cb_b8= () -> { Z80.B = RES(7,Z80.B); 										}; /* RES  7,B		  */
    opcode cb_b9= () -> { Z80.C = RES(7,Z80.C); 										}; /* RES  7,C		  */
    opcode cb_ba= () -> { Z80.D = RES(7,Z80.D); 										}; /* RES  7,D		  */
    opcode cb_bb= () -> { Z80.E = RES(7,Z80.E); 										}; /* RES  7,E		  */
    opcode cb_bc= () -> { Z80.H = RES(7,Z80.H); 										}; /* RES  7,H		  */
    opcode cb_bd= () -> { Z80.L = RES(7,Z80.L); 										}; /* RES  7,L		  */
    opcode cb_be= () -> { WM( HL(), RES(7,RM(HL())) );								}; /* RES  7,(HL)	  */
    opcode cb_bf= () -> { Z80.A = RES(7,Z80.A); 										}; /* RES  7,A		  */

    opcode cb_c0= () -> { Z80.B = SET(0,Z80.B); 										}; /* SET  0,B		  */
    opcode cb_c1= () -> { Z80.C = SET(0,Z80.C); 										}; /* SET  0,C		  */
    opcode cb_c2= () -> { Z80.D = SET(0,Z80.D); 										}; /* SET  0,D		  */
    opcode cb_c3= () -> { Z80.E = SET(0,Z80.E); 										}; /* SET  0,E		  */
    opcode cb_c4= () -> { Z80.H = SET(0,Z80.H); 										}; /* SET  0,H		  */
    opcode cb_c5= () -> { Z80.L = SET(0,Z80.L); 										}; /* SET  0,L		  */
    opcode cb_c6= () -> { WM( HL(), SET(0,RM(HL())) );								}; /* SET  0,(HL)	  */
    opcode cb_c7= () -> { Z80.A = SET(0,Z80.A); 										}; /* SET  0,A		  */

    opcode cb_c8= () -> { Z80.B = SET(1,Z80.B); 										}; /* SET  1,B		  */
    opcode cb_c9= () -> { Z80.C = SET(1,Z80.C); 										}; /* SET  1,C		  */
    opcode cb_ca= () -> { Z80.D = SET(1,Z80.D); 										}; /* SET  1,D		  */
    opcode cb_cb= () -> { Z80.E = SET(1,Z80.E); 										}; /* SET  1,E		  */
    opcode cb_cc= () -> { Z80.H = SET(1,Z80.H); 										}; /* SET  1,H		  */
    opcode cb_cd= () -> { Z80.L = SET(1,Z80.L); 										}; /* SET  1,L		  */
    opcode cb_ce= () -> { WM( HL(), SET(1,RM(HL())) );								}; /* SET  1,(HL)	  */
    opcode cb_cf= () -> { Z80.A = SET(1,Z80.A); 										}; /* SET  1,A		  */

    opcode cb_d0= () -> { Z80.B = SET(2,Z80.B); 										}; /* SET  2,B		  */
    opcode cb_d1= () -> { Z80.C = SET(2,Z80.C); 										}; /* SET  2,C		  */
    opcode cb_d2= () -> { Z80.D = SET(2,Z80.D); 										}; /* SET  2,D		  */
    opcode cb_d3= () -> { Z80.E = SET(2,Z80.E); 										}; /* SET  2,E		  */
    opcode cb_d4= () -> { Z80.H = SET(2,Z80.H); 										}; /* SET  2,H		  */
    opcode cb_d5= () -> { Z80.L = SET(2,Z80.L); 										}; /* SET  2,L		  */
    opcode cb_d6= () -> { WM( HL(), SET(2,RM(HL())) );								};/* SET  2,(HL) 	 */
    opcode cb_d7= () -> { Z80.A = SET(2,Z80.A); 										}; /* SET  2,A		  */

    opcode cb_d8= () -> { Z80.B = SET(3,Z80.B); 										}; /* SET  3,B		  */
    opcode cb_d9= () -> { Z80.C = SET(3,Z80.C); 										}; /* SET  3,C		  */
    opcode cb_da= () -> { Z80.D = SET(3,Z80.D); 										}; /* SET  3,D		  */
    opcode cb_db= () -> { Z80.E = SET(3,Z80.E); 										}; /* SET  3,E		  */
    opcode cb_dc= () -> { Z80.H = SET(3,Z80.H); 										}; /* SET  3,H		  */
    opcode cb_dd= () -> { Z80.L = SET(3,Z80.L); 										}; /* SET  3,L		  */
    opcode cb_de= () -> { WM( HL(), SET(3,RM(HL())) );								}; /* SET  3,(HL)	  */
    opcode cb_df= () -> { Z80.A = SET(3,Z80.A); 										}; /* SET  3,A		  */

    opcode cb_e0= () -> { Z80.B = SET(4,Z80.B); 										}; /* SET  4,B		  */
    opcode cb_e1= () -> { Z80.C = SET(4,Z80.C); 										}; /* SET  4,C		  */
    opcode cb_e2= () -> { Z80.D = SET(4,Z80.D); 										}; /* SET  4,D		  */
    opcode cb_e3= () -> { Z80.E = SET(4,Z80.E); 										}; /* SET  4,E		  */
    opcode cb_e4= () -> { Z80.H = SET(4,Z80.H); 										}; /* SET  4,H		  */
    opcode cb_e5= () -> { Z80.L = SET(4,Z80.L); 										}; /* SET  4,L		  */
    opcode cb_e6= () -> { WM( HL(), SET(4,RM(HL())) );								}; /* SET  4,(HL)	  */
    opcode cb_e7= () -> { Z80.A = SET(4,Z80.A); 										}; /* SET  4,A		  */

    opcode cb_e8= () -> { Z80.B = SET(5,Z80.B); 										}; /* SET  5,B		  */
    opcode cb_e9= () -> { Z80.C = SET(5,Z80.C); 										}; /* SET  5,C		  */
    opcode cb_ea= () -> { Z80.D = SET(5,Z80.D); 										}; /* SET  5,D		  */
    opcode cb_eb= () -> { Z80.E = SET(5,Z80.E); 										}; /* SET  5,E		  */
    opcode cb_ec= () -> { Z80.H = SET(5,Z80.H); 										}; /* SET  5,H		  */
    opcode cb_ed= () -> { Z80.L = SET(5,Z80.L); 										}; /* SET  5,L		  */
    opcode cb_ee= () -> { WM( HL(), SET(5,RM(HL())) );								}; /* SET  5,(HL)	  */
    opcode cb_ef= () -> { Z80.A = SET(5,Z80.A); 										}; /* SET  5,A		  */

    opcode cb_f0= () -> { Z80.B = SET(6,Z80.B); 										}; /* SET  6,B		  */
    opcode cb_f1= () -> { Z80.C = SET(6,Z80.C); 										}; /* SET  6,C		  */
    opcode cb_f2= () -> { Z80.D = SET(6,Z80.D); 										}; /* SET  6,D		  */
    opcode cb_f3= () -> { Z80.E = SET(6,Z80.E); 										}; /* SET  6,E		  */
    opcode cb_f4= () -> { Z80.H = SET(6,Z80.H); 										}; /* SET  6,H		  */
    opcode cb_f5= () -> { Z80.L = SET(6,Z80.L); 										}; /* SET  6,L		  */
    opcode cb_f6= () -> { WM( HL(), SET(6,RM(HL())) );								}; /* SET  6,(HL)	  */
    opcode cb_f7= () -> { Z80.A = SET(6,Z80.A); 										}; /* SET  6,A		  */

    opcode cb_f8= () -> { Z80.B = SET(7,Z80.B); 										}; /* SET  7,B		  */
    opcode cb_f9= () -> { Z80.C = SET(7,Z80.C); 										}; /* SET  7,C		  */
    opcode cb_fa= () -> { Z80.D = SET(7,Z80.D); 										}; /* SET  7,D		  */
    opcode cb_fb= () -> { Z80.E = SET(7,Z80.E); 										}; /* SET  7,E		  */
    opcode cb_fc= () -> { Z80.H = SET(7,Z80.H); 										}; /* SET  7,H		  */
    opcode cb_fd= () -> { Z80.L = SET(7,Z80.L); 										}; /* SET  7,L		  */
    opcode cb_fe= () -> { WM( HL(), SET(7,RM(HL())) );								}; /* SET  7,(HL)	  */
    opcode cb_ff= () -> { Z80.A = SET(7,Z80.A); 										}; /* SET  7,A		  */


/*TODO*////**********************************************************
/*TODO*///* opcodes with DD/FD CB prefix
/*TODO*///* rotate, shift and bit operations with (IX+o)
/*TODO*///**********************************************************/
/*TODO*///opcode xycb_00= () -> { _B = RLC( RM(EA) ); WM( EA,_B );						}; /* RLC  B=(XY+o)	  */
/*TODO*///opcode xycb_01= () -> { _C = RLC( RM(EA) ); WM( EA,_C );						}; /* RLC  C=(XY+o)	  */
/*TODO*///opcode xycb_02= () -> { _D = RLC( RM(EA) ); WM( EA,_D );						}; /* RLC  D=(XY+o)	  */
/*TODO*///opcode xycb_03= () -> { _E = RLC( RM(EA) ); WM( EA,_E );						}; /* RLC  E=(XY+o)	  */
/*TODO*///opcode xycb_04= () -> { _H = RLC( RM(EA) ); WM( EA,_H );						}; /* RLC  H=(XY+o)	  */
/*TODO*///opcode xycb_05= () -> { _L = RLC( RM(EA) ); WM( EA,_L );						}; /* RLC  L=(XY+o)	  */
/*TODO*///opcode xycb_06= () -> { WM( EA, RLC( RM(EA) ) );								}; /* RLC  (XY+o)	  */
/*TODO*///opcode xycb_07= () -> { _A = RLC( RM(EA) ); WM( EA,_A );						}; /* RLC  A=(XY+o)	  */
/*TODO*///
/*TODO*///opcode xycb_08= () -> { _B = RRC( RM(EA) ); WM( EA,_B );						}; /* RRC  B=(XY+o)	  */
/*TODO*///opcode xycb_09= () -> { _C = RRC( RM(EA) ); WM( EA,_C );						}; /* RRC  C=(XY+o)	  */
/*TODO*///opcode xycb_0a= () -> { _D = RRC( RM(EA) ); WM( EA,_D );						}; /* RRC  D=(XY+o)	  */
/*TODO*///opcode xycb_0b= () -> { _E = RRC( RM(EA) ); WM( EA,_E );						}; /* RRC  E=(XY+o)	  */
/*TODO*///opcode xycb_0c= () -> { _H = RRC( RM(EA) ); WM( EA,_H );						}; /* RRC  H=(XY+o)	  */
/*TODO*///opcode xycb_0d= () -> { _L = RRC( RM(EA) ); WM( EA,_L );						}; /* RRC  L=(XY+o)	  */
/*TODO*///opcode xycb_0e= () -> { WM( EA,RRC( RM(EA) ) );								}; /* RRC  (XY+o)	  */
/*TODO*///opcode xycb_0f= () -> { _A = RRC( RM(EA) ); WM( EA,_A );						}; /* RRC  A=(XY+o)	  */
/*TODO*///
/*TODO*///opcode xycb_10= () -> { _B = RL( RM(EA) ); WM( EA,_B );						}; /* RL   B=(XY+o)	  */
/*TODO*///opcode xycb_11= () -> { _C = RL( RM(EA) ); WM( EA,_C );						}; /* RL   C=(XY+o)	  */
/*TODO*///opcode xycb_12= () -> { _D = RL( RM(EA) ); WM( EA,_D );						}; /* RL   D=(XY+o)	  */
/*TODO*///opcode xycb_13= () -> { _E = RL( RM(EA) ); WM( EA,_E );						}; /* RL   E=(XY+o)	  */
/*TODO*///opcode xycb_14= () -> { _H = RL( RM(EA) ); WM( EA,_H );						}; /* RL   H=(XY+o)	  */
/*TODO*///opcode xycb_15= () -> { _L = RL( RM(EA) ); WM( EA,_L );						}; /* RL   L=(XY+o)	  */
    opcode xycb_16= () -> { WM( EA,RL( RM(EA) ) );								}; /* RL   (XY+o)	  */
/*TODO*///opcode xycb_17= () -> { _A = RL( RM(EA) ); WM( EA,_A );						}; /* RL   A=(XY+o)	  */
/*TODO*///
/*TODO*///opcode xycb_18= () -> { _B = RR( RM(EA) ); WM( EA,_B );						}; /* RR   B=(XY+o)	  */
/*TODO*///opcode xycb_19= () -> { _C = RR( RM(EA) ); WM( EA,_C );						}; /* RR   C=(XY+o)	  */
/*TODO*///opcode xycb_1a= () -> { _D = RR( RM(EA) ); WM( EA,_D );						}; /* RR   D=(XY+o)	  */
/*TODO*///opcode xycb_1b= () -> { _E = RR( RM(EA) ); WM( EA,_E );						}; /* RR   E=(XY+o)	  */
/*TODO*///opcode xycb_1c= () -> { _H = RR( RM(EA) ); WM( EA,_H );						}; /* RR   H=(XY+o)	  */
/*TODO*///opcode xycb_1d= () -> { _L = RR( RM(EA) ); WM( EA,_L );						}; /* RR   L=(XY+o)	  */
/*TODO*///opcode xycb_1e= () -> { WM( EA,RR( RM(EA) ) );								}; /* RR   (XY+o)	  */
/*TODO*///opcode xycb_1f= () -> { _A = RR( RM(EA) ); WM( EA,_A );						}; /* RR   A=(XY+o)	  */
/*TODO*///
/*TODO*///opcode xycb_20= () -> { _B = SLA( RM(EA) ); WM( EA,_B );						}; /* SLA  B=(XY+o)	  */
/*TODO*///opcode xycb_21= () -> { _C = SLA( RM(EA) ); WM( EA,_C );						}; /* SLA  C=(XY+o)	  */
/*TODO*///opcode xycb_22= () -> { _D = SLA( RM(EA) ); WM( EA,_D );						}; /* SLA  D=(XY+o)	  */
/*TODO*///opcode xycb_23= () -> { _E = SLA( RM(EA) ); WM( EA,_E );						}; /* SLA  E=(XY+o)	  */
/*TODO*///opcode xycb_24= () -> { _H = SLA( RM(EA) ); WM( EA,_H );						}; /* SLA  H=(XY+o)	  */
/*TODO*///opcode xycb_25= () -> { _L = SLA( RM(EA) ); WM( EA,_L );						}; /* SLA  L=(XY+o)	  */
/*TODO*///opcode xycb_26= () -> { WM( EA,SLA( RM(EA) ) );								}; /* SLA  (XY+o)	  */
/*TODO*///opcode xycb_27= () -> { _A = SLA( RM(EA) ); WM( EA,_A );						}; /* SLA  A=(XY+o)	  */
/*TODO*///
/*TODO*///opcode xycb_28= () -> { _B = SRA( RM(EA) ); WM( EA,_B );						}; /* SRA  B=(XY+o)	  */
/*TODO*///opcode xycb_29= () -> { _C = SRA( RM(EA) ); WM( EA,_C );						}; /* SRA  C=(XY+o)	  */
/*TODO*///opcode xycb_2a= () -> { _D = SRA( RM(EA) ); WM( EA,_D );						}; /* SRA  D=(XY+o)	  */
/*TODO*///opcode xycb_2b= () -> { _E = SRA( RM(EA) ); WM( EA,_E );						}; /* SRA  E=(XY+o)	  */
/*TODO*///opcode xycb_2c= () -> { _H = SRA( RM(EA) ); WM( EA,_H );						}; /* SRA  H=(XY+o)	  */
/*TODO*///opcode xycb_2d= () -> { _L = SRA( RM(EA) ); WM( EA,_L );						}; /* SRA  L=(XY+o)	  */
/*TODO*///opcode xycb_2e= () -> { WM( EA,SRA( RM(EA) ) );								}; /* SRA  (XY+o)	  */
/*TODO*///opcode xycb_2f= () -> { _A = SRA( RM(EA) ); WM( EA,_A );						}; /* SRA  A=(XY+o)	  */
/*TODO*///
/*TODO*///opcode xycb_30= () -> { _B = SLL( RM(EA) ); WM( EA,_B );						}; /* SLL  B=(XY+o)	  */
/*TODO*///opcode xycb_31= () -> { _C = SLL( RM(EA) ); WM( EA,_C );						}; /* SLL  C=(XY+o)	  */
/*TODO*///opcode xycb_32= () -> { _D = SLL( RM(EA) ); WM( EA,_D );						}; /* SLL  D=(XY+o)	  */
/*TODO*///opcode xycb_33= () -> { _E = SLL( RM(EA) ); WM( EA,_E );						}; /* SLL  E=(XY+o)	  */
/*TODO*///opcode xycb_34= () -> { _H = SLL( RM(EA) ); WM( EA,_H );						}; /* SLL  H=(XY+o)	  */
/*TODO*///opcode xycb_35= () -> { _L = SLL( RM(EA) ); WM( EA,_L );						}; /* SLL  L=(XY+o)	  */
/*TODO*///opcode xycb_36= () -> { WM( EA,SLL( RM(EA) ) );								}; /* SLL  (XY+o)	  */
/*TODO*///opcode xycb_37= () -> { _A = SLL( RM(EA) ); WM( EA,_A );						}; /* SLL  A=(XY+o)	  */
/*TODO*///
    opcode xycb_38= () -> { Z80.B = SRL( RM(EA) ); WM( EA,Z80.B );						}; /* SRL  B=(XY+o)	  */
    opcode xycb_39= () -> { Z80.C = SRL( RM(EA) ); WM( EA,Z80.C );						}; /* SRL  C=(XY+o)	  */
    opcode xycb_3a= () -> { Z80.D = SRL( RM(EA) ); WM( EA,Z80.D );						}; /* SRL  D=(XY+o)	  */
    opcode xycb_3b= () -> { Z80.E = SRL( RM(EA) ); WM( EA,Z80.E );						}; /* SRL  E=(XY+o)	  */
    opcode xycb_3c= () -> { Z80.H = SRL( RM(EA) ); WM( EA,Z80.H );						}; /* SRL  H=(XY+o)	  */
    opcode xycb_3d= () -> { Z80.L = SRL( RM(EA) ); WM( EA,Z80.L );						}; /* SRL  L=(XY+o)	  */
    opcode xycb_3e= () -> { WM( EA,SRL( RM(EA) ) );								}; /* SRL  (XY+o)	  */
    opcode xycb_3f= () -> { Z80.A = SRL( RM(EA) ); WM( EA,Z80.A );						}; /* SRL  A=(XY+o)	  */

    opcode xycb_40= new opcode() { public void handler(){ xycb_46.handler();											}}; /* BIT  0,B=(XY+o)  */
    opcode xycb_41= new opcode() { public void handler(){ xycb_46.handler();													  }}; /* BIT	0,C=(XY+o)	*/
    opcode xycb_42= new opcode() { public void handler(){ xycb_46.handler();											}}; /* BIT  0,D=(XY+o)  */
    opcode xycb_43= new opcode() { public void handler(){ xycb_46.handler();											}}; /* BIT  0,E=(XY+o)  */
    opcode xycb_44= new opcode() { public void handler(){ xycb_46.handler();											}}; /* BIT  0,H=(XY+o)  */
    opcode xycb_45= new opcode() { public void handler(){ xycb_46.handler();											}}; /* BIT  0,L=(XY+o)  */
    opcode xycb_46= () -> { BIT_XY(0,RM(EA)); 									}; /* BIT  0,(XY+o)	  */
    opcode xycb_47= new opcode() { public void handler(){ xycb_46.handler();											}}; /* BIT  0,A=(XY+o)  */

    opcode xycb_48= new opcode() { public void handler(){ xycb_4e.handler();											}}; /* BIT  1,B=(XY+o)  */
    opcode xycb_49= new opcode() { public void handler(){ xycb_4e.handler();													  }}; /* BIT	1,C=(XY+o)	*/
    opcode xycb_4a= new opcode() { public void handler(){ xycb_4e.handler();											}}; /* BIT  1,D=(XY+o)  */
    opcode xycb_4b= new opcode() { public void handler(){ xycb_4e.handler();											}}; /* BIT  1,E=(XY+o)  */
    opcode xycb_4c= new opcode() { public void handler(){ xycb_4e.handler();											}}; /* BIT  1,H=(XY+o)  */
    opcode xycb_4d= new opcode() { public void handler(){ xycb_4e.handler();											}}; /* BIT  1,L=(XY+o)  */
    opcode xycb_4e= () -> { BIT_XY(1,RM(EA)); 									}; /* BIT  1,(XY+o)	  */
    opcode xycb_4f= new opcode() { public void handler(){ xycb_4e.handler();											}}; /* BIT  1,A=(XY+o)  */

    opcode xycb_50= new opcode() { public void handler(){ xycb_56.handler();											}}; /* BIT  2,B=(XY+o)  */
    opcode xycb_51= new opcode() { public void handler(){ xycb_56.handler();													  }}; /* BIT	2,C=(XY+o)	*/
    opcode xycb_52= new opcode() { public void handler(){ xycb_56.handler();											}}; /* BIT  2,D=(XY+o)  */
    opcode xycb_53= new opcode() { public void handler(){ xycb_56.handler();											}}; /* BIT  2,E=(XY+o)  */
    opcode xycb_54= new opcode() { public void handler(){ xycb_56.handler();											}}; /* BIT  2,H=(XY+o)  */
    opcode xycb_55= new opcode() { public void handler(){ xycb_56.handler();											}}; /* BIT  2,L=(XY+o)  */
    opcode xycb_56= () -> { BIT_XY(2,RM(EA)); 									}; /* BIT  2,(XY+o)	  */
    opcode xycb_57= new opcode() { public void handler(){ xycb_56.handler();											}}; /* BIT  2,A=(XY+o)  *//*TODO*///

    opcode xycb_58= new opcode() { public void handler(){ xycb_5e.handler();											}}; /* BIT  3,B=(XY+o)  */
    opcode xycb_59= new opcode() { public void handler(){ xycb_5e.handler();													  }}; /* BIT	3,C=(XY+o)	*/
    opcode xycb_5a= new opcode() { public void handler(){ xycb_5e.handler();											}}; /* BIT  3,D=(XY+o)  */
    opcode xycb_5b= new opcode() { public void handler(){ xycb_5e.handler();											}}; /* BIT  3,E=(XY+o)  */
    opcode xycb_5c= new opcode() { public void handler(){ xycb_5e.handler();											}}; /* BIT  3,H=(XY+o)  */
    opcode xycb_5d= new opcode() { public void handler(){ xycb_5e.handler();											}}; /* BIT  3,L=(XY+o)  */
    opcode xycb_5e= () -> { BIT_XY(3,RM(EA)); 									}; /* BIT  3,(XY+o)	  */
    opcode xycb_5f= new opcode() { public void handler(){ xycb_5e.handler();											}}; /* BIT  3,A=(XY+o)  */

    opcode xycb_60= new opcode() { public void handler(){ xycb_66.handler();											}}; /* BIT  4,B=(XY+o)  */
    opcode xycb_61= new opcode() { public void handler(){ xycb_66.handler();													  }}; /* BIT	4,C=(XY+o)	*/
    opcode xycb_62= new opcode() { public void handler(){ xycb_66.handler();											}}; /* BIT  4,D=(XY+o)  */
    opcode xycb_63= new opcode() { public void handler(){ xycb_66.handler();											}}; /* BIT  4,E=(XY+o)  */
    opcode xycb_64= new opcode() { public void handler(){ xycb_66.handler();											}}; /* BIT  4,H=(XY+o)  */
    opcode xycb_65= new opcode() { public void handler(){ xycb_66.handler();											}}; /* BIT  4,L=(XY+o)  */
    opcode xycb_66= () -> { BIT_XY(4,RM(EA)); 									}; /* BIT  4,(XY+o)	  */
    opcode xycb_67= new opcode() { public void handler(){ xycb_66.handler();											}}; /* BIT  4,A=(XY+o)  */

    opcode xycb_68= new opcode() { public void handler(){ xycb_6e.handler();											}}; /* BIT  5,B=(XY+o)  */
    opcode xycb_69= new opcode() { public void handler(){ xycb_6e.handler();													  }}; /* BIT	5,C=(XY+o)	*/
    opcode xycb_6a= new opcode() { public void handler(){ xycb_6e.handler();											}}; /* BIT  5,D=(XY+o)  */
    opcode xycb_6b= new opcode() { public void handler(){ xycb_6e.handler();											}}; /* BIT  5,E=(XY+o)  */
    opcode xycb_6c= new opcode() { public void handler(){ xycb_6e.handler();											}}; /* BIT  5,H=(XY+o)  */
    opcode xycb_6d= new opcode() { public void handler(){ xycb_6e.handler();											}}; /* BIT  5,L=(XY+o)  */
    opcode xycb_6e= () -> { BIT_XY(5,RM(EA)); 									}; /* BIT  5,(XY+o)	  */
    opcode xycb_6f= new opcode() { public void handler(){ xycb_6e.handler();											}}; /* BIT  5,A=(XY+o)  */

    opcode xycb_70= new opcode() { public void handler(){ xycb_76.handler();											}}; /* BIT  6,B=(XY+o)  */
    opcode xycb_71= new opcode() { public void handler(){ xycb_76.handler();													  }}; /* BIT	6,C=(XY+o)	*/
    opcode xycb_72= new opcode() { public void handler(){ xycb_76.handler();											}}; /* BIT  6,D=(XY+o)  */
    opcode xycb_73= new opcode() { public void handler(){ xycb_76.handler();											}}; /* BIT  6,E=(XY+o)  */
    opcode xycb_74= new opcode() { public void handler(){ xycb_76.handler();											}}; /* BIT  6,H=(XY+o)  */
    opcode xycb_75= new opcode() { public void handler(){ xycb_76.handler();											}}; /* BIT  6,L=(XY+o)  */
    opcode xycb_76= () -> { BIT_XY(6,RM(EA)); 									}; /* BIT  6,(XY+o)	  */
    opcode xycb_77= new opcode() { public void handler(){ xycb_76.handler();											}}; /* BIT  6,A=(XY+o)  */

    opcode xycb_78= new opcode() { public void handler(){ xycb_7e.handler();											}}; /* BIT  7,B=(XY+o)  */
    opcode xycb_79= new opcode() { public void handler(){ xycb_7e.handler();													  }}; /* BIT	7,C=(XY+o)	*/
    opcode xycb_7a= new opcode() { public void handler(){ xycb_7e.handler();											}}; /* BIT  7,D=(XY+o)  */
    opcode xycb_7b= new opcode() { public void handler(){ xycb_7e.handler();											}}; /* BIT  7,E=(XY+o)  */
    opcode xycb_7c= new opcode() { public void handler(){ xycb_7e.handler();											}}; /* BIT  7,H=(XY+o)  */
    opcode xycb_7d= new opcode() { public void handler(){ xycb_7e.handler();											}}; /* BIT  7,L=(XY+o)  */
    opcode xycb_7e= () -> { BIT_XY(7,RM(EA)); 									}; /* BIT  7,(XY+o)	  */
    opcode xycb_7f= new opcode() { public void handler(){ xycb_7e.handler();											}}; /* BIT  7,A=(XY+o)  */

/*TODO*///opcode xycb_80= () -> { _B = RES(0, RM(EA) ); WM( EA,_B );					}; /* RES  0,B=(XY+o)  */
/*TODO*///opcode xycb_81= () -> { _C = RES(0, RM(EA) ); WM( EA,_C );					}; /* RES  0,C=(XY+o)  */
/*TODO*///opcode xycb_82= () -> { _D = RES(0, RM(EA) ); WM( EA,_D );					}; /* RES  0,D=(XY+o)  */
/*TODO*///opcode xycb_83= () -> { _E = RES(0, RM(EA) ); WM( EA,_E );					}; /* RES  0,E=(XY+o)  */
/*TODO*///opcode xycb_84= () -> { _H = RES(0, RM(EA) ); WM( EA,_H );					}; /* RES  0,H=(XY+o)  */
/*TODO*///opcode xycb_85= () -> { _L = RES(0, RM(EA) ); WM( EA,_L );					}; /* RES  0,L=(XY+o)  */
    opcode xycb_86= () -> { WM( EA, RES(0,RM(EA)) );								}; /* RES  0,(XY+o)	  */
/*TODO*///opcode xycb_87= () -> { _A = RES(0, RM(EA) ); WM( EA,_A );					}; /* RES  0,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_88= () -> { _B = RES(1, RM(EA) ); WM( EA,_B );					}; /* RES  1,B=(XY+o)  */
/*TODO*///opcode xycb_89= () -> { _C = RES(1, RM(EA) ); WM( EA,_C );					}; /* RES  1,C=(XY+o)  */
/*TODO*///opcode xycb_8a= () -> { _D = RES(1, RM(EA) ); WM( EA,_D );					}; /* RES  1,D=(XY+o)  */
/*TODO*///opcode xycb_8b= () -> { _E = RES(1, RM(EA) ); WM( EA,_E );					}; /* RES  1,E=(XY+o)  */
/*TODO*///opcode xycb_8c= () -> { _H = RES(1, RM(EA) ); WM( EA,_H );					}; /* RES  1,H=(XY+o)  */
/*TODO*///opcode xycb_8d= () -> { _L = RES(1, RM(EA) ); WM( EA,_L );					}; /* RES  1,L=(XY+o)  */
    opcode xycb_8e= () -> { WM( EA, RES(1,RM(EA)) );								}; /* RES  1,(XY+o)	  */
/*TODO*///opcode xycb_8f= () -> { _A = RES(1, RM(EA) ); WM( EA,_A );					}; /* RES  1,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_90= () -> { _B = RES(2, RM(EA) ); WM( EA,_B );					}; /* RES  2,B=(XY+o)  */
/*TODO*///opcode xycb_91= () -> { _C = RES(2, RM(EA) ); WM( EA,_C );					}; /* RES  2,C=(XY+o)  */
/*TODO*///opcode xycb_92= () -> { _D = RES(2, RM(EA) ); WM( EA,_D );					}; /* RES  2,D=(XY+o)  */
/*TODO*///opcode xycb_93= () -> { _E = RES(2, RM(EA) ); WM( EA,_E );					}; /* RES  2,E=(XY+o)  */
/*TODO*///opcode xycb_94= () -> { _H = RES(2, RM(EA) ); WM( EA,_H );					}; /* RES  2,H=(XY+o)  */
/*TODO*///opcode xycb_95= () -> { _L = RES(2, RM(EA) ); WM( EA,_L );					}; /* RES  2,L=(XY+o)  */
    opcode xycb_96= () -> { WM( EA, RES(2,RM(EA)) );								}; /* RES  2,(XY+o)	  */
/*TODO*///opcode xycb_97= () -> { _A = RES(2, RM(EA) ); WM( EA,_A );					}; /* RES  2,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_98= () -> { _B = RES(3, RM(EA) ); WM( EA,_B );					}; /* RES  3,B=(XY+o)  */
/*TODO*///opcode xycb_99= () -> { _C = RES(3, RM(EA) ); WM( EA,_C );					}; /* RES  3,C=(XY+o)  */
/*TODO*///opcode xycb_9a= () -> { _D = RES(3, RM(EA) ); WM( EA,_D );					}; /* RES  3,D=(XY+o)  */
/*TODO*///opcode xycb_9b= () -> { _E = RES(3, RM(EA) ); WM( EA,_E );					}; /* RES  3,E=(XY+o)  */
/*TODO*///opcode xycb_9c= () -> { _H = RES(3, RM(EA) ); WM( EA,_H );					}; /* RES  3,H=(XY+o)  */
/*TODO*///opcode xycb_9d= () -> { _L = RES(3, RM(EA) ); WM( EA,_L );					}; /* RES  3,L=(XY+o)  */
    opcode xycb_9e= () -> { WM( EA, RES(3,RM(EA)) );								}; /* RES  3,(XY+o)	  */
/*TODO*///opcode xycb_9f= () -> { _A = RES(3, RM(EA) ); WM( EA,_A );					}; /* RES  3,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_a0= () -> { _B = RES(4, RM(EA) ); WM( EA,_B );					}; /* RES  4,B=(XY+o)  */
/*TODO*///opcode xycb_a1= () -> { _C = RES(4, RM(EA) ); WM( EA,_C );					}; /* RES  4,C=(XY+o)  */
/*TODO*///opcode xycb_a2= () -> { _D = RES(4, RM(EA) ); WM( EA,_D );					}; /* RES  4,D=(XY+o)  */
/*TODO*///opcode xycb_a3= () -> { _E = RES(4, RM(EA) ); WM( EA,_E );					}; /* RES  4,E=(XY+o)  */
/*TODO*///opcode xycb_a4= () -> { _H = RES(4, RM(EA) ); WM( EA,_H );					}; /* RES  4,H=(XY+o)  */
/*TODO*///opcode xycb_a5= () -> { _L = RES(4, RM(EA) ); WM( EA,_L );					}; /* RES  4,L=(XY+o)  */
    opcode xycb_a6= () -> { WM( EA, RES(4,RM(EA)) );								}; /* RES  4,(XY+o)	  */
/*TODO*///opcode xycb_a7= () -> { _A = RES(4, RM(EA) ); WM( EA,_A );					}; /* RES  4,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_a8= () -> { _B = RES(5, RM(EA) ); WM( EA,_B );					}; /* RES  5,B=(XY+o)  */
/*TODO*///opcode xycb_a9= () -> { _C = RES(5, RM(EA) ); WM( EA,_C );					}; /* RES  5,C=(XY+o)  */
/*TODO*///opcode xycb_aa= () -> { _D = RES(5, RM(EA) ); WM( EA,_D );					}; /* RES  5,D=(XY+o)  */
/*TODO*///opcode xycb_ab= () -> { _E = RES(5, RM(EA) ); WM( EA,_E );					}; /* RES  5,E=(XY+o)  */
/*TODO*///opcode xycb_ac= () -> { _H = RES(5, RM(EA) ); WM( EA,_H );					}; /* RES  5,H=(XY+o)  */
/*TODO*///opcode xycb_ad= () -> { _L = RES(5, RM(EA) ); WM( EA,_L );					}; /* RES  5,L=(XY+o)  */
    opcode xycb_ae= () -> { WM( EA, RES(5,RM(EA)) );								}; /* RES  5,(XY+o)	  */
/*TODO*///opcode xycb_af= () -> { _A = RES(5, RM(EA) ); WM( EA,_A );					}; /* RES  5,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_b0= () -> { _B = RES(6, RM(EA) ); WM( EA,_B );					}; /* RES  6,B=(XY+o)  */
/*TODO*///opcode xycb_b1= () -> { _C = RES(6, RM(EA) ); WM( EA,_C );					}; /* RES  6,C=(XY+o)  */
/*TODO*///opcode xycb_b2= () -> { _D = RES(6, RM(EA) ); WM( EA,_D );					}; /* RES  6,D=(XY+o)  */
/*TODO*///opcode xycb_b3= () -> { _E = RES(6, RM(EA) ); WM( EA,_E );					}; /* RES  6,E=(XY+o)  */
/*TODO*///opcode xycb_b4= () -> { _H = RES(6, RM(EA) ); WM( EA,_H );					}; /* RES  6,H=(XY+o)  */
/*TODO*///opcode xycb_b5= () -> { _L = RES(6, RM(EA) ); WM( EA,_L );					}; /* RES  6,L=(XY+o)  */
    opcode xycb_b6= () -> { WM( EA, RES(6,RM(EA)) );								}; /* RES  6,(XY+o)	  */
/*TODO*///opcode xycb_b7= () -> { _A = RES(6, RM(EA) ); WM( EA,_A );					}; /* RES  6,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_b8= () -> { _B = RES(7, RM(EA) ); WM( EA,_B );					}; /* RES  7,B=(XY+o)  */
/*TODO*///opcode xycb_b9= () -> { _C = RES(7, RM(EA) ); WM( EA,_C );					}; /* RES  7,C=(XY+o)  */
/*TODO*///opcode xycb_ba= () -> { _D = RES(7, RM(EA) ); WM( EA,_D );					}; /* RES  7,D=(XY+o)  */
/*TODO*///opcode xycb_bb= () -> { _E = RES(7, RM(EA) ); WM( EA,_E );					}; /* RES  7,E=(XY+o)  */
/*TODO*///opcode xycb_bc= () -> { _H = RES(7, RM(EA) ); WM( EA,_H );					}; /* RES  7,H=(XY+o)  */
/*TODO*///opcode xycb_bd= () -> { _L = RES(7, RM(EA) ); WM( EA,_L );					}; /* RES  7,L=(XY+o)  */
    opcode xycb_be= () -> { WM( EA, RES(7,RM(EA)) );								}; /* RES  7,(XY+o)	  */
/*TODO*///opcode xycb_bf= () -> { _A = RES(7, RM(EA) ); WM( EA,_A );					}; /* RES  7,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_c0= () -> { _B = SET(0, RM(EA) ); WM( EA,_B );					}; /* SET  0,B=(XY+o)  */
/*TODO*///opcode xycb_c1= () -> { _C = SET(0, RM(EA) ); WM( EA,_C );					}; /* SET  0,C=(XY+o)  */
/*TODO*///opcode xycb_c2= () -> { _D = SET(0, RM(EA) ); WM( EA,_D );					}; /* SET  0,D=(XY+o)  */
/*TODO*///opcode xycb_c3= () -> { _E = SET(0, RM(EA) ); WM( EA,_E );					}; /* SET  0,E=(XY+o)  */
/*TODO*///opcode xycb_c4= () -> { _H = SET(0, RM(EA) ); WM( EA,_H );					}; /* SET  0,H=(XY+o)  */
/*TODO*///opcode xycb_c5= () -> { _L = SET(0, RM(EA) ); WM( EA,_L );					}; /* SET  0,L=(XY+o)  */
    opcode xycb_c6= () -> { WM( EA, SET(0,RM(EA)) );								}; /* SET  0,(XY+o)	  */
/*TODO*///opcode xycb_c7= () -> { _A = SET(0, RM(EA) ); WM( EA,_A );					}; /* SET  0,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_c8= () -> { _B = SET(1, RM(EA) ); WM( EA,_B );					}; /* SET  1,B=(XY+o)  */
/*TODO*///opcode xycb_c9= () -> { _C = SET(1, RM(EA) ); WM( EA,_C );					}; /* SET  1,C=(XY+o)  */
/*TODO*///opcode xycb_ca= () -> { _D = SET(1, RM(EA) ); WM( EA,_D );					}; /* SET  1,D=(XY+o)  */
/*TODO*///opcode xycb_cb= () -> { _E = SET(1, RM(EA) ); WM( EA,_E );					}; /* SET  1,E=(XY+o)  */
/*TODO*///opcode xycb_cc= () -> { _H = SET(1, RM(EA) ); WM( EA,_H );					}; /* SET  1,H=(XY+o)  */
/*TODO*///opcode xycb_cd= () -> { _L = SET(1, RM(EA) ); WM( EA,_L );					}; /* SET  1,L=(XY+o)  */
    opcode xycb_ce= () -> { WM( EA, SET(1,RM(EA)) );								}; /* SET  1,(XY+o)	  */
/*TODO*///opcode xycb_cf= () -> { _A = SET(1, RM(EA) ); WM( EA,_A );					}; /* SET  1,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_d0= () -> { _B = SET(2, RM(EA) ); WM( EA,_B );					}; /* SET  2,B=(XY+o)  */
/*TODO*///opcode xycb_d1= () -> { _C = SET(2, RM(EA) ); WM( EA,_C );					}; /* SET  2,C=(XY+o)  */
/*TODO*///opcode xycb_d2= () -> { _D = SET(2, RM(EA) ); WM( EA,_D );					}; /* SET  2,D=(XY+o)  */
/*TODO*///opcode xycb_d3= () -> { _E = SET(2, RM(EA) ); WM( EA,_E );					}; /* SET  2,E=(XY+o)  */
/*TODO*///opcode xycb_d4= () -> { _H = SET(2, RM(EA) ); WM( EA,_H );					}; /* SET  2,H=(XY+o)  */
/*TODO*///opcode xycb_d5= () -> { _L = SET(2, RM(EA) ); WM( EA,_L );					}; /* SET  2,L=(XY+o)  */
    opcode xycb_d6= () -> { WM( EA, SET(2,RM(EA)) );								}; /* SET  2,(XY+o)	  */
/*TODO*///opcode xycb_d7= () -> { _A = SET(2, RM(EA) ); WM( EA,_A );					}; /* SET  2,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_d8= () -> { _B = SET(3, RM(EA) ); WM( EA,_B );					}; /* SET  3,B=(XY+o)  */
/*TODO*///opcode xycb_d9= () -> { _C = SET(3, RM(EA) ); WM( EA,_C );					}; /* SET  3,C=(XY+o)  */
/*TODO*///opcode xycb_da= () -> { _D = SET(3, RM(EA) ); WM( EA,_D );					}; /* SET  3,D=(XY+o)  */
/*TODO*///opcode xycb_db= () -> { _E = SET(3, RM(EA) ); WM( EA,_E );					}; /* SET  3,E=(XY+o)  */
/*TODO*///opcode xycb_dc= () -> { _H = SET(3, RM(EA) ); WM( EA,_H );					}; /* SET  3,H=(XY+o)  */
/*TODO*///opcode xycb_dd= () -> { _L = SET(3, RM(EA) ); WM( EA,_L );					}; /* SET  3,L=(XY+o)  */
    opcode xycb_de= () -> { WM( EA, SET(3,RM(EA)) );								}; /* SET  3,(XY+o)	  */
/*TODO*///opcode xycb_df= () -> { _A = SET(3, RM(EA) ); WM( EA,_A );					}; /* SET  3,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_e0= () -> { _B = SET(4, RM(EA) ); WM( EA,_B );					}; /* SET  4,B=(XY+o)  */
/*TODO*///opcode xycb_e1= () -> { _C = SET(4, RM(EA) ); WM( EA,_C );					}; /* SET  4,C=(XY+o)  */
/*TODO*///opcode xycb_e2= () -> { _D = SET(4, RM(EA) ); WM( EA,_D );					}; /* SET  4,D=(XY+o)  */
/*TODO*///opcode xycb_e3= () -> { _E = SET(4, RM(EA) ); WM( EA,_E );					}; /* SET  4,E=(XY+o)  */
/*TODO*///opcode xycb_e4= () -> { _H = SET(4, RM(EA) ); WM( EA,_H );					}; /* SET  4,H=(XY+o)  */
/*TODO*///opcode xycb_e5= () -> { _L = SET(4, RM(EA) ); WM( EA,_L );					}; /* SET  4,L=(XY+o)  */
    opcode xycb_e6= () -> { WM( EA, SET(4,RM(EA)) );								}; /* SET  4,(XY+o)	  */
/*TODO*///opcode xycb_e7= () -> { _A = SET(4, RM(EA) ); WM( EA,_A );					}; /* SET  4,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_e8= () -> { _B = SET(5, RM(EA) ); WM( EA,_B );					}; /* SET  5,B=(XY+o)  */
/*TODO*///opcode xycb_e9= () -> { _C = SET(5, RM(EA) ); WM( EA,_C );					}; /* SET  5,C=(XY+o)  */
/*TODO*///opcode xycb_ea= () -> { _D = SET(5, RM(EA) ); WM( EA,_D );					}; /* SET  5,D=(XY+o)  */
/*TODO*///opcode xycb_eb= () -> { _E = SET(5, RM(EA) ); WM( EA,_E );					}; /* SET  5,E=(XY+o)  */
/*TODO*///opcode xycb_ec= () -> { _H = SET(5, RM(EA) ); WM( EA,_H );					}; /* SET  5,H=(XY+o)  */
/*TODO*///opcode xycb_ed= () -> { _L = SET(5, RM(EA) ); WM( EA,_L );					}; /* SET  5,L=(XY+o)  */
    opcode xycb_ee= () -> { WM( EA, SET(5,RM(EA)) );								}; /* SET  5,(XY+o)	  */
/*TODO*///opcode xycb_ef= () -> { _A = SET(5, RM(EA) ); WM( EA,_A );					}; /* SET  5,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_f0= () -> { _B = SET(6, RM(EA) ); WM( EA,_B );					}; /* SET  6,B=(XY+o)  */
/*TODO*///opcode xycb_f1= () -> { _C = SET(6, RM(EA) ); WM( EA,_C );					}; /* SET  6,C=(XY+o)  */
/*TODO*///opcode xycb_f2= () -> { _D = SET(6, RM(EA) ); WM( EA,_D );					}; /* SET  6,D=(XY+o)  */
/*TODO*///opcode xycb_f3= () -> { _E = SET(6, RM(EA) ); WM( EA,_E );					}; /* SET  6,E=(XY+o)  */
/*TODO*///opcode xycb_f4= () -> { _H = SET(6, RM(EA) ); WM( EA,_H );					}; /* SET  6,H=(XY+o)  */
/*TODO*///opcode xycb_f5= () -> { _L = SET(6, RM(EA) ); WM( EA,_L );					}; /* SET  6,L=(XY+o)  */
    opcode xycb_f6= () -> { WM( EA, SET(6,RM(EA)) );								}; /* SET  6,(XY+o)	  */
/*TODO*///opcode xycb_f7= () -> { _A = SET(6, RM(EA) ); WM( EA,_A );					}; /* SET  6,A=(XY+o)  */
/*TODO*///
/*TODO*///opcode xycb_f8= () -> { _B = SET(7, RM(EA) ); WM( EA,_B );					}; /* SET  7,B=(XY+o)  */
/*TODO*///opcode xycb_f9= () -> { _C = SET(7, RM(EA) ); WM( EA,_C );					}; /* SET  7,C=(XY+o)  */
/*TODO*///opcode xycb_fa= () -> { _D = SET(7, RM(EA) ); WM( EA,_D );					}; /* SET  7,D=(XY+o)  */
/*TODO*///opcode xycb_fb= () -> { _E = SET(7, RM(EA) ); WM( EA,_E );					}; /* SET  7,E=(XY+o)  */
/*TODO*///opcode xycb_fc= () -> { _H = SET(7, RM(EA) ); WM( EA,_H );					}; /* SET  7,H=(XY+o)  */
/*TODO*///opcode xycb_fd= () -> { _L = SET(7, RM(EA) ); WM( EA,_L );					}; /* SET  7,L=(XY+o)  */
    opcode xycb_fe= () -> { WM( EA, SET(7,RM(EA)) );								}; /* SET  7,(XY+o)	  */
/*TODO*///opcode xycb_ff= () -> { _A = SET(7, RM(EA) ); WM( EA,_A );					}; /* SET  7,A=(XY+o)  */
/*TODO*///
/*TODO*///OP(illegal,1) {
/*TODO*///	logerror("Z80 #%d ill. opcode $%02x $%02x\n",
/*TODO*///			cpu_getactivecpu(), cpu_readop((_PCD-1)&0xffff), cpu_readop(_PCD));
/*TODO*///}
    opcode illegal_1 = new opcode() {
        public void handler() {
            throw new UnsupportedOperationException("unimplemented");
        }
    };

    /**********************************************************
     * IX register related opcodes (DD prefix)
     **********************************************************/
    opcode dd_00= new opcode() { public void handler(){ illegal_1.handler(); op_00.handler();									}}; /* DB   DD		  */
    opcode dd_01= new opcode() { public void handler(){ illegal_1.handler(); op_01.handler();									}}; /* DB   DD		  */
    opcode dd_02= new opcode() { public void handler(){ illegal_1.handler(); op_02.handler();									}}; /* DB   DD		  */
    opcode dd_03= new opcode() { public void handler(){ illegal_1.handler(); op_03.handler();									}}; /* DB   DD		  */
    opcode dd_04= new opcode() { public void handler(){ illegal_1.handler(); op_04.handler();									}}; /* DB   DD		  */
    opcode dd_05= new opcode() { public void handler(){ illegal_1.handler(); op_05.handler();									}}; /* DB   DD		  */
    opcode dd_06= new opcode() { public void handler(){ illegal_1.handler(); op_06.handler();									}}; /* DB   DD		  */
    opcode dd_07= new opcode() { public void handler(){ illegal_1.handler(); op_07.handler();									}}; /* DB   DD		  */

    opcode dd_08= new opcode() { public void handler(){ illegal_1.handler(); op_08.handler();									}}; /* DB   DD		  */
    opcode dd_09= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IX=ADD16(Z80.IX,BC()); 									}; /* ADD  IX,BC 	  */
    opcode dd_0a= new opcode() { public void handler(){ illegal_1.handler(); op_0a.handler();									}}; /* DB   DD		  */
    opcode dd_0b= new opcode() { public void handler(){ illegal_1.handler(); op_0b.handler();									}}; /* DB   DD		  */
    opcode dd_0c= new opcode() { public void handler(){ illegal_1.handler(); op_0c.handler();									}}; /* DB   DD		  */
    opcode dd_0d= new opcode() { public void handler(){ illegal_1.handler(); op_0d.handler();									}}; /* DB   DD		  */
    opcode dd_0e= new opcode() { public void handler(){ illegal_1.handler(); op_0e.handler();									}}; /* DB   DD		  */
    opcode dd_0f= new opcode() { public void handler(){ illegal_1.handler(); op_0f.handler();									}}; /* DB   DD		  */

    opcode dd_10= new opcode() { public void handler(){ illegal_1.handler(); op_10.handler();									}}; /* DB   DD		  */
    opcode dd_11= new opcode() { public void handler(){ illegal_1.handler(); op_11.handler();									}}; /* DB   DD		  */
    opcode dd_12= new opcode() { public void handler(){ illegal_1.handler(); op_12.handler();									}}; /* DB   DD		  */
    opcode dd_13= new opcode() { public void handler(){ illegal_1.handler(); op_13.handler();									}}; /* DB   DD		  */
    opcode dd_14= new opcode() { public void handler(){ illegal_1.handler(); op_14.handler();									}}; /* DB   DD		  */
    opcode dd_15= new opcode() { public void handler(){ illegal_1.handler(); op_15.handler();									}}; /* DB   DD		  */
    opcode dd_16= new opcode() { public void handler(){ illegal_1.handler(); op_16.handler();									}}; /* DB   DD		  */
    opcode dd_17= new opcode() { public void handler(){ illegal_1.handler(); op_17.handler();									}}; /* DB   DD		  */

    opcode dd_18= new opcode() { public void handler(){ illegal_1.handler(); op_18.handler();									}}; /* DB   DD		  */
    opcode dd_19= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IX=ADD16(Z80.IX,DE()); 									}; /* ADD  IX,DE 	  */
    opcode dd_1a= new opcode() { public void handler(){ illegal_1.handler(); op_1a.handler();}}; /* DB   DD		  */
    opcode dd_1b= new opcode() { public void handler(){ illegal_1.handler(); op_1b.handler();}}; /* DB   DD		  */
    opcode dd_1c= new opcode() { public void handler(){ illegal_1.handler(); op_1c.handler();}}; /* DB   DD		  */
    opcode dd_1d= new opcode() { public void handler(){ illegal_1.handler(); op_1d.handler();}}; /* DB   DD		  */
    opcode dd_1e= new opcode() { public void handler(){ illegal_1.handler(); op_1e.handler();}}; /* DB   DD		  */
    opcode dd_1f= new opcode() { public void handler(){ illegal_1.handler(); op_1f.handler();}}; /* DB   DD		  */

    opcode dd_20= new opcode() { public void handler(){ illegal_1.handler(); op_20.handler();}}; /* DB   DD		  */
    opcode dd_21= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IX = ARG16();									}; /* LD   IX,w		  */
    opcode dd_22= () -> { Z80.R = (Z80.R+1)&0xFF; EA = ARG16(); WM16( EA, Z80.IX );				}; /* LD   (w),IX	  */
    opcode dd_23= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IX = (Z80.IX+1)& 0xFFFF;											}; /* INC  IX		  */
/*TODO*///opcode dd_24= () -> { Z80.R = (Z80.R+1)&0xFF; _HX = INC(_HX);									}; /* INC  HX		  */
/*TODO*///opcode dd_25= () -> { Z80.R = (Z80.R+1)&0xFF; _HX = DEC(_HX);									}; /* DEC  HX		  */
/*TODO*///opcode dd_26= () -> { Z80.R = (Z80.R+1)&0xFF; _HX = ARG();										}; /* LD   HX,n		  */
    opcode dd_27= new opcode() { public void handler(){ illegal_1.handler(); op_27.handler();									}}; /* DB   DD		  */
   
    opcode dd_28= new opcode() { public void handler(){ illegal_1.handler(); op_28.handler();									}}; /* DB   DD		  */
    opcode dd_29= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IX=ADD16(Z80.IX,Z80.IX); 									}; /* ADD  IX,IX 	  */
    opcode dd_2a= () -> { Z80.R = (Z80.R+1)&0xFF; EA = ARG16(); Z80.IX=RM16( EA);				}; /* LD   IX,(w)	  */
    opcode dd_2b= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IX = (Z80.IX-1)& 0xFFFF;											}; /* DEC  IX		  */
/*TODO*///opcode dd_2c= () -> { Z80.R = (Z80.R+1)&0xFF; _LX = INC(_LX);									}; /* INC  LX		  */
/*TODO*///opcode dd_2d= () -> { Z80.R = (Z80.R+1)&0xFF; _LX = DEC(_LX);									}; /* DEC  LX		  */
/*TODO*///opcode dd_2e= () -> { Z80.R = (Z80.R+1)&0xFF; _LX = ARG();										}; /* LD   LX,n		  */
    opcode dd_2f= new opcode() { public void handler(){ illegal_1.handler(); op_2f.handler();									}}; /* DB   DD		  */

    opcode dd_30= new opcode() { public void handler(){ illegal_1.handler(); op_30.handler();									}}; /* DB   DD		  */
    opcode dd_31= new opcode() { public void handler(){ illegal_1.handler(); op_31.handler();									}}; /* DB   DD		  */
    opcode dd_32= new opcode() { public void handler(){ illegal_1.handler(); op_32.handler();									}}; /* DB   DD		  */
    opcode dd_33= new opcode() { public void handler(){ illegal_1.handler(); op_33.handler();									}}; /* DB   DD		  */
    opcode dd_34= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, INC(RM(EA)) );						}; /* INC  (IX+o)	  */
    opcode dd_35= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, DEC(RM(EA)) );						}; /* DEC  (IX+o)	  */
    opcode dd_36= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, ARG() ); 							}; /* LD   (IX+o),n	  */
    opcode dd_37= new opcode() { public void handler(){ illegal_1.handler(); op_37.handler();									}}; /* DB   DD		  */
    
    opcode dd_38= new opcode() { public void handler(){ illegal_1.handler(); op_38.handler();									}}; /* DB   DD		  */
   /*TODO*///opcode dd_39= () -> { Z80.R = (Z80.R+1)&0xFF; ADD16(IX,SP); 									}; /* ADD  IX,SP 	  */
/*TODO*///opcode dd_3a= () -> { illegal_1(); op_3a();									}; /* DB   DD		  */
/*TODO*///opcode dd_3b= () -> { illegal_1(); op_3b();									}; /* DB   DD		  */
/*TODO*///opcode dd_3c= () -> { illegal_1(); op_3c();									}; /* DB   DD		  */
/*TODO*///opcode dd_3d= () -> { illegal_1(); op_3d();									}; /* DB   DD		  */
/*TODO*///opcode dd_3e= () -> { illegal_1(); op_3e();									}; /* DB   DD		  */
/*TODO*///opcode dd_3f= () -> { illegal_1(); op_3f();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_40= () -> { illegal_1(); op_40();									}; /* DB   DD		  */
/*TODO*///opcode dd_41= () -> { illegal_1(); op_41();									}; /* DB   DD		  */
/*TODO*///opcode dd_42= () -> { illegal_1(); op_42();									}; /* DB   DD		  */
/*TODO*///opcode dd_43= () -> { illegal_1(); op_43();									}; /* DB   DD		  */
/*TODO*///opcode dd_44= () -> { Z80.R = (Z80.R+1)&0xFF; _B = _HX; 										}; /* LD   B,HX		  */
/*TODO*///opcode dd_45= () -> { Z80.R = (Z80.R+1)&0xFF; _B = _LX; 										}; /* LD   B,LX		  */
    opcode dd_46= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); Z80.B = RM(EA); 								}; /* LD   B,(IX+o)	  */
/*TODO*///opcode dd_47= () -> { illegal_1(); op_47();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_48= () -> { illegal_1(); op_48();									}; /* DB   DD		  */
/*TODO*///opcode dd_49= () -> { illegal_1(); op_49();									}; /* DB   DD		  */
/*TODO*///opcode dd_4a= () -> { illegal_1(); op_4a();									}; /* DB   DD		  */
/*TODO*///opcode dd_4b= () -> { illegal_1(); op_4b();									}; /* DB   DD		  */
/*TODO*///opcode dd_4c= () -> { Z80.R = (Z80.R+1)&0xFF; _C = _HX; 										}; /* LD   C,HX		  */
/*TODO*///opcode dd_4d= () -> { Z80.R = (Z80.R+1)&0xFF; _C = _LX; 										}; /* LD   C,LX		  */
    opcode dd_4e= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); Z80.C = RM(EA); 								}; /* LD   C,(IX+o)	  */
/*TODO*///opcode dd_4f= () -> { illegal_1(); op_4f();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_50= () -> { illegal_1(); op_50();									}; /* DB   DD		  */
/*TODO*///opcode dd_51= () -> { illegal_1(); op_51();									}; /* DB   DD		  */
/*TODO*///opcode dd_52= () -> { illegal_1(); op_52();									}; /* DB   DD		  */
/*TODO*///opcode dd_53= () -> { illegal_1(); op_53();									}; /* DB   DD		  */
/*TODO*///opcode dd_54= () -> { Z80.R = (Z80.R+1)&0xFF; _D = _HX; 										}; /* LD   D,HX		  */
/*TODO*///opcode dd_55= () -> { Z80.R = (Z80.R+1)&0xFF; _D = _LX; 										}; /* LD   D,LX		  */
    opcode dd_56= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); Z80.D = RM(EA); 								}; /* LD   D,(IX+o)	  */
/*TODO*///opcode dd_57= () -> { illegal_1(); op_57();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_58= () -> { illegal_1(); op_58();									}; /* DB   DD		  */
/*TODO*///opcode dd_59= () -> { illegal_1(); op_59();									}; /* DB   DD		  */
/*TODO*///opcode dd_5a= () -> { illegal_1(); op_5a();									}; /* DB   DD		  */
/*TODO*///opcode dd_5b= () -> { illegal_1(); op_5b();									}; /* DB   DD		  */
/*TODO*///opcode dd_5c= () -> { Z80.R = (Z80.R+1)&0xFF; _E = _HX; 										}; /* LD   E,HX		  */
/*TODO*///opcode dd_5d= () -> { Z80.R = (Z80.R+1)&0xFF; _E = _LX; 										}; /* LD   E,LX		  */
    opcode dd_5e= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); Z80.E = RM(EA); 								}; /* LD   E,(IX+o)	  */
/*TODO*///opcode dd_5f= () -> { illegal_1(); op_5f();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_60= () -> { Z80.R = (Z80.R+1)&0xFF; _HX = _B; 										}; /* LD   HX,B		  */
/*TODO*///opcode dd_61= () -> { Z80.R = (Z80.R+1)&0xFF; _HX = _C; 										}; /* LD   HX,C		  */
/*TODO*///opcode dd_62= () -> { Z80.R = (Z80.R+1)&0xFF; _HX = _D; 										}; /* LD   HX,D		  */
/*TODO*///opcode dd_63= () -> { Z80.R = (Z80.R+1)&0xFF; _HX = _E; 										}; /* LD   HX,E		  */
/*TODO*///opcode dd_64= () -> { 														}; /* LD   HX,HX 	  */
/*TODO*///opcode dd_65= () -> { Z80.R = (Z80.R+1)&0xFF; _HX = _LX;										}; /* LD   HX,LX 	  */
    opcode dd_66= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); Z80.H = RM(EA); 								}; /* LD   H,(IX+o)	  */
/*TODO*///opcode dd_67= () -> { Z80.R = (Z80.R+1)&0xFF; _HX = _A; 										}; /* LD   HX,A		  */
/*TODO*///
/*TODO*///opcode dd_68= () -> { Z80.R = (Z80.R+1)&0xFF; _LX = _B; 										}; /* LD   LX,B		  */
/*TODO*///opcode dd_69= () -> { Z80.R = (Z80.R+1)&0xFF; _LX = _C; 										}; /* LD   LX,C		  */
/*TODO*///opcode dd_6a= () -> { Z80.R = (Z80.R+1)&0xFF; _LX = _D; 										}; /* LD   LX,D		  */
/*TODO*///opcode dd_6b= () -> { Z80.R = (Z80.R+1)&0xFF; _LX = _E; 										}; /* LD   LX,E		  */
/*TODO*///opcode dd_6c= () -> { Z80.R = (Z80.R+1)&0xFF; _LX = _HX;										}; /* LD   LX,HX 	  */
/*TODO*///opcode dd_6d= () -> { 														}; /* LD   LX,LX 	  */
    opcode dd_6e= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); Z80.L = RM(EA); 								}; /* LD   L,(IX+o)	  */
/*TODO*///opcode dd_6f= () -> { Z80.R = (Z80.R+1)&0xFF; _LX = _A; 										}; /* LD   LX,A		  */
/*TODO*///
    opcode dd_70= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, Z80.B );								}; /* LD   (IX+o),B	  */
    opcode dd_71= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, Z80.C );								}; /* LD   (IX+o),C	  */
    opcode dd_72= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, Z80.D );								}; /* LD   (IX+o),D	  */
    opcode dd_73= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, Z80.E );								}; /* LD   (IX+o),E	  */
    opcode dd_74= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, Z80.H );								}; /* LD   (IX+o),H	  */
    opcode dd_75= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, Z80.L );								}; /* LD   (IX+o),L	  */
/*TODO*///opcode dd_76= () -> { illegal_1(); op_76();									};		  /* DB   DD		  */
    opcode dd_77= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); WM( EA, Z80.A );								}; /* LD   (IX+o),A	  */
/*TODO*///
/*TODO*///opcode dd_78= () -> { illegal_1(); op_78();									}; /* DB   DD		  */
/*TODO*///opcode dd_79= () -> { illegal_1(); op_79();									}; /* DB   DD		  */
/*TODO*///opcode dd_7a= () -> { illegal_1(); op_7a();									}; /* DB   DD		  */
/*TODO*///opcode dd_7b= () -> { illegal_1(); op_7b();									}; /* DB   DD		  */
/*TODO*///opcode dd_7c= () -> { Z80.R = (Z80.R+1)&0xFF; _A = _HX; 										}; /* LD   A,HX		  */
/*TODO*///opcode dd_7d= () -> { Z80.R = (Z80.R+1)&0xFF; _A = _LX; 										}; /* LD   A,LX		  */
    opcode dd_7e= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); Z80.A = RM(EA); 								}; /* LD   A,(IX+o)	  */
/*TODO*///opcode dd_7f= () -> { illegal_1(); op_7f();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_80= () -> { illegal_1(); op_80();									}; /* DB   DD		  */
/*TODO*///opcode dd_81= () -> { illegal_1(); op_81();									}; /* DB   DD		  */
/*TODO*///opcode dd_82= () -> { illegal_1(); op_82();									}; /* DB   DD		  */
/*TODO*///opcode dd_83= () -> { illegal_1(); op_83();									}; /* DB   DD		  */
/*TODO*///opcode dd_84= () -> { Z80.R = (Z80.R+1)&0xFF; ADD(_HX); 										}; /* ADD  A,HX		  */
/*TODO*///opcode dd_85= () -> { Z80.R = (Z80.R+1)&0xFF; ADD(_LX); 										}; /* ADD  A,LX		  */
    opcode dd_86= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); ADD(RM(EA)); 								}; /* ADD  A,(IX+o)	  */
/*TODO*///opcode dd_87= () -> { illegal_1(); op_87();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_88= () -> { illegal_1(); op_88();									}; /* DB   DD		  */
/*TODO*///opcode dd_89= () -> { illegal_1(); op_89();									}; /* DB   DD		  */
/*TODO*///opcode dd_8a= () -> { illegal_1(); op_8a();									}; /* DB   DD		  */
/*TODO*///opcode dd_8b= () -> { illegal_1(); op_8b();									}; /* DB   DD		  */
/*TODO*///opcode dd_8c= () -> { Z80.R = (Z80.R+1)&0xFF; ADC(_HX); 										}; /* ADC  A,HX		  */
/*TODO*///opcode dd_8d= () -> { Z80.R = (Z80.R+1)&0xFF; ADC(_LX); 										}; /* ADC  A,LX		  */
    opcode dd_8e= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); ADC(RM(EA)); 								}; /* ADC  A,(IX+o)	  */
/*TODO*///opcode dd_8f= () -> { illegal_1(); op_8f();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_90= () -> { illegal_1(); op_90();									}; /* DB   DD		  */
/*TODO*///opcode dd_91= () -> { illegal_1(); op_91();									}; /* DB   DD		  */
/*TODO*///opcode dd_92= () -> { illegal_1(); op_92();									}; /* DB   DD		  */
/*TODO*///opcode dd_93= () -> { illegal_1(); op_93();									}; /* DB   DD		  */
/*TODO*///opcode dd_94= () -> { Z80.R = (Z80.R+1)&0xFF; SUB(_HX); 										}; /* SUB  HX		  */
/*TODO*///opcode dd_95= () -> { Z80.R = (Z80.R+1)&0xFF; SUB(_LX); 										}; /* SUB  LX		  */
    opcode dd_96= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); SUB(RM(EA)); 								}; /* SUB  (IX+o)	  */
/*TODO*///opcode dd_97= () -> { illegal_1(); op_97();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_98= () -> { illegal_1(); op_98();									}; /* DB   DD		  */
/*TODO*///opcode dd_99= () -> { illegal_1(); op_99();									}; /* DB   DD		  */
/*TODO*///opcode dd_9a= () -> { illegal_1(); op_9a();									}; /* DB   DD		  */
/*TODO*///opcode dd_9b= () -> { illegal_1(); op_9b();									}; /* DB   DD		  */
/*TODO*///opcode dd_9c= () -> { Z80.R = (Z80.R+1)&0xFF; SBC(_HX); 										}; /* SBC  A,HX		  */
/*TODO*///opcode dd_9d= () -> { Z80.R = (Z80.R+1)&0xFF; SBC(_LX); 										}; /* SBC  A,LX		  */
/*TODO*///opcode dd_9e= () -> { Z80.R = (Z80.R+1)&0xFF; EAX; SBC(RM(EA)); 								}; /* SBC  A,(IX+o)	  */
/*TODO*///opcode dd_9f= () -> { illegal_1(); op_9f();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_a0= () -> { illegal_1(); op_a0();									}; /* DB   DD		  */
/*TODO*///opcode dd_a1= () -> { illegal_1(); op_a1();									}; /* DB   DD		  */
/*TODO*///opcode dd_a2= () -> { illegal_1(); op_a2();									}; /* DB   DD		  */
/*TODO*///opcode dd_a3= () -> { illegal_1(); op_a3();									}; /* DB   DD		  */
/*TODO*///opcode dd_a4= () -> { Z80.R = (Z80.R+1)&0xFF; AND(_HX); 										}; /* AND  HX		  */
/*TODO*///opcode dd_a5= () -> { Z80.R = (Z80.R+1)&0xFF; AND(_LX); 										}; /* AND  LX		  */
    opcode dd_a6= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); AND(RM(EA)); 								}; /* AND  (IX+o)	  */
/*TODO*///opcode dd_a7= () -> { illegal_1(); op_a7();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_a8= () -> { illegal_1(); op_a8();									}; /* DB   DD		  */
/*TODO*///opcode dd_a9= () -> { illegal_1(); op_a9();									}; /* DB   DD		  */
/*TODO*///opcode dd_aa= () -> { illegal_1(); op_aa();									}; /* DB   DD		  */
/*TODO*///opcode dd_ab= () -> { illegal_1(); op_ab();									}; /* DB   DD		  */
/*TODO*///opcode dd_ac= () -> { Z80.R = (Z80.R+1)&0xFF; XOR(_HX); 										}; /* XOR  HX		  */
/*TODO*///opcode dd_ad= () -> { Z80.R = (Z80.R+1)&0xFF; XOR(_LX); 										}; /* XOR  LX		  */
    opcode dd_ae= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); XOR(RM(EA)); 								}; /* XOR  (IX+o)	  */
/*TODO*///opcode dd_af= () -> { illegal_1(); op_af();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_b0= () -> { illegal_1(); op_b0();									}; /* DB   DD		  */
/*TODO*///opcode dd_b1= () -> { illegal_1(); op_b1();									}; /* DB   DD		  */
/*TODO*///opcode dd_b2= () -> { illegal_1(); op_b2();									}; /* DB   DD		  */
/*TODO*///opcode dd_b3= () -> { illegal_1(); op_b3();									}; /* DB   DD		  */
/*TODO*///opcode dd_b4= () -> { Z80.R = (Z80.R+1)&0xFF; OR(_HX);											}; /* OR   HX		  */
/*TODO*///opcode dd_b5= () -> { Z80.R = (Z80.R+1)&0xFF; OR(_LX);											}; /* OR   LX		  */
    opcode dd_b6= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); OR(RM(EA));									}; /* OR   (IX+o)	  */
/*TODO*///opcode dd_b7= () -> { illegal_1(); op_b7();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_b8= () -> { illegal_1(); op_b8();									}; /* DB   DD		  */
/*TODO*///opcode dd_b9= () -> { illegal_1(); op_b9();									}; /* DB   DD		  */
/*TODO*///opcode dd_ba= () -> { illegal_1(); op_ba();									}; /* DB   DD		  */
/*TODO*///opcode dd_bb= () -> { illegal_1(); op_bb();									}; /* DB   DD		  */
/*TODO*///opcode dd_bc= () -> { Z80.R = (Z80.R+1)&0xFF; CP(_HX);											}; /* CP   HX		  */
/*TODO*///opcode dd_bd= () -> { Z80.R = (Z80.R+1)&0xFF; CP(_LX);											}; /* CP   LX		  */
    opcode dd_be= () -> { Z80.R = (Z80.R+1)&0xFF; EAX(); CP(RM(EA));									}; /* CP   (IX+o)	  */
/*TODO*///opcode dd_bf= () -> { illegal_1(); op_bf();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_c0= () -> { illegal_1(); op_c0();									}; /* DB   DD		  */
/*TODO*///opcode dd_c1= () -> { illegal_1(); op_c1();									}; /* DB   DD		  */
/*TODO*///opcode dd_c2= () -> { illegal_1(); op_c2();									}; /* DB   DD		  */
/*TODO*///opcode dd_c3= () -> { illegal_1(); op_c3();									}; /* DB   DD		  */
/*TODO*///opcode dd_c4= () -> { illegal_1(); op_c4();									}; /* DB   DD		  */
/*TODO*///opcode dd_c5= () -> { illegal_1(); op_c5();									}; /* DB   DD		  */
/*TODO*///opcode dd_c6= () -> { illegal_1(); op_c6();									}; /* DB   DD		  */
/*TODO*///opcode dd_c7= () -> { illegal_1(); op_c7();									};		  /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_c8= () -> { illegal_1(); op_c8();									}; /* DB   DD		  */
/*TODO*///opcode dd_c9= () -> { illegal_1(); op_c9();									}; /* DB   DD		  */
/*TODO*///opcode dd_ca= () -> { illegal_1(); op_ca();									}; /* DB   DD		  */
    opcode dd_cb= new opcode() { 
        public void handler(){ 
            Z80.R = (Z80.R+1)&0xFF; 
            EAX(); 
            int op = ARG();
            z80_ICount[0] -= cc[Z80_TABLE_xycb][op];
            Z80xycb[op].handler();//EXEC(xycb,ARG());							
    }};/* **   DD CB xx	  */
/*TODO*///opcode dd_cc= () -> { illegal_1(); op_cc();									}; /* DB   DD		  */
/*TODO*///opcode dd_cd= () -> { illegal_1(); op_cd();									}; /* DB   DD		  */
/*TODO*///opcode dd_ce= () -> { illegal_1(); op_ce();									}; /* DB   DD		  */
/*TODO*///opcode dd_cf= () -> { illegal_1(); op_cf();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_d0= () -> { illegal_1(); op_d0();									}; /* DB   DD		  */
/*TODO*///opcode dd_d1= () -> { illegal_1(); op_d1();									}; /* DB   DD		  */
/*TODO*///opcode dd_d2= () -> { illegal_1(); op_d2();									}; /* DB   DD		  */
/*TODO*///opcode dd_d3= () -> { illegal_1(); op_d3();									}; /* DB   DD		  */
/*TODO*///opcode dd_d4= () -> { illegal_1(); op_d4();									}; /* DB   DD		  */
/*TODO*///opcode dd_d5= () -> { illegal_1(); op_d5();									}; /* DB   DD		  */
/*TODO*///opcode dd_d6= () -> { illegal_1(); op_d6();									}; /* DB   DD		  */
/*TODO*///opcode dd_d7= () -> { illegal_1(); op_d7();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_d8= () -> { illegal_1(); op_d8();									}; /* DB   DD		  */
/*TODO*///opcode dd_d9= () -> { illegal_1(); op_d9();									}; /* DB   DD		  */
/*TODO*///opcode dd_da= () -> { illegal_1(); op_da();									}; /* DB   DD		  */
/*TODO*///opcode dd_db= () -> { illegal_1(); op_db();									}; /* DB   DD		  */
/*TODO*///opcode dd_dc= () -> { illegal_1(); op_dc();									}; /* DB   DD		  */
/*TODO*///opcode dd_dd= () -> { illegal_1(); op_dd();									}; /* DB   DD		  */
/*TODO*///opcode dd_de= () -> { illegal_1(); op_de();									}; /* DB   DD		  */
/*TODO*///opcode dd_df= () -> { illegal_1(); op_df();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_e0= () -> { illegal_1(); op_e0();									}; /* DB   DD		  */
    opcode dd_e1= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IX=POP();											}; /* POP  IX		  */
/*TODO*///opcode dd_e2= () -> { illegal_1(); op_e2();									}; /* DB   DD		  */
    opcode dd_e3= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IX=EXSP(Z80.IX); 										}; /* EX   (SP),IX	  */
/*TODO*///opcode dd_e4= () -> { illegal_1(); op_e4();									}; /* DB   DD		  */
    opcode dd_e5= () -> { Z80.R = (Z80.R+1)&0xFF; PUSH( Z80.IX );										}; /* PUSH IX		  */
/*TODO*///opcode dd_e6= () -> { illegal_1(); op_e6();									}; /* DB   DD		  */
/*TODO*///opcode dd_e7= () -> { illegal_1(); op_e7();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_e8= () -> { illegal_1(); op_e8();									}; /* DB   DD		  */
    opcode dd_e9= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.PC = Z80.IX &0xFFFF; change_pc16(Z80.PC); 					}; /* JP   (IX)		  */
/*TODO*///opcode dd_ea= () -> { illegal_1(); op_ea();									}; /* DB   DD		  */
/*TODO*///opcode dd_eb= () -> { illegal_1(); op_eb();									}; /* DB   DD		  */
/*TODO*///opcode dd_ec= () -> { illegal_1(); op_ec();									}; /* DB   DD		  */
/*TODO*///opcode dd_ed= () -> { illegal_1(); op_ed();									}; /* DB   DD		  */
/*TODO*///opcode dd_ee= () -> { illegal_1(); op_ee();									}; /* DB   DD		  */
/*TODO*///opcode dd_ef= () -> { illegal_1(); op_ef();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_f0= () -> { illegal_1(); op_f0();									}; /* DB   DD		  */
/*TODO*///opcode dd_f1= () -> { illegal_1(); op_f1();									}; /* DB   DD		  */
/*TODO*///opcode dd_f2= () -> { illegal_1(); op_f2();									}; /* DB   DD		  */
/*TODO*///opcode dd_f3= () -> { illegal_1(); op_f3();									}; /* DB   DD		  */
/*TODO*///opcode dd_f4= () -> { illegal_1(); op_f4();									}; /* DB   DD		  */
/*TODO*///opcode dd_f5= () -> { illegal_1(); op_f5();									}; /* DB   DD		  */
/*TODO*///opcode dd_f6= () -> { illegal_1(); op_f6();									}; /* DB   DD		  */
/*TODO*///opcode dd_f7= () -> { illegal_1(); op_f7();									}; /* DB   DD		  */
/*TODO*///
/*TODO*///opcode dd_f8= () -> { illegal_1(); op_f8();									}; /* DB   DD		  */
/*TODO*///opcode dd_f9= () -> { Z80.R = (Z80.R+1)&0xFF; _SP = _IX;										}; /* LD   SP,IX 	  */
/*TODO*///opcode dd_fa= () -> { illegal_1(); op_fa();									}; /* DB   DD		  */
/*TODO*///opcode dd_fb= () -> { illegal_1(); op_fb();									}; /* DB   DD		  */
/*TODO*///opcode dd_fc= () -> { illegal_1(); op_fc();									}; /* DB   DD		  */
/*TODO*///opcode dd_fd= () -> { illegal_1(); op_fd();									}; /* DB   DD		  */
/*TODO*///opcode dd_fe= () -> { illegal_1(); op_fe();									}; /* DB   DD		  */
/*TODO*///opcode dd_ff= () -> { illegal_1(); op_ff();									}; /* DB   DD		  */
/*TODO*///
/*TODO*////**********************************************************
/*TODO*/// * IY register related opcodes (FD prefix)
/*TODO*/// **********************************************************/
/*TODO*///opcode fd_00= () -> { illegal_1(); op_00();									}; /* DB   FD		  */
/*TODO*///opcode fd_01= () -> { illegal_1(); op_01();									}; /* DB   FD		  */
/*TODO*///opcode fd_02= () -> { illegal_1(); op_02();									}; /* DB   FD		  */
/*TODO*///opcode fd_03= () -> { illegal_1(); op_03();									}; /* DB   FD		  */
/*TODO*///opcode fd_04= () -> { illegal_1(); op_04();									}; /* DB   FD		  */
/*TODO*///opcode fd_05= () -> { illegal_1(); op_05();									}; /* DB   FD		  */
/*TODO*///opcode fd_06= () -> { illegal_1(); op_06();									}; /* DB   FD		  */
/*TODO*///opcode fd_07= () -> { illegal_1(); op_07();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_08= () -> { illegal_1(); op_08();									}; /* DB   FD		  */
    opcode fd_09= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IY=ADD16(Z80.IY,BC()); 									}; /* ADD  IY,BC 	  */
/*TODO*///opcode fd_0a= () -> { illegal_1(); op_0a();									}; /* DB   FD		  */
/*TODO*///opcode fd_0b= () -> { illegal_1(); op_0b();									}; /* DB   FD		  */
/*TODO*///opcode fd_0c= () -> { illegal_1(); op_0c();									}; /* DB   FD		  */
/*TODO*///opcode fd_0d= () -> { illegal_1(); op_0d();									}; /* DB   FD		  */
/*TODO*///opcode fd_0e= () -> { illegal_1(); op_0e();									}; /* DB   FD		  */
/*TODO*///opcode fd_0f= () -> { illegal_1(); op_0f();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_10= () -> { illegal_1(); op_10();									}; /* DB   FD		  */
/*TODO*///opcode fd_11= () -> { illegal_1(); op_11();									}; /* DB   FD		  */
/*TODO*///opcode fd_12= () -> { illegal_1(); op_12();									}; /* DB   FD		  */
/*TODO*///opcode fd_13= () -> { illegal_1(); op_13();									}; /* DB   FD		  */
/*TODO*///opcode fd_14= () -> { illegal_1(); op_14();									}; /* DB   FD		  */
/*TODO*///opcode fd_15= () -> { illegal_1(); op_15();									}; /* DB   FD		  */
/*TODO*///opcode fd_16= () -> { illegal_1(); op_16();									}; /* DB   FD		  */
/*TODO*///opcode fd_17= () -> { illegal_1(); op_17();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_18= () -> { illegal_1(); op_18();									}; /* DB   FD		  */
    opcode fd_19= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IY=ADD16(Z80.IY,DE()); 									}; /* ADD  IY,DE 	  */
/*TODO*///opcode fd_1a= () -> { illegal_1(); op_1a();									}; /* DB   FD		  */
/*TODO*///opcode fd_1b= () -> { illegal_1(); op_1b();									}; /* DB   FD		  */
/*TODO*///opcode fd_1c= () -> { illegal_1(); op_1c();									}; /* DB   FD		  */
/*TODO*///opcode fd_1d= () -> { illegal_1(); op_1d();									}; /* DB   FD		  */
/*TODO*///opcode fd_1e= () -> { illegal_1(); op_1e();									}; /* DB   FD		  */
/*TODO*///opcode fd_1f= () -> { illegal_1(); op_1f();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_20= () -> { illegal_1(); op_20();									}; /* DB   FD		  */
    opcode fd_21= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IY = ARG16();									}; /* LD   IY,w		  */
    opcode fd_22= () -> { Z80.R = (Z80.R+1)&0xFF; EA = ARG16(); WM16( EA, Z80.IY );				}; /* LD   (w),IY	  */
    opcode fd_23= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IY = (Z80.IY+1)&0xFFFF;											}; /* INC  IY		  */
/*TODO*///opcode fd_24= () -> { Z80.R = (Z80.R+1)&0xFF; _HY = INC(_HY);									}; /* INC  HY		  */
/*TODO*///opcode fd_25= () -> { Z80.R = (Z80.R+1)&0xFF; _HY = DEC(_HY);									}; /* DEC  HY		  */
/*TODO*///opcode fd_26= () -> { Z80.R = (Z80.R+1)&0xFF; _HY = ARG();										}; /* LD   HY,n		  */
/*TODO*///opcode fd_27= () -> { illegal_1(); op_27();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_28= () -> { illegal_1(); op_28();									}; /* DB   FD		  */
    opcode fd_29= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IY=ADD16(Z80.IY,Z80.IY); 									}; /* ADD  IY,IY 	  */
    opcode fd_2a= () -> { Z80.R = (Z80.R+1)&0xFF; EA = ARG16(); Z80.IY=RM16( EA);				}; /* LD   IY,(w)	  */
    opcode fd_2b= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IY = (Z80.IY-1)&0xFFFF;											}; /* DEC  IY		  */
/*TODO*///opcode fd_2c= () -> { Z80.R = (Z80.R+1)&0xFF; _LY = INC(_LY);									}; /* INC  LY		  */
/*TODO*///opcode fd_2d= () -> { Z80.R = (Z80.R+1)&0xFF; _LY = DEC(_LY);									}; /* DEC  LY		  */
/*TODO*///opcode fd_2e= () -> { Z80.R = (Z80.R+1)&0xFF; _LY = ARG();										}; /* LD   LY,n		  */
/*TODO*///opcode fd_2f= () -> { illegal_1(); op_2f();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_30= () -> { illegal_1(); op_30();									}; /* DB   FD		  */
/*TODO*///opcode fd_31= () -> { illegal_1(); op_31();									}; /* DB   FD		  */
/*TODO*///opcode fd_32= () -> { illegal_1(); op_32();									}; /* DB   FD		  */
/*TODO*///opcode fd_33= () -> { illegal_1(); op_33();									}; /* DB   FD		  */
    opcode fd_34= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, INC(RM(EA)) );						}; /* INC  (IY+o)	  */
    opcode fd_35= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, DEC(RM(EA)) );						}; /* DEC  (IY+o)	  */
    opcode fd_36= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, ARG() ); 							}; /* LD   (IY+o),n	  */
/*TODO*///opcode fd_37= () -> { illegal_1(); op_37();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_38= () -> { illegal_1(); op_38();									}; /* DB   FD		  */
/*TODO*///opcode fd_39= () -> { Z80.R = (Z80.R+1)&0xFF; ADD16(IY,SP); 									}; /* ADD  IY,SP 	  */
/*TODO*///opcode fd_3a= () -> { illegal_1(); op_3a();									}; /* DB   FD		  */
/*TODO*///opcode fd_3b= () -> { illegal_1(); op_3b();									}; /* DB   FD		  */
/*TODO*///opcode fd_3c= () -> { illegal_1(); op_3c();									}; /* DB   FD		  */
/*TODO*///opcode fd_3d= () -> { illegal_1(); op_3d();									}; /* DB   FD		  */
/*TODO*///opcode fd_3e= () -> { illegal_1(); op_3e();									}; /* DB   FD		  */
/*TODO*///opcode fd_3f= () -> { illegal_1(); op_3f();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_40= () -> { illegal_1(); op_40();									}; /* DB   FD		  */
/*TODO*///opcode fd_41= () -> { illegal_1(); op_41();									}; /* DB   FD		  */
/*TODO*///opcode fd_42= () -> { illegal_1(); op_42();									}; /* DB   FD		  */
/*TODO*///opcode fd_43= () -> { illegal_1(); op_43();									}; /* DB   FD		  */
/*TODO*///opcode fd_44= () -> { Z80.R = (Z80.R+1)&0xFF; _B = _HY; 										}; /* LD   B,HY		  */
/*TODO*///opcode fd_45= () -> { Z80.R = (Z80.R+1)&0xFF; _B = _LY; 										}; /* LD   B,LY		  */
    opcode fd_46= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); Z80.B = RM(EA); 								}; /* LD   B,(IY+o)	  */
/*TODO*///opcode fd_47= () -> { illegal_1(); op_47();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_48= () -> { illegal_1(); op_48();									}; /* DB   FD		  */
/*TODO*///opcode fd_49= () -> { illegal_1(); op_49();									}; /* DB   FD		  */
/*TODO*///opcode fd_4a= () -> { illegal_1(); op_4a();									}; /* DB   FD		  */
/*TODO*///opcode fd_4b= () -> { illegal_1(); op_4b();									}; /* DB   FD		  */
/*TODO*///opcode fd_4c= () -> { Z80.R = (Z80.R+1)&0xFF; _C = _HY; 										}; /* LD   C,HY		  */
/*TODO*///opcode fd_4d= () -> { Z80.R = (Z80.R+1)&0xFF; _C = _LY; 										}; /* LD   C,LY		  */
    opcode fd_4e= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); Z80.C = RM(EA); 								}; /* LD   C,(IY+o)	  */
/*TODO*///opcode fd_4f= () -> { illegal_1(); op_4f();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_50= () -> { illegal_1(); op_50();									}; /* DB   FD		  */
/*TODO*///opcode fd_51= () -> { illegal_1(); op_51();									}; /* DB   FD		  */
/*TODO*///opcode fd_52= () -> { illegal_1(); op_52();									}; /* DB   FD		  */
/*TODO*///opcode fd_53= () -> { illegal_1(); op_53();									}; /* DB   FD		  */
/*TODO*///opcode fd_54= () -> { Z80.R = (Z80.R+1)&0xFF; _D = _HY; 										}; /* LD   D,HY		  */
/*TODO*///opcode fd_55= () -> { Z80.R = (Z80.R+1)&0xFF; _D = _LY; 										}; /* LD   D,LY		  */
    opcode fd_56= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); Z80.D = RM(EA); 								}; /* LD   D,(IY+o)	  */
/*TODO*///opcode fd_57= () -> { illegal_1(); op_57();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_58= () -> { illegal_1(); op_58();									}; /* DB   FD		  */
/*TODO*///opcode fd_59= () -> { illegal_1(); op_59();									}; /* DB   FD		  */
/*TODO*///opcode fd_5a= () -> { illegal_1(); op_5a();									}; /* DB   FD		  */
/*TODO*///opcode fd_5b= () -> { illegal_1(); op_5b();									}; /* DB   FD		  */
/*TODO*///opcode fd_5c= () -> { Z80.R = (Z80.R+1)&0xFF; _E = _HY; 										}; /* LD   E,HY		  */
/*TODO*///opcode fd_5d= () -> { Z80.R = (Z80.R+1)&0xFF; _E = _LY; 										}; /* LD   E,LY		  */
    opcode fd_5e= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); Z80.E = RM(EA); 								}; /* LD   E,(IY+o)	  */
/*TODO*///opcode fd_5f= () -> { illegal_1(); op_5f();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_60= () -> { Z80.R = (Z80.R+1)&0xFF; _HY = _B; 										}; /* LD   HY,B		  */
/*TODO*///opcode fd_61= () -> { Z80.R = (Z80.R+1)&0xFF; _HY = _C; 										}; /* LD   HY,C		  */
/*TODO*///opcode fd_62= () -> { Z80.R = (Z80.R+1)&0xFF; _HY = _D; 										}; /* LD   HY,D		  */
/*TODO*///opcode fd_63= () -> { Z80.R = (Z80.R+1)&0xFF; _HY = _E; 										}; /* LD   HY,E		  */
/*TODO*///opcode fd_64= () -> { Z80.R = (Z80.R+1)&0xFF;													}; /* LD   HY,HY 	  */
/*TODO*///opcode fd_65= () -> { Z80.R = (Z80.R+1)&0xFF; _HY = _LY;										}; /* LD   HY,LY 	  */
    opcode fd_66= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); Z80.H = RM(EA); 								}; /* LD   H,(IY+o)	  */
/*TODO*///opcode fd_67= () -> { Z80.R = (Z80.R+1)&0xFF; _HY = _A; 										}; /* LD   HY,A		  */
/*TODO*///
/*TODO*///opcode fd_68= () -> { Z80.R = (Z80.R+1)&0xFF; _LY = _B; 										}; /* LD   LY,B		  */
/*TODO*///opcode fd_69= () -> { Z80.R = (Z80.R+1)&0xFF; _LY = _C; 										}; /* LD   LY,C		  */
/*TODO*///opcode fd_6a= () -> { Z80.R = (Z80.R+1)&0xFF; _LY = _D; 										}; /* LD   LY,D		  */
/*TODO*///opcode fd_6b= () -> { Z80.R = (Z80.R+1)&0xFF; _LY = _E; 										}; /* LD   LY,E		  */
/*TODO*///opcode fd_6c= () -> { Z80.R = (Z80.R+1)&0xFF; _LY = _HY;										}; /* LD   LY,HY 	  */
/*TODO*///opcode fd_6d= () -> { Z80.R = (Z80.R+1)&0xFF;													}; /* LD   LY,LY 	  */
    opcode fd_6e= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); Z80.L = RM(EA); 								}; /* LD   L,(IY+o)	  */
/*TODO*///opcode fd_6f= () -> { Z80.R = (Z80.R+1)&0xFF; _LY = _A; 										}; /* LD   LY,A		  */

    opcode fd_70= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, Z80.B );								}; /* LD   (IY+o),B	  */
    opcode fd_71= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, Z80.C );								}; /* LD   (IY+o),C	  */
    opcode fd_72= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, Z80.D );								}; /* LD   (IY+o),D	  */
    opcode fd_73= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, Z80.E );								}; /* LD   (IY+o),E	  */
    opcode fd_74= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, Z80.H );								}; /* LD   (IY+o),H	  */
    opcode fd_75= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, Z80.L );								}; /* LD   (IY+o),L	  */
/*TODO*///opcode fd_76= () -> { illegal_1(); op_76();									};		  /* DB   FD		  */
    opcode fd_77= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); WM( EA, Z80.A );								}; /* LD   (IY+o),A	  */

/*TODO*///opcode fd_78= () -> { illegal_1(); op_78();									}; /* DB   FD		  */
/*TODO*///opcode fd_79= () -> { illegal_1(); op_79();									}; /* DB   FD		  */
/*TODO*///opcode fd_7a= () -> { illegal_1(); op_7a();									}; /* DB   FD		  */
/*TODO*///opcode fd_7b= () -> { illegal_1(); op_7b();									}; /* DB   FD		  */
/*TODO*///opcode fd_7c= () -> { Z80.R = (Z80.R+1)&0xFF; _A = _HY; 										}; /* LD   A,HY		  */
/*TODO*///opcode fd_7d= () -> { Z80.R = (Z80.R+1)&0xFF; _A = _LY; 										}; /* LD   A,LY		  */
    opcode fd_7e= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); Z80.A = RM(EA); 								}; /* LD   A,(IY+o)	  */
/*TODO*///opcode fd_7f= () -> { illegal_1(); op_7f();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_80= () -> { illegal_1(); op_80();									}; /* DB   FD		  */
/*TODO*///opcode fd_81= () -> { illegal_1(); op_81();									}; /* DB   FD		  */
/*TODO*///opcode fd_82= () -> { illegal_1(); op_82();									}; /* DB   FD		  */
/*TODO*///opcode fd_83= () -> { illegal_1(); op_83();									}; /* DB   FD		  */
/*TODO*///opcode fd_84= () -> { Z80.R = (Z80.R+1)&0xFF; ADD(_HY); 										}; /* ADD  A,HY		  */
/*TODO*///opcode fd_85= () -> { Z80.R = (Z80.R+1)&0xFF; ADD(_LY); 										}; /* ADD  A,LY		  */
    opcode fd_86= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); ADD(RM(EA)); 								}; /* ADD  A,(IY+o)	  */
/*TODO*///opcode fd_87= () -> { illegal_1(); op_87();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_88= () -> { illegal_1(); op_88();									}; /* DB   FD		  */
/*TODO*///opcode fd_89= () -> { illegal_1(); op_89();									}; /* DB   FD		  */
/*TODO*///opcode fd_8a= () -> { illegal_1(); op_8a();									}; /* DB   FD		  */
/*TODO*///opcode fd_8b= () -> { illegal_1(); op_8b();									}; /* DB   FD		  */
/*TODO*///opcode fd_8c= () -> { Z80.R = (Z80.R+1)&0xFF; ADC(_HY); 										}; /* ADC  A,HY		  */
/*TODO*///opcode fd_8d= () -> { Z80.R = (Z80.R+1)&0xFF; ADC(_LY); 										}; /* ADC  A,LY		  */
    opcode fd_8e= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); ADC(RM(EA)); 								}; /* ADC  A,(IY+o)	  */
/*TODO*///opcode fd_8f= () -> { illegal_1(); op_8f();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_90= () -> { illegal_1(); op_90();									}; /* DB   FD		  */
/*TODO*///opcode fd_91= () -> { illegal_1(); op_91();									}; /* DB   FD		  */
/*TODO*///opcode fd_92= () -> { illegal_1(); op_92();									}; /* DB   FD		  */
/*TODO*///opcode fd_93= () -> { illegal_1(); op_93();									}; /* DB   FD		  */
/*TODO*///opcode fd_94= () -> { Z80.R = (Z80.R+1)&0xFF; SUB(_HY); 										}; /* SUB  HY		  */
/*TODO*///opcode fd_95= () -> { Z80.R = (Z80.R+1)&0xFF; SUB(_LY); 										}; /* SUB  LY		  */
    opcode fd_96= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); SUB(RM(EA)); 								}; /* SUB  (IY+o)	  */
/*TODO*///opcode fd_97= () -> { illegal_1(); op_97();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_98= () -> { illegal_1(); op_98();									}; /* DB   FD		  */
/*TODO*///opcode fd_99= () -> { illegal_1(); op_99();									}; /* DB   FD		  */
/*TODO*///opcode fd_9a= () -> { illegal_1(); op_9a();									}; /* DB   FD		  */
/*TODO*///opcode fd_9b= () -> { illegal_1(); op_9b();									}; /* DB   FD		  */
/*TODO*///opcode fd_9c= () -> { Z80.R = (Z80.R+1)&0xFF; SBC(_HY); 										}; /* SBC  A,HY		  */
/*TODO*///opcode fd_9d= () -> { Z80.R = (Z80.R+1)&0xFF; SBC(_LY); 										}; /* SBC  A,LY		  */
    opcode fd_9e= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); SBC(RM(EA)); 								}; /* SBC  A,(IY+o)	  */
/*TODO*///opcode fd_9f= () -> { illegal_1(); op_9f();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_a0= () -> { illegal_1(); op_a0();									}; /* DB   FD		  */
/*TODO*///opcode fd_a1= () -> { illegal_1(); op_a1();									}; /* DB   FD		  */
/*TODO*///opcode fd_a2= () -> { illegal_1(); op_a2();									}; /* DB   FD		  */
/*TODO*///opcode fd_a3= () -> { illegal_1(); op_a3();									}; /* DB   FD		  */
/*TODO*///opcode fd_a4= () -> { Z80.R = (Z80.R+1)&0xFF; AND(_HY); 										}; /* AND  HY		  */
/*TODO*///opcode fd_a5= () -> { Z80.R = (Z80.R+1)&0xFF; AND(_LY); 										}; /* AND  LY		  */
    opcode fd_a6= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); AND(RM(EA)); 								}; /* AND  (IY+o)	  */
/*TODO*///opcode fd_a7= () -> { illegal_1(); op_a7();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_a8= () -> { illegal_1(); op_a8();									}; /* DB   FD		  */
/*TODO*///opcode fd_a9= () -> { illegal_1(); op_a9();									}; /* DB   FD		  */
/*TODO*///opcode fd_aa= () -> { illegal_1(); op_aa();									}; /* DB   FD		  */
/*TODO*///opcode fd_ab= () -> { illegal_1(); op_ab();									}; /* DB   FD		  */
/*TODO*///opcode fd_ac= () -> { Z80.R = (Z80.R+1)&0xFF; XOR(_HY); 										}; /* XOR  HY		  */
/*TODO*///opcode fd_ad= () -> { Z80.R = (Z80.R+1)&0xFF; XOR(_LY); 										}; /* XOR  LY		  */
    opcode fd_ae= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); XOR(RM(EA)); 								}; /* XOR  (IY+o)	  */
/*TODO*///opcode fd_af= () -> { illegal_1(); op_af();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_b0= () -> { illegal_1(); op_b0();									}; /* DB   FD		  */
/*TODO*///opcode fd_b1= () -> { illegal_1(); op_b1();									}; /* DB   FD		  */
/*TODO*///opcode fd_b2= () -> { illegal_1(); op_b2();									}; /* DB   FD		  */
/*TODO*///opcode fd_b3= () -> { illegal_1(); op_b3();									}; /* DB   FD		  */
/*TODO*///opcode fd_b4= () -> { Z80.R = (Z80.R+1)&0xFF; OR(_HY);											}; /* OR   HY		  */
/*TODO*///opcode fd_b5= () -> { Z80.R = (Z80.R+1)&0xFF; OR(_LY);											}; /* OR   LY		  */
    opcode fd_b6= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); OR(RM(EA));									}; /* OR   (IY+o)	  */
/*TODO*///opcode fd_b7= () -> { illegal_1(); op_b7();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_b8= () -> { illegal_1(); op_b8();									}; /* DB   FD		  */
/*TODO*///opcode fd_b9= () -> { illegal_1(); op_b9();									}; /* DB   FD		  */
/*TODO*///opcode fd_ba= () -> { illegal_1(); op_ba();									}; /* DB   FD		  */
/*TODO*///opcode fd_bb= () -> { illegal_1(); op_bb();									}; /* DB   FD		  */
/*TODO*///opcode fd_bc= () -> { Z80.R = (Z80.R+1)&0xFF; CP(_HY);											}; /* CP   HY		  */
/*TODO*///opcode fd_bd= () -> { Z80.R = (Z80.R+1)&0xFF; CP(_LY);											}; /* CP   LY		  */
    opcode fd_be= () -> { Z80.R = (Z80.R+1)&0xFF; EAY(); CP(RM(EA));									}; /* CP   (IY+o)	  */
    opcode fd_bf= new opcode() { public void handler(){ illegal_1.handler(); op_bf.handler();									}}; /* DB   FD		  */

    opcode fd_c0= new opcode() { public void handler(){ illegal_1.handler(); op_c0.handler();									}}; /* DB   FD		  */
    opcode fd_c1= new opcode() { public void handler(){ illegal_1.handler(); op_c1.handler();									}}; /* DB   FD		  */
    opcode fd_c2= new opcode() { public void handler(){ illegal_1.handler(); op_c2.handler();									}}; /* DB   FD		  */
    opcode fd_c3= new opcode() { public void handler(){ illegal_1.handler(); op_c3.handler();									}}; /* DB   FD		  */
    opcode fd_c4= new opcode() { public void handler(){ illegal_1.handler(); op_c4.handler();									}}; /* DB   FD		  */
    opcode fd_c5= new opcode() { public void handler(){ illegal_1.handler(); op_c5.handler();									}}; /* DB   FD		  */
    opcode fd_c6= new opcode() { public void handler(){ illegal_1.handler(); op_c6.handler();									}}; /* DB   FD		  */
    opcode fd_c7= new opcode() { public void handler(){ illegal_1.handler(); op_c7.handler();									}}; /* DB   FD		  */

    opcode fd_c8= new opcode() { public void handler(){ illegal_1.handler(); op_c8.handler();									}}; /* DB   FD		  */
    opcode fd_c9= new opcode() { public void handler(){ illegal_1.handler(); op_c9.handler();									}}; /* DB   FD		  */
    opcode fd_ca= new opcode() { public void handler(){ illegal_1.handler(); op_ca.handler();									}}; /* DB   FD		  */
    opcode fd_cb= new opcode() { 
        public void handler(){ 
            Z80.R = (Z80.R+1)&0xFF; 
            EAY(); 
            int op = ARG();
            z80_ICount[0] -= cc[Z80_TABLE_xycb][op];
            Z80xycb[op].handler();//EXEC(xycb,ARG());							
    }};/* **   FD CB xx	  */ 
    opcode fd_cc= new opcode() { public void handler(){ illegal_1.handler(); op_cc.handler();									}}; /* DB   FD		  */
    opcode fd_cd= new opcode() { public void handler(){ illegal_1.handler(); op_cd.handler();									}}; /* DB   FD		  */
    opcode fd_ce= new opcode() { public void handler(){ illegal_1.handler(); op_ce.handler();									}}; /* DB   FD		  */
    opcode fd_cf= new opcode() { public void handler(){ illegal_1.handler(); op_cf.handler();									}}; /* DB   FD		  */

    opcode fd_d0= new opcode() { public void handler(){ illegal_1.handler(); op_d0.handler();									}}; /* DB   FD		  */
    opcode fd_d1= new opcode() { public void handler(){ illegal_1.handler(); op_d1.handler();									}}; /* DB   FD		  */
    opcode fd_d2= new opcode() { public void handler(){ illegal_1.handler(); op_d2.handler();									}}; /* DB   FD		  */
    opcode fd_d3= new opcode() { public void handler(){ illegal_1.handler(); op_d3.handler();									}}; /* DB   FD		  */
    opcode fd_d4= new opcode() { public void handler(){ illegal_1.handler(); op_d4.handler();									}}; /* DB   FD		  */
    opcode fd_d5= new opcode() { public void handler(){ illegal_1.handler(); op_d5.handler();									}}; /* DB   FD		  */
    opcode fd_d6= new opcode() { public void handler(){ illegal_1.handler(); op_d6.handler();									}}; /* DB   FD		  */
    opcode fd_d7= new opcode() { public void handler(){ illegal_1.handler(); op_d7.handler();									}}; /* DB   FD		  */

    opcode fd_d8= new opcode() { public void handler(){ illegal_1.handler(); op_d8.handler();									}}; /* DB   FD		  */
    opcode fd_d9= new opcode() { public void handler(){ illegal_1.handler(); op_d9.handler();									}}; /* DB   FD		  */
    opcode fd_da= new opcode() { public void handler(){ illegal_1.handler(); op_da.handler();									}}; /* DB   FD		  */
    opcode fd_db= new opcode() { public void handler(){ illegal_1.handler(); op_db.handler();									}}; /* DB   FD		  */
    opcode fd_dc= new opcode() { public void handler(){ illegal_1.handler(); op_dc.handler();									}}; /* DB   FD		  */
    opcode fd_dd= new opcode() { public void handler(){ illegal_1.handler(); op_dd.handler();									}}; /* DB   FD		  */
    opcode fd_de= new opcode() { public void handler(){ illegal_1.handler(); op_de.handler();									}}; /* DB   FD		  */
    opcode fd_df= new opcode() { public void handler(){ illegal_1.handler(); op_df.handler();									}}; /* DB   FD		  */

    opcode fd_e0= new opcode() { public void handler(){ illegal_1.handler(); op_e0.handler();									}}; /* DB   FD		  */
    opcode fd_e1= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.IY=POP();											}; /* POP  IY		  */
/*TODO*///opcode fd_e2= () -> { illegal_1(); op_e2();									}; /* DB   FD		  */
/*TODO*///opcode fd_e3= () -> { Z80.R = (Z80.R+1)&0xFF; EXSP(IY); 										}; /* EX   (SP),IY	  */
/*TODO*///opcode fd_e4= () -> { illegal_1(); op_e4();									}; /* DB   FD		  */
    opcode fd_e5= () -> { Z80.R = (Z80.R+1)&0xFF; PUSH( Z80.IY );										}; /* PUSH IY		  */
/*TODO*///opcode fd_e6= () -> { illegal_1(); op_e6();									}; /* DB   FD		  */
/*TODO*///opcode fd_e7= () -> { illegal_1(); op_e7();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_e8= () -> { illegal_1(); op_e8();									}; /* DB   FD		  */
    opcode fd_e9= () -> { Z80.R = (Z80.R+1)&0xFF; Z80.PC = Z80.IY&0xFFFF; change_pc16(Z80.PC); 					}; /* JP   (IY)		  */
/*TODO*///opcode fd_ea= () -> { illegal_1(); op_ea();									}; /* DB   FD		  */
/*TODO*///opcode fd_eb= () -> { illegal_1(); op_eb();									}; /* DB   FD		  */
/*TODO*///opcode fd_ec= () -> { illegal_1(); op_ec();									}; /* DB   FD		  */
/*TODO*///opcode fd_ed= () -> { illegal_1(); op_ed();									}; /* DB   FD		  */
/*TODO*///opcode fd_ee= () -> { illegal_1(); op_ee();									}; /* DB   FD		  */
/*TODO*///opcode fd_ef= () -> { illegal_1(); op_ef();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_f0= () -> { illegal_1(); op_f0();									}; /* DB   FD		  */
/*TODO*///opcode fd_f1= () -> { illegal_1(); op_f1();									}; /* DB   FD		  */
/*TODO*///opcode fd_f2= () -> { illegal_1(); op_f2();									}; /* DB   FD		  */
/*TODO*///opcode fd_f3= () -> { illegal_1(); op_f3();									}; /* DB   FD		  */
/*TODO*///opcode fd_f4= () -> { illegal_1(); op_f4();									}; /* DB   FD		  */
/*TODO*///opcode fd_f5= () -> { illegal_1(); op_f5();									}; /* DB   FD		  */
/*TODO*///opcode fd_f6= () -> { illegal_1(); op_f6();									}; /* DB   FD		  */
/*TODO*///opcode fd_f7= () -> { illegal_1(); op_f7();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///opcode fd_f8= () -> { illegal_1(); op_f8();									}; /* DB   FD		  */
/*TODO*///opcode fd_f9= () -> { Z80.R = (Z80.R+1)&0xFF; _SP = _IY;										}; /* LD   SP,IY 	  */
/*TODO*///opcode fd_fa= () -> { illegal_1(); op_fa();									}; /* DB   FD		  */
/*TODO*///opcode fd_fb= () -> { illegal_1(); op_fb();									}; /* DB   FD		  */
/*TODO*///opcode fd_fc= () -> { illegal_1(); op_fc();									}; /* DB   FD		  */
/*TODO*///opcode fd_fd= () -> { illegal_1(); op_fd();									}; /* DB   FD		  */
/*TODO*///opcode fd_fe= () -> { illegal_1(); op_fe();									}; /* DB   FD		  */
/*TODO*///opcode fd_ff= () -> { illegal_1(); op_ff();									}; /* DB   FD		  */
/*TODO*///
/*TODO*///OP(illegal,2)
/*TODO*///{
/*TODO*///	logerror("Z80 #%d ill. opcode $ed $%02x\n",
/*TODO*///			cpu_getactivecpu(), cpu_readop((_PCD-1)&0xffff));
/*TODO*///}
    opcode illegal_2 = new opcode() {
        public void handler() {
            throw new UnsupportedOperationException("unimplemented");
        }
    };

    /**********************************************************
     * special opcodes (ED prefix)
     **********************************************************/
    opcode ed_00= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_01= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_02= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_03= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_04= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_05= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_06= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_07= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
     
    opcode ed_08= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_09= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_0a= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_0b= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_0c= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_0d= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_0e= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_0f= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    
    opcode ed_10= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_11= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_12= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_13= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_14= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_15= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_16= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_17= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    
    opcode ed_18= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_19= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_1a= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_1b= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_1c= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_1d= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_1e= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_1f= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    
    opcode ed_20= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_21= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_22= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_23= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_24= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_25= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_26= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_27= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    
    opcode ed_28= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_29= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_2a= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_2b= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_2c= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_2d= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_2e= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_2f= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    
    opcode ed_30= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_31= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_32= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_33= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_34= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_35= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_36= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_37= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
     
    opcode ed_38= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_39= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_3a= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_3b= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_3c= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_3d= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_3e= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_3f= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_40= () -> { Z80.B = IN(BC()); Z80.F = (Z80.F & CF) | SZP[Z80.B]; 				}; /* IN   B,(C) 	  */
    opcode ed_41= () -> { OUT(BC(),Z80.B);											}; /* OUT  (C),B 	  */
    opcode ed_42= () -> { SBC16( BC() );											}; /* SBC  HL,BC 	  */
    opcode ed_43= () -> { EA = ARG16(); WM16( EA, BC() );						}; /* LD   (w),BC	  */
    opcode ed_44= () -> { NEG();													}; /* NEG			  */
    opcode ed_45= () -> { RETN();													}; /* RETN;			  */
    opcode ed_46= () -> { Z80.IM = 0;												}; /* IM   0 		  */
    opcode ed_47= () -> { LD_I_A(); 												}; /* LD   I,A		  */

/*TODO*///opcode ed_48= () -> { _C = IN(_BC); _F = (_F & CF) | SZP[_C]; 				}; /* IN   C,(C) 	  */
    opcode ed_49= () -> { OUT(BC(),Z80.C);											}; /* OUT  (C),C 	  */
    opcode ed_4a= () -> { ADC16( BC() );											}; /* ADC  HL,BC 	  */
    opcode ed_4b= () -> { EA = ARG16(); BC(RM16( EA));						}; /* LD   BC,(w)	  */
    opcode ed_4c= () -> { NEG();													}; /* NEG			  */
    opcode ed_4d= () -> { RETI();													}; /* RETI			  */
    opcode ed_4e= () -> { Z80.IM = 0;												}; /* IM   0 		  */
/*TODO*///opcode ed_4f= () -> { LD_R_A; 												}; /* LD   R,A		  */

    opcode ed_50= () -> { Z80.D = IN(BC()); Z80.F = (Z80.F & CF) | SZP[Z80.D]; 				}; /* IN   D,(C) 	  */
    opcode ed_51= () -> { OUT(BC(),Z80.D);											}; /* OUT  (C),D 	  */
    opcode ed_52= () -> { SBC16( DE() );											}; /* SBC  HL,DE 	  */
    opcode ed_53= () -> { EA = ARG16(); WM16( EA, DE() );						}; /* LD   (w),DE	  */
    opcode ed_54= () -> { NEG();													}; /* NEG			  */
    opcode ed_55= () -> { RETN();													}; /* RETN;			  */
    opcode ed_56= () -> { Z80.IM = 1;												}; /* IM   1 		  */
    opcode ed_57= () -> { LD_A_I(); 												}; /* LD   A,I		  */

    opcode ed_58= () -> { Z80.E = IN(BC()); Z80.F = (Z80.F & CF) | SZP[Z80.E]; 				}; /* IN   E,(C) 	  */
    opcode ed_59= () -> { OUT(BC(),Z80.E);											}; /* OUT  (C),E 	  */
    opcode ed_5a= () -> { ADC16( DE() );											}; /* ADC  HL,DE 	  */
    opcode ed_5b= () -> { EA = ARG16(); DE(RM16( EA));						}; /* LD   DE,(w)	  */
    opcode ed_5c= () -> { NEG();													}; /* NEG			  */
    opcode ed_5d= () -> { RETI();													}; /* RETI			  */
    opcode ed_5e= () -> { Z80.IM = 2;												}; /* IM   2 		  */
    opcode ed_5f= () -> { LD_A_R(); 												}; /* LD   A,R		  */

    opcode ed_60= () -> { Z80.H = IN(BC()); Z80.F = (Z80.F & CF) | SZP[Z80.H]; 				}; /* IN   H,(C) 	  */
    opcode ed_61= () -> { OUT(BC(),Z80.H);											}; /* OUT  (C),H 	  */
    opcode ed_62= () -> { SBC16( HL() );											}; /* SBC  HL,HL 	  */
    opcode ed_63= () -> { EA = ARG16(); WM16( EA, HL() );						}; /* LD   (w),HL	  */
    opcode ed_64= () -> { NEG();													}; /* NEG			  */
    opcode ed_65= () -> { RETN();													}; /* RETN;			  */
    opcode ed_66= () -> { Z80.IM = 0;												}; /* IM   0 		  */
    opcode ed_67= () -> { RRD();													}; /* RRD  (HL)		  */

/*TODO*///opcode ed_68= () -> { _L = IN(_BC); _F = (_F & CF) | SZP[_L]; 				}; /* IN   L,(C) 	  */
    opcode ed_69= () -> { OUT(BC(),Z80.L);											}; /* OUT  (C),L 	  */
    opcode ed_6a= () -> { ADC16( HL() );											}; /* ADC  HL,HL 	  */
    opcode ed_6b= () -> { EA = ARG16(); HL(RM16( EA));						}; /* LD   HL,(w)	  */
    opcode ed_6c= () -> { NEG();													}; /* NEG			  */
    opcode ed_6d= () -> { RETI();													}; /* RETI			  */
    opcode ed_6e= () -> { Z80.IM = 0;												}; /* IM   0 		  */
    opcode ed_6f= () -> { RLD();													}; /* RLD  (HL)		  */

/*TODO*///opcode ed_70= () -> { UINT8 res = IN(_BC); _F = (_F & CF) | SZP[res]; 		}; /* IN   0,(C) 	  */
    opcode ed_71= () -> { OUT(BC(),0); 											}; /* OUT  (C),0 	  */
    opcode ed_72= () -> { SBC16( Z80.SP );											}; /* SBC  HL,SP 	  */
    opcode ed_73= () -> { EA = ARG16(); WM16( EA, Z80.SP );						}; /* LD   (w),SP	  */
    opcode ed_74= () -> { NEG();													}; /* NEG			  */
    opcode ed_75= () -> { RETN();													}; /* RETN;			  */
    opcode ed_76= () -> { Z80.IM = 1;												}; /* IM   1 		  */
    opcode ed_77= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED,77 	  */

    opcode ed_78= () -> { Z80.A = IN(BC()); Z80.F = (Z80.F & CF) | SZP[Z80.A]; 				}; /* IN   E,(C) 	  */
    opcode ed_79= () -> { OUT(BC(),Z80.A);											}; /* OUT  (C),E 	  */
    opcode ed_7a= () -> { ADC16( Z80.SP );											}; /* ADC  HL,SP 	  */
    opcode ed_7b= () -> { EA = ARG16(); Z80.SP=RM16( EA);						}; /* LD   SP,(w)	  */
    opcode ed_7c= () -> { NEG();													}; /* NEG			  */
    opcode ed_7d= () -> { RETI();													}; /* RETI			  */
    opcode ed_7e= () -> { Z80.IM = 2;												}; /* IM   2 		  */
    opcode ed_7f= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED,7F 	  */

    opcode ed_80= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_81= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_82= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_83= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_84= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_85= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_86= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_87= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
 
    opcode ed_88= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_89= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_8a= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_8b= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_8c= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_8d= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_8e= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_8f= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_90= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_91= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_92= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_93= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_94= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_95= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_96= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_97= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
 
    opcode ed_98= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_99= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_9a= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_9b= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_9c= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_9d= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_9e= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_9f= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_a0= () -> { LDI();													}; /* LDI			  */
    opcode ed_a1= () -> { CPI();													}; /* CPI			  */
    opcode ed_a2= () -> { INI();													}; /* INI			  */
    opcode ed_a3= () -> { OUTI();													}; /* OUTI			  */
    opcode ed_a4= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_a5= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_a6= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_a7= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

/*TODO*///opcode ed_a8= () -> { LDD;													}; /* LDD			  */
/*TODO*///opcode ed_a9= () -> { CPD;													}; /* CPD			  */
    opcode ed_aa= () -> { IND();													}; /* IND			  */
    opcode ed_ac= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ad= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ae= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_af= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_b0= () -> { LDIR();													}; /* LDIR			  */
    opcode ed_b1= () -> { CPIR();													}; /* CPIR			  */
/*TODO*///opcode ed_b2= () -> { INIR;													}; /* INIR			  */
/*TODO*///opcode ed_b3= () -> { OTIR;													}; /* OTIR			  */
    opcode ed_b4= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_b5= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_b6= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_b7= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_b8= () -> { LDDR();													}; /* LDDR			  */
/*TODO*///opcode ed_b9= () -> { CPDR;													}; /* CPDR			  */
/*TODO*///opcode ed_ba= () -> { INDR;													}; /* INDR			  */
/*TODO*///opcode ed_bb= () -> { OTDR;													}; /* OTDR			  */
    opcode ed_bc= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_bd= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_be= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_bf= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_c0= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_c1= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_c2= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_c3= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_c4= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_c5= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_c6= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_c7= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_c8= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_c9= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ca= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_cb= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_cc= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_cd= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ce= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_cf= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_d0= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_d1= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_d2= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_d3= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_d4= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_d5= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_d6= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_d7= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_d8= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_d9= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_da= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_db= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_dc= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_dd= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_de= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_df= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */

    opcode ed_e0= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_e1= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_e2= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_e3= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_e4= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_e5= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_e6= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_e7= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    
    opcode ed_e8= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_e9= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ea= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_eb= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ec= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ed= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ee= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ef= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    
    opcode ed_f0= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_f1= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_f2= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_f3= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_f4= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_f5= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_f6= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_f7= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    
    opcode ed_f8= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_f9= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_fa= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_fb= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_fc= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_fd= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_fe= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    opcode ed_ff= new opcode() { public void handler(){ illegal_2.handler();											}}; /* DB   ED		  */
    
    /**********************************************************
     * main opcodes
     **********************************************************/
    opcode op_00= () -> { 														}; /* NOP			  */
    opcode op_01= () -> { BC(ARG16());											}; /* LD   BC,w		  */
    opcode op_02= () -> { WM( BC(), Z80.A );											}; /* LD   (BC),A	  */
    opcode op_03= () -> { BC((BC() + 1) & 0xffff);													}; /* INC  BC		  */
    opcode op_04= () -> { Z80.B = INC(Z80.B);											}; /* INC  B 		  */
    opcode op_05= () -> { Z80.B = DEC(Z80.B);											}; /* DEC  B 		  */
    opcode op_06= () -> { Z80.B = ARG(); 											}; /* LD   B,n		  */
    opcode op_07= () -> { RLCA();													}; /* RLCA			  */

    opcode op_08= () -> { EX_AF();													}; /* EX   AF,AF'      */
    opcode op_09= () -> { HL(ADD16(HL(),BC()));											}; /* ADD  HL,BC 	  */
    opcode op_0a= () -> { Z80.A = RM(BC());											}; /* LD   A,(BC)	  */
    opcode op_0b= () -> { BC((BC() - 1) & 0xffff); 									}; /* DEC  BC		  */
    opcode op_0c= () -> { Z80.C = INC(Z80.C);											}; /* INC  C 		  */
    opcode op_0d= () -> { Z80.C = DEC(Z80.C);											}; /* DEC  C 		  */
    opcode op_0e= () -> { Z80.C = ARG(); 											}; /* LD   C,n		  */
    opcode op_0f= () -> { RRCA();													}; /* RRCA			  */

    opcode op_10= () -> { Z80.B= (Z80.B - 1)&0xFF; JR_COND( Z80.B!=0, 0x10 );								}; /* DJNZ o 		  */
    opcode op_11= () -> { DE(ARG16());											}; /* LD   DE,w		  */
    opcode op_12= () -> { WM( DE(), Z80.A );											}; /* LD   (DE),A	  */
    opcode op_13= () -> { DE((DE() + 1) & 0xffff);													}; /* INC  DE		  */
    opcode op_14= () -> { Z80.D = INC(Z80.D);											}; /* INC  D 		  */
    opcode op_15= () -> { Z80.D = DEC(Z80.D);											}; /* DEC  D 		  */
    opcode op_16= () -> { Z80.D = ARG(); 											}; /* LD   D,n		  */
    opcode op_17= () -> { RLA();													}; /* RLA			  */
    
    opcode op_18= () -> { JR();													}; /* JR   o 		  */
    opcode op_19= () -> { HL(ADD16(HL(),DE()));											}; /* ADD  HL,DE 	  */
    opcode op_1a= () -> { Z80.A = RM(DE());											}; /* LD   A,(DE)	  */
    opcode op_1b= () -> { DE((DE() - 1) & 0xffff); 									}; /* DEC  DE		  */
    opcode op_1c= () -> { Z80.E = INC(Z80.E);											}; /* INC  E 		  */
    opcode op_1d= () -> { Z80.E = DEC(Z80.E);											}; /* DEC  E 		  */
    opcode op_1e= () -> { Z80.E = ARG(); 											}; /* LD   E,n		  */
    opcode op_1f= () -> { RRA();													}; /* RRA			  */

    opcode op_20= () -> { JR_COND( (Z80.F & ZF)==0, 0x20 );							}; /* JR   NZ,o		  */
    opcode op_21= () -> { HL(ARG16());											}; /* LD   HL,w		  */
    opcode op_22= () -> { EA = ARG16(); WM16( EA, HL() );						}; /* LD   (w),HL	  */
    opcode op_23= () -> { HL((HL() + 1) & 0xffff);													}; /* INC  HL		  */
    opcode op_24= () -> { Z80.H = INC(Z80.H);											}; /* INC  H 		  */
    opcode op_25= () -> { Z80.H = DEC(Z80.H);											}; /* DEC  H 		  */
    opcode op_26= () -> { Z80.H = ARG(); 											}; /* LD   H,n		  */
    opcode op_27= () -> { DAA();													}; /* DAA			  */

    opcode op_28= () -> { JR_COND( (Z80.F & ZF)!=0, 0x28 );								}; /* JR   Z,o		  */
    opcode op_29= () -> { HL(ADD16(HL(),HL()));											}; /* ADD  HL,HL 	  */
    opcode op_2a= () -> { EA = ARG16(); HL(RM16( EA));						}; /* LD   HL,(w)	  */
    opcode op_2b= () -> { HL((HL() - 1) & 0xffff); 									}; /* DEC  HL		  */
    opcode op_2c= () -> { Z80.L = INC(Z80.L);											}; /* INC  L 		  */
    opcode op_2d= () -> { Z80.L = DEC(Z80.L);											}; /* DEC  L 		  */
    opcode op_2e= () -> { Z80.L = ARG(); 											}; /* LD   L,n		  */
    opcode op_2f= () -> { Z80.A ^= 0xff; Z80.F = (Z80.F&(SF|ZF|PF|CF))|HF|NF|(Z80.A&(YF|XF)); }; /* CPL			  */

    opcode op_30= () -> { JR_COND( (Z80.F & CF)==0, 0x30 );							}; /* JR   NC,o		  */
    opcode op_31= () -> { Z80.SP = ARG16();											}; /* LD   SP,w		  */
    opcode op_32= () -> { EA = ARG16(); WM( EA, Z80.A ); 							}; /* LD   (w),A 	  */
    opcode op_33= () -> { Z80.SP= (Z80.SP+1) & 0xFFFF;													}; /* INC  SP		  */
    opcode op_34= () -> { WM( HL(), INC(RM(HL())) );								}; /* INC  (HL)		  */
    opcode op_35= () -> { WM( HL(), DEC(RM(HL())) );								}; /* DEC  (HL)		  */
    opcode op_36= () -> { WM( HL(), ARG() );										}; /* LD   (HL),n	  */
    opcode op_37= () -> { Z80.F = (Z80.F & (SF|ZF|PF)) | CF | (Z80.A & (YF|XF));			}; /* SCF			  */

    opcode op_38= () -> { JR_COND( (Z80.F & CF)!=0, 0x38 );								}; /* JR   C,o		  */
    opcode op_39= () -> { HL(ADD16(HL(),Z80.SP));											}; /* ADD  HL,SP 	  */
    opcode op_3a= () -> { EA = ARG16(); Z80.A = RM( EA );							}; /* LD   A,(w) 	  */
    opcode op_3b= () -> { Z80.SP= (Z80.SP-1) & 0xFFFF;													}; /* DEC  SP		  */
    opcode op_3c= () -> { Z80.A = INC(Z80.A);											}; /* INC  A 		  */
    opcode op_3d= () -> { Z80.A = DEC(Z80.A);											}; /* DEC  A 		  */
    opcode op_3e= () -> { Z80.A = ARG(); 											}; /* LD   A,n		  */
    opcode op_3f= () -> { Z80.F = ((Z80.F&(SF|ZF|PF|CF))|((Z80.F&CF)<<4)|(Z80.A&(YF|XF)))^CF; }; /* CCF			  */

    opcode op_40= () -> { 														}; /* LD   B,B		  */
    opcode op_41= () -> { Z80.B = Z80.C;												}; /* LD   B,C		  */
    opcode op_42= () -> { Z80.B = Z80.D;												}; /* LD   B,D		  */
    opcode op_43= () -> { Z80.B = Z80.E;												}; /* LD   B,E		  */
    opcode op_44= () -> { Z80.B = Z80.H;												}; /* LD   B,H		  */
    opcode op_45= () -> { Z80.B = Z80.L;												}; /* LD   B,L		  */
    opcode op_46= () -> { Z80.B = RM(HL());											}; /* LD   B,(HL)	  */
    opcode op_47= () -> { Z80.B = Z80.A;												}; /* LD   B,A		  */

    opcode op_48= () -> { Z80.C = Z80.B;												}; /* LD   C,B		  */
    opcode op_49= () -> { 														}; /* LD   C,C		  */
    opcode op_4a= () -> { Z80.C = Z80.D;												}; /* LD   C,D		  */
    opcode op_4b= () -> { Z80.C = Z80.E;												}; /* LD   C,E		  */
    opcode op_4c= () -> { Z80.C = Z80.H;												}; /* LD   C,H		  */
    opcode op_4d= () -> { Z80.C = Z80.L;												}; /* LD   C,L		  */
    opcode op_4e= () -> { Z80.C = RM(HL());											}; /* LD   C,(HL)	  */
    opcode op_4f= () -> { Z80.C = Z80.A;												}; /* LD   C,A		  */

    opcode op_50= () -> { Z80.D = Z80.B;												}; /* LD   D,B		  */
    opcode op_51= () -> { Z80.D = Z80.C;												}; /* LD   D,C		  */
    opcode op_52= () -> { 														}; /* LD   D,D		  */
    opcode op_53= () -> { Z80.D = Z80.E;												}; /* LD   D,E		  */
    opcode op_54= () -> { Z80.D = Z80.H;												}; /* LD   D,H		  */
    opcode op_55= () -> { Z80.D = Z80.L;												}; /* LD   D,L		  */
    opcode op_56= () -> { Z80.D = RM(HL());											}; /* LD   D,(HL)	  */
    opcode op_57= () -> { Z80.D = Z80.A;												}; /* LD   D,A		  */

    opcode op_58= () -> { Z80.E = Z80.B;												}; /* LD   E,B		  */
    opcode op_59= () -> { Z80.E = Z80.C;												}; /* LD   E,C		  */
    opcode op_5a= () -> { Z80.E = Z80.D;												}; /* LD   E,D		  */
    opcode op_5b= () -> { 														}; /* LD   E,E		  */
    opcode op_5c= () -> { Z80.E = Z80.H;												}; /* LD   E,H		  */
    opcode op_5d= () -> { Z80.E = Z80.L;												}; /* LD   E,L		  */
    opcode op_5e= () -> { Z80.E = RM(HL());											}; /* LD   E,(HL)	  */
    opcode op_5f= () -> { Z80.E = Z80.A;												}; /* LD   E,A		  */

    opcode op_60= () -> { Z80.H = Z80.B;												}; /* LD   H,B		  */
    opcode op_61= () -> { Z80.H = Z80.C;												}; /* LD   H,C		  */
    opcode op_62= () -> { Z80.H = Z80.D;												}; /* LD   H,D		  */
    opcode op_63= () -> { Z80.H = Z80.E;												}; /* LD   H,E		  */
    opcode op_64= () -> { 														}; /* LD   H,H		  */
    opcode op_65= () -> { Z80.H = Z80.L;												}; /* LD   H,L		  */
    opcode op_66= () -> { Z80.H = RM(HL());											}; /* LD   H,(HL)	  */
    opcode op_67= () -> { Z80.H = Z80.A;												}; /* LD   H,A		  */

    opcode op_68= () -> { Z80.L = Z80.B;												}; /* LD   L,B		  */
    opcode op_69= () -> { Z80.L = Z80.C;												}; /* LD   L,C		  */
    opcode op_6a= () -> { Z80.L = Z80.D;												}; /* LD   L,D		  */
    opcode op_6b= () -> { Z80.L = Z80.E;												}; /* LD   L,E		  */
    opcode op_6c= () -> { Z80.L = Z80.H;												}; /* LD   L,H		  */
    opcode op_6d= () -> { 														}; /* LD   L,L		  */
    opcode op_6e= () -> { Z80.L = RM(HL());											}; /* LD   L,(HL)	  */
    opcode op_6f= () -> { Z80.L = Z80.A;												}; /* LD   L,A		  */

    opcode op_70= () -> { WM( HL(), Z80.B );											}; /* LD   (HL),B	  */
    opcode op_71= () -> { WM( HL(), Z80.C );											}; /* LD   (HL),C	  */
    opcode op_72= () -> { WM( HL(), Z80.D );											}; /* LD   (HL),D	  */
    opcode op_73= () -> { WM( HL(), Z80.E );											}; /* LD   (HL),E	  */
    opcode op_74= () -> { WM( HL(), Z80.H );											}; /* LD   (HL),H	  */
    opcode op_75= () -> { WM( HL(), Z80.L );											}; /* LD   (HL),L	  */
    opcode op_76= () -> { ENTER_HALT(); 											}; /* HALT			  */
    opcode op_77= () -> { WM( HL(), Z80.A );											}; /* LD   (HL),A	  */

    opcode op_78= () -> { Z80.A = Z80.B;												}; /* LD   A,B		  */
    opcode op_79= () -> { Z80.A = Z80.C;												}; /* LD   A,C		  */
    opcode op_7a= () -> { Z80.A = Z80.D;												}; /* LD   A,D		  */
    opcode op_7b= () -> { Z80.A = Z80.E;												}; /* LD   A,E		  */
    opcode op_7c= () -> { Z80.A = Z80.H;												}; /* LD   A,H		  */
    opcode op_7d= () -> { Z80.A = Z80.L;												}; /* LD   A,L		  */
    opcode op_7e= () -> { Z80.A = RM(HL());											}; /* LD   A,(HL)	  */
    opcode op_7f= () -> { 														}; /* LD   A,A		  *//*TODO*///

    opcode op_80= () -> { ADD(Z80.B);												}; /* ADD  A,B		  */
    opcode op_81= () -> { ADD(Z80.C);												}; /* ADD  A,C		  */
    opcode op_82= () -> { ADD(Z80.D);												}; /* ADD  A,D		  */
    opcode op_83= () -> { ADD(Z80.E);												}; /* ADD  A,E		  */
    opcode op_84= () -> { ADD(Z80.H);												}; /* ADD  A,H		  */
    opcode op_85= () -> { ADD(Z80.L);												}; /* ADD  A,L		  */
    opcode op_86= () -> { ADD(RM(HL()));											}; /* ADD  A,(HL)	  */
    opcode op_87= () -> { ADD(Z80.A);												}; /* ADD  A,A		  */

    opcode op_88= () -> { ADC(Z80.B);												}; /* ADC  A,B		  */
    opcode op_89= () -> { ADC(Z80.C);												}; /* ADC  A,C		  */
    opcode op_8a= () -> { ADC(Z80.D);												}; /* ADC  A,D		  */
    opcode op_8b= () -> { ADC(Z80.E);												}; /* ADC  A,E		  */
    opcode op_8c= () -> { ADC(Z80.H);												}; /* ADC  A,H		  */
    opcode op_8d= () -> { ADC(Z80.L);												}; /* ADC  A,L		  */
    opcode op_8e= () -> { ADC(RM(HL()));											}; /* ADC  A,(HL)	  */
    opcode op_8f= () -> { ADC(Z80.A);												}; /* ADC  A,A		  */

    opcode op_90= () -> { SUB(Z80.B);												}; /* SUB  B 		  */
    opcode op_91= () -> { SUB(Z80.C);												}; /* SUB  C 		  */
    opcode op_92= () -> { SUB(Z80.D);												}; /* SUB  D 		  */
    opcode op_93= () -> { SUB(Z80.E);												}; /* SUB  E 		  */
    opcode op_94= () -> { SUB(Z80.H);												}; /* SUB  H 		  */
    opcode op_95= () -> { SUB(Z80.L);												}; /* SUB  L 		  */
    opcode op_96= () -> { SUB(RM(HL()));											}; /* SUB  (HL)		  */
    opcode op_97= () -> { SUB(Z80.A);												}; /* SUB  A 		  */

    opcode op_98= () -> { SBC(Z80.B);												}; /* SBC  A,B		  */
    opcode op_99= () -> { SBC(Z80.C);												}; /* SBC  A,C		  */
    opcode op_9a= () -> { SBC(Z80.D);												}; /* SBC  A,D		  */
    opcode op_9b= () -> { SBC(Z80.E);												}; /* SBC  A,E		  */
    opcode op_9c= () -> { SBC(Z80.H);												}; /* SBC  A,H		  */
    opcode op_9d= () -> { SBC(Z80.L);												}; /* SBC  A,L		  */
    opcode op_9e= () -> { SBC(RM(HL()));											}; /* SBC  A,(HL)	  */
    opcode op_9f= () -> { SBC(Z80.A);												}; /* SBC  A,A		  */

    opcode op_a0= () -> { AND(Z80.B);												}; /* AND  B 		  */
    opcode op_a1= () -> { AND(Z80.C);												}; /* AND  C 		  */
    opcode op_a2= () -> { AND(Z80.D);												}; /* AND  D 		  */
    opcode op_a3= () -> { AND(Z80.E);												}; /* AND  E 		  */
    opcode op_a4= () -> { AND(Z80.H);												}; /* AND  H 		  */
    opcode op_a5= () -> { AND(Z80.L);												}; /* AND  L 		  */
    opcode op_a6= () -> { AND(RM(HL()));											}; /* AND  (HL)		  */
    opcode op_a7= () -> { AND(Z80.A);												}; /* AND  A 		  */

    opcode op_a8= () -> { XOR(Z80.B);												}; /* XOR  B 		  */
    opcode op_a9= () -> { XOR(Z80.C);												}; /* XOR  C 		  */
    opcode op_aa= () -> { XOR(Z80.D);												}; /* XOR  D 		  */
    opcode op_ab= () -> { XOR(Z80.E);												}; /* XOR  E 		  */
    opcode op_ac= () -> { XOR(Z80.H);												}; /* XOR  H 		  */
    opcode op_ad= () -> { XOR(Z80.L);												}; /* XOR  L 		  */
    opcode op_ae= () -> { XOR(RM(HL()));											}; /* XOR  (HL)		  */
    opcode op_af= () -> { XOR(Z80.A);												}; /* XOR  A 		  */

    opcode op_b0= () -> { OR(Z80.B); 												}; /* OR   B 		  */
    opcode op_b1= () -> { OR(Z80.C); 												}; /* OR   C 		  */
    opcode op_b2= () -> { OR(Z80.D); 												}; /* OR   D 		  */
    opcode op_b3= () -> { OR(Z80.E); 												}; /* OR   E 		  */
    opcode op_b4= () -> { OR(Z80.H); 												}; /* OR   H 		  */
    opcode op_b5= () -> { OR(Z80.L); 												}; /* OR   L 		  */
    opcode op_b6= () -> { OR(RM(HL()));											}; /* OR   (HL)		  */
    opcode op_b7= () -> { OR(Z80.A); 												}; /* OR   A 		  */

    opcode op_b8= () -> { CP(Z80.B); 												}; /* CP   B 		  */
    opcode op_b9= () -> { CP(Z80.C); 												}; /* CP   C 		  */
    opcode op_ba= () -> { CP(Z80.D); 												}; /* CP   D 		  */
    opcode op_bb= () -> { CP(Z80.E); 												}; /* CP   E 		  */
    opcode op_bc= () -> { CP(Z80.H); 												}; /* CP   H 		  */
    opcode op_bd= () -> { CP(Z80.L); 												}; /* CP   L 		  */
    opcode op_be= () -> { CP(RM(HL()));											}; /* CP   (HL)		  */
    opcode op_bf= () -> { CP(Z80.A); 												}; /* CP   A 		  */
    
    opcode op_c0= () -> { RET_COND( (Z80.F & ZF)==0, 0xc0 );							}; /* RET  NZ		  */
    opcode op_c1= () -> { BC(POP());												}; /* POP  BC		  */
    opcode op_c2= () -> { JP_COND( (Z80.F & ZF)==0 );									}; /* JP   NZ,a		  */
    opcode op_c3= () -> { JP(); 													}; /* JP   a 		  */
    opcode op_c4= () -> { CALL_COND( (Z80.F & ZF)==0, 0xc4 );							}; /* CALL NZ,a		  */
    opcode op_c5= () -> { PUSH( BC() ); 											}; /* PUSH BC		  */
    opcode op_c6= () -> { ADD(ARG()); 											}; /* ADD  A,n		  */
    opcode op_c7= () -> { RST(0x00);												}; /* RST  0 		  */

    opcode op_c8= () -> { RET_COND( (Z80.F & ZF)!=0, 0xc8 );								}; /* RET  Z 		  */
    opcode op_c9= () -> { Z80.PC=POP(); change_pc16(Z80.PC); 							}; /* RET			  */
    opcode op_ca= () -> { JP_COND( (Z80.F & ZF)!=0 ); 									}; /* JP   Z,a		  */
    opcode op_cb = new opcode() {
        public void handler() {
            Z80.R = (Z80.R + 1) & 0xFF;
            int op = ROP();
            z80_ICount[0] -= cc[Z80_TABLE_cb][op];
            Z80cb[op].handler();//EXEC(cb, ROP());
        }
    };/* **** CB xx 	  */
    opcode op_cc= () -> { CALL_COND( (Z80.F & ZF)!=0, 0xcc ); 							}; /* CALL Z,a		  */
    opcode op_cd= () -> { CALL(); 												}; /* CALL a 		  */
    opcode op_ce= () -> { ADC(ARG()); 											}; /* ADC  A,n		  */
    opcode op_cf= () -> { RST(0x08);												}; /* RST  1 		  */

    opcode op_d0= () -> { RET_COND( (Z80.F & CF)==0, 0xd0 );							}; /* RET  NC		  */
    opcode op_d1= () -> { DE(POP());												}; /* POP  DE		  */
    opcode op_d2= () -> { JP_COND( (Z80.F & CF)==0 );									}; /* JP   NC,a		  */
    opcode op_d3= () -> { int n = (ARG() | (Z80.A << 8))&0xFFFF; OUT( n, Z80.A );			}; /* OUT  (n),A 	  */
    opcode op_d4= () -> { CALL_COND( (Z80.F & CF)==0, 0xd4 );							}; /* CALL NC,a		  */
    opcode op_d5= () -> { PUSH( DE() ); 											}; /* PUSH DE		  */
    opcode op_d6= () -> { SUB(ARG()); 											}; /* SUB  n 		  */
    opcode op_d7= () -> { RST(0x10);												}; /* RST  2 		  */

    opcode op_d8= () -> { RET_COND( (Z80.F & CF)!=0, 0xd8 );								}; /* RET  C 		  */
    opcode op_d9= () -> { EXX();													}; /* EXX			  */
    opcode op_da= () -> { JP_COND( (Z80.F & CF)!=0 ); 									}; /* JP   C,a		  */
    opcode op_db= () -> { int n = (ARG() | (Z80.A << 8))&0xFFFF; Z80.A = IN( n );			}; /* IN   A,(n) 	  */
    opcode op_dc= () -> { CALL_COND( (Z80.F & CF)!=0, 0xdc ); 							}; /* CALL C,a		  */
    opcode op_dd = new opcode() {
        public void handler() {
            Z80.R = (Z80.R + 1) & 0xFF;
            int op = ROP();
            z80_ICount[0] -= cc[Z80_TABLE_xy][op];
            Z80dd[op].handler();//EXEC(dd, ROP());
        }
    };/* **** DD xx 	  */
    opcode op_de= () -> { SBC(ARG()); 											}; /* SBC  A,n		  */
    opcode op_df= () -> { RST(0x18);												}; /* RST  3 		  */

    opcode op_e0= () -> { RET_COND( (Z80.F & PF)==0, 0xe0 );							}; /* RET  PO		  */
    opcode op_e1= () -> { HL(POP());												}; /* POP  HL		  */
    opcode op_e2= () -> { JP_COND( (Z80.F & PF)==0 );									}; /* JP   PO,a		  */
    opcode op_e3= () -> { HL(EXSP(HL()));												}; /* EX   HL,(SP)	  */
    opcode op_e4= () -> { CALL_COND( (Z80.F & PF)==0, 0xe4 );							}; /* CALL PO,a		  */
    opcode op_e5= () -> { PUSH( HL() ); 											}; /* PUSH HL		  */
    opcode op_e6= () -> { AND(ARG()); 											}; /* AND  n 		  */
    opcode op_e7= () -> { RST(0x20);												}; /* RST  4 		  */

    opcode op_e8= () -> { RET_COND( (Z80.F & PF)!=0, 0xe8 );								}; /* RET  PE		  */
    opcode op_e9= () -> { Z80.PC = HL(); change_pc16(Z80.PC);							}; /* JP   (HL)		  */
    opcode op_ea= () -> { JP_COND( (Z80.F & PF)!=0 ); 									}; /* JP   PE,a		  */
    opcode op_eb= () -> { EX_DE_HL();												}; /* EX   DE,HL 	  */
    opcode op_ec= () -> { CALL_COND( (Z80.F & PF)!=0, 0xec ); 							}; /* CALL PE,a		  */
    opcode op_ed = new opcode() {
        public void handler() {
            Z80.R = (Z80.R + 1) & 0xFF;
            int op = ROP();
            z80_ICount[0] -= cc[Z80_TABLE_ed][op];
            Z80ed[op].handler();//EXEC(ed, ROP());
        }
    };/* **** ED xx 	  */
    opcode op_ee= () -> { XOR(ARG()); 											}; /* XOR  n 		  */
    opcode op_ef= () -> { RST(0x28);												}; /* RST  5 		  */

    opcode op_f0= () -> { RET_COND( (Z80.F & SF)==0, 0xf0 );							}; /* RET  P 		  */
    opcode op_f1= () -> { AF(POP());												}; /* POP  AF		  */
    opcode op_f2= () -> { JP_COND( (Z80.F & SF)==0 );									}; /* JP   P,a		  */
    opcode op_f3= () -> { Z80.IFF1 = Z80.IFF2 = 0;										}; /* DI 			  */
    opcode op_f4= () -> { CALL_COND( (Z80.F & SF)==0, 0xf4 );							}; /* CALL P,a		  */
    opcode op_f5= () -> { PUSH( AF() ); 											}; /* PUSH AF		  */
    opcode op_f6= () -> { OR(ARG());												}; /* OR   n 		  */
    opcode op_f7= () -> { RST(0x30);												}; /* RST  6 		  */

    opcode op_f8= () -> { RET_COND( (Z80.F & SF)!=0, 0xf8 );								}; /* RET  M 		  */
    opcode op_f9= () -> { Z80.SP = HL();												}; /* LD   SP,HL 	  */
    opcode op_fa= () -> { JP_COND((Z80.F & SF)!=0);										}; /* JP   M,a		  */
    opcode op_fb= () -> { EI(); 													}; /* EI 			  */
    opcode op_fc= () -> { CALL_COND( (Z80.F & SF)!=0, 0xfc ); 							}; /* CALL M,a		  */
    opcode op_fd = new opcode() {
        public void handler() {
            Z80.R = (Z80.R + 1) & 0xFF;
            int op = ROP();
            z80_ICount[0] -= cc[Z80_TABLE_xy][op];
            Z80fd[op].handler();//EXEC(fd, ROP());
        }
    };/* **** FD xx 	  */
    opcode op_fe= () -> { CP(ARG());												}; /* CP   n 		  */
    opcode op_ff= () -> { RST(0x38);												}; /* RST  7 		  */

    static void take_interrupt()
    {
       if( Z80.IFF1!=0 )
       {
           
            int irq_vector;

            /* there isn't a valid previous program counter */
            Z80.PREPC = -1;

            /* Check if processor was halted */
            LEAVE_HALT();

/*TODO*///        if( Z80.irq_max!=0 )           /* daisy chain mode */
/*TODO*///        {
/*TODO*///            if( Z80.request_irq >= 0 )
/*TODO*///            {
/*TODO*///                /* Clear both interrupt flip flops */
/*TODO*///                Z80.IFF1 = Z80.IFF2 = 0;
/*TODO*///                irq_vector = Z80.irq[Z80.request_irq].interrupt_entry(Z80.irq[Z80.request_irq].irq_param);
/*TODO*///				LOG(("Z80 #%d daisy chain irq_vector $%02x\n", cpu_getactivecpu(), irq_vector));
/*TODO*///                Z80.request_irq = -1;
/*TODO*///            } else return;
/*TODO*///        }
/*TODO*///        else
/*TODO*///        {
            /* Clear both interrupt flip flops */
            Z80.IFF1 = Z80.IFF2 = 0;
            /* call back the cpu interface to retrieve the vector */
            irq_vector = (Z80.irq_callback).handler(0);
			//LOG(("Z80 #%d single int. irq_vector $%02x\n", cpu_getactivecpu(), irq_vector));
/*TODO*///        }

        /* Interrupt mode 2. Call [Z80.I:databyte] */
        if( Z80.IM == 2 )
        {
            irq_vector = (irq_vector & 0xff) | (Z80.I << 8);
            PUSH( Z80.PC );
	    Z80.PC = RM16( irq_vector);
            //LOG(("Z80 #%d IM2 [$%04x] = $%04x\n",cpu_getactivecpu() , irq_vector, _PCD));
            /* CALL opcode timing */
            Z80.extra_cycles += cc[Z80_TABLE_op][0xcd];
        }
        else
        /* Interrupt mode 1. RST 38h */
        if( Z80.IM == 1 )
        {
	    //LOG(("Z80 #%d IM1 $0038\n",cpu_getactivecpu() ));
            PUSH( Z80.PC );
            Z80.PC = 0x0038;
            /* RST $38 + 'interrupt latency' cycles */
            Z80.extra_cycles += cc[Z80_TABLE_op][0xff] + cc[Z80_TABLE_ex][0xff];
        }
        else
        {
            throw new UnsupportedOperationException("Not supported yet.");
            /* Interrupt mode 0. We check for CALL and JP instructions, */
/*TODO*///            /* if neither of these were found we assume a 1 byte opcode */
/*TODO*///            /* was placed on the databus                                */
/*TODO*///			LOG(("Z80 #%d IM0 $%04x\n",cpu_getactivecpu() , irq_vector));
/*TODO*///            switch (irq_vector & 0xff0000)
/*TODO*///            {
/*TODO*///                case 0xcd0000:  /* call */
/*TODO*///                    PUSH( PC );
/*TODO*///					_PCD = irq_vector & 0xffff;
/*TODO*///					 /* CALL $xxxx + 'interrupt latency' cycles */
/*TODO*///					Z80.extra_cycles += cc[Z80_TABLE_op][0xcd] + cc[Z80_TABLE_ex][0xff];
/*TODO*///                    break;
/*TODO*///                case 0xc30000:  /* jump */
/*TODO*///                    _PCD = irq_vector & 0xffff;
/*TODO*///					/* JP $xxxx + 2 cycles */
/*TODO*///					Z80.extra_cycles += cc[Z80_TABLE_op][0xc3] + cc[Z80_TABLE_ex][0xff];
/*TODO*///                    break;
/*TODO*///				default:		/* rst (or other opcodes?) */
/*TODO*///                    PUSH( PC );
/*TODO*///                    _PCD = irq_vector & 0x0038;
/*TODO*///					/* RST $xx + 2 cycles */
/*TODO*///					Z80.extra_cycles += cc[Z80_TABLE_op][_PCD] + cc[Z80_TABLE_ex][_PCD];
/*TODO*///                    break;
/*TODO*///            }
        }
            change_pc(Z80.PC);
        }
    }

    /****************************************************************************
     * Reset registers to their initial values
     ****************************************************************************/
    @Override
    public void reset(Object param) {
/*TODO*///	Z80_DaisyChain *daisy_chain = (Z80_DaisyChain *)param;
    	int i, p;
        int oldval, newval, val;
        int padd, padc, psub, psbc;
        padd = 0 * 256;
        padc = 256 * 256;
        psub = 0 * 256;
        psbc = 256 * 256;
        for (oldval = 0; oldval < 256; oldval++) {
            for (newval = 0; newval < 256; newval++) {
                /* add or adc w/o carry set */
                val = newval - oldval;

                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_Add[padd] = SF;
                    } else {
                        SZHVC_Add[padd] = 0;
                    }
                } else {
                    SZHVC_Add[padd] = ZF;
                }

                SZHVC_Add[padd] |= (newval & (YF | XF));/* undocumented flag bits 5+3 */

                if ((newval & 0x0f) < (oldval & 0x0f)) {
                    SZHVC_Add[padd] |= HF;
                }
                if (newval < oldval) {
                    SZHVC_Add[padd] |= CF;
                }
                if (((val ^ oldval ^ 0x80) & (val ^ newval) & 0x80) != 0) {
                    SZHVC_Add[padd] |= VF;
                }
                padd++;

                /* adc with carry set */
                val = newval - oldval - 1;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_Add[padc] = SF;
                    } else {
                        SZHVC_Add[padc] = 0;
                    }
                } else {
                    SZHVC_Add[padc] = ZF;
                }

                SZHVC_Add[padc] |= (newval & (YF | XF));/* undocumented flag bits 5+3 */

                if ((newval & 0x0f) <= (oldval & 0x0f)) {
                    SZHVC_Add[padc] |= HF;
                }
                if (newval <= oldval) {
                    SZHVC_Add[padc] |= CF;
                }
                if (((val ^ oldval ^ 0x80) & (val ^ newval) & 0x80) != 0) {
                    SZHVC_Add[padc] |= VF;
                }
                padc++;

                /* cp, sub or sbc w/o carry set */
                val = oldval - newval;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_sub[psub] = NF | SF;
                    } else {
                        SZHVC_sub[psub] = NF;
                    }
                } else {
                    SZHVC_sub[psub] = NF | ZF;
                }

                SZHVC_sub[psub] |= (newval & (YF | XF));/* undocumented flag bits 5+3 */
                
                if ((newval & 0x0f) > (oldval & 0x0f)) {
                    SZHVC_sub[psub] |= HF;
                }
                if (newval > oldval) {
                    SZHVC_sub[psub] |= CF;
                }
                if (((val ^ oldval) & (oldval ^ newval) & 0x80) != 0) {
                    SZHVC_sub[psub] |= VF;
                }
                psub++;

                /* sbc with carry set */
                val = oldval - newval - 1;
                if (newval != 0) {
                    if ((newval & 0x80) != 0) {
                        SZHVC_sub[psbc] = NF | SF;
                    } else {
                        SZHVC_sub[psbc] = NF;
                    }
                } else {
                    SZHVC_sub[psbc] = NF | ZF;
                }

                SZHVC_sub[psbc] |= (newval & (YF | XF));/* undocumented flag bits 5+3 */
                
                if ((newval & 0x0f) >= (oldval & 0x0f)) {
                    SZHVC_sub[psbc] |= HF;
                }
                if (newval >= oldval) {
                    SZHVC_sub[psbc] |= CF;
                }
                if (((val ^ oldval) & (oldval ^ newval) & 0x80) != 0) {
                    SZHVC_sub[psbc] |= VF;
                }
                psbc++;
            }
        }
        for (i = 0; i < 256; i++) {
            p = 0;
            if ((i & 0x01) != 0) {
                ++p;
            }
            if ((i & 0x02) != 0) {
                ++p;
            }
            if ((i & 0x04) != 0) {
                ++p;
            }
            if ((i & 0x08) != 0) {
                ++p;
            }
            if ((i & 0x10) != 0) {
                ++p;
            }
            if ((i & 0x20) != 0) {
                ++p;
            }
            if ((i & 0x40) != 0) {
                ++p;
            }
            if ((i & 0x80) != 0) {
                ++p;
            }
            SZ[i] = (i != 0) ? i & 0x80 : 0x40;
            SZ[i] |= (i & (0x20 | 0x08));/* undocumented flag bits 5+3 */
            
            SZ_BIT[i] = (i != 0) ? i & 0x80 : 0x40 | 0x04;
            SZ_BIT[i] |= (i & (0x20 | 0x08));/* undocumented flag bits 5+3 */
            
            SZP[i] = SZ[i] | (((p & 1) != 0) ? 0 : 0x04);
            SZHV_inc[i] = SZ[i];
            if (i == 0x80) {
                SZHV_inc[i] |= 0x04;
            }
            if ((i & 0x0f) == 0x00) {
                SZHV_inc[i] |= 0x10;
            }
            SZHV_dec[i] = SZ[i] | 0x02;
            if (i == 0x7f) {
                SZHV_dec[i] |= 0x04;
            }
            if ((i & 0x0f) == 0x0f) {
                SZHV_dec[i] |= 0x10;
            }
        }

        //memset(&Z80, 0, sizeof(Z80));
	Z80.IX = Z80.IY = 0xffff; /* IX and IY are FFFF after a reset! */
	Z80.F = ZF;			/* Zero flag is set */
	Z80.request_irq = -1;
	Z80.service_irq = -1;
        Z80.nmi_state = CLEAR_LINE;
	Z80.irq_state = CLEAR_LINE;

/*TODO*///    if( daisy_chain )
/*TODO*///	{
/*TODO*///		while( daisy_chain->irq_param != -1 && Z80.irq_max < Z80_MAXDAISY )
/*TODO*///		{
/*TODO*///            /* set callbackhandler after reti */
/*TODO*///			Z80.irq[Z80.irq_max] = *daisy_chain;
/*TODO*///            /* device reset */
/*TODO*///			if( Z80.irq[Z80.irq_max].reset )
/*TODO*///				Z80.irq[Z80.irq_max].reset(Z80.irq[Z80.irq_max].irq_param);
/*TODO*///			Z80.irq_max++;
/*TODO*///            daisy_chain++;
/*TODO*///        }
/*TODO*///    }

        change_pc(Z80.PC);
    }

    @Override
    public void exit() {
        SZHVC_Add = null;
        SZHVC_sub = null;
    }

    /****************************************************************************
     * Execute 'cycles' T-states. Return number of T-states really executed
     ****************************************************************************/
    @Override
    public int execute(int cycles) {
        z80_ICount[0] = cycles - Z80.extra_cycles;
        Z80.extra_cycles = 0;

        do {
            Z80.PREPC = Z80.PC & 0xFFFF;
            Z80.R = (Z80.R + 1) & 0xFF;//_R++;
            int op = ROP();
            z80_ICount[0] -= cc[Z80_TABLE_op][op];
            Z80op[op].handler();//EXEC_INLINE(op, ROP());
        } while (z80_ICount[0] > 0);

        z80_ICount[0] -= Z80.extra_cycles;
        Z80.extra_cycles = 0;

        return cycles - z80_ICount[0];
    }

    /****************************************************************************
     * Burn 'cycles' T-states. Adjust R register for the lost time
     ****************************************************************************/
    public burnPtr burn_function = (int cycles) -> {
        if( cycles > 0 )
	{
		/* NOP takes 4 cycles per instruction */
		int n = (cycles + 3) / 4;
		Z80.R = (Z80.R + n)&0xFF;
		z80_ICount[0] -= 4 * n;
	}
    };

    /****************************************************************************
     * Get all registers in given buffer
     ****************************************************************************/
    @Override
    public Object get_context() {
        Z80_Regs Regs = new Z80_Regs();
        Regs.PREPC=Z80.PREPC;
        Regs.PC=Z80.PC;
        Regs.SP=Z80.SP;

        Regs.A=Z80.A;
        Regs.F=Z80.F;
        Regs.B=Z80.B;
        Regs.C=Z80.C;
        Regs.D=Z80.D;
        Regs.E=Z80.E;
        Regs.H=Z80.H;
        Regs.L=Z80.L;
        Regs.IX=Z80.IX;
        Regs.IY=Z80.IY;
        Regs.A2=Z80.A2;
        Regs.F2=Z80.F2;
        Regs.B2=Z80.B2;
        Regs.C2=Z80.C2;
        Regs.D2=Z80.D2;
        Regs.E2=Z80.E2;
        Regs.H2=Z80.H2;
        Regs.L2=Z80.L2;

        Regs.R = Z80.R;
        Regs.R2 = Z80.R2;
        Regs.IFF1 = Z80.IFF1;
        Regs.IFF2 = Z80.IFF2;
        Regs.HALT = Z80.HALT;
        Regs.IM = Z80.IM;
        Regs.I = Z80.I;
        Regs.irq_max = Z80.irq_max;
        Regs.request_irq = Z80.request_irq;
        Regs.service_irq = Z80.service_irq;
        Regs.nmi_state = Z80.nmi_state;
        Regs.irq_state = Z80.irq_state;
        // public int /*UNIT8*/ int_state[] = new int[Z80_MAXDAISY];  //DAISY CHAIN TODO!!
        // public Z80_DaisyChain[] irq = new Z80_DaisyChain[Z80_MAXDAISY];
        Regs.irq_callback = Z80.irq_callback;
        Regs.extra_cycles = Z80.extra_cycles;
        return Regs;
    }

    /****************************************************************************
     * Set all registers to given values
     ****************************************************************************/
    @Override
    public void set_context(Object reg) {
        Z80_Regs Regs = (Z80_Regs)reg;
        Z80.PREPC=Regs.PREPC;
        Z80.PC=Regs.PC;
        Z80.SP=Regs.SP;
        
        Z80.A=Regs.A;
        Z80.F=Regs.F;
        Z80.B=Regs.B;
        Z80.C=Regs.C;
        Z80.D=Regs.D;
        Z80.E=Regs.E;
        Z80.H=Regs.H;
        Z80.L=Regs.L;
        Z80.IX=Regs.IX;
        Z80.IY=Regs.IY;
        Z80.A2=Regs.A2;
        Z80.F2=Regs.F2;
        Z80.B2=Regs.B2;
        Z80.C2=Regs.C2;
        Z80.D2=Regs.D2;
        Z80.E2=Regs.E2;
        Z80.H2=Regs.H2;
        Z80.L2=Regs.L2;

        Z80.R = Regs.R;
        Z80.R2 = Regs.R2;
        Z80.IFF1 = Regs.IFF1;
        Z80.IFF2 = Regs.IFF2;
        Z80.HALT = Regs.HALT;
        Z80.IM = Regs.IM;
        Z80.I = Regs.I;
        Z80.irq_max = Regs.irq_max;
        Z80.request_irq = Regs.request_irq;
        Z80.service_irq = Regs.service_irq;
        Z80.nmi_state = Regs.nmi_state;
        Z80.irq_state = Regs.irq_state;
        // public int /*UNIT8*/ int_state[] = new int[Z80_MAXDAISY];//DAISY CHAIN TODO!!
        // public Z80_DaisyChain[] irq = new Z80_DaisyChain[Z80_MAXDAISY];
        Z80.irq_callback = Regs.irq_callback;
        Z80.extra_cycles = Regs.extra_cycles;
        change_pc(Z80.PC);
    }
    
    /****************************************************************************
     * Get a pointer to a cycle count table
     ****************************************************************************/
    @Override
    public int[] get_cycle_table(int which) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	if (which >= 0 && which <= Z80_TABLE_xycb)
/*TODO*///		return cc[which];
/*TODO*///	return NULL;
    }

    /****************************************************************************
     * Set a new cycle count table
     ****************************************************************************/
    @Override
    public void set_cycle_table(int which, int[] new_table) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	if (which >= 0 && which <= Z80_TABLE_ex)
/*TODO*///		cc[which] = new_table;
    }
    
    /****************************************************************************
     * Return program counter
     ****************************************************************************/
    @Override
    public int get_pc() {
        return Z80.PC & 0xFFFF;
    }

    /****************************************************************************
     * Set program counter
     ****************************************************************************/
    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	_PC = val;
/*TODO*///	change_pc(_PCD);
    }

    /****************************************************************************
     * Return stack pointer
     ****************************************************************************/
    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	return _SPD;
    }

    /****************************************************************************
     * Set stack pointer
     ****************************************************************************/
    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	_SP = val;
    }

    /****************************************************************************
     * Return a specific register
     ****************************************************************************/
    @Override
    public int get_reg(int regnum) {
	switch( regnum )
	{
/*TODO*///		case Z80_PC: return Z80.PC.w.l;
/*TODO*///		case Z80_SP: return Z80.SP.w.l;
/*TODO*///		case Z80_AF: return Z80.AF.w.l;
    		case Z80_BC: return BC();
/*TODO*///		case Z80_DE: return Z80.DE.w.l;
/*TODO*///		case Z80_HL: return Z80.HL.w.l;
/*TODO*///		case Z80_IX: return Z80.IX.w.l;
/*TODO*///		case Z80_IY: return Z80.IY.w.l;
/*TODO*///        case Z80_R: return (Z80.R & 0x7f) | (Z80.R2 & 0x80);
/*TODO*///		case Z80_I: return Z80.I;
/*TODO*///		case Z80_AF2: return Z80.AF2.w.l;
/*TODO*///		case Z80_BC2: return Z80.BC2.w.l;
/*TODO*///		case Z80_DE2: return Z80.DE2.w.l;
/*TODO*///		case Z80_HL2: return Z80.HL2.w.l;
    		case Z80_IM: return Z80.IM;
/*TODO*///		case Z80_IFF1: return Z80.IFF1;
/*TODO*///		case Z80_IFF2: return Z80.IFF2;
/*TODO*///		case Z80_HALT: return Z80.HALT;
/*TODO*///		case Z80_NMI_STATE: return Z80.nmi_state;
/*TODO*///		case Z80_IRQ_STATE: return Z80.irq_state;
/*TODO*///		case Z80_DC0: return Z80.int_state[0];
/*TODO*///		case Z80_DC1: return Z80.int_state[1];
/*TODO*///		case Z80_DC2: return Z80.int_state[2];
/*TODO*///		case Z80_DC3: return Z80.int_state[3];
                 case REG_PREVIOUSPC: return Z80.PREPC;
		default:
			if( regnum <= REG_SP_CONTENTS )
			{
				int/*unsigned*/ offset = (Z80.SP + 2 * (REG_SP_CONTENTS - regnum))&0xFFFF;
				if( offset < 0xffff )
					return RM( offset ) | ( RM( offset + 1) << 8 );
			}
    	}
/*TODO*///    return 0;
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /****************************************************************************
     * Set a specific register
     ****************************************************************************/
    @Override
    public void set_reg(int regnum, int val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	switch( regnum )
/*TODO*///	{
/*TODO*///		case Z80_PC: Z80.PC.w.l = val; break;
/*TODO*///		case Z80_SP: Z80.SP.w.l = val; break;
/*TODO*///		case Z80_AF: Z80.AF.w.l = val; break;
/*TODO*///		case Z80_BC: Z80.BC.w.l = val; break;
/*TODO*///		case Z80_DE: Z80.DE.w.l = val; break;
/*TODO*///		case Z80_HL: Z80.HL.w.l = val; break;
/*TODO*///		case Z80_IX: Z80.IX.w.l = val; break;
/*TODO*///		case Z80_IY: Z80.IY.w.l = val; break;
/*TODO*///        case Z80_R: Z80.R = val; Z80.R2 = val & 0x80; break;
/*TODO*///		case Z80_I: Z80.I = val; break;
/*TODO*///		case Z80_AF2: Z80.AF2.w.l = val; break;
/*TODO*///		case Z80_BC2: Z80.BC2.w.l = val; break;
/*TODO*///		case Z80_DE2: Z80.DE2.w.l = val; break;
/*TODO*///		case Z80_HL2: Z80.HL2.w.l = val; break;
/*TODO*///		case Z80_IM: Z80.IM = val; break;
/*TODO*///		case Z80_IFF1: Z80.IFF1 = val; break;
/*TODO*///		case Z80_IFF2: Z80.IFF2 = val; break;
/*TODO*///		case Z80_HALT: Z80.HALT = val; break;
/*TODO*///		case Z80_NMI_STATE: z80_set_nmi_line(val); break;
/*TODO*///		case Z80_IRQ_STATE: z80_set_irq_line(0,val); break;
/*TODO*///		case Z80_DC0: Z80.int_state[0] = val; break;
/*TODO*///		case Z80_DC1: Z80.int_state[1] = val; break;
/*TODO*///		case Z80_DC2: Z80.int_state[2] = val; break;
/*TODO*///		case Z80_DC3: Z80.int_state[3] = val; break;
/*TODO*///        default:
/*TODO*///			if( regnum <= REG_SP_CONTENTS )
/*TODO*///			{
/*TODO*///				unsigned offset = _SPD + 2 * (REG_SP_CONTENTS - regnum);
/*TODO*///				if( offset < 0xffff )
/*TODO*///				{
/*TODO*///					WM( offset, val & 0xff );
/*TODO*///					WM( offset+1, (val >> 8) & 0xff );
/*TODO*///				}
/*TODO*///			}
/*TODO*///    }
    }

    /****************************************************************************
     * Set NMI line state
     ****************************************************************************/
    @Override
    public void set_nmi_line(int state) {
        if( Z80.nmi_state == state ) return;

	//LOG(("Z80 #%d set_nmi_line %d\n", cpu_getactivecpu(), state));
        Z80.nmi_state = state;
	if( state == CLEAR_LINE ) return;

	//LOG(("Z80 #%d take NMI\n", cpu_getactivecpu()));
	Z80.PREPC = -1;			/* there isn't a valid previous program counter */
	LEAVE_HALT(); 		/* Check if processor was halted */

	Z80.IFF1 = 0;
        PUSH( Z80.PC );
	Z80.PC = 0x0066;
	Z80.extra_cycles += 11;
    }

    /****************************************************************************
     * Set IRQ line state
     ****************************************************************************/
    @Override
    public void set_irq_line(int irqline, int state) {
        //LOG(("Z80 #%d set_irq_line %d\n",cpu_getactivecpu() , state));
        Z80.irq_state = state;
        if( state == CLEAR_LINE ) return;

	if( Z80.irq_max!=0 )
	{
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///		int daisychain, device, int_state;
/*TODO*///		daisychain = (*Z80.irq_callback)(irqline);
/*TODO*///		device = daisychain >> 8;
/*TODO*///		int_state = daisychain & 0xff;
/*TODO*///		LOG(("Z80 #%d daisy chain $%04x -> device %d, state $%02x",cpu_getactivecpu(), daisychain, device, int_state));
/*TODO*///
/*TODO*///		if( Z80.int_state[device] != int_state )
/*TODO*///		{
/*TODO*///			LOG((" change\n"));
/*TODO*///			/* set new interrupt status */
/*TODO*///            Z80.int_state[device] = int_state;
/*TODO*///			/* check interrupt status */
/*TODO*///			Z80.request_irq = Z80.service_irq = -1;
/*TODO*///
/*TODO*///            /* search higher IRQ or IEO */
/*TODO*///			for( device = 0 ; device < Z80.irq_max ; device ++ )
/*TODO*///			{
/*TODO*///				/* IEO = disable ? */
/*TODO*///				if( Z80.int_state[device] & Z80_INT_IEO )
/*TODO*///				{
/*TODO*///					Z80.request_irq = -1;		/* if IEO is disable , masking lower IRQ */
/*TODO*///					Z80.service_irq = device;	/* set highest interrupt service device */
/*TODO*///				}
/*TODO*///				/* IRQ = request ? */
/*TODO*///				if( Z80.int_state[device] & Z80_INT_REQ )
/*TODO*///					Z80.request_irq = device;
/*TODO*///			}
/*TODO*///			LOG(("Z80 #%d daisy chain service_irq $%02x, request_irq $%02x\n", cpu_getactivecpu(), Z80.service_irq, Z80.request_irq));
/*TODO*///			if( Z80.request_irq < 0 ) return;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			LOG((" no change\n"));
/*TODO*///			return;
/*TODO*///		}
        }
	take_interrupt();
    }

    /****************************************************************************
     * Set IRQ vector callback
     ****************************************************************************/
    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        //LOG(("Z80 #%d set_irq_callback $%08x\n",cpu_getactivecpu() , (int)callback));
        Z80.irq_callback = callback;
    }

    /****************************************************************************
     * Save CPU state
     ****************************************************************************/
    @Override
    public void cpu_state_save(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_save_UINT16(file, "z80", cpu, "AF", &Z80.AF.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "BC", &Z80.BC.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "DE", &Z80.DE.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "HL", &Z80.HL.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "IX", &Z80.IX.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "IY", &Z80.IY.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "PC", &Z80.PC.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "SP", &Z80.SP.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "AF2", &Z80.AF2.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "BC2", &Z80.BC2.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "DE2", &Z80.DE2.w.l, 1);
/*TODO*///	state_save_UINT16(file, "z80", cpu, "HL2", &Z80.HL2.w.l, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "R", &Z80.R, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "R2", &Z80.R2, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "IFF1", &Z80.IFF1, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "IFF2", &Z80.IFF2, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "HALT", &Z80.HALT, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "IM", &Z80.IM, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "I", &Z80.I, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "irq_max", &Z80.irq_max, 1);
/*TODO*///	state_save_INT8(file, "z80", cpu, "request_irq", &Z80.request_irq, 1);
/*TODO*///	state_save_INT8(file, "z80", cpu, "service_irq", &Z80.service_irq, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "int_state", Z80.int_state, 4);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "nmi_state", &Z80.nmi_state, 1);
/*TODO*///	state_save_UINT8(file, "z80", cpu, "irq_state", &Z80.irq_state, 1);
/*TODO*///	/* daisy chain needs to be saved by z80ctc.c somehow */
    }

    /****************************************************************************
     * Load CPU state
     ****************************************************************************/
    @Override
    public void cpu_state_load(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
/*TODO*///	int cpu = cpu_getactivecpu();
/*TODO*///	state_load_UINT16(file, "z80", cpu, "AF", &Z80.AF.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "BC", &Z80.BC.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "DE", &Z80.DE.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "HL", &Z80.HL.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "IX", &Z80.IX.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "IY", &Z80.IY.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "PC", &Z80.PC.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "SP", &Z80.SP.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "AF2", &Z80.AF2.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "BC2", &Z80.BC2.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "DE2", &Z80.DE2.w.l, 1);
/*TODO*///	state_load_UINT16(file, "z80", cpu, "HL2", &Z80.HL2.w.l, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "R", &Z80.R, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "R2", &Z80.R2, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "IFF1", &Z80.IFF1, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "IFF2", &Z80.IFF2, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "HALT", &Z80.HALT, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "IM", &Z80.IM, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "I", &Z80.I, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "irq_max", &Z80.irq_max, 1);
/*TODO*///	state_load_INT8(file, "z80", cpu, "request_irq", &Z80.request_irq, 1);
/*TODO*///	state_load_INT8(file, "z80", cpu, "service_irq", &Z80.service_irq, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "int_state", Z80.int_state, 4);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "nmi_state", &Z80.nmi_state, 1);
/*TODO*///	state_load_UINT8(file, "z80", cpu, "irq_state", &Z80.irq_state, 1);
/*TODO*///    /* daisy chain needs to be restored by z80ctc.c somehow */
    }

    /****************************************************************************
     * Return a formatted string for a register
     ****************************************************************************/
    @Override
    public String cpu_info(Object context, int regnum) {
         
/*TODO*///const char *z80_info(void *context, int regnum)
/*TODO*///{
/*TODO*///	static char buffer[32][47+1];
/*TODO*///	static int which = 0;
/*TODO*///	Z80_Regs *r = context;
/*TODO*///
/*TODO*///	which = ++which % 32;
/*TODO*///    buffer[which][0] = '\0';
/*TODO*///	if( !context )
/*TODO*///		r = &Z80;
/*TODO*///
            switch( regnum )
            {
/*TODO*///		case CPU_INFO_REG+Z80_PC: sprintf(buffer[which], "PC:%04X", r->PC.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_SP: sprintf(buffer[which], "SP:%04X", r->SP.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_AF: sprintf(buffer[which], "AF:%04X", r->AF.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_BC: sprintf(buffer[which], "BC:%04X", r->BC.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_DE: sprintf(buffer[which], "DE:%04X", r->DE.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_HL: sprintf(buffer[which], "HL:%04X", r->HL.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_IX: sprintf(buffer[which], "IX:%04X", r->IX.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_IY: sprintf(buffer[which], "IY:%04X", r->IY.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_R: sprintf(buffer[which], "R:%02X", (r->R & 0x7f) | (r->R2 & 0x80)); break;
/*TODO*///		case CPU_INFO_REG+Z80_I: sprintf(buffer[which], "I:%02X", r->I); break;
/*TODO*///		case CPU_INFO_REG+Z80_AF2: sprintf(buffer[which], "AF'%04X", r->AF2.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_BC2: sprintf(buffer[which], "BC'%04X", r->BC2.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_DE2: sprintf(buffer[which], "DE'%04X", r->DE2.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_HL2: sprintf(buffer[which], "HL'%04X", r->HL2.w.l); break;
/*TODO*///		case CPU_INFO_REG+Z80_IM: sprintf(buffer[which], "IM:%X", r->IM); break;
/*TODO*///		case CPU_INFO_REG+Z80_IFF1: sprintf(buffer[which], "IFF1:%X", r->IFF1); break;
/*TODO*///		case CPU_INFO_REG+Z80_IFF2: sprintf(buffer[which], "IFF2:%X", r->IFF2); break;
/*TODO*///		case CPU_INFO_REG+Z80_HALT: sprintf(buffer[which], "HALT:%X", r->HALT); break;
/*TODO*///		case CPU_INFO_REG+Z80_NMI_STATE: sprintf(buffer[which], "NMI:%X", r->nmi_state); break;
/*TODO*///		case CPU_INFO_REG+Z80_IRQ_STATE: sprintf(buffer[which], "IRQ:%X", r->irq_state); break;
/*TODO*///		case CPU_INFO_REG+Z80_DC0: if(Z80.irq_max >= 1) sprintf(buffer[which], "DC0:%X", r->int_state[0]); break;
/*TODO*///		case CPU_INFO_REG+Z80_DC1: if(Z80.irq_max >= 2) sprintf(buffer[which], "DC1:%X", r->int_state[1]); break;
/*TODO*///		case CPU_INFO_REG+Z80_DC2: if(Z80.irq_max >= 3) sprintf(buffer[which], "DC2:%X", r->int_state[2]); break;
/*TODO*///		case CPU_INFO_REG+Z80_DC3: if(Z80.irq_max >= 4) sprintf(buffer[which], "DC3:%X", r->int_state[3]); break;
/*TODO*///        case CPU_INFO_FLAGS:
/*TODO*///			sprintf(buffer[which], "%c%c%c%c%c%c%c%c",
/*TODO*///				r->AF.b.l & 0x80 ? 'S':'.',
/*TODO*///				r->AF.b.l & 0x40 ? 'Z':'.',
/*TODO*///				r->AF.b.l & 0x20 ? '5':'.',
/*TODO*///				r->AF.b.l & 0x10 ? 'H':'.',
/*TODO*///				r->AF.b.l & 0x08 ? '3':'.',
/*TODO*///				r->AF.b.l & 0x04 ? 'P':'.',
/*TODO*///				r->AF.b.l & 0x02 ? 'N':'.',
/*TODO*///				r->AF.b.l & 0x01 ? 'C':'.');
/*TODO*///			break;
		case CPU_INFO_NAME: return "Z80";
                case CPU_INFO_FAMILY: return "Zilog Z80";
		case CPU_INFO_VERSION: return "3.1";
		case CPU_INFO_FILE: return "z80.java";
		case CPU_INFO_CREDITS: return "Copyright (C) 1998,1999 Juergen Buchmueller, all rights reserved.";
/*TODO*///		case CPU_INFO_REG_LAYOUT: return (const char *)z80_reg_layout;
/*TODO*///		case CPU_INFO_WIN_LAYOUT: return (const char *)z80_win_layout;
            }
/*TODO*///	return buffer[which];
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    opcode cb_30 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_31 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_32 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_33 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_34 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_35 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_36 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode cb_37 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_00 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_01 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_02 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_03 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_04 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_05 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_06 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_07 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_08 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_09 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_0a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};  
    opcode xycb_0b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_0c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_0d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_0e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_0f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_10 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_11 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_12 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_13 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_14 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_15 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_17 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_18 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_19 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_1a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_1b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_1c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_1d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_1e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_1f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_20 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_21 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_22 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_23 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_24 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_25 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_26 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_27 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_28 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_29 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_2a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_2b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_2c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_2d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_2e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_2f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_30 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_31 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_32 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_33 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_34 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_35 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_36 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_37 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_80 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_81 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_82 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_83 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_84 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_85 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_87 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_88 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_89 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_8a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_8b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_8c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_8d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_8f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_90 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_91 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_92 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_93 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_94 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_95 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_97 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_98 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_99 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_9a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_9b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_9c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_9d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_9f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_a0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_a1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_a2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_a3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_a4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_a5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_a7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_aa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ac = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ad = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_af = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_b0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_b1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_b2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_b3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_b4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_b5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_b7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_b8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_b9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ba = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_bb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_bc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_bd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_bf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_c0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_c1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_c2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_c3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_c4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_c5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_c7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_c8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_c9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ca = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_cb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_cc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_cd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_cf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_d0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_d1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_d2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_d3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_d4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_d5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_d7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_d8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_d9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_da = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_db = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_dc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_dd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}}; 
    opcode xycb_df = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_e0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_e1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_e2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_e3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_e4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_e5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_e7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_e8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_e9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ea = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_eb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ec = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ed = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ef = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_f0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_f1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_f2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_f3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_f4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_f5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_f7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_f8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_f9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_fa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_fb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_fc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_fd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode xycb_ff = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_24 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_25 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_26 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_2c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_2d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_2e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_39 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_3f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_40 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_41 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_42 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_43 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_44 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_45 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_47 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_48 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_49 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_4f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_50 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_51 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_52 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_53 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_54 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_55 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_57 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_58 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_59 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_5f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_60 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_61 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_62 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_63 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_64 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_65 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_67 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_68 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_69 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_6f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_76 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_78 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_79 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_7f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_80 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_81 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_82 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_83 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_84 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_85 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_87 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_88 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_89 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_8f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_90 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_91 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_92 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_93 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_94 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_95 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_97 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_98 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_99 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_9f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_aa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ac = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ad = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_af = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_b9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ba = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_bb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_bc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_bd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_bf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_c9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ca = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_cc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_cd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ce = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_cf = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_d9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_da = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_db = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_dc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_dd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}}; 
    opcode dd_de = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_df = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_e8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ea = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_eb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ec = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ed = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ee = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ef = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_f9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_fe = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode dd_ff = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};    
    opcode fd_00 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_01 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_02 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_03 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_04 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_05 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_06 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_07 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_08 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};  
    opcode fd_0b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_0f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_10 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_11 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_12 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_13 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_14 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_15 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_16 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_17 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_18 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_1f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_20 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_24 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_25 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_26 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_27 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_28 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_2f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_30 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_31 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_32 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_33 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_37 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_38 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_39 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3e = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_3f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_40 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_41 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_42 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_43 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_44 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_45 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_47 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_48 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_49 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_4f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_50 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_51 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_52 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_53 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_54 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_55 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_57 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_58 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_59 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_5f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_60 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_61 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_62 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_63 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_64 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_65 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_67 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_68 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_69 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_6f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_76 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_78 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_79 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_7f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_80 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_81 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_82 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_83 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_84 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_85 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_87 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_88 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_89 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_8f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_90 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_91 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_92 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_93 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_94 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_95 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_97 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_98 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_99 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9a = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9b = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9c = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9d = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_9f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_aa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ac = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ad = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_af = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_b9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ba = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_bb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_bc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_bd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_e8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ea = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_eb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ec = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ed = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ee = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ef = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f0 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f1 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f4 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f5 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f6 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f7 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_f9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fa = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fc = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fd = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_fe = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode fd_ff = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_48 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_4f = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_68 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_70 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a8 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_a9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ab = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b2 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b3 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_b9 = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_ba = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
    opcode ed_bb = new opcode() { public void handler(){ throw new UnsupportedOperationException("unimplemented");}};
   
    public abstract interface opcode {

        public abstract void handler();
    }
    
    opcode[] Z80op = {
        op_00, op_01, op_02, op_03, op_04, op_05, op_06, op_07,
        op_08, op_09, op_0a, op_0b, op_0c, op_0d, op_0e, op_0f,
        op_10, op_11, op_12, op_13, op_14, op_15, op_16, op_17,
        op_18, op_19, op_1a, op_1b, op_1c, op_1d, op_1e, op_1f,
        op_20, op_21, op_22, op_23, op_24, op_25, op_26, op_27,
        op_28, op_29, op_2a, op_2b, op_2c, op_2d, op_2e, op_2f,
        op_30, op_31, op_32, op_33, op_34, op_35, op_36, op_37,
        op_38, op_39, op_3a, op_3b, op_3c, op_3d, op_3e, op_3f,
        op_40, op_41, op_42, op_43, op_44, op_45, op_46, op_47,
        op_48, op_49, op_4a, op_4b, op_4c, op_4d, op_4e, op_4f,
        op_50, op_51, op_52, op_53, op_54, op_55, op_56, op_57,
        op_58, op_59, op_5a, op_5b, op_5c, op_5d, op_5e, op_5f,
        op_60, op_61, op_62, op_63, op_64, op_65, op_66, op_67,
        op_68, op_69, op_6a, op_6b, op_6c, op_6d, op_6e, op_6f,
        op_70, op_71, op_72, op_73, op_74, op_75, op_76, op_77,
        op_78, op_79, op_7a, op_7b, op_7c, op_7d, op_7e, op_7f,
        op_80, op_81, op_82, op_83, op_84, op_85, op_86, op_87,
        op_88, op_89, op_8a, op_8b, op_8c, op_8d, op_8e, op_8f,
        op_90, op_91, op_92, op_93, op_94, op_95, op_96, op_97,
        op_98, op_99, op_9a, op_9b, op_9c, op_9d, op_9e, op_9f,
        op_a0, op_a1, op_a2, op_a3, op_a4, op_a5, op_a6, op_a7,
        op_a8, op_a9, op_aa, op_ab, op_ac, op_ad, op_ae, op_af,
        op_b0, op_b1, op_b2, op_b3, op_b4, op_b5, op_b6, op_b7,
        op_b8, op_b9, op_ba, op_bb, op_bc, op_bd, op_be, op_bf,
        op_c0, op_c1, op_c2, op_c3, op_c4, op_c5, op_c6, op_c7,
        op_c8, op_c9, op_ca, op_cb, op_cc, op_cd, op_ce, op_cf,
        op_d0, op_d1, op_d2, op_d3, op_d4, op_d5, op_d6, op_d7,
        op_d8, op_d9, op_da, op_db, op_dc, op_dd, op_de, op_df,
        op_e0, op_e1, op_e2, op_e3, op_e4, op_e5, op_e6, op_e7,
        op_e8, op_e9, op_ea, op_eb, op_ec, op_ed, op_ee, op_ef,
        op_f0, op_f1, op_f2, op_f3, op_f4, op_f5, op_f6, op_f7,
        op_f8, op_f9, op_fa, op_fb, op_fc, op_fd, op_fe, op_ff
    };
    opcode[] Z80cb = {
        cb_00, cb_01, cb_02, cb_03, cb_04, cb_05, cb_06, cb_07,
        cb_08, cb_09, cb_0a, cb_0b, cb_0c, cb_0d, cb_0e, cb_0f,
        cb_10, cb_11, cb_12, cb_13, cb_14, cb_15, cb_16, cb_17,
        cb_18, cb_19, cb_1a, cb_1b, cb_1c, cb_1d, cb_1e, cb_1f,
        cb_20, cb_21, cb_22, cb_23, cb_24, cb_25, cb_26, cb_27,
        cb_28, cb_29, cb_2a, cb_2b, cb_2c, cb_2d, cb_2e, cb_2f,
        cb_30, cb_31, cb_32, cb_33, cb_34, cb_35, cb_36, cb_37,
        cb_38, cb_39, cb_3a, cb_3b, cb_3c, cb_3d, cb_3e, cb_3f,
        cb_40, cb_41, cb_42, cb_43, cb_44, cb_45, cb_46, cb_47,
        cb_48, cb_49, cb_4a, cb_4b, cb_4c, cb_4d, cb_4e, cb_4f,
        cb_50, cb_51, cb_52, cb_53, cb_54, cb_55, cb_56, cb_57,
        cb_58, cb_59, cb_5a, cb_5b, cb_5c, cb_5d, cb_5e, cb_5f,
        cb_60, cb_61, cb_62, cb_63, cb_64, cb_65, cb_66, cb_67,
        cb_68, cb_69, cb_6a, cb_6b, cb_6c, cb_6d, cb_6e, cb_6f,
        cb_70, cb_71, cb_72, cb_73, cb_74, cb_75, cb_76, cb_77,
        cb_78, cb_79, cb_7a, cb_7b, cb_7c, cb_7d, cb_7e, cb_7f,
        cb_80, cb_81, cb_82, cb_83, cb_84, cb_85, cb_86, cb_87,
        cb_88, cb_89, cb_8a, cb_8b, cb_8c, cb_8d, cb_8e, cb_8f,
        cb_90, cb_91, cb_92, cb_93, cb_94, cb_95, cb_96, cb_97,
        cb_98, cb_99, cb_9a, cb_9b, cb_9c, cb_9d, cb_9e, cb_9f,
        cb_a0, cb_a1, cb_a2, cb_a3, cb_a4, cb_a5, cb_a6, cb_a7,
        cb_a8, cb_a9, cb_aa, cb_ab, cb_ac, cb_ad, cb_ae, cb_af,
        cb_b0, cb_b1, cb_b2, cb_b3, cb_b4, cb_b5, cb_b6, cb_b7,
        cb_b8, cb_b9, cb_ba, cb_bb, cb_bc, cb_bd, cb_be, cb_bf,
        cb_c0, cb_c1, cb_c2, cb_c3, cb_c4, cb_c5, cb_c6, cb_c7,
        cb_c8, cb_c9, cb_ca, cb_cb, cb_cc, cb_cd, cb_ce, cb_cf,
        cb_d0, cb_d1, cb_d2, cb_d3, cb_d4, cb_d5, cb_d6, cb_d7,
        cb_d8, cb_d9, cb_da, cb_db, cb_dc, cb_dd, cb_de, cb_df,
        cb_e0, cb_e1, cb_e2, cb_e3, cb_e4, cb_e5, cb_e6, cb_e7,
        cb_e8, cb_e9, cb_ea, cb_eb, cb_ec, cb_ed, cb_ee, cb_ef,
        cb_f0, cb_f1, cb_f2, cb_f3, cb_f4, cb_f5, cb_f6, cb_f7,
        cb_f8, cb_f9, cb_fa, cb_fb, cb_fc, cb_fd, cb_fe, cb_ff
    };

    opcode[] Z80dd = {
        dd_00, dd_01, dd_02, dd_03, dd_04, dd_05, dd_06, dd_07,
        dd_08, dd_09, dd_0a, dd_0b, dd_0c, dd_0d, dd_0e, dd_0f,
        dd_10, dd_11, dd_12, dd_13, dd_14, dd_15, dd_16, dd_17,
        dd_18, dd_19, dd_1a, dd_1b, dd_1c, dd_1d, dd_1e, dd_1f,
        dd_20, dd_21, dd_22, dd_23, dd_24, dd_25, dd_26, dd_27,
        dd_28, dd_29, dd_2a, dd_2b, dd_2c, dd_2d, dd_2e, dd_2f,
        dd_30, dd_31, dd_32, dd_33, dd_34, dd_35, dd_36, dd_37,
        dd_38, dd_39, dd_3a, dd_3b, dd_3c, dd_3d, dd_3e, dd_3f,
        dd_40, dd_41, dd_42, dd_43, dd_44, dd_45, dd_46, dd_47,
        dd_48, dd_49, dd_4a, dd_4b, dd_4c, dd_4d, dd_4e, dd_4f,
        dd_50, dd_51, dd_52, dd_53, dd_54, dd_55, dd_56, dd_57,
        dd_58, dd_59, dd_5a, dd_5b, dd_5c, dd_5d, dd_5e, dd_5f,
        dd_60, dd_61, dd_62, dd_63, dd_64, dd_65, dd_66, dd_67,
        dd_68, dd_69, dd_6a, dd_6b, dd_6c, dd_6d, dd_6e, dd_6f,
        dd_70, dd_71, dd_72, dd_73, dd_74, dd_75, dd_76, dd_77,
        dd_78, dd_79, dd_7a, dd_7b, dd_7c, dd_7d, dd_7e, dd_7f,
        dd_80, dd_81, dd_82, dd_83, dd_84, dd_85, dd_86, dd_87,
        dd_88, dd_89, dd_8a, dd_8b, dd_8c, dd_8d, dd_8e, dd_8f,
        dd_90, dd_91, dd_92, dd_93, dd_94, dd_95, dd_96, dd_97,
        dd_98, dd_99, dd_9a, dd_9b, dd_9c, dd_9d, dd_9e, dd_9f,
        dd_a0, dd_a1, dd_a2, dd_a3, dd_a4, dd_a5, dd_a6, dd_a7,
        dd_a8, dd_a9, dd_aa, dd_ab, dd_ac, dd_ad, dd_ae, dd_af,
        dd_b0, dd_b1, dd_b2, dd_b3, dd_b4, dd_b5, dd_b6, dd_b7,
        dd_b8, dd_b9, dd_ba, dd_bb, dd_bc, dd_bd, dd_be, dd_bf,
        dd_c0, dd_c1, dd_c2, dd_c3, dd_c4, dd_c5, dd_c6, dd_c7,
        dd_c8, dd_c9, dd_ca, dd_cb, dd_cc, dd_cd, dd_ce, dd_cf,
        dd_d0, dd_d1, dd_d2, dd_d3, dd_d4, dd_d5, dd_d6, dd_d7,
        dd_d8, dd_d9, dd_da, dd_db, dd_dc, dd_dd, dd_de, dd_df,
        dd_e0, dd_e1, dd_e2, dd_e3, dd_e4, dd_e5, dd_e6, dd_e7,
        dd_e8, dd_e9, dd_ea, dd_eb, dd_ec, dd_ed, dd_ee, dd_ef,
        dd_f0, dd_f1, dd_f2, dd_f3, dd_f4, dd_f5, dd_f6, dd_f7,
        dd_f8, dd_f9, dd_fa, dd_fb, dd_fc, dd_fd, dd_fe, dd_ff
    };
    opcode[] Z80ed = {
        ed_00, ed_01, ed_02, ed_03, ed_04, ed_05, ed_06, ed_07,
        ed_08, ed_09, ed_0a, ed_0b, ed_0c, ed_0d, ed_0e, ed_0f,
        ed_10, ed_11, ed_12, ed_13, ed_14, ed_15, ed_16, ed_17,
        ed_18, ed_19, ed_1a, ed_1b, ed_1c, ed_1d, ed_1e, ed_1f,
        ed_20, ed_21, ed_22, ed_23, ed_24, ed_25, ed_26, ed_27,
        ed_28, ed_29, ed_2a, ed_2b, ed_2c, ed_2d, ed_2e, ed_2f,
        ed_30, ed_31, ed_32, ed_33, ed_34, ed_35, ed_36, ed_37,
        ed_38, ed_39, ed_3a, ed_3b, ed_3c, ed_3d, ed_3e, ed_3f,
        ed_40, ed_41, ed_42, ed_43, ed_44, ed_45, ed_46, ed_47,
        ed_48, ed_49, ed_4a, ed_4b, ed_4c, ed_4d, ed_4e, ed_4f,
        ed_50, ed_51, ed_52, ed_53, ed_54, ed_55, ed_56, ed_57,
        ed_58, ed_59, ed_5a, ed_5b, ed_5c, ed_5d, ed_5e, ed_5f,
        ed_60, ed_61, ed_62, ed_63, ed_64, ed_65, ed_66, ed_67,
        ed_68, ed_69, ed_6a, ed_6b, ed_6c, ed_6d, ed_6e, ed_6f,
        ed_70, ed_71, ed_72, ed_73, ed_74, ed_75, ed_76, ed_77,
        ed_78, ed_79, ed_7a, ed_7b, ed_7c, ed_7d, ed_7e, ed_7f,
        ed_80, ed_81, ed_82, ed_83, ed_84, ed_85, ed_86, ed_87,
        ed_88, ed_89, ed_8a, ed_8b, ed_8c, ed_8d, ed_8e, ed_8f,
        ed_90, ed_91, ed_92, ed_93, ed_94, ed_95, ed_96, ed_97,
        ed_98, ed_99, ed_9a, ed_9b, ed_9c, ed_9d, ed_9e, ed_9f,
        ed_a0, ed_a1, ed_a2, ed_a3, ed_a4, ed_a5, ed_a6, ed_a7,
        ed_a8, ed_a9, ed_aa, ed_ab, ed_ac, ed_ad, ed_ae, ed_af,
        ed_b0, ed_b1, ed_b2, ed_b3, ed_b4, ed_b5, ed_b6, ed_b7,
        ed_b8, ed_b9, ed_ba, ed_bb, ed_bc, ed_bd, ed_be, ed_bf,
        ed_c0, ed_c1, ed_c2, ed_c3, ed_c4, ed_c5, ed_c6, ed_c7,
        ed_c8, ed_c9, ed_ca, ed_cb, ed_cc, ed_cd, ed_ce, ed_cf,
        ed_d0, ed_d1, ed_d2, ed_d3, ed_d4, ed_d5, ed_d6, ed_d7,
        ed_d8, ed_d9, ed_da, ed_db, ed_dc, ed_dd, ed_de, ed_df,
        ed_e0, ed_e1, ed_e2, ed_e3, ed_e4, ed_e5, ed_e6, ed_e7,
        ed_e8, ed_e9, ed_ea, ed_eb, ed_ec, ed_ed, ed_ee, ed_ef,
        ed_f0, ed_f1, ed_f2, ed_f3, ed_f4, ed_f5, ed_f6, ed_f7,
        ed_f8, ed_f9, ed_fa, ed_fb, ed_fc, ed_fd, ed_fe, ed_ff
    };
    opcode[] Z80fd = {
        fd_00, fd_01, fd_02, fd_03, fd_04, fd_05, fd_06, fd_07,
        fd_08, fd_09, fd_0a, fd_0b, fd_0c, fd_0d, fd_0e, fd_0f,
        fd_10, fd_11, fd_12, fd_13, fd_14, fd_15, fd_16, fd_17,
        fd_18, fd_19, fd_1a, fd_1b, fd_1c, fd_1d, fd_1e, fd_1f,
        fd_20, fd_21, fd_22, fd_23, fd_24, fd_25, fd_26, fd_27,
        fd_28, fd_29, fd_2a, fd_2b, fd_2c, fd_2d, fd_2e, fd_2f,
        fd_30, fd_31, fd_32, fd_33, fd_34, fd_35, fd_36, fd_37,
        fd_38, fd_39, fd_3a, fd_3b, fd_3c, fd_3d, fd_3e, fd_3f,
        fd_40, fd_41, fd_42, fd_43, fd_44, fd_45, fd_46, fd_47,
        fd_48, fd_49, fd_4a, fd_4b, fd_4c, fd_4d, fd_4e, fd_4f,
        fd_50, fd_51, fd_52, fd_53, fd_54, fd_55, fd_56, fd_57,
        fd_58, fd_59, fd_5a, fd_5b, fd_5c, fd_5d, fd_5e, fd_5f,
        fd_60, fd_61, fd_62, fd_63, fd_64, fd_65, fd_66, fd_67,
        fd_68, fd_69, fd_6a, fd_6b, fd_6c, fd_6d, fd_6e, fd_6f,
        fd_70, fd_71, fd_72, fd_73, fd_74, fd_75, fd_76, fd_77,
        fd_78, fd_79, fd_7a, fd_7b, fd_7c, fd_7d, fd_7e, fd_7f,
        fd_80, fd_81, fd_82, fd_83, fd_84, fd_85, fd_86, fd_87,
        fd_88, fd_89, fd_8a, fd_8b, fd_8c, fd_8d, fd_8e, fd_8f,
        fd_90, fd_91, fd_92, fd_93, fd_94, fd_95, fd_96, fd_97,
        fd_98, fd_99, fd_9a, fd_9b, fd_9c, fd_9d, fd_9e, fd_9f,
        fd_a0, fd_a1, fd_a2, fd_a3, fd_a4, fd_a5, fd_a6, fd_a7,
        fd_a8, fd_a9, fd_aa, fd_ab, fd_ac, fd_ad, fd_ae, fd_af,
        fd_b0, fd_b1, fd_b2, fd_b3, fd_b4, fd_b5, fd_b6, fd_b7,
        fd_b8, fd_b9, fd_ba, fd_bb, fd_bc, fd_bd, fd_be, fd_bf,
        fd_c0, fd_c1, fd_c2, fd_c3, fd_c4, fd_c5, fd_c6, fd_c7,
        fd_c8, fd_c9, fd_ca, fd_cb, fd_cc, fd_cd, fd_ce, fd_cf,
        fd_d0, fd_d1, fd_d2, fd_d3, fd_d4, fd_d5, fd_d6, fd_d7,
        fd_d8, fd_d9, fd_da, fd_db, fd_dc, fd_dd, fd_de, fd_df,
        fd_e0, fd_e1, fd_e2, fd_e3, fd_e4, fd_e5, fd_e6, fd_e7,
        fd_e8, fd_e9, fd_ea, fd_eb, fd_ec, fd_ed, fd_ee, fd_ef,
        fd_f0, fd_f1, fd_f2, fd_f3, fd_f4, fd_f5, fd_f6, fd_f7,
        fd_f8, fd_f9, fd_fa, fd_fb, fd_fc, fd_fd, fd_fe, fd_ff
    };
    opcode[] Z80xycb = {
        xycb_00, xycb_01, xycb_02, xycb_03, xycb_04, xycb_05, xycb_06, xycb_07,
        xycb_08, xycb_09, xycb_0a, xycb_0b, xycb_0c, xycb_0d, xycb_0e, xycb_0f,
        xycb_10, xycb_11, xycb_12, xycb_13, xycb_14, xycb_15, xycb_16, xycb_17,
        xycb_18, xycb_19, xycb_1a, xycb_1b, xycb_1c, xycb_1d, xycb_1e, xycb_1f,
        xycb_20, xycb_21, xycb_22, xycb_23, xycb_24, xycb_25, xycb_26, xycb_27,
        xycb_28, xycb_29, xycb_2a, xycb_2b, xycb_2c, xycb_2d, xycb_2e, xycb_2f,
        xycb_30, xycb_31, xycb_32, xycb_33, xycb_34, xycb_35, xycb_36, xycb_37,
        xycb_38, xycb_39, xycb_3a, xycb_3b, xycb_3c, xycb_3d, xycb_3e, xycb_3f,
        xycb_40, xycb_41, xycb_42, xycb_43, xycb_44, xycb_45, xycb_46, xycb_47,
        xycb_48, xycb_49, xycb_4a, xycb_4b, xycb_4c, xycb_4d, xycb_4e, xycb_4f,
        xycb_50, xycb_51, xycb_52, xycb_53, xycb_54, xycb_55, xycb_56, xycb_57,
        xycb_58, xycb_59, xycb_5a, xycb_5b, xycb_5c, xycb_5d, xycb_5e, xycb_5f,
        xycb_60, xycb_61, xycb_62, xycb_63, xycb_64, xycb_65, xycb_66, xycb_67,
        xycb_68, xycb_69, xycb_6a, xycb_6b, xycb_6c, xycb_6d, xycb_6e, xycb_6f,
        xycb_70, xycb_71, xycb_72, xycb_73, xycb_74, xycb_75, xycb_76, xycb_77,
        xycb_78, xycb_79, xycb_7a, xycb_7b, xycb_7c, xycb_7d, xycb_7e, xycb_7f,
        xycb_80, xycb_81, xycb_82, xycb_83, xycb_84, xycb_85, xycb_86, xycb_87,
        xycb_88, xycb_89, xycb_8a, xycb_8b, xycb_8c, xycb_8d, xycb_8e, xycb_8f,
        xycb_90, xycb_91, xycb_92, xycb_93, xycb_94, xycb_95, xycb_96, xycb_97,
        xycb_98, xycb_99, xycb_9a, xycb_9b, xycb_9c, xycb_9d, xycb_9e, xycb_9f,
        xycb_a0, xycb_a1, xycb_a2, xycb_a3, xycb_a4, xycb_a5, xycb_a6, xycb_a7,
        xycb_a8, xycb_a9, xycb_aa, xycb_ab, xycb_ac, xycb_ad, xycb_ae, xycb_af,
        xycb_b0, xycb_b1, xycb_b2, xycb_b3, xycb_b4, xycb_b5, xycb_b6, xycb_b7,
        xycb_b8, xycb_b9, xycb_ba, xycb_bb, xycb_bc, xycb_bd, xycb_be, xycb_bf,
        xycb_c0, xycb_c1, xycb_c2, xycb_c3, xycb_c4, xycb_c5, xycb_c6, xycb_c7,
        xycb_c8, xycb_c9, xycb_ca, xycb_cb, xycb_cc, xycb_cd, xycb_ce, xycb_cf,
        xycb_d0, xycb_d1, xycb_d2, xycb_d3, xycb_d4, xycb_d5, xycb_d6, xycb_d7,
        xycb_d8, xycb_d9, xycb_da, xycb_db, xycb_dc, xycb_dd, xycb_de, xycb_df,
        xycb_e0, xycb_e1, xycb_e2, xycb_e3, xycb_e4, xycb_e5, xycb_e6, xycb_e7,
        xycb_e8, xycb_e9, xycb_ea, xycb_eb, xycb_ec, xycb_ed, xycb_ee, xycb_ef,
        xycb_f0, xycb_f1, xycb_f2, xycb_f3, xycb_f4, xycb_f5, xycb_f6, xycb_f7,
        xycb_f8, xycb_f9, xycb_fa, xycb_fb, xycb_fc, xycb_fd, xycb_fe, xycb_ff
    };
    
    /***
     * 
     * arcadeflex functions
     */
    @Override
    public Object init_context() {
        Object reg = new Z80_Regs();
        return reg;
    }
    
    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc);
    }
    
    @Override
    public int memory_read(int offset) {
        return cpu_readmem16(offset);
    }

    @Override
    public void memory_write(int offset, int data) {
        cpu_writemem16(offset, data);
    }

    @Override
    public void internal_interrupt(int type) {
        //doesn't exist in z80 cpu
    }
}
