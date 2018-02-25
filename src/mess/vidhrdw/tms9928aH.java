/*
 * ported to v0.37b6
 * using automatic conversion tool v0.01
 */ 
package mess.vidhrdw;

public class tms9928aH {

public static final int TMS9928A_PALETTE_SIZE           =16;
public static final int TMS9928A_COLORTABLE_SIZE        =32;

/*
** The different models
*/

public static final int TMS99x8A	=(1);
public static final int TMS99x8		=(2);

typedef struct {
    /* TMS9928A internal settings */
    UINT8 ReadAhead,Regs[8],StatusReg,oldStatusReg;
    int Addr,FirstByte,INT,BackColour,Change,mode;
    int colour,pattern,nametbl,spriteattribute,spritepattern;
    int colourmask,patternmask;
    void (*INTCallback)(int);
    /* memory */
    UINT8 *vMem, *dBackMem;
    struct osd_bitmap *tmpbmp;
    int vramsize, model;
    /* emulation settings */
    int LimitSprites; /* max 4 sprites on a row, like original TMS9918A */
    /* dirty tables */
    char anyDirtyColour, anyDirtyName, anyDirtyPattern;
    char *DirtyColour, *DirtyName, *DirtyPattern;
} TMS9928A;    
}
