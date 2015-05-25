package com.carrotsearch.hppcrt;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Reused buffer visualization routines.
 */
public class KTypeBufferVisualizer<KType>
{
    /**
     * Visually depict the distribution of keys.
     * 
     * @param characters
     *          The number of characters to "squeeze" the entire buffer into.
     * @return Returns a sequence of characters where '.' depicts an empty
     *         fragment of the internal buffer and 'X' depicts full or nearly full
     *         capacity within the buffer's range and anything between 1 and 9 is between.
     */
    public static <KType> String visualizeKeyDistribution(
            /*! #if ($TemplateOptions.KTypeGeneric) !*/final Object[] /*! #else KType [] #end !*/buffer,
            final int characters) {

        final StringBuilder b = new StringBuilder();
        final char[] chars = ".123456789X".toCharArray();

        final int max = buffer.length - 1;

        for (int i = 1, start = -1; i <= characters; i++) {

            final int end = (int) ((long) i * max / characters);

            if (start + 1 <= end) {
                int taken = 0;
                int slots = 0;
                for (int slot = start + 1; slot <= end; slot++, slots++) {

                    if (!Intrinsics.<KType> isEmpty(buffer[slot])) {
                        taken++;
                    }
                }
                b.append(chars[Math.min(chars.length - 1, taken * chars.length / slots)]);
                start = end;
            }
        }
        while (b.length() < characters) {
            b.append(' ');
        }
        return b.toString();
    }
}
