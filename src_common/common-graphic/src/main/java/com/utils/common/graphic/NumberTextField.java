package com.utils.common.graphic;

import javafx.scene.control.TextField;

/**
 * A TextField that only accepts numeric values
 */
public class NumberTextField extends TextField {

    /**
     * Constructor
     */
    public NumberTextField() {

        super();
    }

    /**
     * Constructor
     * @param number initial value
     */
    public NumberTextField(int number) {

        super(String.valueOf(number));
    }

    @Override
    public void replaceText(int start, int end, String text) {

        if(validate(text)) {

            super.replaceText(start, end, text);
        }
    }

    @Override
    public void replaceSelection(String text) {

        if(validate(text)) {

            super.replaceSelection(text);
        }
    }

    /**
     * Same as {@link TextField#getText()} but already parsed as a number
     * @return the field value
     */
    public int getNumber() {

        try {

            return Integer.valueOf(getText());
        }
        catch(NumberFormatException e) {

            return 0;
        }
    }

    private boolean validate(String text) {

        return text.matches("[0-9]*");
    }
}
