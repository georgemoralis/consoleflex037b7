/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package old.cpu.z80;

/**
 *
 * @author george
 */
public class z80H {
    
    
    public static final int   Z80_PC=1;
    public static final int   Z80_SP=2;
    public static final int   Z80_AF=3;
    public static final int   Z80_BC=4;
    public static final int   Z80_DE=5;
    public static final int   Z80_HL=6;
    public static final int   Z80_IX=7;
    public static final int   Z80_IY=8;
    public static final int   Z80_AF2=9;
    public static final int   Z80_BC2=10;
    public static final int   Z80_DE2=11;
    public static final int   Z80_HL2=12;
    public static final int   Z80_R=13;
    public static final int   Z80_I=14;
    public static final int   Z80_IM=15;
    public static final int   Z80_IFF1=16;
    public static final int   Z80_IFF2=17;
    public static final int   Z80_HALT=18;
    public static final int   Z80_NMI_STATE=19;
    public static final int   Z80_IRQ_STATE=20;
    public static final int   Z80_DC0=21;
    public static final int   Z80_DC1=22;
    public static final int   Z80_DC2=23;
    public static final int   Z80_DC3=24;
    public static final int   Z80_NMI_NESTING=25;

    
    public static final int Z80_IGNORE_INT  = -1;          /* Ignore interrupt*/
    public static final int Z80_NMI_INT     = -2;	/* Execute NMI	*/
    public static final int Z80_IRQ_INT     =-1000;	/* Execute IRQ*/    
    
    /* Port number written to when entering/leaving HALT state */
    public static final int  Z80_HALT_PORT  = 0x10000;
}
