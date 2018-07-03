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
/**
 * ported to 0.37b7
 */
package WIP.sound;

public class tms36xxH {

    public static final int MAX_TMS36XX = 4;

    /* subtypes */
    public static final int MM6221AA = 21;
    /* Phoenix (fixed melodies) */
    public static final int TMS3615 = 15;
    /* Naughty Boy, Pleiads (13 notes, one output) */
    public static final int TMS3617 = 17;

    /* Monster Bash (13 notes, six outputs) */

 /* The interface structure */
    public static class TMS36XXinterface {

        public TMS36XXinterface(int num, int[] mixing_level, int[] subtype, int[] basefreq, double[][] decay){
            this.num = num;
            this.mixing_level = mixing_level;
            this.subtype = subtype;
            this.basefreq = basefreq;
            this.decay = decay;
            this.speed = new double[MAX_TMS36XX];
        }
        public TMS36XXinterface(int num, int[] mixing_level, int[] subtype, int[] basefreq, double[][] decay, double[] speed) {
            this.num = num;
            this.mixing_level = mixing_level;
            this.subtype = subtype;
            this.basefreq = basefreq;
            this.decay = decay;
            this.speed = speed;
        }
        int num;
        int[] mixing_level;//[MAX_TMS36XX];
        int[] subtype;//[MAX_TMS36XX];
        int[] basefreq;//[MAX_TMS36XX];		/* base frequecnies of the chips */
        double[][] decay;//[MAX_TMS36XX][6];	/* decay times for the six harmonic notes */
        double[] speed;//[MAX_TMS36XX];		/* tune speed (meaningful for the TMS3615 only) */
    }

}
