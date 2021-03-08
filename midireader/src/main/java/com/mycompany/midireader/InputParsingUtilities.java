/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.midireader;

/**
 *
 * @author thoma
 */
public class InputParsingUtilities {
    
    public static void checkNumberOfInputsIn(String[] args)
        throws NotOneInputException {
        if (args.length != 1) {
            throw new NotOneInputException(
                "Length of argument string is not equal to 1.");
        }
    }
    
}
