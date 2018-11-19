/*
**
** File: scc.c -- software implementation of the Konami SCC
** emulator. More than one SCC supported, and SCC+ as well as
** normal megarom SCC.
**
** For information on the scc, see:
**    http://www.msxnet.org/tech/scc.html
** and
**    http://www.msxnet.org/tech/SoundCartridge.html
**
** Todo:
** Check the sound emulation itself (scc-u.c)
**
** By Sean Young <sean@msxnet.org>
*/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package WIP.sound;

import static WIP.sound.sccH.*;
import static WIP.mame.sndintrfH.*;
import static old.arcadeflex.osdepend.*;

import WIP.arcadeflex.libc_v2.ShortPtr;
import old.sound.streams.StreamInitPtr;

import static arcadeflex.libc.cstring.*;
import static WIP.arcadeflex.fucPtr.*;

import static old.sound.streams.*;
import static WIP.mame.mame.Machine;
import static WIP.mame.sndintrf.*;

import static arcadeflex.libc.cstdio.*;

public class scc extends snd_interface
{

    @Override
    public int chips_num(MachineSound msound) {
        return MAX_SCC;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return 0;
    }

    @Override
    public int start(MachineSound msound) {
        return 1;
    }

    @Override
    public void stop() {
        //NO FUNCTIONAL CODE IS NECCESARY
    }

    @Override
    public void update() {
        //NO FUNCTIONAL CODE IS NECCESARY
    }

    @Override
    public void reset() {
        //NO FUNCTIONAL CODE IS NECCESARY
    }
	
	/* the struct */
	public static class KONSCC {
	    public int[]	Regs = new int[17] ;
	    public int[] 	Waves = new int[5*32] ;
	
	    public double	UpdateStep=0.0;
	    public int		SampleRate=0;
	    public int		Channel=0;
	
	    /* state variables */
	    public int         Counter0, Counter1, Counter2, Counter3, Counter4 = 0;
	};
	
	public static KONSCC[] SCC= new KONSCC[MAX_SCC];
	
	/* the registers */
	public static int SCC_1FINE       =	(0);
	public static int SCC_1COARSE     =	(1);
	public static int SCC_2FINE       =	(2);
	public static int SCC_2COARSE     =	(3);
	public static int SCC_3FINE       =	(4);
	public static int SCC_3COARSE     =	(5);
	public static int SCC_4FINE       =	(6);
	public static int SCC_4COARSE     =	(7);
	public static int SCC_5FINE       =	(8);
	public static int SCC_5COARSE     =	(9);
	public static int SCC_1VOL        =	(10);
	public static int SCC_2VOL        =	(11);
	public static int SCC_3VOL        =	(12);
	public static int SCC_4VOL        =	(13);
	public static int SCC_5VOL        =	(14);
	public static int SCC_ENABLE      =	(15);
	public static int SCC_DEFORM      =	(16);
	
	/*
	** reset all chip registers.
	*/
	public static void SCCResetChip(int num)
	{
	    int i;
	    
	    System.out.println(SCC[num]);
	    System.out.println(num);
	    if (SCC[num] == null) {
	    	
		    SCC[num]=new KONSCC();
	    }
	    
	    System.out.println(SCC[num].Regs);
	
	    /* initialize hardware registers */
	    memset (SCC[num].Regs, 0, 17) ;
	    for( i=10; i<15; i++ ) SCC[num].Regs[i] = 10;
	
	    SCC[num].Counter0 = SCC[num].Counter1 = SCC[num].Counter2 = 0;
	    SCC[num].Counter3 = SCC[num].Counter4 = 0;
	
	    /* waves memory is not reset */
	}
	
	static int[] Mask = { 0xff, 0xf, 0xff, 0xf, 0xff, 0xf,
			   0xff, 0xf, 0xff, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0x1f };
	
	public static void SCCWriteReg (int n, int r, int v, int t)
	{
	    //char buf[40];
	    
	    r &= 0xff;
	    v &= 0xff;
	
	    switch (t) {
		case SCC_MEGAROM:
		case SCC_PLUSCOMP:
		    if (r < 0x80) {
			/* 0 - 0x7f is wave forms, 0x60-0x7f for channel 4 and 5 */
			SCC[n].Waves[r] = v ;
			if (r >= 0x60) {
			    SCC[n].Waves[r + 0x20] = v ;
			}
		    } else if (r < 0xa0) {
			/* 9880-988f = 9890-989f */
			SCC[n].Regs[r & 15] = (v & Mask[r & 15]) ;
		    } else switch (t) {
			case SCC_MEGAROM:
			    if (r >= 0xe0) {
				SCC[n].Regs[SCC_DEFORM] = v ;
				if (v != 0) logerror("SCC: %02xh written to unemulated register\n", v);
	
			    }
			    break ;
			case SCC_PLUSCOMP:
			    if ( (r < 0xe0) && (r >= 0xc0) ) {
				SCC[n].Regs[SCC_DEFORM] = v ;
				if (v != 0) logerror("SCC: %02xh written to unemulated register\n", v);
			    }
			    break ;
			}
		    break ;
		case SCC_PLUSEXT:
		    if (r < 0xa0) {
			SCC[n].Waves[r] = v ;
		    } else if (r < 0xc0) {
			SCC[n].Regs[r & 15] = (v & Mask[r & 15]) ;
		    } else if (r < 0xe0) {
			SCC[n].Regs[SCC_DEFORM] = v ;
			if (v != 0) logerror("SCC: %02xh written to unemulated register\n", v);
		    }
		    break ;
	    }
	}
	
	public static StreamInitPtr SCCUpdate16 = new StreamInitPtr() {
        public void handler(int num, ShortPtr buffer, int length) {
        	/*TODO*///#define DATATYPE UINT16
    		/*TODO*///#define DATACONV(A) ( (A) * 3)
    		/*TODO*///#undef DATACONV
    		/*TODO*///#undef DATATYPE 
        }
    };
		
    public static StreamInitPtr SCCUpdate8 = new StreamInitPtr() {
        public void handler(int num, ShortPtr buffer, int length) {
			/*TODO*///#define DATATYPE UINT8
			/*TODO*///#define DATACONV(A) ( (A) / 75)
			/*TODO*///#undef DATACONV
			/*TODO*///#undef DATATYPE
        }
    };
	
	public static void SCCSetClock (int chip, int clock) {
		System.out.println("Chip: "+chip);
		System.out.println("Clock: "+clock);
		System.out.println(SCC[chip]);
		System.out.println(SCC[chip].SampleRate);
	    SCC[chip].UpdateStep = 512.0 * (double)clock /
		((double)SCC[chip].SampleRate / 4.0);
	}
	
	static int SCCInit(MachineSound msound,int chip,
	                int clock,int volume,int sample_rate,int sample_bits) {
	    String name="SCC";
	    //int vol;
            System.out.println("SCCInit");
	    //memset (SCC[chip],0,sizeof (struct KONSCC) );
	    SCC= new KONSCC[MAX_SCC];
	    for (int i=0;i<MAX_SCC;i++)
	    	SCC[i]=new KONSCC();
	    System.out.println(sample_rate);
	    System.out.println(chip);
	    SCC[chip]=new KONSCC();
	    SCC[chip].SampleRate = sample_rate;
	
	    sprintf (name, "Konami SCC/SCC+ #%d",chip);
	
	    /*SCC[chip].Channel = stream_init (name,volume,sample_rate,sample_bits,chip,
		(sample_bits == 16) ? SCCUpdate16 : SCCUpdate8);*/
	System.out.println("Antes "+SCC[chip].Channel);
		SCC[chip].Channel = stream_init (name,volume,sample_rate,chip,SCCUpdate16);
                System.out.println("Despues "+SCC[chip].Channel);
	
	    if (SCC[chip].Channel == -1) return 1;
	
	    SCCSetClock (chip, clock);
	    SCCResetChip (chip);
	
	    return 0;
	}
	
	public static ShStartPtr SCC_sh_start = new ShStartPtr() { 
		public int handler(MachineSound msound)  {
                    System.out.println("SCC_sh_start");
		    int chip;
		    //const struct CustomSound_interface *intf = msound.sound_interface;
		
		    for (chip=0;chip<1;chip++) {
				//if (SCCInit (msound,chip,3579545, 20, Machine.sample_rate, Machine.sample_bits) )
				if (SCCInit (msound,chip,3579545, 20, Machine.sample_rate, 16) == 1)
					return 1;
			}
		    return 0;
	} };
}
