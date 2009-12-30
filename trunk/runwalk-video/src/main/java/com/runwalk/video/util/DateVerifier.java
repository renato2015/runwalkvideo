package com.runwalk.video.util;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;


    public class DateVerifier extends InputVerifier{


    @Override
        public boolean shouldYieldFocus(JComponent input) {
            boolean inputOK = verify(input);

            if (inputOK) {
                return true;
            }


            String failedVerificationMessage = "Date must be in the MMM DD, YYYY format. For example: Apr 17, 2008";
            JOptionPane.showMessageDialog(null, //no owner frame
                                          failedVerificationMessage, 
                                          "Invalid Date Format", //title
                                          JOptionPane.WARNING_MESSAGE);

            //Reinstall the input verifier.
            input.setInputVerifier(this);
            return false;
        }
      public boolean verify(JComponent input) {
        if (!(input instanceof JFormattedTextField))
          return true; 
                return ((JFormattedTextField) input).isEditValid();
      }

    }
