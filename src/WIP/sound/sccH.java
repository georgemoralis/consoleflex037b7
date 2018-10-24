package WIP.sound;

/*
**
**
** File: scc.h -- header file for software implementation of
**	Konami's SCC sound chip for MSX. SCC and SCC+.
**
** Based on Sound.c in fMSX by (C) Ville Hallik (ville@physic.ut.ee) 1996
**
** By Sean Young <sean@msxnet.org>
*/

public class sccH {

	/* different types of SCC modes/types */
	public static final int SCC_MEGAROM		= (0);	/* if emulating megarom SCC */
	public static final int SCC_PLUSCOMP	= (1);	/* if emulating SCC+ in compatiblity mode */
	public static final int SCC_PLUSEXT	= (2);	/* if emulating SCC+ in extended mode */
	
	/*
	void SCCSetClock (int chip, int clock);
	void SCCResetChip (int chip);
	void SCCWriteReg (int chip, int reg, int value, int type);
	*/
	
	public static final int MAX_SCC	= (4);
	//#define SCC_INTERPOLATE
	
}