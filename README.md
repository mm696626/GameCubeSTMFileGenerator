# GameCubeSTMFileGenerator

### GameCube STM File Generator
* A tool allows you to generate STM files (the music files used in Paper Mario: The Thousand-Year Door, Fire Emblem: Path of Radiance, and Cubivore)

### Music Notes
* Audio must be in DSP format split into two channels
* This tool will attempt to auto select the other track of your song, so name it either with the same file name appended with _L and _R or (channel 0) and (channel 1)
    * Example: mario_L.dsp and mario_R.dsp or luigi (channel 0).dsp and luigi (channel 1).dsp

### Music Replacement Notes
* If you are using LoopingAudioConverter, do edits like sample rate and volume to a BRSTM before converting to DSP
  * If you don't, it will mess up the loop point to the point the game would crash if the STM is loaded
* For nonlooping jingles and such, Put your sound into an MP3 or WAV and use LoopingAudioConverter to make it a start to end looping DSP
  * Be sure to press the Fix Nonlooping STM Header button before using your STM in game
  * If you don't do this hacky workaround with nonlooping audio, the game will also crash
    * I have no idea why this happens
* To ensure your STM will work in game, open it with vgmstream and see if it loads
  * https://katiefrogs.github.io/vgmstream-web/

### Special Thanks/Credits
* This documentation on the STM header and DSP format helped a lot too
    * https://www.metroid2002.com/retromodding/wiki/DSP_(File_Format)
    * https://hack64.net/wiki/doku.php?id=paper_mario_the_thousand_year_door:soundfolder