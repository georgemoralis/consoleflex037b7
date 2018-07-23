/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
 /*
 *  Ported to 0.37b7
 */
package WIP.mame;

import static WIP.arcadeflex.fucPtr.*;

public class sndintrfH {

    public static class MachineSound {

        public MachineSound(int sound_type, Object sound_interface) {
            this.sound_type = sound_type;
            this.sound_interface = sound_interface;
        }

        public MachineSound() {
            this(0, null);
        }

        public static MachineSound[] create(int n) {
            MachineSound[] a = new MachineSound[n];
            for (int k = 0; k < n; k++) {
                a[k] = new MachineSound();
            }
            return a;
        }

        public int sound_type;
        public Object sound_interface;
    }

    public static final int SOUND_DUMMY = 0;
    public static final int SOUND_CUSTOM = 1;
    public static final int SOUND_SAMPLES = 2;
    public static final int SOUND_DAC = 3;
    public static final int SOUND_AY8910 = 4;
    public static final int SOUND_YM2203 = 5;
    public static final int SOUND_YM2151 = 6;
    public static final int SOUND_YM2608 = 7;
    public static final int SOUND_YM2610 = 8;
    public static final int SOUND_YM2610B = 9;
    public static final int SOUND_YM2612 = 10;
    public static final int SOUND_YM3438 = 11;/* same as YM2612 */
    public static final int SOUND_YM2413 = 12;/* YM3812 with predefined instruments */
    public static final int SOUND_YM3812 = 13;
    public static final int SOUND_YM3526 = 14;/*100% YM3812 compatible, less features */
    public static final int SOUND_YMZ280B = 15;
    public static final int SOUND_Y8950 = 16;/* YM3526 compatible with delta-T ADPCM */
    public static final int SOUND_SN76477 = 17;
    public static final int SOUND_SN76496 = 18;
    public static final int SOUND_POKEY = 19;
    public static final int SOUND_TIA = 20;/* stripped down Pokey */
    public static final int SOUND_NES = 21;
    public static final int SOUND_ASTROCADE = 22;/* Custom I/O chip from Bally/Midway */
    public static final int SOUND_NAMCO = 23;
    public static final int SOUND_TMS36XX = 24;/* currently TMS3615 and TMS3617 */
    public static final int SOUND_TMS5110 = 25;
    public static final int SOUND_TMS5220 = 26;
    public static final int SOUND_VLM5030 = 27;
    public static final int SOUND_ADPCM = 28;
    public static final int SOUND_OKIM6295 = 29;/* ROM-based ADPCM system */
    public static final int SOUND_MSM5205 = 30;/* CPU-based ADPCM system */
    public static final int SOUND_UPD7759 = 31;/* ROM-based ADPCM system */
    public static final int SOUND_HC55516 = 32;/* Harris family of CVSD CODECs */
    public static final int SOUND_K005289 = 33;/* Konami 005289 */
    public static final int SOUND_K007232 = 34;/* Konami 007232 */
    public static final int SOUND_K051649 = 35;/* Konami 051649 */
    public static final int SOUND_K053260 = 36;/* Konami 053260 */
    public static final int SOUND_SEGAPCM = 37;
    public static final int SOUND_RF5C68 = 38;
    public static final int SOUND_CEM3394 = 39;
    public static final int SOUND_C140 = 40;
    public static final int SOUND_QSOUND = 41;
    public static final int SOUND_SAA1099 = 42;
    public static final int SOUND_SPEAKER = 43;
    public static final int SOUND_WAVE = 44;
    public static final int SOUND_BEEP = 45;
    public static final int SOUND_COUNT = 46;


    /* structure for SOUND_CUSTOM sound drivers */
    public static class CustomSound_interface {

        public CustomSound_interface(ShStartPtr sh_start, ShStopPtr sh_stop, ShUpdatePtr sh_update) {
            this.sh_start = sh_start;
            this.sh_stop = sh_stop;
            this.sh_update = sh_update;
        }

        public ShStartPtr sh_start;
        public ShStopPtr sh_stop;
        public ShUpdatePtr sh_update;

    }
}
