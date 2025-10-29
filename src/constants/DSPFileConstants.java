package constants;

public class DSPFileConstants {

    //data offsets
    public static final long SAMPLE_RATE_OFFSET = 0x8;
    public static final long LOOP_FLAG_OFFSET = 0xC;
    public static final long LOOP_START_OFFSET = 0x10;
    public static final long AUDIO_DATA_OFFSET = 0x60;

    //data length
    public static final int SAMPLE_RATE_LENGTH_IN_BYTES = 4;
    public static final int LOOP_FLAG_LENGTH_IN_BYTES = 2;
    public static final int LOOP_START_LENGTH_IN_BYTES = 4;
}