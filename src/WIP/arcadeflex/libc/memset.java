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
package WIP.arcadeflex.libc;

import static WIP.arcadeflex.libc_v2.*;

public class memset {

    public static void memset(UBytePtr ptr, int value, int length) {
        for (int i = 0; i < length; i++) {
            ptr.write(i, value);
        }
    }

    public static void memset(int[] dst, int value, int size) {
        for (int mem = 0; mem < size; mem++) {
            dst[mem] = value;
        }
    }

    public static void memset(short[] dst, int value, int size) {
        for (int mem = 0; mem < size; mem++) {
            dst[mem] = (short) value;
        }
    }
}
