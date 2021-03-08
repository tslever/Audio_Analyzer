/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.midireader;


import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
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
    
    /*private static final float PPQ = 0.0f;
    private static final float SMPTE_24 = 24.0f;
    private static final float SMPTE_25 = 25.0f;
    private static final float SMPTE_30 = 30.0f;
    private static final float SMPTE_30DROP = 29.97f;*/
    
    private static final int NOTE_ON = 0x90;
    private static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES =
        {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    public static void main(String[] args)
        throws NotOneInputException,
               InvalidMidiDataException,
               IOException,
               FunctionalityNotImplementedException {
        
        InputParsingUtilities.checkNumberOfInputsIn(args);
        // throws NotOneInputException
        
        String filename = args[0];
        File file = new File(filename);
        Sequence sequence = MidiSystem.getSequence(file);
        // throws InvalidMidiDataException, IOException
        /*if (!(sequence.getDivisionType() == PPQ)) {
            throw new FunctionalityNotImplementedException(
                "No functionality implemented to handle SMPTE division type.");
        }*/
        System.out.println(
            "Sequence sequence" + "\n" +
            "Length of sequence in ticks: " + sequence.getTickLength() + "\n" +
            "Length of sequence in microseconds: " +
            sequence.getMicrosecondLength());
        
        int trackNumber = 0;
        for (Track track : sequence.getTracks()) {
            
            trackNumber++;
            System.out.println(
                "-Ss-Track " + trackNumber + "\n" +
                "-Ss-Length of track in ticks: " + track.ticks() + "\n" +
                "-Ss-Number of MidiEvents in track: " + track.size());
            
            for (int i = 0; i < track.size(); i++) {
                
                MidiEvent midiEvent = track.get(i);
                System.out.println(
                    "-Ss--T" + trackNumber + "-" +
                    "MidiEvent " + i + "\n" +
                    "-Ss--T" + trackNumber + "-" +
                    "Tick of MidiEvent: " + midiEvent.getTick());
                
                MidiMessage midiMessage = midiEvent.getMessage();
                if (midiMessage instanceof ShortMessage) {
                    ShortMessage shortMessage = (ShortMessage)midiMessage;
                    System.out.println(
                        "-Ss--T" + trackNumber + "-" +
                        "Class of MidiMessage of MidiEvent: ShortMessage" + "\n" +
                        "-Ss--T" + trackNumber + "-" +
                        "MIDI channel of ShortMessage of MidiEvent: " +
                        shortMessage.getChannel());
                    if (shortMessage.getCommand() == NOTE_ON) {
                        String command = "NOTE_ON";
                        int pianoKey = shortMessage.getData1();
                        int note = pianoKey % 12;
                        String noteName = NOTE_NAMES[note];
                        int octave = (pianoKey / 12) - 1;
                        int velocity = shortMessage.getData2();
                        System.out.println(
                            "-Ss--T" + trackNumber + "-" +
                            "Command of ShortMessage of MidiEvent: " + command + "\n" +
                            "-Ss--T" + trackNumber + "-" +
                            "Pitch of ShortMessage of MidiEvent: " + noteName + octave + "\n" +
                            "-Ss--T" + trackNumber + "-" +
                            "Velocity of ShortMessage of MidiEvent: " + velocity);
                    }
                    else if (shortMessage.getCommand() == NOTE_ON) {
                        String command = "NOTE_OFF";
                        int pianoKey = shortMessage.getData1();
                        int note = pianoKey % 12;
                        String noteName = NOTE_NAMES[note];
                        int octave = (pianoKey / 12) - 1;
                        int velocity = shortMessage.getData2();
                        System.out.println(
                            "-Ss--T" + trackNumber + "-" +
                            "Command of ShortMessage of MidiEvent: " + command + "\n" +
                            "-Ss--T" + trackNumber + "-" +
                            "Pitch of ShortMessage of MidiEvent: " + noteName + octave + "\n" +
                            "-Ss--T" + trackNumber + "-" +
                            "Velocity of ShortMessage of MidiEvent: " + velocity);
                    }
                    else {
                        System.out.println(
                            "-Ss--T" + trackNumber + "-" +
                            "Command of ShortMessage of MidiEvent: " + shortMessage.getCommand());
                    }
                }
                else if (midiMessage instanceof MetaMessage) {
                    MetaMessage metaMessage = (MetaMessage)midiMessage;
                    System.out.println(
                        "-Ss--T" + trackNumber + "-" +
                        "Class of MidiMessage of MidiEvent: MetaMessage");
                    //metaMessage.
                }
                else {
                    System.out.println(
                        "-Ss--T" + trackNumber + "-" +
                        "Class of MidiMessage of MidiEvent: " + midiMessage.getClass());
                }
            }
        }
    }
}
