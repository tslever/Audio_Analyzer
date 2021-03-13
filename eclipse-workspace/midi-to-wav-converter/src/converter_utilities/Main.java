package converter_utilities;


import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.util.ArrayList;
import java.util.TreeMap;
import midi_parsing_utilities.MidiParser;
import midi_parsing_utilities.TickCommandKeyAndVelocity;


public class Main {

	public static void main(String[] args) throws NotOneInputException, IOException, InvalidMidiDataException {

		InputParsingUtilities.checkNumberOfInputsIn(args); // throws NotOneInputException
		
		String filename = args[0];
		File file = new File(filename);
		Sequence sequence = MidiSystem.getSequence(file); // throws IOException, InvalidMidiDataException
		
		TreeMap<Integer, ArrayList<TickCommandKeyAndVelocity>> treeMapOfChannelsAndArrayLists =
			MidiParser.getTreeMapOfChannelsAndArrayListsFrom(sequence);
		
		for (int channel : treeMapOfChannelsAndArrayLists.keySet()) {
			for (TickCommandKeyAndVelocity tickCommandKeyAndVelocity : treeMapOfChannelsAndArrayLists.get(0)) {
				System.out.println(
					"Channel: " + channel + " | " +
					"Tick: " + tickCommandKeyAndVelocity.getTick() + " | " +
					"Command: " + tickCommandKeyAndVelocity.getCommand() + " | " +
					"Key: " + tickCommandKeyAndVelocity.getKey() + " | " +
					"Velocity: " + tickCommandKeyAndVelocity.getVelocity()
				);
			}	
		}

	}


}
