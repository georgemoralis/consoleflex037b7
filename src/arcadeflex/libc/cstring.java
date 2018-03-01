package arcadeflex.libc;

/**
 *
 * @author shadow
 */
public class cstring {

    /**
     * Get string length
     *
     * @param str
     * @return
     */
    public static int strlen(String str) {
        return str.length();
    }
    /**
     * memset
     * @param dst
     * @param value
     * @param size 
     */
    public static void memset(char[] dst, int value, int size) {
        for (int mem = 0; mem < size; mem++) {
            dst[mem] = (char) value;
        }
    }
    /**
     * Locate last occurrence of character in string Returns a pointer to the last occurrence of character in the C string str.
     * @param str
     * @param ch
     * @return 
     */
    public static String strrchr(String str,char ch)
    {
        int found = str.lastIndexOf(ch);
        if(found==-1)//not found
        {
            return null;
        }
        else
        {
            return Integer.toString(found);//return in String
        }
    }
    /**
     * Locate first occurrence of character in string Returns a pointer to the first occurrence of character in the C string str.
     * @param str
     * @param ch
     * @return 
     */
    public static String strchr(String str,char ch)
    {
        int found = str.indexOf(ch);
        if(found==-1)//not found
        {
            return null;
        }
        else
        {
            return Integer.toString(found);//return in String
        }
    }
    
}
