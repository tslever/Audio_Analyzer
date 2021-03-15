package wav_assembling_utilities;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;
import javax.sound.midi.Sequence;
import midi_parsing_utilities.MidiParser;
import midi_parsing_utilities.TickCommandKeyAndVelocity;


public class WavAssembler {
	
	
	private static double samplingFrequency = 44100.0;
	private static double microsecondsPerSecond = 1000000.0;
	private static int maximumNumberOfChannels = 16;
	private static int numberOfMidiKeys = 128;
	private static int sizeOfShort = 2;
	private static double theoreticalMaximumValueOfTimeSeries = 32767;

	
	public static short[] assembleTimeSeriesBasedOn(
		Sequence sequence, TreeMap<Integer, ArrayList<TickCommandKeyAndVelocity>> treeMapOfChannelsAndArrayLists)
		throws NoteAlreadyOnException, NoteAlreadyOffException {
		
		int timeSeriesDataPerTick = (int)Math.ceil(
			samplingFrequency / microsecondsPerSecond *
			(double)sequence.getMicrosecondLength() / (double)sequence.getTickLength()
		);
		
		double[] timeSeries = new double[(int)sequence.getTickLength() * timeSeriesDataPerTick];
		
		int[][] velocities = new int[maximumNumberOfChannels][numberOfMidiKeys];
		
		int j;
		double noteFrequency;
		int k;
		for (int i = 0; i < sequence.getTickLength(); i++) {
			
			for (int channel : treeMapOfChannelsAndArrayLists.keySet()) {
				
				//for (TickCommandKeyAndVelocity tickCommandKeyAndVelocity : treeMapOfChannelsAndArrayLists.get(channel)) {
				Iterator<TickCommandKeyAndVelocity> iterator = treeMapOfChannelsAndArrayLists.get(channel).iterator();
				while (iterator.hasNext()) {
					TickCommandKeyAndVelocity tickCommandKeyAndVelocity = iterator.next();
				
					if (tickCommandKeyAndVelocity.getTick() > i) {
						continue;
					}
					
					else if (tickCommandKeyAndVelocity.getTick() == i) {
						
						if (tickCommandKeyAndVelocity.getCommand() == MidiParser.getNoteOn()) {
						
							if (velocities[channel][tickCommandKeyAndVelocity.getKey()] > 0) {
								throw new NoteAlreadyOnException("Note already on.");
							}
							
							velocities[channel][tickCommandKeyAndVelocity.getKey()] =
								tickCommandKeyAndVelocity.getVelocity();
							
						}
						
						if (tickCommandKeyAndVelocity.getCommand() == MidiParser.getNoteOff()) {
							
							if (velocities[channel][tickCommandKeyAndVelocity.getKey()] == 0) {
								throw new NoteAlreadyOffException("Note already off.");
							}
							
							velocities[channel][tickCommandKeyAndVelocity.getKey()] = 0;
							
						}
						
						//treeMapOfChannelsAndArrayLists.get(channel).remove(tickCommandKeyAndVelocity);
						iterator.remove();
						
					}
					
				}
				
				for (j = 0; j < numberOfMidiKeys; j++) {
					
					if (velocities[channel][j] > 0) {
						
						noteFrequency = 440.0 * Math.pow(2.0, ((double)j-69.0)/12.0);
						
						for (k = i * timeSeriesDataPerTick; k < (i+1) * timeSeriesDataPerTick; k++) {
							
							timeSeries[k] += velocities[channel][j]/2.0 * Math.sin(2.0 * Math.PI * noteFrequency / samplingFrequency * (double)k) + 1.0;
							
						}
						
					}
					
				}
				
			}
			
		}
		
		double actualMaximumValueOfTimeSeries = -1.0;
		for (int i = 0; i < timeSeries.length; i++ ) {
			if (timeSeries[i] > actualMaximumValueOfTimeSeries) {
				actualMaximumValueOfTimeSeries = timeSeries[i];
			}
		}
		
		short[] timeSeriesAsShortArray = new short[timeSeries.length];
		for (int i = 0; i < timeSeries.length; i++) {
			timeSeriesAsShortArray[i] =
				(short)(timeSeries[i] / actualMaximumValueOfTimeSeries * theoreticalMaximumValueOfTimeSeries);
		}
		
		return timeSeriesAsShortArray;
		
	}
	
	
	public static void writeToWavFile(short[] timeSeries, String filename)
		throws FileNotFoundException, IOException {
		
		int numberOfBytesInTimeSeries = timeSeries.length * sizeOfShort;
		int numberOfBytesInWavFile = 44 + numberOfBytesInTimeSeries;
		
		byte[] dataForWavFile = new byte[numberOfBytesInWavFile];
		
		// Write RIFF header.
		dataForWavFile[0] = 'R';
		dataForWavFile[1] = 'I';
		dataForWavFile[2] = 'F';
		dataForWavFile[3] = 'F';
		
		int numberOfBytesAfterFirstEightBytes = numberOfBytesInWavFile - 8;
		byte[] bytesRepresentingNumberOfBytesAfterFirstEightBytes = intToByteArray(numberOfBytesAfterFirstEightBytes);
		dataForWavFile[4] = bytesRepresentingNumberOfBytesAfterFirstEightBytes[0];
		dataForWavFile[5] = bytesRepresentingNumberOfBytesAfterFirstEightBytes[1];
		dataForWavFile[6] = bytesRepresentingNumberOfBytesAfterFirstEightBytes[2];
		dataForWavFile[7] = bytesRepresentingNumberOfBytesAfterFirstEightBytes[3];
		
		dataForWavFile[8] = 'W';
		dataForWavFile[9] = 'A';
		dataForWavFile[10] = 'V';
		dataForWavFile[11] = 'E';
		
        // Write format subchunk.
		dataForWavFile[12] = 'f';
		dataForWavFile[13] = 'm';
		dataForWavFile[14] = 't';
		dataForWavFile[15] = ' ';
		
        int numberOfBytesInFormatSubchunkAfterFirstEightBytes = 16;
        byte[] bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes =
            intToByteArray(numberOfBytesInFormatSubchunkAfterFirstEightBytes);
        dataForWavFile[16] = bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes[0];
        dataForWavFile[17] = bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes[1];
        dataForWavFile[18] = bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes[2];
        dataForWavFile[19] = bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes[3];
        
        short audioFormat = 1;
        byte[] bytesRepresentingAudioFormat = shortToByteArray(audioFormat);
        dataForWavFile[20] = bytesRepresentingAudioFormat[0];
        dataForWavFile[21] = bytesRepresentingAudioFormat[1];
        
        short numberOfChannels = 1;
        byte[] bytesRepresentingNumberOfChannels = shortToByteArray(numberOfChannels);
        dataForWavFile[22] = bytesRepresentingNumberOfChannels[0];
        dataForWavFile[23] = bytesRepresentingNumberOfChannels[1];
        
        byte[] bytesRepresentingSampleRate = intToByteArray((int)samplingFrequency);
        dataForWavFile[24] = bytesRepresentingSampleRate[0];
        dataForWavFile[25] = bytesRepresentingSampleRate[1];
        dataForWavFile[26] = bytesRepresentingSampleRate[2];
        dataForWavFile[27] = bytesRepresentingSampleRate[3];
        
        int byteRate = (int)numberOfChannels * (int)samplingFrequency * sizeOfShort;
        byte[] bytesRepresentingByteRate = intToByteArray(byteRate);
        dataForWavFile[28] = bytesRepresentingByteRate[0];
        dataForWavFile[29] = bytesRepresentingByteRate[1];
        dataForWavFile[30] = bytesRepresentingByteRate[2];
        dataForWavFile[31] = bytesRepresentingByteRate[3];
        
        short blockAlign = (short)((int)numberOfChannels * sizeOfShort);
        byte[] bytesRepresentingBlockAlign = shortToByteArray(blockAlign);
        dataForWavFile[32] = bytesRepresentingBlockAlign[0];
        dataForWavFile[33] = bytesRepresentingBlockAlign[1];
        
        byte[] bytesRepresentingBitsPerSample = shortToByteArray((short)(sizeOfShort * 8));
        dataForWavFile[34] = bytesRepresentingBitsPerSample[0];
        dataForWavFile[35] = bytesRepresentingBitsPerSample[1];
        
        // Write data subchunk.
        dataForWavFile[36] = 'd';
        dataForWavFile[37] = 'a';
        dataForWavFile[38] = 't';
        dataForWavFile[39] = 'a';
        
        byte[] bytesRepresentingNumberOfBytesInData = intToByteArray(numberOfBytesInTimeSeries);
        dataForWavFile[40] = bytesRepresentingNumberOfBytesInData[0];
        dataForWavFile[41] = bytesRepresentingNumberOfBytesInData[1];
        dataForWavFile[42] = bytesRepresentingNumberOfBytesInData[2];
        dataForWavFile[43] = bytesRepresentingNumberOfBytesInData[3];
        
        byte[] byteArrayRepresentingSample;
        //double pitchFrequency = 440.0;
        for (int i = 0; i < timeSeries.length; i++) {
            //byteArrayRepresentingSample = shortToByteArray((short)(32767.0/2.0 *
            //    (Math.sin(2.0 * Math.PI * pitchFrequency / (double)samplingFrequency * (double)i) + 1.0)));
            byteArrayRepresentingSample = shortToByteArray(timeSeries[i]);
        	dataForWavFile[44 + 2*i] = byteArrayRepresentingSample[0];
            dataForWavFile[44 + 2*i + 1] = byteArrayRepresentingSample[1];
        }
        
        FileOutputStream fileOutputStream =
        	new FileOutputStream(filename.substring(0, filename.length()-3) + "wav"); // throws FileNotFoundException
        fileOutputStream.write(dataForWavFile); // throws IOException
        fileOutputStream.close();
		
	}
	
	
	private static byte[] intToByteArray(int intToUse) {
        return new byte[] {
            (byte)intToUse,
            (byte)((intToUse >> 8) & 0xFF),
            (byte)((intToUse >> 16) & 0xFF),
            (byte)((intToUse >> 24) & 0xFF),
        };
	}
	
	
    private static byte[] shortToByteArray(short shortToUse) {
        return new byte[] {
            (byte)shortToUse,
            (byte)((shortToUse >> 8) & 0xFF)
        };
    }
	
}
