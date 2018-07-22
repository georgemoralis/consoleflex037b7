/**
 * changelog
 * 28/08/2017 - Added ShortPtr(short[] m) constructor
 */
package WIP.arcadeflex;

import java.util.Random;

/**
 * @author shadow
 */
public class libc_v2 {

    private static Random rand = new Random();
    public static int argc;
    public static String[] argv;

    /*
     *   return next random number
     */
    public static int rand() {
        return rand.nextInt();
    }

    /*
     *  Convert command-line parameters
     */
    public static void ConvertArguments(String mainClass, String[] arguments) {
        argc = arguments.length + 1;
        argv = new String[argc];
        argv[0] = mainClass;
        for (int i = 1; i < argc; i++) {
            argv[i] = arguments[i - 1];
        }
    }

    /**
     * function equals to c sprintf syntax
     *
     * @param str
     * @param arguments
     * @return
     */
    public static String sprintf(String str, Object... arguments) {
        return String.format(str, arguments);
    }

    /**
     * function equals to c memcmp function
     */
    public static int memcmp(char[] dist, int dstoffs, String src, int size) {
        char[] srcc = src.toCharArray();
        for (int i = 0; i < size; i++) {
            if (dist[(dstoffs + i)] != srcc[i]) {
                return -1;

            }
        }
        return 0;
    }

    /**
     * Returns the sizeof an char array
     */
    public static int sizeof(char[] array) {
        return array.length;
    }
    public static int sizeof(int[] array) {
        return array.length;
    }

    /**
     * memset
     */
    public static void memset(UBytePtr buf, int offset, int value, int size) {
        for (int i = 0; i < size; i++) {
            buf.write(i + offset, value);
        }
    }

    public static void memset(ShortPtr buf, int value, int size) {
        for (int i = 0; i < size; i++) {
            buf.write(i, (short) value);
        }
    }

    /**
     * function equals to c bool
     */
    public static int BOOL(int value) {
        return value != 0 ? 1 : 0;
    }

    public static int BOOL(boolean value) {
        return value ? 1 : 0;
    }

    /**
     * function equals to c NOT
     */
    public static int NOT(int value) {
        return value == 0 ? 1 : 0;
    }

    public static int NOT(boolean value) {
        return !value ? 1 : 0;
    }

    /**
     * ***********************************
     * <p>
     * Unsigned Byte Pointer Emulation ***********************************
     */
    public static class UBytePtr {

        public int bsize = 1;
        public char[] memory;
        public int offset;

        public UBytePtr() {
        }

        public UBytePtr(int size) {
            memory = new char[size];
            offset = 0;
        }

        public UBytePtr(char[] m) {
            set(m, 0);
        }

        public UBytePtr(char[] m, int b) {
            set(m, b);
        }

        public UBytePtr(UBytePtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public UBytePtr(UBytePtr cp) {
            set(cp.memory, cp.offset);
        }

        public void set(char[] m, int b) {
            memory = m;
            offset = b;
        }

        public void set(char[] m) {
            memory = m;
            offset = 0;
        }

        public void set(UBytePtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public void inc() {
            offset += bsize;
        }

        public void dec() {
            offset -= bsize;
        }

        public void inc(int count) {
            offset += count * bsize;
        }

        public void dec(int count) {
            offset -= count * bsize;
        }

        public char read() {
            return (char) (memory[offset] & 0xFF);
        }

        public int READ_WORD(int index) {
            //return (char)(((memory[offset + 1 + index] << 8)& 0xFF) | (memory[offset + index]& 0xFF));
            /*return (char)((( memory[offset+index]&0xFF) << 0)
             | (( memory[offset+index + 1]&0xFF) << 8));*/
            return memory[offset + index + 1] << 8 | memory[offset + index];

        }

        public int READ_DWORD(int index)//unchecked!
        {
            /*return( ((memory[offset + 3 + index] << 24)& 0xFF)
             | ((memory[offset + 2 + index] << 16)& 0xFF)
             | ((memory[offset + 1 + index] << 8)& 0xFF)
             | ((memory[offset + index]& 0xFF)));*/
            int myNumber = ((memory[offset + index] & 0xFF) << 0)
                    | ((memory[offset + index + 1] & 0xFF) << 8)
                    | ((memory[offset + index + 2] & 0xFF) << 16)
                    | ((memory[offset + index + 3] & 0xFF) << 24);
            return myNumber;
        }

        public char read(int index) {
            //if(offset+index>memory.length-1)
            //    return 0;
            //System.out.println("libc_v2 offset: "+offset);
            //System.out.println("libc_v2 offset: "+index);
            //System.out.println("libc_v2 memory: "+memory.length);
            
            try {
            return (char) (memory[offset + index] & 0xFF); //return only the first 8bits
            } catch (Exception e){
                System.out.println("libc_v2 offset: "+offset);
                System.out.println("libc_v2 index: "+index);
                System.out.println("libc_v2 memory: "+memory.length);
                e.printStackTrace(System.out);
            }
            return memory[0];
        }

        public char readinc() {
            return (char) ((memory[(this.offset++)]) & 0xFF);
        }

        public char readdec() {
            return (char) ((memory[(this.offset--)]) & 0xFF);
        }

        public void WRITE_WORD(int index, int value) {
            memory[offset + index + 1] = (char) (value >> 8 & 0xFF);
            memory[offset + index] = (char) (value & 0xFF);
            //memory[offset + index] = (char)(value & 0xFF);
            //memory[offset + index+ 1] = (char)((value >> 8)&0xFF);
        }

        public void write(int index, int value) {            
               memory[offset + index] = (char) (value & 0xFF);//store 8 bits only             
        }

        public void write(int value) {
            memory[offset] = (char) (value & 0xFF);//store 8 bits only
        }

        public void writeinc(int value) {
            this.memory[(this.offset++)] = (char) (value & 0xFF);//store 8 bits only
        }

        public void writedec(int value) {
            this.memory[(this.offset--)] = (char) (value & 0xFF);//store 8 bits only
        }
    }

    /*
     *     Unsigned Short Ptr emulation
     *
     */
    public static class UShortPtr {

        public int bsize = 2;
        public char[] memory;
        public int offset;

        public UShortPtr() {
        }

        public UShortPtr(int size) {
            memory = new char[size];
            offset = 0;
        }

        public UShortPtr(char[] m) {
            set(m, 0);
        }

        public UShortPtr(char[] m, int b) {
            set(m, b);
        }

        public UShortPtr(UShortPtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public UShortPtr(UShortPtr cp) {
            set(cp.memory, cp.offset);
        }

        public UShortPtr(UBytePtr cp) {
            set(cp.memory, cp.offset);
        }

        public UShortPtr(UBytePtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public void set(char[] m, int b) {
            memory = m;
            offset = b;
        }

        public void set(char[] m) {
            memory = m;
            offset = 0;
        }

        public char read(int index) {
            return (char) (memory[offset + 1 + index * 2] << 8 | memory[offset + index * 2]);
        }

        public void write(int index, char value) {
            memory[offset + index * 2] = (char) (value & 0xFF);
            memory[offset + index * 2 + 1] = (char) ((value >> 8) & 0xFF);
        }

    }

    /**
     * Byte Pointer emulation
     */
    public static class BytePtr {

        public int bsize = 1;
        public byte[] memory;
        public int offset;

        public BytePtr() {
        }

        public BytePtr(int size) {
            memory = new byte[size];
            offset = 0;
        }

        public BytePtr(byte[] m) {
            set(m, 0);
        }

        public BytePtr(BytePtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public BytePtr(UBytePtr cp) {
            memory = new byte[cp.memory.length];
            for (int i = 0; i < cp.memory.length; i++) {
                memory[i] = (byte) cp.memory[i];
            }
            offset = cp.offset;
        }

        public void set(byte[] m) {
            memory = m;
            offset = 0;
        }

        public void set(byte[] m, int offs) {
            memory = m;
            offset = offs;
        }

        public byte read() {
            return (memory[offset]);
        }

        public byte read(int index) {
            return (memory[offset + index]);
        }
    }

    public static class ShortPtr {

        public int bsize = 2;
        public byte[] memory;
        public int offset;

        public ShortPtr() {
        }

        public ShortPtr(int size) {
            memory = new byte[size * bsize];
            offset = 0;
        }

        public ShortPtr(byte[] m) {
            set(m, 0);
        }

        public ShortPtr(ShortPtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public ShortPtr(ShortPtr cp) {
            set(cp.memory, cp.offset);
        }

        public ShortPtr(short[] m) {
            memory = new byte[m.length * 2];
            for (int i = 0; i < m.length; i++) {
                memory[i * 2] = (byte) (m[i] & 0xff);
                memory[i * 2 + 1] = (byte) ((m[i] >>> 8) & 0xff);
            }
            offset = 0;
        }

        public void set(byte[] m) {
            memory = m;
            offset = 0;
        }

        public void set(byte[] m, int offs) {
            memory = m;
            offset = offs;
        }

        public short read() {
            return (short) ((memory[offset + 1] & 0xFF) << 8 | (memory[offset] & 0xFF));
        }

        public short read(int index) {
            return (short) ((memory[offset + 1 + index * 2] & 0xFF) << 8 | (memory[offset + index * 2] & 0xFF));
        }

        public short readinc() {
            short read = (short) ((memory[offset + 1] & 0xFF) << 8 | (memory[offset] & 0xFF));
            offset += bsize;
            return read;
        }

        public void write(int index, short data) {
            memory[offset + index * 2] = (byte) (data & 0xff);
            memory[offset + index * 2 + 1] = (byte) ((data >>> 8) & 0xff);
        }

        public void write(short data) {
            memory[offset] = (byte) (data & 0xff);
            memory[offset + 1] = (byte) ((data >>> 8) & 0xff);
        }

        public void writeinc(short data) {
            memory[offset] = (byte) (data & 0xff);
            memory[offset + 1] = (byte) ((data >>> 8) & 0xff);
            offset += bsize;
        }

        public void inc(int count) {
            offset += count * bsize;
        }

        public void inc() {
            offset += bsize;
        }

        public void dec(int count) {
            offset -= count * bsize;
        }
    }
    public static class UByteArray {

        public UByteArray(int size) {
            memory = new char[size];
            offset = 0;
        }

        public UByteArray(char[] m) {
            set(m, 0);
        }

        public UByteArray(char[] m, int b) {
            set(m, b);
        }

        public UByteArray(UBytePtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public UByteArray(UByteArray cp, int b) {
            set(cp.memory, cp.offset + b);
        }
        public UByteArray(UByteArray cp) {
            set(cp.memory, cp.offset);
        }

        public char read(int offs) {
            return (char)(memory[offs + offset]&0xFF);
        }

        public char read() {
            return (char)(memory[offset]&0xFF);
        }

        public void write(int offs, int value) {
            memory[offset + offs] = (char) (value&0xFF);
        }

        public void set(char[] m, int b) {
            memory = m;
            offset = b;
        }

        public char[] memory;
        public int offset;
    }
    public static class UShortArray {

        public UShortArray(int size) {
            memory = new char[size];
            offset = 0;
        }

        public UShortArray(char[] m) {
            set(m, 0);
        }

        public UShortArray(char[] m, int b) {
            set(m, b);
        }

        public UShortArray(UBytePtr cp, int b) {
            set(cp.memory, cp.offset + b);
        }

        public UShortArray(UShortArray cp, int b) {
            set(cp.memory, cp.offset + b);
        }
        public UShortArray(UShortArray cp) {
            set(cp.memory, cp.offset);
        }

        public char read(int offs) {
            return memory[offs + offset];
        }

        public char read() {
            return memory[offset];
        }

        public void write(int offs, int value) {
            memory[offset + offs] = (char) value;
        }

        public void set(char[] m, int b) {
            memory = m;
            offset = b;
        }

        public char[] memory;
        public int offset;
    }

    public static class IntSubArray {

        public int[] buffer;
        public int offset;

        public IntSubArray(int size) {
            this.buffer = new int[size];
            this.offset = 0;
        }

        public IntSubArray(int[] buffer) {
            this.buffer = buffer;
            this.offset = 0;
        }

        public IntSubArray(IntSubArray subarray) {
            this.buffer = subarray.buffer;
            this.offset = subarray.offset;
        }

        public IntSubArray(IntSubArray subarray, int offset) {
            this.buffer = subarray.buffer;
            this.offset = subarray.offset + offset;
        }

        public IntSubArray(int[] buffer, int offset) {
            this.buffer = buffer;
            this.offset = offset;
        }

        public int read() {
            return buffer[offset];
        }

        public int readinc() {
            return buffer[offset++];
        }

        public int read(int index) {
            return buffer[index + offset];
        }

        public void write(int value) {
            buffer[offset] = value;
        }

        public void write(int index, int value) {
            buffer[index + offset] = value;
        }

        public void writeinc(int value) {
            buffer[offset++] = value;
        }

        public void writedec(int value) {
            buffer[offset--] = value;
        }

    }

    /**
     * Convert a char array to an unsigned integer
     *
     * @param b
     * @return
     */
    public static long charArrayToLong(char[] b) {
        int start = 0;
        int i = 0;
        int len = 4;
        int cnt = 0;
        char[] tmp = new char[len];
        for (i = start; i < (start + len); i++) {
            tmp[cnt] = b[i];
            cnt++;
        }
        long accum = 0;
        i = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
            accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
            i++;
        }
        return accum;
    }

    /**
     * Convert a char array to a unsigned short
     *
     * @param b
     * @return
     */
    public static int charArrayToInt(char[] b) {
        int start = 0;
        int low = b[start] & 0xff;
        int high = b[start + 1] & 0xff;
        return (int) (high << 8 | low);
    }
}
