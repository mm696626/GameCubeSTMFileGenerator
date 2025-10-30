package constants;

public class DSPFileConstants {

    //data offsets
    public static final long SAMPLE_RATE_OFFSET = 0x08;
    public static final long LOOP_START_OFFSET = 0x10;
    public static final long AUDIO_DATA_OFFSET = 0x60;

    //data length
    public static final int LOOP_START_LENGTH_IN_BYTES = 0x04;
    public static final int DSP_HEADER_LENGTH_IN_BYTES = 0x60;
}