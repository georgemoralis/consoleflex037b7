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
 * ported to v0.37b7
 *
 */
package WIP.sound;

public class samplesH {

    public static class Samplesinterface {

        public Samplesinterface(int chan, int vol, String[] names) {
            channels = chan;
            volume = vol;
            samplenames = names;
        }

        public int channels;
        /* number of discrete audio channels needed */
        public int volume;
        /* global volume for all samples */
        public String[] samplenames;
    }
}
