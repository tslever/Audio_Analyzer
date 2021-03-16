package wav_assembling_utilities;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.sound.midi.Sequence;
import java.util.ArrayList;
import java.util.TreeMap;
import midi_parsing_utilities.MidiParser;
import midi_parsing_utilities.TickCommandKeyAndVelocity;


public class WavAssembler {

	private static double samplingFrequency = 44100.0;
	private static double microsecondsPerSecond = 1000000.0;
	private static double theoreticalMaximumOfTimeSeries = 32767.0;
	private static int sizeOfShort = 2;
	
	public static short[] assembleTimeSeriesBasedOn(
		Sequence sequence, TreeMap<Integer, ArrayList<TickCommandKeyAndVelocity>> treeMapOfChannelsAndArrayLists)
		throws StartTickHasNoCorrespondingEndTickException {
		
		int timeSeriesDataPerTick = (int)Math.ceil(
			samplingFrequency / microsecondsPerSecond *
			(double)sequence.getMicrosecondLength() / (double)sequence.getTickLength()
		);
		
		double[] timeSeries = new double[(int)sequence.getTickLength() * timeSeriesDataPerTick];
		
		int i;
		TickCommandKeyAndVelocity tickCommandNoteOnKeyAndVelocity;
		int startTick;
		boolean startTickHasNoCorrespondingEndTick;
		int j;
		TickCommandKeyAndVelocity tickCommandNoteOffKeyAndVelocity;
		int endTick = -1;
		int lengthOfSinusoidInTicks;
		double noteFrequency;
		
		for (int channel : treeMapOfChannelsAndArrayLists.keySet()) {
			
			ArrayList<TickCommandKeyAndVelocity> arrayList = treeMapOfChannelsAndArrayLists.get(channel);
			
			for (i = 0; i < arrayList.size(); i++) {
				
				tickCommandNoteOnKeyAndVelocity = arrayList.get(i);
				
				if (tickCommandNoteOnKeyAndVelocity.getCommand() == MidiParser.getNoteOff()) continue;
				
				startTick = tickCommandNoteOnKeyAndVelocity.getTick();
				
				startTickHasNoCorrespondingEndTick = true;
				
				for (j = i + 1; j < arrayList.size(); j++) {
					
					tickCommandNoteOffKeyAndVelocity = arrayList.get(j);
					
					if (tickCommandNoteOffKeyAndVelocity.getCommand() == MidiParser.getNoteOn()) continue;
					
					if ((tickCommandNoteOffKeyAndVelocity.getKey() == tickCommandNoteOnKeyAndVelocity.getKey()) &&
						(!tickCommandNoteOffKeyAndVelocity.getUsedToInsertSinusoidInTimeSeries())) {
						
						endTick = tickCommandNoteOffKeyAndVelocity.getTick();
						
						tickCommandNoteOffKeyAndVelocity.setUsedToInsertSinusoidInTimeSeries();
						
						startTickHasNoCorrespondingEndTick = false;
						
						break;
						
					}
					
				}
				
				if (startTickHasNoCorrespondingEndTick) {
					throw new StartTickHasNoCorrespondingEndTickException("Start tick has no corresponding end tick.");
				}
				
				lengthOfSinusoidInTicks = endTick - startTick;
				
				for (j = 0; j < lengthOfSinusoidInTicks * timeSeriesDataPerTick; j++) {
					
					noteFrequency = 440.0 * Math.pow(2.0, ((double)tickCommandNoteOnKeyAndVelocity.getKey()-69.0)/12.0);
					
					timeSeries[startTick * timeSeriesDataPerTick + j] +=
						(double)tickCommandNoteOnKeyAndVelocity.getVelocity() / 2.0
						* Math.sin(2.0 * Math.PI * noteFrequency / samplingFrequency * (double)j) + 1.0;
					
				}
				
			}
			
		}
		
		double actualMaximumOfTimeSeries = -1.0;
		for (i = 0; i < timeSeries.length; i++ ) {
			if (timeSeries[i] > actualMaximumOfTimeSeries) {
				actualMaximumOfTimeSeries = timeSeries[i];
			}
		}
		
		short[] timeSeriesAsShortArray = new short[timeSeries.length];
		for (i = 0; i < timeSeries.length; i++) {
			timeSeriesAsShortArray[i] =
				(short)(timeSeries[i] / actualMaximumOfTimeSeries * theoreticalMaximumOfTimeSeries);
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
