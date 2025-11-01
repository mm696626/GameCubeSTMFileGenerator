# GameCubeSTMFileGenerator

### GameCube STM File Generator
* A tool allows you to generate STM files (the music files used in Cubivore: Survival of the Fittest, Fire Emblem: Path of Radiance, and Paper Mario: The Thousand-Year Door)
* All of these games were developed by Intelligent Systems. If there are any more games this format is used in, simply rename the file to whatever the file name is

### Music Replacement Notes
* Audio must be in DSP format split into two channels
* This tool will attempt to auto select the other track of your song, so name it either with the same file name appended with _L and _R or (channel 0) and (channel 1)
    * Example: mario_L.dsp and mario_R.dsp or luigi (channel 0).dsp and luigi (channel 1).dsp
* If you are using LoopingAudioConverter, do edits like sample rate to a BRSTM before converting to DSP
  * If you don't, it will mess up the loop point to the point the game would crash if the STM is loaded (the tool will actually check that and stop the output STM from being created)
  * You can get around this by putting your song into an MP3 or WAV first using LoopingAudioConverter or vgmstream and then creating the DSPs that way
* For nonlooping jingles and such, put your sound into an MP3 or WAV and use LoopingAudioConverter to make it a start to end looping DSP
  * Be sure to press the Fix Nonlooping STM Header button before using your STM in game
* To ensure your STM will work in game, open it with vgmstream and see if it loads (if it doesn't, try it in game to see since I've had cases where it didn't load there, but worked perfectly in game. If that doesn't work, then try again)
  * https://katiefrogs.github.io/vgmstream-web/

### Mono STM Files
* Cubivore: Survival of the Fittest has none
* Fire Emblem: Path of Radiance
  * gcfe_bgm_evt_mist2_32k.stm
* Paper Mario: The Thousand-Year Door
  * evt_dot1_32k.stm
  * ff_disc_set1_32k.stm
  * ff_mail_chakusin1_32k.stm
  * ff_mail_chakusin2_32k.stm
  * ff_mail_chakusin3_32k.stm
  * stg_rsh_a1_32k.stm
  * stg_rsh_b1_32k.stm
* There is support to write such files if the game crashes if they're stereo 
  * The game doesn't crash, but it's neat anyway
  * Of course, stereo STMs are the most common format, but it's an option with the Generate Mono STM button
    * Everything other generation option uses stereo

### STM Format Documentation
* STM Header
  * 0x02 bytes - Version Number? (always 512)
  * 0x02 bytes - Sample Rate
  * 0x04 bytes - Channel Count (most of the time it's 2) (if mono, then 1)
  * 0x04 bytes - Audio Channel Data Length (it's the audio data + whatever rounding gets you to the next 0x20 boundary. If the file size is divisible, then keep it as is)
  * 0x04 bytes - STM Loop Start (must be the start loop in the DSP header - 2 / 2) (the game will crash if the lowest byte isn't 0x02, 0x42, 0x82, or 0xC2 since the loop in the STM header must be exactly divisible by 0x20) (All 4 bytes are FF if non looping)
  * 0x04 bytes - Audio Channel Data Length (same as above)
  * 0x04 bytes - Audio Channel Data Length (same as above)
  * 0x04 bytes - STM Loop Start (same as above) (All 4 bytes are 00 if non looping)
  * 0x04 bytes - STM Loop Start (same as above) (All 4 bytes are 00 if non looping)
  * 0x20 bytes of 00 padding

* The Rest of the STM
  * Left Channel DSP Header (0x60 bytes)
  * Right Channel DSP Header (fill with 0's if mono) (0x60 bytes)
  * Left Channel Audio Data (pad it to the next 0x20 boundary. If already 0x20 aligned, then nothing)
  * 0x20 bytes of padding (used for interleaving) (still there if mono)
  * Right Channel Audio Data (pad it to the next 0x20 boundary. If already 0x20 aligned, then nothing) (only write if stereo)
  * 0x8000 bytes of 00 padding at the end of the file

### Special Thanks/Credits
* This documentation on the STM header and DSP format helped a lot too
    * https://www.metroid2002.com/retromodding/wiki/DSP_(File_Format)
    * https://hack64.net/wiki/doku.php?id=paper_mario_the_thousand_year_door:soundfolder
* Big shout out to these sources for helping me identify the song names
  * https://hack64.net/wiki/doku.php?id=paper_mario_the_thousand_year_door:stm_list
  * https://www.youtube.com/playlist?list=PLESFnlO3kNnqwJTeHkRuB9Ak10pajrKo-
  * https://serenesforest.net/music/path-of-radiance-game-rip/
  * https://fireemblem.fandom.com/wiki/List_of_Music_in_Fire_Emblem:_Path_of_Radiance
  * https://www.youtube.com/playlist?list=PLRB_l-VPlUGuvp2vDehWYGH9acnZ6qgsQ