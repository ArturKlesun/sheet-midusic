package BackEnd;

import GraphTmp.GraphMusica;
import Musica.NotnyStan;

import java.io.OutputStream;
import	java.io.PrintStream;

import	javax.sound.midi.MidiMessage;
import	javax.sound.midi.ShortMessage;
import	javax.sound.midi.Receiver;



/**	Displays the file format information of a MIDI file.
 */
public class DumpReceiver
        implements	Receiver
{

    public static long seByteCount = 0;
    public static long smByteCount = 0;
    public static long seCount = 0;
    public static long smCount = 0;

    private NotnyStan stan;

    public DumpReceiver(NotnyStan stan) {
        this.stan = stan;
    }

    public void close() {}
    long sPrev = 0;
    public void send(MidiMessage message, long lTimeStamp) {
        lTimeStamp /= 1000;
        short outp;
        if (lTimeStamp - sPrev < Short.MAX_VALUE) outp = (short)(lTimeStamp - sPrev);
        else outp = Short.MAX_VALUE;
        sPrev = lTimeStamp;
        

        int tune = ((ShortMessage) message).getData1();
        int forca = ((ShortMessage)message).getData2();
        System.out.println("Midi-message: " + tune + " " + forca + " ");
        if (tune <= 32 || tune >= 100) {
        	// Handle instrument change/pitch-bend/tune/etc // Actually, useless
        	return;
        }
        stan.addNote( tune, forca, outp );

    }

}
