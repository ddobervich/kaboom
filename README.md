# kaboom
Template code for our audio recognition project

To use:  
create /music directory and add .wav files of all songs  
create /prints directory to store .csv files for song fingerprints  

Run indexMusicLibrary.java to loop over all .wav files in /music and create a .csv file for each in /prints  
Run performMatch.java to load all .csv files in /prints and match audio from your mic with the music database from /prints  

# notes
You can display the audio wave, the fft frequency spectrum, or the spectrograph of either the mic or an audio file with the various Plotter classes.  

performMatch currently only uses strongest frequencies from 3 different frequency bands as the fingerprints.  It has a lot of false positive hits as a result.  To improve this, use combinatorial hashing as described in https://www.ee.columbia.edu/~dpwe/papers/Wang03-shazam.pdf  
