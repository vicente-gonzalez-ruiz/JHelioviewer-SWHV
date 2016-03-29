package org.helioviewer.jhv.data.datatype.event;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.helioviewer.jhv.base.Regex;

public class JHVEventParameter {
    /** The name of the parameter */
    private String parameterName;

    /** How the parameter is displayed */
    private String parameterDisplayName;

    /** The value of the parameter */
    private String parameterValue;

    /**
     * Creates a JHVEvent parameter with a parameter name, parameter display
     * name and parameter value.
     *
     * @param parameterName
     *            the parameter name
     * @param parameterDisplayName
     *            the parameter display name
     * @param parameterValue
     *            the parameter value
     */
    public JHVEventParameter(String parameterName, String parameterDisplayName, String parameterValue) {
        this.parameterName = parameterName.intern();
        this.parameterDisplayName = parameterDisplayName.intern();
        this.parameterValue = parameterValue.intern();
    }

    /**
     * Gets the parameter name.
     *
     * @return the name of the parameter
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Sets the parameter name.
     *
     * @param parameterName
     *            the name of the parameter
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Gets the parameter display name.
     *
     * @return the parameter display name
     */
    public String getParameterDisplayName() {
        return parameterDisplayName;
    }

    /**
     * Sets the parameter display name.
     *
     * @param parameterDisplayName
     *            the display name of the parameter
     */
    public void setParameterDisplayName(String parameterDisplayName) {
        this.parameterDisplayName = parameterDisplayName;
    }

    /**
     * Gets the value of the parameter.
     *
     * @return the value of the parameter
     */
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * Sets the value of the parameter.
     *
     * @param parameterValue
     *            the value of the parameter
     */
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    /**
     * Gets the parameter display value
     */
    public String getDisplayParameterValue() {
        return beautifyParameterValue(parameterValue);
    }

    private String beautifyParameterValue(String parameterValue) {
        if (isDouble(parameterValue) && !isInteger(parameterValue)) {
            NumberFormat formatter = new DecimalFormat("0.###E0");
            String result = formatter.format(Double.parseDouble(parameterValue));
            if (result.contains("E0")) {
                return result.substring(0, result.length() - 2);
            } else {
                return result;
            }
        } else {
            return parameterValue;
        }
    }

    private boolean isDouble(String value) {
        return value != null && Regex.FloatingPoint.matcher(value).matches();
    }

    private boolean isInteger(String value) {
        return value != null && Regex.Integer.matcher(value).matches();
    }

    @Override
    public String toString() {
        return parameterValue;
    }

}
