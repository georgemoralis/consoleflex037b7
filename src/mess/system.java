/** *
 * Ported to mess 0.37b6
 */
package mess;

import static mess.messH.*;
import static mess.systems.coleco.*;
import static mess.systems.sms.*;
import static mess.systems.nes.*;

import static mess.systems.spectrum.*;
import static mess.systems.spec128.*;
import static mess.systems.specpls3.*;
import static mess.systems.pentagon.*;
import static mess.systems.scorpion.*;
import static mess.systems.timex.*;

import static mess.systems.amstrad.*;

import static mess.systems.msx.*;

import static mess.systems.a2600.*;
import static mess.systems.zx.*;

public class system {

    public static GameDriver drivers[] = {
        /*TODO*////****************CONSOLES****************************************************/
        /*TODO*///
        /*TODO*///	/* ATARI */
        driver_a2600, 	/* Atari 2600									  */
        /*TODO*///	DRIVER( a5200 ) 	/* Atari 5200									  */
        /*TODO*///	DRIVER( a7800 ) 	/* Atari 7800									  */
        /*TODO*///
        /*TODO*///	/* BALLY */
        /*TODO*///	DRIVER( astrocde )	/* Bally Astrocade								  */
        /*TODO*///
        /*TODO*///	/* FAIRCHILD */
        /*TODO*///	DRIVER( channelf )	/* Channel F									  */
        /*TODO*///
        /*TODO*///    /* COLECO */
        driver_coleco, /* ColecoVision (Original BIOS )				  */
        /*TODO*///#if 0						/* Please dont include these next 2 in a distribution, they are Hacks	*/
        /*TODO*///	DRIVER( colecofb )	/* ColecoVision (Fast BIOS load)				  */
        /*TODO*///	DRIVER( coleconb )	/* ColecoVision (No BIOS load)					  */
        /*TODO*///#endif
        /*TODO*///	/* NINTENDO */
        driver_nes, /* Nintendo Entertainment System				  */
        driver_nespal,/* Nintendo Entertainment System				  */
        driver_famicom,
        /*TODO*///	DRIVER( gameboy )	/* Nintendo GameBoy Handheld					  */
        /*TODO*///    DRIVER (snes)		/* Nintendo Super Nintendo                        */
        /*TODO*////*	DRIVER (vboy)	*/	/* Nintendo Virtual Boy                           */
        /*TODO*///
        /*TODO*///	/* NEC */
        /*TODO*///	DRIVER( pce )		/* PC/Engine - Turbo Graphics-16  NEC 1989-1993   */
        /*TODO*///
        /*TODO*///	/* SEGA */
        driver_gamegear, /* Sega Game Gear Handheld						  */
        driver_sms, /* Sega Sega Master System						  */
        /*TODO*///	DRIVER( genesis )	/* Sega Genesis/MegaDrive						  */
        /*TODO*///
        /*TODO*///	/* GCE */
        /*TODO*///	DRIVER( vectrex )	/* General Consumer Electric Vectrex - 1982-1984  */
        /*TODO*///						/* (aka Milton-Bradley Vectrex) 				  */
        /*TODO*///	DRIVER( raaspec )	/* RA+A Spectrum - Modified Vectrex 			  */
        /*TODO*///
        /*TODO*///	/* ENTEX */
        /*TODO*///	DRIVER( advision )	/* Adventurevision								  */
        /*TODO*///
        /*TODO*///	/* Magnavox */
        /*TODO*///TESTDRIVER( odyssey2 )	/* Magnavox Odyssey 2 - 1978-1983				  */
        /*TODO*///
        /*TODO*///
        /*TODO*///	/* CAPCOM */
        /*TODO*///	DRIVER( sfzch ) 	/* CPS Changer (Street Fighter ZERO)			  */
        /*TODO*///
        /*TODO*////****************COMPUTERS****************************************************/
        /*TODO*///
        /*TODO*///	/* APPLE */
        /*TODO*////*
        /*TODO*/// * CPU Model			 Month				 Year
        /*TODO*/// * -------------		 -----				 ----
        /*TODO*/// *
        /*TODO*/// * Apple I				 July				 1976
        /*TODO*/// * Apple II 			 April				 1977
        /*TODO*/// * Apple II Plus		 June				 1979
        /*TODO*/// * Apple III			 May				 1980
        /*TODO*/// * Apple IIe			 January			 1983
        /*TODO*/// * Apple III Plus		 December			 1983
        /*TODO*/// * Apple IIe Enhanced	 March				 1985
        /*TODO*/// * Apple IIc			 April				 1984
        /*TODO*/// * Apple IIc ROM 0		 ?					 1985
        /*TODO*/// * Apple IIc ROM 3		 September			 1986
        /*TODO*/// * Apple IIgs			 September			 1986
        /*TODO*/// * Apple IIe Platinum	 January			 1987
        /*TODO*/// * Apple IIgs ROM 01	 September			 1987
        /*TODO*/// * Apple IIc ROM 4		 ?					 198?
        /*TODO*/// * Apple IIc Plus		 September			 1988
        /*TODO*/// * Apple IIgs ROM 3 	 August 			 1989
        /*TODO*/// */
        /*TODO*///	DRIVER( apple1 )	/* 1976 Apple 1 								  */
        /*TODO*///	DRIVER( apple2c )	/* 1984 Apple //c								  */
        /*TODO*///	DRIVER( apple2c0 )	/* 1986 Apple //c (3.5 ROM) 					  */
        /*TODO*///	DRIVER( apple2cp )	/* 1988 Apple //c+								  */
        /*TODO*///	DRIVER( apple2e )	/* 1983 Apple //e								  */
        /*TODO*///	DRIVER( apple2ee )	/* 1985 Apple //e Enhanced						  */
        /*TODO*///	DRIVER( apple2ep )	/* 1987 Apple //e Platinum						  */
        /*TODO*////*	DRIVER( mac512k )*/	/* 1984 Apple Macintosh 512k					  */
        /*TODO*///    DRIVER( mac512ke )  /* 1986 Apple Macintosh 512ke                     */
        /*TODO*///	DRIVER( macplus )	/* 1986 Apple Macintosh Plus					  */
        /*TODO*////*	DRIVER( mac2 )*/	/* 1987 Apple Macintosh II						  */
        /*TODO*///	DRIVER( lisa2 ) 	/*												  */
        /*TODO*///
        /*TODO*///	/* ATARI */
        /*TODO*///	DRIVER( a400 )		/* 1979 Atari 400								  */
        /*TODO*///	DRIVER( a400pal )	/* 1979 Atari 400 PAL							  */
        /*TODO*///	DRIVER( a800 )		/* 1979 Atari 800								  */
        /*TODO*///	DRIVER( a800pal )	/* 1979 Atari 800 PAL							  */
        /*TODO*///	DRIVER( a800xl )	/* 1983 Atari 800 XL							  */
        /*TODO*///
        /*TODO*///#ifndef MESS_EXCLUDE_CBM
        /*TODO*///	/* COMMODORE */
        /*TODO*///	DRIVER( kim1 )		/* Commodore (MOS) KIM-1 1975					  */
        /*TODO*///
        /*TODO*///	DRIVER( pet )		/* PET2001/CBM20xx Series (Basic 1) 			  */
        /*TODO*///	DRIVER( cbm30 ) 	/* Commodore 30xx (Basic 2) 					  */
        /*TODO*///	DRIVER( cbm30b )	/* Commodore 30xx (Basic 2) (business keyboard)   */
        /*TODO*///	DRIVER( cbm40 ) 	/* Commodore 40xx FAT (CRTC) 60Hz				  */
        /*TODO*///	DRIVER( cbm40pal )	/* Commodore 40xx FAT (CRTC) 50Hz				  */
        /*TODO*///	DRIVER( cbm40b )	/* Commodore 40xx THIN (business keyboard)		  */
        /*TODO*///	DRIVER( cbm80 ) 	/* Commodore 80xx 60Hz							  */
        /*TODO*///	DRIVER( cbm80pal )	/* Commodore 80xx 50Hz							  */
        /*TODO*///	DRIVER( cbm80ger )	/* Commodore 80xx German (50Hz) 				  */
        /*TODO*///	DRIVER( cbm80swe )	/* Commodore 80xx Swedish (50Hz)				  */
        /*TODO*///	DRIVER( superpet )	/* Commodore SP9000/MMF9000 (50Hz)				  */
        /*TODO*///
        /*TODO*///	DRIVER( vic20 ) 	/* Commodore Vic-20 NTSC						  */
        /*TODO*///	DRIVER( vc20 )		/* Commodore Vic-20 PAL 						  */
        /*TODO*///	DRIVER( vic20swe )	/* Commodore Vic-20 Sweden						  */
        /*TODO*///	DRIVER( vic20i )	/* Commodore Vic-20 IEEE488 Interface			  */
        /*TODO*///
        /*TODO*///	DRIVER( max )		/* Max (Japan)/Ultimax (US)/VC10 (German)		  */
        /*TODO*///	DRIVER( c64 )		/* Commodore 64 - NTSC							  */
        /*TODO*///	DRIVER( c64pal )	/* Commodore 64 - PAL							  */
        /*TODO*///	DRIVER( vic64s )	/* Commodore VIC64S (Swedish)					  */
        /*TODO*///	DRIVER( cbm4064 )	/* Commodore CBM4064							  */
        /*TODO*///TESTDRIVER( sx64 )		/* Commodore SX 64 - PAL						  */
        /*TODO*///	DRIVER( c64gs ) 	/* Commodore 64 - NTSC							  */
        /*TODO*///
        /*TODO*///	DRIVER( cbm500 )	/* Commodore 500/P128-40						  */
        /*TODO*///	DRIVER( cbm610 )    /* Commodore 610/B128LP                           */
        /*TODO*///	DRIVER( cbm620 )    /* Commodore 620/B256LP                           */
        /*TODO*///	DRIVER( cbm710 )    /* Commodore 710/B128HP                           */
        /*TODO*///	DRIVER( cbm720 )    /* Commodore 620/B256HP                           */
        /*TODO*///
        /*TODO*///	DRIVER( c16 )		/* Commodore 16 								  */
        /*TODO*///	DRIVER( c16hun )	/* Commodore 16 Hungarian Character Set Hack	  */
        /*TODO*///	DRIVER( c16c )		/* Commodore 16  c1551							  */
        /*TODO*///TESTDRIVER( c16v )		/* Commodore 16  vc1541 						  */
        /*TODO*///	DRIVER( plus4 ) 	/* Commodore +4  c1551							  */
        /*TODO*///	DRIVER( plus4c )	/* Commodore +4  vc1541 						  */
        /*TODO*///TESTDRIVER( plus4v )	/* Commodore +4 								  */
        /*TODO*///	DRIVER( c364 )		/* Commodore 364 - Prototype					  */
        /*TODO*///
        /*TODO*///	DRIVER( c128 )		/* Commodore 128 - NTSC 						  */
        /*TODO*///	DRIVER( c128ger )	/* Commodore 128 - PAL (german) 				  */
        /*TODO*///	DRIVER( c128fra )	/* Commodore 128 - PAL (french) 				  */
        /*TODO*///	DRIVER( c128ita )	/* Commodore 128 - PAL (italian)				  */
        /*TODO*///
        /*TODO*///	DRIVER( amiga ) 	/* Commodore Amiga								  */
        /*TODO*///
        /*TODO*///	DRIVER( c65 )		/* C65 / C64DX (Prototype, NTSC, 911001)		  */
        /*TODO*///	DRIVER( c65e )		/* C65 / C64DX (Prototype, NTSC, 910828)		  */
        /*TODO*///	DRIVER( c65d )		/* C65 / C64DX (Prototype, NTSC, 910626)		  */
        /*TODO*///	DRIVER( c65c )		/* C65 / C64DX (Prototype, NTSC, 910523)		  */
        /*TODO*///	DRIVER( c65ger )	/* C65 / C64DX (Prototype, German PAL, 910429)	  */
        /*TODO*///	DRIVER( c65a )		/* C65 / C64DX (Prototype, NTSC, 910111)		  */
        /*TODO*///
        /*TODO*///#endif
        /*TODO*///
        /*TODO*///#ifndef MESS_EXCLUDE_AMSTRAD
        //	DRIVER( cpc464 )	/* Amstrad (Schneider in Germany) 1984			  */
        driver_cpc464,
        //	DRIVER( cpc664 )	/* Amstrad (Schneider in Germany) 1985			  */
        driver_cpc664,
        //	DRIVER( cpc6128 )	/* Amstrad (Schneider in Germany) 1985			  */
        driver_cpc6128,
        /*TODO*////*	DRIVER( cpc464p )*/	/* Amstrad CPC464  Plus - 1987					  */
        /*TODO*////*	DRIVER( cpc6128p )*//* Amstrad CPC6128 Plus - 1987					  */
        /*TODO*///	DRIVER( pcw8256 )	/* 198? PCW8256 								  */
        /*TODO*///	DRIVER( pcw8512 )	/* 198? PCW8512 								  */
        /*TODO*///	DRIVER( pcw9256 )	/* 198? PCW9256 								  */
        /*TODO*///	DRIVER( pcw9512 )	/* 198? PCW9512 (+) 							  */
        /*TODO*///	DRIVER( pcw10 ) 	/* 198? PCW10									  */
        /*TODO*///	DRIVER( pcw16 ) 	/* 1995 PCW16									  */
        /*TODO*///	DRIVER( nc100 ) 	/* 19?? NC100									  */
        /*TODO*///#endif
        /*TODO*///#ifndef MESS_EXCLUDE_ACORN
        /*TODO*///TESTDRIVER( z88 )		/*												  */
        /*TODO*///TESTDRIVER( avigo ) 	/*												  */
        /*TODO*///#endif
        /*TODO*///#ifndef MESS_EXCLUDE_AMSTRAD
        /*TODO*///	/* VEB MIKROELEKTRONIK */
        //	DRIVER( kccomp )	/* KC compact									  */
        driver_kccomp,
        /*TODO*///	DRIVER( kc85_4 )	/* KC 85/4										  */
        /*TODO*///#endif
        /*TODO*///
        /*TODO*///	/* CANTAB */
        /*TODO*///	DRIVER( jupiter )	/* Jupiter Ace									  */
        /*TODO*///
        /*TODO*///	/* INTELLIGENT SOFTWARE */
        /*TODO*///	DRIVER( ep128 ) 	/* Enterprise 128 k 							  */
        /*TODO*///	DRIVER( ep128a )	/* Enterprise 128 k 							  */
        /*TODO*///
        /*TODO*///	/* NON LINEAR SYSTEMS */
        /*TODO*///	DRIVER( kaypro )	/* Kaypro 2X									  */
        /*TODO*///
        /*TODO*///	/* MICROBEE SYSTEMS */
        /*TODO*///	DRIVER( mbee )		/* Microbee 									  */
        /*TODO*///	DRIVER( mbee56k )	/* Microbee 56K (CP/M)							  */
        /*TODO*///
        /*TODO*///	/* TANDY RADIO SHACK */
        /*TODO*///	DRIVER( trs80l1 )	/* TRS-80 Model I	- Radio Shack Level I BASIC   */
        /*TODO*///	DRIVER( trs80 ) 	/* TRS-80 Model I	- Radio Shack Level II BASIC  */
        /*TODO*///	DRIVER( trs80alt )	/* TRS-80 Model I	- R/S L2 BASIC				  */
        /*TODO*///TESTDRIVER( trs80m3 )	/* TRS-80 Model III - Radio Shack/Tandy 		  */
        /*TODO*///	DRIVER( coco )		/* Color Computer								  */
        /*TODO*///	DRIVER( coco3 ) 	/* Color Computer 3 							  */
        /*TODO*///	DRIVER( coco3h ) /* Hacked Color Computer 3 (6309)						  */
        /*TODO*///	DRIVER( cp400 ) 	/* Prologica CP400								  */
        /*TODO*///	DRIVER( mc10 )		/* MC-10									      */
        /*TODO*///
        /*TODO*///	/* DRAGON DATA LTD */
        /*TODO*///	DRIVER( dragon32 )	/* Dragon32 									  */
        /*TODO*///
        /*TODO*///	/* EACA */
        /*TODO*///	DRIVER( cgenie )	/* Colour Genie 								  */
        /*TODO*///	DRIVER( sys80 ) 	/* System 80									  */
        /*TODO*///
        /*TODO*///	/* VIDEO TECHNOLOGY */
        /*TODO*///	DRIVER( laser110 )	/* Laser 110									  */
        /*TODO*///	DRIVER( laser200 )	/* Laser 200									  */
        /*TODO*///	DRIVER( laser210 )	/* Laser 210 (indentical to Laser 200 ?)		  */
        /*TODO*///	DRIVER( laser310 )	/* Laser 310 (210 with diff. keyboard and RAM)	  */
        /*TODO*///	DRIVER( vz200 ) 	/* Dick Smith Electronics / Sanyo VZ200 		  */
        /*TODO*///	DRIVER( vz300 ) 	/* Dick Smith Electronics / Sanyo VZ300 		  */
        /*TODO*///	DRIVER( fellow )	/* Salora Fellow (Finland)						  */
        /*TODO*///	DRIVER( tx8000 )	/* Texet TX-8000 (U.K.) 						  */
        /*TODO*///	DRIVER( laser350 )	/* Laser 350									  */
        /*TODO*///	DRIVER( laser500 )	/* Laser 500									  */
        /*TODO*///	DRIVER( laser700 )	/* Laser 700									  */
        /*TODO*///
        /*TODO*///	/* TANGERINE */
        /*TODO*///	DRIVER( microtan )	/* Microtan 65									  */
        /*TODO*///	DRIVER( oric1 ) 	/* Oric 1										  */
        /*TODO*///	DRIVER( orica ) 	/* Oric Atmos									  */
        /*TODO*///
        /*TODO*///	/* TEXAS INSTRUMENTS */
        /*TODO*////*DRIVER( ti99_2_24 )*/ /* Texas Instruments TI 99/2					  */
        /*TODO*////*DRIVER( ti99_2_32 )*/ /* Texas Instruments TI 99/2					  */
        /*TODO*///	DRIVER( ti99_4 )	/* Texas Instruments TI 99/4					  */
        /*TODO*///	DRIVER( ti99_4e )	/* Texas Instruments TI 99/4E					  */
        /*TODO*///	DRIVER( ti99_4a )	/* Texas Instruments TI 99/4A					  */
        /*TODO*///	DRIVER( ti99_4ae )	/* Texas Instruments TI 99/4AE					  */
        /*TODO*///
        /*TODO*///#ifndef MESS_EXCLUDE_IBMPC
        /*TODO*///	/* IBM & Clones */
        /*TODO*///	DRIVER( pc )		/* PC											  */
        /*TODO*///	DRIVER( pcmda ) 	/* PC/XT with MDA (MGA aka Hercules)		      */
        /*TODO*///	DRIVER( pccga ) 	/* PC/XT with CGA							      */
        /*TODO*///
        /*TODO*///	DRIVER( t1000hx )	/* Tandy 1000HX (similiar to PCJr)				  */
        /*TODO*///
        /*TODO*///	DRIVER( pc1512 )	/* Amstrad PC1512 (XT, CGA compatible)			  */
        /*TODO*///	DRIVER( pc1640 )    /* Amstrad PC1640 (XT, EGA compatible)			  */
        /*TODO*///
        /*TODO*///TESTDRIVER( xtcga ) 	/* 												  */
        /*TODO*///	DRIVER( xtvga ) 	/*												  */
        /*TODO*///	DRIVER( atcga ) 	/*												  */
        /*TODO*///TESTDRIVER( atvga ) 	/*												  */
        /*TODO*///#endif
        /*TODO*///
        /*TODO*///	/* PHILIPS */
        /*TODO*///	DRIVER( p2000t )	/* Philips - P2000T 							  */
        /*TODO*///	DRIVER( p2000m )	/* Philips - P2000M 							  */
        /*TODO*///
        /*TODO*///	/* COMPUKIT */
        /*TODO*///	DRIVER( uk101 ) 	/*												  */
        /*TODO*///
        /*TODO*///	/* OHIO SCIENTIFIC */
        /*TODO*///	DRIVER( superbrd )	/*												  */
        /*TODO*///
        /*TODO*///#ifndef MESS_EXCLUDE_SINCLAIR
        /*TODO*///	/* SINCLAIR */
        /*TODO*///	DRIVER( zx80 )		/* Sinclair ZX-80								  */
        driver_zx80,
        /*TODO*///	DRIVER( zx81 )		/* Sinclair ZX-81								  */
        driver_zx81,
        /*TODO*///	DRIVER( ts1000 )	/* Timex Sinclair 1000							  */
        driver_ts1000,
        /*TODO*///	DRIVER( aszmic )	/* ASZMIC ZX-81 ROM swap						  */
        driver_aszmic,
        /*TODO*///	DRIVER( pc8300 )	/* Your Computer - PC8300						  */
        driver_pc8300,
        /*TODO*///	DRIVER( pow3000 )	/* Creon Enterprises - Power 3000				  */
        driver_pow3000,
        /*TODO*///
        //	DRIVER( spectrum )	/* Sinclair ZX Spectrum 48k
        driver_spectrum,
        //	DRIVER( specbusy )	/*												  */
        driver_specbusy,
        //	DRIVER( specgrot )	/*												  */
        driver_specgrot,
        //	DRIVER( specimc )	/*												  */
        driver_specimc,
        //	DRIVER( speclec )	/*												  */
        driver_speclec,
        //	DRIVER( inves ) 	/*												  */
        driver_inves,
        //	DRIVER( tk90x ) 	/*												  */
        driver_tk90x,
        //	DRIVER( tk95 )		/*												  */
        driver_tk95,
        //	DRIVER( tc2048 )	/*												  */
        driver_tc2048,
        //	DRIVER( ts2068 )	/*												  */
        driver_ts2068,
        /*TODO*///
        //	DRIVER( spec128 )	/* Spectrum 									  */
        driver_spec128,
        //	DRIVER( spec128s )	/* Spectrum 									  */
        driver_spec128s,
        //	DRIVER( specpls2 )	/* Spectrum 									  */
        driver_specpls2,
        //	DRIVER( specpl2a )	/* Spectrum 									  */
        driver_specpl2a,
        //	DRIVER( specp2fr )	/*												  */
        driver_specp2fr,
        //	DRIVER( specp2sp )	/*												  */
        driver_specp2sp,
        //	DRIVER( specpls3 )	/* Spectrum Plus 3								  */
        driver_specpls3,
        //	DRIVER( specp3sp )	/*												  */
        driver_specp3sp,
        //	DRIVER( specpl3e )	/*												  */
        driver_specpl3e,
        //	DRIVER( specpls4 )	/*
        driver_specpls4,
        //	DRIVER( scorpio )	/*
        driver_scorpion,
        //	DRIVER( pentagon )	/*
        driver_pentagon,
        /*TODO*///#endif
        /*TODO*///
        /*TODO*///	/* ASCII & MICROSOFT */
        /*TODO*///	DRIVER( msx )		/* MSX											  */
        driver_msx,
        /*TODO*///	DRIVER( msxj )		/* MSX Jap										  */
        driver_msxj,
        /*TODO*///	DRIVER( msxkr ) 	/* MSX Korean									  */
        driver_msxkr,
        /*TODO*///	DRIVER( msxuk ) 	/* MSX UK										  */
        driver_msxuk,
        /*TODO*///
        /*TODO*///	/* NASCOM MICROCOMPUTERS */
        /*TODO*///	DRIVER( nascom1 )	/* Nascom 1 									  */
        /*TODO*///	DRIVER( nascom2 )	/* Nascom 2 									  */
        /*TODO*///
        /*TODO*///	/* ACORN */
        /*TODO*///#ifndef MESS_EXCLUDE_ACORN
        /*TODO*///	DRIVER( atom )		/* Acorn Atom									  */
        /*TODO*///	DRIVER( bbca )		/* BBC Micro									  */
        /*TODO*///	DRIVER( bbcb )		/* BBC Micro									  */
        /*TODO*///	DRIVER( a310 )		/* Acorn Archimedes 310 						  */
        /*TODO*///#endif
        /*TODO*///
        /*TODO*///	/* MILES GORDON TECHNOLOGY */
        /*TODO*///	DRIVER( coupe ) 	/*												  */
        /*TODO*///
        /*TODO*///#ifndef MESS_EXCLUDE_SHARP
        /*TODO*///	/* SHARP */
        /*TODO*///	DRIVER( pc1251 )	/* Pocket Computer 1251 						  */
        /*TODO*///	DRIVER( pc1401 )	/* Pocket Computer 1401 						  */
        /*TODO*///	DRIVER( pc1402 )	/* Pocket Computer 1402 						  */
        /*TODO*///	DRIVER( pc1350 )	/* Pocket Computer 1350 						  */
        /*TODO*///
        /*TODO*///	DRIVER( mz700 ) 	/* Sharp MZ700									  */
        /*TODO*///	DRIVER( mz700j )	/* Sharp MZ700 Japan							  */
        /*TODO*///#endif
        /*TODO*///
        /*TODO*///	/* MOTOROLA */
        /*TODO*////*	DRIVER( mekd2 )*/ 	/* Motorola Evaluation Kit						  */
        /*TODO*///
        /*TODO*///	/* DEC */
        /*TODO*///	DRIVER( pdp1 )		/* DEC PDP1 for SPACEWAR! - 1962				  */
        /*TODO*///
        /*TODO*///	/* MEMOTECH */
        /*TODO*///	DRIVER( mtx512 )	/* Memotech MTX512								  */
        /*TODO*///
        /*TODO*///	/* MATTEL */
        /*TODO*///	DRIVER( aquarius )	/*												  */
        /*TODO*///
        /*TODO*////****************OTHERS******************************************************/
        /*TODO*///
        /*TODO*///#if 0
        /*TODO*///    DRIVER( arcadia )   /* Arcadia 2001                                   */
        /*TODO*///	DRIVER( atarist )	/* Atari ST 									  */
        /*TODO*///	DRIVER( channelf )	/* Fairchild Channel F VES - 1976				  */
        /*TODO*///	DRIVER( coco2 ) 	/* Color Computer 2 							  */
        /*TODO*///	DRIVER( intv )		/* Mattel Intellivision - 1979 AKA INTV 		  */
        /*TODO*///	DRIVER( jaguar )	/* Atari Jaguar 								  */
        /*TODO*///	DRIVER( lynx )		/* Atari Lynx Handheld							  */
        /*TODO*///	DRIVER( odyssey )	/* Magnavox Odyssey - analogue (1972)			  */
        /*TODO*///	DRIVER( trs80_m2 )	/* TRS-80 Model II -							  */
        /*TODO*///	DRIVER( x68000 )	/* X68000										  */
        /*TODO*///#endif
        null
    };
}
