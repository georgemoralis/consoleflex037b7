package WIP.arcadeflex.libc;

/**
 *
 * void * memcpy ( void * destination, const void * source, size_t num ); Copy
 * block of memory Copies the values of num bytes from the location pointed to
 * by source directly to the memory block pointed to by destination.
 *
 * The underlying type of the objects pointed to by both the source and
 * destination pointers are irrelevant for this function; The result is a binary
 * copy of the data.
 *
 * The function does not check for any terminating null character in source - it
 * always copies exactly num bytes.
 *
 * To avoid overflows, the size of the arrays pointed to by both the destination
 * and source parameters, shall be at least num bytes, and should not overlap
 * (for overlapping memory blocks, memmove is a safer approach).
 */
import static WIP.arcadeflex.libc_v2.*;

public class memcpy {

    public static void memcpy(UBytePtr dst, int dstoffs, UBytePtr src, int size) {
        for (int i = 0; i < Math.min(size, src.memory.length); i++) {
            dst.write(i + dstoffs, src.read(i));
        }
    }

    public static void memcpy(UBytePtr dst, int dstoffs, int[] src, int size) {
        for (int i = 0; i < Math.min(size, src.length); i++) {
            dst.write(i + dstoffs, src[i]);
        }
    }

    public static void memcpy(UBytePtr dst, int[] src, int size) {
        for (int i = 0; i < Math.min(size, src.length); i++) {
            dst.write(i, src[i]);
        }
    }
    public static void memcpy(UBytePtr dst, char[] src, int size) {
        for (int i = 0; i < Math.min(size, src.length); i++) {
            dst.write(i, src[i]);
        }
    }

    public static void memcpy(UBytePtr dst, UBytePtr src, int size) {
        for (int i = 0; i < Math.min(size, src.memory.length); i++) {
            dst.write(i, src.read(i));
        }
    }
    public static void memcpy(UBytePtr dst,int dstoffs, UBytePtr src,int srcoffs, int size) {
        for (int i = 0; i < Math.min(size, src.memory.length); i++) {
            dst.write(i+dstoffs, src.read(i+srcoffs));
        }
    }
    public static void memcpy(UBytePtr dst,UBytePtr src,int srcoffs, int size) {
        for (int i = 0; i < Math.min(size, src.memory.length); i++) {
            dst.write(i, src.read(i+srcoffs));
        }
    }

    public static void memcpy(char[] dst, char[] src, int size) {
        System.arraycopy(src, 0, dst, 0, Math.min(size, src.length));
    }
}
