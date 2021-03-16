package converter_utilities;


import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import midi_parsing_utilities.MidiParser;
import midi_parsing_utilities.TickCommandKeyAndVelocity;
import wav_assembling_utilities.StartTickHasNoCorrespondingEndTickException;
import wav_assembling_utilities.WavAssembler;
//import wav_assembling_utilities.NoteAlreadyOnException;
//import wav_assembling_utilities.NoteAlreadyOffException;


public class Main {
	
	public static void main(String[] args)
		throws NotOneInputException,
			   IOException,
			   InvalidMidiDataException,
			   StartTickHasNoCorrespondingEndTickException {

		InputParsingUtilities.checkNumberOfInputsIn(args); // throws NotOneInputException
		
		String filename = args[0];
		File file = new File(filename);
		Sequence sequence = MidiSystem.getSequence(file); // throws IOException, InvalidMidiDataException
		
		TreeMap<Integer, ArrayList<TickCommandKeyAndVelocity>> treeMapOfChannelsAndArrayLists =
			MidiParser.getTreeMapOfChannelsAndArrayListsFrom(sequence);
		//printTableOfChannelsTicksCommandsKeysAndVelocities(treeMapOfChannelsAndArrayLists);
		
		short[] timeSeries = WavAssembler.assembleTimeSeriesBasedOn(sequence, treeMapOfChannelsAndArrayLists);

		WavAssembler.writeToWavFile(timeSeries, filename);
		
	}

	
	/*private static void printTableOfChannelsTicksCommandsKeysAndVelocities(
		TreeMap<Integer, ArrayList<TickCommandKeyAndVelocity>> treeMapOfChannelsAndArrayLists) {
		
		for (int channel : treeMapOfChannelsAndArrayLists.keySet()) {
			for (TickCommandKeyAndVelocity tickCommandKeyAndVelocity : treeMapOfChannelsAndArrayLists.get(channel)) {
				System.out.println(
					"Channel: " + channel + " | " +
					"Tick: " + tickCommandKeyAndVelocity.getTick() + " | " +
					"Command: " + tickCommandKeyAndVelocity.getCommand() + " | " +
					"Key: " + tickCommandKeyAndVelocity.getKey() + " | " +
					"Velocity: " + tickCommandKeyAndVelocity.getVelocity()
				);
			}
		}
		
	}*/

}
