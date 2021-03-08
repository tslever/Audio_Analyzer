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
public class NotOneInputException extends Exception {
    public NotOneInputException(String errorMessage) {
        super(errorMessage);
    }
}
