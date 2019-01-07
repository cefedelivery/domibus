package eu.domibus.core.crypto;

import mockit.Deencapsulation;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class DomibusPropertyProviderImplTest {

    private String customValue;

    private Object result;

    private String propertyName = "domibus.property.name";

    private Properties domibusProperties = new Properties();

    private Properties domibusDefaultProperties = new Properties();

    private DomibusPropertyProviderImpl domibusPropertyProvider = new DomibusPropertyProviderImpl();

    @Before
    public void setUp() {
        Deencapsulation.setField(domibusPropertyProvider, "domibusProperties", domibusProperties);
        Deencapsulation.setField(domibusPropertyProvider, "domibusDefaultProperties", domibusDefaultProperties);
    }

    @Test(expected = IllegalStateException.class)
    public void throwsIllegalStateExceptionWhenRetrievingAnIntegerPropertyHavingBothItsCustomValueAndItsDefaultValueMissing() {
        givenMissingCustomValue();
        givenMissingDefaultValue();

        whenRetrievingTheIntegerProperty();
    }

    @Test(expected = IllegalStateException.class)
    public void throwsIllegalStateExceptionWhenRetrievingAnIntegerPropertyHavingItsCustomValueMissingAndItsDefaultValueInvalid() {
        givenMissingCustomValue();
        givenDefaultValue("INVALID_INTEGER_VALUE");

        whenRetrievingTheIntegerProperty();
    }

    @Test
    public void returnsTheDefaultValueWhenRetrievingAnIntegerPropertyHavingItsCustomValueMissingAndItsDefaultValueValid() {
        givenMissingCustomValue();
        givenDefaultValue("42");

        whenRetrievingTheIntegerProperty();

        thenPropertyValueTakenFromDefaults("The integer property value should have been taken from the default properties when the custom value is missing", 42);
    }

    @Test
    public void returnsTheDefaultValueWhenRetrievingAnIntegerPropertyHavingItsCustomValueInvalidAndItsDefaultValueValid() {
        givenCustomValue("INVALID_INTEGER_VALUE");
        givenDefaultValue("-13");

        whenRetrievingTheIntegerProperty();

        thenPropertyValueTakenFromDefaults("The integer property value should have been taken from the default properties when the custom value is invalid", -13);
    }

    @Test
    public void returnsTheCustomValueWhenRetrievingAnIntegerPropertyHavingItsCustomValueValid_IgnoringValidDefaultValue() {
        givenCustomValue("1659");
        givenDefaultValue("0");

        whenRetrievingTheIntegerProperty();

        thenPropertyValueTakenFromDefaults(
                "The integer property value should have been taken from the custom properties when the custom value valid (ignores valid default value)", 1659);
    }

    @Test
    public void returnsTheCustomValueWhenRetrievingAnIntegerPropertyHavingItsCustomValueValid_IgnoringMissingDefaultValue() {
        givenCustomValue("1");
        givenMissingDefaultValue();

        whenRetrievingTheIntegerProperty();

        thenPropertyValueTakenFromDefaults(
                "The integer property value should have been taken from the custom properties when the custom value valid (ignores missing default value)", 1);
    }

    @Test
    public void returnsTheCustomValueWhenRetrievingAnIntegerPropertyHavingItsCustomValueValid_IgnoringInvalidDefaultValue() {
        givenCustomValue("-0712853");
        givenDefaultValue("INVALID_INTEGER_VALUE");

        whenRetrievingTheIntegerProperty();

        thenPropertyValueTakenFromDefaults(
                "The integer property value should have been taken from the custom properties when the custom value valid (ignores invalid default value)", -712853);
    }

    @Test(expected = IllegalStateException.class)
    public void throwsIllegalStateExceptionWhenRetrievingABooleanPropertyHavingBothItsCustomValueAndItsDefaultValueMissing() {
        givenMissingCustomValue();
        givenMissingDefaultValue();

        whenRetrievingTheBooleanProperty();
    }

    @Test(expected = IllegalStateException.class)
    public void throwsIllegalStateExceptionWhenRetrievingABooleanPropertyHavingItsCustomValueMissingAndItsDefaultValueInvalid() {
        givenMissingCustomValue();
        givenDefaultValue("INVALID_BOOLEAN_VALUE");

        whenRetrievingTheBooleanProperty();
    }

    @Test
    public void returnsTheDefaultValueWhenRetrievingABooleanPropertyHavingItsCustomValueMissingAndItsDefaultValueValid() {
        givenMissingCustomValue();
        givenDefaultValue("true");

        whenRetrievingTheBooleanProperty();

        thenPropertyValueTakenFromDefaults("The boolean property value should have been taken from the default properties when the custom value is missing", Boolean.TRUE);
    }

    @Test
    public void returnsTheDefaultValueWhenRetrievingABooleanPropertyHavingItsCustomValueInvalidAndItsDefaultValueValid() {
        givenCustomValue("INVALID_BOOLEAN_VALUE");
        givenDefaultValue("on");

        whenRetrievingTheBooleanProperty();

        thenPropertyValueTakenFromDefaults("The boolean property value should have been taken from the default properties when the custom value is invalid", Boolean.TRUE);
    }

    @Test
    public void returnsTheCustomValueWhenRetrievingAnBooleanPropertyHavingItsCustomValueValid_IgnoringValidDefaultValue() {
        givenCustomValue("T"); // "T" stands for "T[RUE]" so Boolean.TRUE --> check BooleanUtils#toBooleanObject(String)
        givenDefaultValue("0");

        whenRetrievingTheBooleanProperty();

        thenPropertyValueTakenFromDefaults(
                "The boolean property value should have been taken from the custom properties when the custom value valid (ignores valid default value)", true);
    }

    @Test
    public void returnsTheCustomValueWhenRetrievingAnBooleanPropertyHavingItsCustomValueValid_IgnoringMissingDefaultValue() {
        givenCustomValue("no");
        givenMissingDefaultValue();

        whenRetrievingTheBooleanProperty();

        thenPropertyValueTakenFromDefaults(
                "The boolean property value should have been taken from the custom properties when the custom value valid (ignores missing default value)", false);
    }

    @Test
    public void returnsTheCustomValueWhenRetrievingAnBooleanPropertyHavingItsCustomValueValid_IgnoringInvalidDefaultValue() {
        givenCustomValue("off");
        givenDefaultValue("INVALID_BOOLEAN_VALUE");

        whenRetrievingTheBooleanProperty();

        thenPropertyValueTakenFromDefaults(
                "The boolean property value should have been taken from the custom properties when the custom value valid (ignores invalid default value)", false);
    }


    private void givenMissingCustomValue() {
        givenCustomValue(null);
    }

    private void givenCustomValue(String customValue) {
        this.customValue = customValue;
    }

    private void givenMissingDefaultValue() {
        domibusDefaultProperties.remove(propertyName);
    }

    private void givenDefaultValue(String defaultValue) {
        domibusDefaultProperties.put(propertyName, defaultValue);
    }

    private void whenRetrievingTheIntegerProperty() {
        result = Deencapsulation.invoke(domibusPropertyProvider, "getIntegerInternal", new Class[] {String.class, String.class}, propertyName, customValue);
    }

    private void whenRetrievingTheBooleanProperty() {
        result = Deencapsulation.invoke(domibusPropertyProvider, "getBooleanInternal", new Class[] {String.class, String.class}, propertyName, customValue);
    }

    private void thenPropertyValueTakenFromDefaults(String message, Object expectedValue) {
        Assert.assertEquals(message, result, expectedValue);
    }
}