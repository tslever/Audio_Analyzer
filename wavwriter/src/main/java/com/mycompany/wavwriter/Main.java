/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.midi.to.annotated.wav.vconverter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Math;


/**
 *
 * @author thoma
 */
public class Main {
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        int sampleRate = 44100;
        int numberOfSamples = sampleRate * 10;
        short numberOfChannels = 1;
        short bitsPerSample = 16;
        int numberOfBytesOfData =
            numberOfSamples * (int)numberOfChannels * (int)bitsPerSample/8;
        int numberOfBytesInWAVFile = numberOfBytesOfData + 44;
        
        byte[] audio = new byte[numberOfBytesInWAVFile];
        
        
        // Write RIFF header.
        audio[0] = 'R';
        audio[1] = 'I';
        audio[2] = 'F';
        audio[3] = 'F';
        
        int numberOfBytesAfterFirstEightBytes = numberOfBytesInWAVFile - 8;
        byte[] bytesRepresentingNumberOfBytesAfterFirstEightBytes =
            intToByteArray(numberOfBytesAfterFirstEightBytes);
        audio[4] = bytesRepresentingNumberOfBytesAfterFirstEightBytes[0];
        audio[5] = bytesRepresentingNumberOfBytesAfterFirstEightBytes[1];
        audio[6] = bytesRepresentingNumberOfBytesAfterFirstEightBytes[2];
        audio[7] = bytesRepresentingNumberOfBytesAfterFirstEightBytes[3];
        
        audio[8] = 'W';
        audio[9] = 'A';
        audio[10] = 'V';
        audio[11] = 'E';
        
        
        // Write format subchunk.
        audio[12] = 'f';
        audio[13] = 'm';
        audio[14] = 't';
        audio[15] = ' ';
        
        int numberOfBytesInFormatSubchunkAfterFirstEightBytes = 16;
        byte[] bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes =
            intToByteArray(numberOfBytesInFormatSubchunkAfterFirstEightBytes);
        audio[16] = bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes[0];
        audio[17] = bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes[1];
        audio[18] = bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes[2];
        audio[19] = bytesRepresentingNumberOfBytesInFormatSubchunkAfterFirstEightBytes[3];
        
        short audioFormat = 1;
        byte[] bytesRepresentingAudioFormat = shortToByteArray(audioFormat);
        audio[20] = bytesRepresentingAudioFormat[0];
        audio[21] = bytesRepresentingAudioFormat[1];
        
        byte[] bytesRepresentingNumberOfChannels =
            shortToByteArray(numberOfChannels);
        audio[22] = bytesRepresentingNumberOfChannels[0];
        audio[23] = bytesRepresentingNumberOfChannels[1];
        
        byte[] bytesRepresentingSampleRate = intToByteArray(sampleRate);
        audio[24] = bytesRepresentingSampleRate[0];
        audio[25] = bytesRepresentingSampleRate[1];
        audio[26] = bytesRepresentingSampleRate[2];
        audio[27] = bytesRepresentingSampleRate[3];
        
        int byteRate =
            (int)numberOfChannels * sampleRate * (int)bitsPerSample/8;
        byte[] bytesRepresentingByteRate = intToByteArray(byteRate);
        audio[28] = bytesRepresentingByteRate[0];
        audio[29] = bytesRepresentingByteRate[1];
        audio[30] = bytesRepresentingByteRate[2];
        audio[31] = bytesRepresentingByteRate[3];
        
        short blockAlign =
            (short)((int)numberOfChannels * (int)bitsPerSample/8);
        byte[] bytesRepresentingBlockAlign = shortToByteArray(blockAlign);
        audio[32] = bytesRepresentingBlockAlign[0];
        audio[33] = bytesRepresentingBlockAlign[1];
        
        byte[] bytesRepresentingBitsPerSample = shortToByteArray(bitsPerSample);
        audio[34] = bytesRepresentingBitsPerSample[0];
        audio[35] = bytesRepresentingBitsPerSample[1];
        
        
        // Write data subchunk.
        audio[36] = 'd';
        audio[37] = 'a';
        audio[38] = 't';
        audio[39] = 'a';
        
        byte[] bytesRepresentingNumberOfBytesInData =
            intToByteArray(numberOfBytesOfData);
        audio[40] = bytesRepresentingNumberOfBytesInData[0];
        audio[41] = bytesRepresentingNumberOfBytesInData[1];
        audio[42] = bytesRepresentingNumberOfBytesInData[2];
        audio[43] = bytesRepresentingNumberOfBytesInData[3];
        
        byte[] byteArrayRepresentingSample;
        double pitchFrequency = 440.0;
        for (int i = 0; i < numberOfSamples; i++) {
            byteArrayRepresentingSample = shortToByteArray((short)(32767.0/2.0 *
                (Math.sin(2.0 * Math.PI * pitchFrequency / (double)sampleRate * (double)i) + 1.0)));
            audio[44 + 2*i] = byteArrayRepresentingSample[0];
            audio[44 + 2*i + 1] = byteArrayRepresentingSample[1];
        }
        
        FileOutputStream fileOutputStream = new FileOutputStream("A440.wav");
        // throws FileNotFoundException
        fileOutputStream.write(audio);
        // throws IOException
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
