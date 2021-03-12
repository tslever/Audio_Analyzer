/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.midireader;


import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;


/**
 *
 * @author thoma
 */
public class Main {
    
    private static final int NOTE_ON = 0x90;
    private static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES =
        {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    public static void main(String[] args)
        throws NotOneInputException,
               InvalidMidiDataException,
               IOException {
        
        InputParsingUtilities.checkNumberOfInputsIn(args);
        // throws NotOneInputException
        
        String filename = args[0];
        File file = new File(filename);
        Sequence sequence = MidiSystem.getSequence(file);
        // throws InvalidMidiDataException, IOException
        System.out.println(
            "Sequence sequence" + "\n" +
            "Length of sequence in microseconds: " +
            sequence.getMicrosecondLength() + "\n" +
            "Length of sequence in ticks: " + sequence.getTickLength() + "\n");
        
        for (Track track : sequence.getTracks()) {    
            
            for (int i = 0; i < track.size(); i++) {
                
                MidiEvent midiEvent = track.get(i);
                
                MidiMessage midiMessage = midiEvent.getMessage();
                
                if (midiMessage instanceof ShortMessage) {
                    
                    ShortMessage shortMessage = (ShortMessage)midiMessage;
                    
                    if (shortMessage.getCommand() == NOTE_ON) {
                        
                        int pianoKey = shortMessage.getData1();
                        int note = pianoKey % 12;
                        String noteName = NOTE_NAMES[note];
                        int octave = (pianoKey / 12) - 1;                        
                        int velocity = shortMessage.getData2();
                        
                        System.out.println(
                            "Tick: " + midiEvent.getTick() + " | " +
                            "Channel: " + shortMessage.getChannel() + " | " +
                            "Command: NOTE_ON | " +
                            "Note: " + noteName + octave + " | " +
                            "Velocity: " + velocity);
                    }
                    else if (shortMessage.getCommand() == NOTE_OFF) {
                        
                        int pianoKey = shortMessage.getData1();
                        int note = pianoKey % 12;
                        String noteName = NOTE_NAMES[note];
                        int octave = (pianoKey / 12) - 1;
                        int velocity = shortMessage.getData2();
                        
                        System.out.println(
                            "Tick: " + midiEvent.getTick() + " | " +
                            "Channel: " + shortMessage.getChannel() + " | " +
                            "Command: NOTE_OFF | " +
                            "Note: " + noteName + octave + " | " +
                            "Velocity: " + velocity);
                    }
                    else { }
                    // shortMessage.getCommand() does not equal NOTE_ON.
                    // shortMessage.getCommand() does not equal NOTE_OFF.
                }
                else { }
                // midiMessage.getClass() does not result in ShortMessage.
            }
        }
    }
}
