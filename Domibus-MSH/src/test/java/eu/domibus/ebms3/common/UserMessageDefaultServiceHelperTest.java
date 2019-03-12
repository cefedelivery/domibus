package eu.domibus.ebms3.common;

import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.common.model.MessageProperties;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.PartyInfo;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.To;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.messaging.MessageConstants;
import mockit.Expectations;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static eu.domibus.ebms3.common.UserMessageDefaultServiceHelperTest.PartyIdBuilder.aPartyId;
import static eu.domibus.ebms3.common.UserMessageDefaultServiceHelperTest.PropertyBuilder.aProperty;

/**
 * @author Sebastian-Ion TINCU
 */
public class UserMessageDefaultServiceHelperTest {

    @Tested
    private UserMessageDefaultServiceHelper userMessageDefaultServiceHelper;

    private String originalSender;

    private String finalRecipient;

    private String partyTo;

    private UserMessage userMessage;

    private MessageProperties messageProperties;

    private boolean sameOriginalSender;

    private boolean sameFinalRecipient;

    @Test
    public void returnsNullWhenRetrievingTheOriginalSenderAndTheUserMessageIsNull() {
        givenNullUserMessage();

        whenRetrievingTheOriginalSender();

        thenOriginalSenderIsNull("The original sender should have been null when the user message is null");
    }

    @Test
    public void returnsNullWhenRetrievingTheOriginalSenderForUserMessagesHavingNullMessageProperties() {
        givenUserMessage();
        givenNullMessageProperties();

        whenRetrievingTheOriginalSender();

        thenOriginalSenderIsNull("The original sender should have been null when the user message has null message properties holder");
    }

    @Test
    public void returnsTheOriginalSenderPropertyValueIgnoringOtherPropertiesWhenRetrievingTheOriginalSenderForUserMessagesContainingTheCorrectProperty() {
        givenUserMessage();
        givenMessageProperties();
        givenProperties(
                aProperty().withName("dummy").withValue("dummy").build(),
                aProperty().withName(MessageConstants.FINAL_RECIPIENT).withValue("recipient").build(),
                aProperty().withName(MessageConstants.ORIGINAL_SENDER).withValue("sender").build(),
                aProperty().withName("another_dummy").withValue("another_dummy").build()
        );

        whenRetrievingTheOriginalSender();

        thenOriginalSenderIsEqualTo("sender", "The original sender should have been correctly set when the user message contains the original sender property");
    }

    @Test
    public void returnsNullWhenRetrievingTheFinalRecipientAndTheUserMessageIsNull() {
        givenNullUserMessage();

        whenRetrievingTheFinalRecipient();

        thenFinalRecipientIsNull("The final recipient should have been null when the user message is null");
    }

    @Test
    public void returnsNullWhenRetrievingTheFinalRecipientForUserMessagesHavingNullMessageProperties() {
        givenUserMessage();
        givenNullMessageProperties();

        whenRetrievingTheFinalRecipient();

        thenFinalRecipientIsNull("The final recipient should have been null when the user message has null message properties holder");
    }

    @Test
    public void returnsTheFinalRecipientPropertyValueIgnoringOtherPropertiesWhenRetrievingTheFinalRecipientForUserMessagesContainingTheCorrectProperty() {
        givenUserMessage();
        givenMessageProperties();
        givenProperties(
                aProperty().withName("dummy").withValue("dummy").build(),
                aProperty().withName(MessageConstants.ORIGINAL_SENDER).withValue("sender").build(),
                aProperty().withName(MessageConstants.FINAL_RECIPIENT).withValue("recipient").build(),
                aProperty().withName("another_dummy").withValue("another_dummy").build()
        );

        whenRetrievingTheFinalRecipient();

        thenFinalRecipientIsEqualTo("recipient", "The final recipient should have been correctly set when the user message contains the final recipient property");
    }

    @Test
    public void returnsNullWhenRetrievingTheReceivingPartyForUserMessagesHavingNoPartyIdentifiersForTheReceivingParty() {
        givenUserMessage();
        givenEmptyPartyIdentifiers();

        whenRetrievingTheToParty();

        thenPartyToIsNull("The receiving party should have been null when the user message has no party identifiers for the receiving party");
    }

    @Test
    public void returnsFirsFoundPartyIdentifierWhenRetrievingTheReceivingPartyForUserMessagesHavingMultiplePartyIdentifiersForTheReceivingParty() {
        givenUserMessage();
        givenPartyIdentifiers(
                aPartyId().withValue("first").build());

        whenRetrievingTheToParty();

        thenPartyToIsEqualTo("first", "The receiving party should have been found when the user message has party identifiers for the receiving party");
    }

    @Test
    public void returnsFalseIfTheProvidedOriginalSenderIsEmptyWhenCheckingWhetherTheUserMessageContainsTheOriginalSender() {
        givenUserMessageHavingMessageInfo();
        givenEmptyOriginalSender();

        whenCheckingTheUserMessageForContainingTheOriginalSender();

        thenTheProvidedOriginalSenderIsNotTheSameAsTheOneContainedInsideTheUserMessage(
                "Should have returned false when checking if an empty original sender is contained inside the user message");
    }

    @Test
    public void returnsFalseIfTheProvidedOriginalSenderDoesNotMatchTheOneContainedInTheUserMessageWhenCheckingWhetherTheUserMessageContainsTheOriginalSender() {
        givenUserMessageHavingMessageInfo();
        givenOriginalSender("notMatchingSender");
        givenUserMessageOriginalSender("sender");

        whenCheckingTheUserMessageForContainingTheOriginalSender();

        thenTheProvidedOriginalSenderIsNotTheSameAsTheOneContainedInsideTheUserMessage(
                "Should have returned false when checking if a non-matching original sender is contained inside the user message");
    }

    @Test
    public void returnsTrueIfTheProvidedOriginalSenderMatchesTheOneContainedInTheUserMessageWhenCheckingWhetherTheUserMessageContainsTheOriginalSender() {
        givenUserMessageHavingMessageInfo();
        givenOriginalSender("sender");
        givenUserMessageOriginalSender("sender");

        whenCheckingTheUserMessageForContainingTheOriginalSender();

        thenTheProvidedOriginalSenderIsTheSameAsTheOneContainedInsideTheUserMessage(
                "Should have returned true when checking if a matching original sender is contained inside the user message");
    }

    @Test
    public void returnsFalseIfTheProvidedFinalRecipientIsEmptyWhenCheckingWhetherTheUserMessageContainsTheFinalRecipient() {
        givenUserMessageHavingMessageInfo();
        givenEmptyFinalRecipient();

        whenCheckingTheUserMessageForContainingTheFinalRecipient();

        thenTheProvidedFinalRecipientIsNotTheSameAsTheOneContainedInsideTheUserMessage(
                "Should have returned false when checking if an empty final recipient is contained inside the user message");
    }

    @Test
    public void returnsFalseIfTheProvidedFinalRecipientDoesNotMatchTheOneContainedInTheUserMessageWhenCheckingWhetherTheUserMessageContainsTheFinalRecipient() {
        givenUserMessageHavingMessageInfo();
        givenFinalRecipient("notMatchingReceiver");
        givenUserMessageFinalRecipient("receiver");

        whenCheckingTheUserMessageForContainingTheFinalRecipient();

        thenTheProvidedFinalRecipientIsNotTheSameAsTheOneContainedInsideTheUserMessage(
                "Should have returned false when checking if a non-matching final recipient is contained inside the user message");
    }

    @Test
    public void returnsTrueIfTheProvidedFinalRecipientMatchesTheOneContainedInTheUserMessageWhenCheckingWhetherTheUserMessageContainsTheFinalRecipient() {
        givenUserMessageHavingMessageInfo();
        givenFinalRecipient("receiver");
        givenUserMessageFinalRecipient("receiver");

        whenCheckingTheUserMessageForContainingTheFinalRecipient();

        thenTheProvidedFinalRecipientIsTheSameAsTheOneContainedInsideTheUserMessage(
                "Should have returned true when checking if a matching final recipient is contained inside the user message");
    }

    private void givenNullUserMessage() {
        givenUserMessage(null);
    }

    private void givenUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    private void givenUserMessage() {
        givenUserMessage(new UserMessage());
    }

    private void givenUserMessageHavingMessageInfo() {
        UserMessage userMessage = new UserMessage();
        userMessage.setMessageInfo(new MessageInfo());
        givenUserMessage(userMessage);
    }

    private void givenNullMessageProperties() {
        givenMessageProperties(null);
    }

    private void givenMessageProperties() {
        givenMessageProperties(new MessageProperties());
    }

    private void givenMessageProperties(MessageProperties messageProperties) {
        this.messageProperties = messageProperties;
        this.userMessage.setMessageProperties(messageProperties);
    }

    private void givenProperties(Property... properties) {
        givenProperties(Arrays.asList(properties));
    }

    private void givenProperties(Collection<Property> properties) {
        messageProperties.getProperty().addAll(properties);
    }

    private void givenEmptyPartyIdentifiers() {
        givenPartyIdentifiers();
    }

    private void givenPartyIdentifiers(PartyId... properties) {
        givenPartyIdentifiers(Arrays.asList(properties));
    }

    private void givenPartyIdentifiers(Collection<PartyId> properties) {
        To to = new To();
        to.getPartyId().addAll(properties);
        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setTo(to);
        userMessage.setPartyInfo(partyInfo);
    }

    private void givenEmptyOriginalSender() {
        givenOriginalSender("");
    }

    private void givenOriginalSender(String originalSender) {
        this.originalSender = originalSender;
    }

    private void givenUserMessageOriginalSender(String originalSender) {
        new Expectations(userMessageDefaultServiceHelper) {{
            userMessageDefaultServiceHelper.getOriginalSender(userMessage); result = originalSender;
        }};
    }

    private void givenEmptyFinalRecipient() {
        givenFinalRecipient("");
    }

    private void givenFinalRecipient(String finalRecipient) {
        this.finalRecipient = finalRecipient;
    }

    private void givenUserMessageFinalRecipient(String finalRecipient) {
        new Expectations(userMessageDefaultServiceHelper) {{
            userMessageDefaultServiceHelper.getFinalRecipient(userMessage); result = finalRecipient;
        }};
    }

    private void whenRetrievingTheOriginalSender() {
        originalSender = userMessageDefaultServiceHelper.getOriginalSender(userMessage);
    }

    private void whenRetrievingTheFinalRecipient() {
        finalRecipient = userMessageDefaultServiceHelper.getFinalRecipient(userMessage);
    }

    private void whenRetrievingTheToParty() {
        partyTo = userMessageDefaultServiceHelper.getPartyTo(userMessage);
    }

    private void whenCheckingTheUserMessageForContainingTheOriginalSender() {
        sameOriginalSender = userMessageDefaultServiceHelper.isSameOriginalSender(userMessage, originalSender);
    }

    private void whenCheckingTheUserMessageForContainingTheFinalRecipient() {
        sameFinalRecipient = userMessageDefaultServiceHelper.isSameFinalRecipient(userMessage, finalRecipient);
    }

    private void thenOriginalSenderIsNull(String message) {
        thenOriginalSenderIsEqualTo(null, message);
    }

    private void thenOriginalSenderIsEqualTo(String expected, String message) {
        Assert.assertEquals(message, expected, originalSender);
    }

    private void thenFinalRecipientIsNull(String message) {
        thenFinalRecipientIsEqualTo(null, message);
    }

    private void thenFinalRecipientIsEqualTo(String expected, String message) {
        Assert.assertEquals(message, expected, finalRecipient);
    }

    private void thenPartyToIsNull(String message) {
        thenPartyToIsEqualTo(null, message);
    }

    private void thenPartyToIsEqualTo(String expected, String message) {
        Assert.assertEquals(message, expected, partyTo);
    }

    private void thenTheProvidedOriginalSenderIsTheSameAsTheOneContainedInsideTheUserMessage(String message) {
        Assert.assertTrue(message, sameOriginalSender);
    }

    private void thenTheProvidedOriginalSenderIsNotTheSameAsTheOneContainedInsideTheUserMessage(String message) {
        Assert.assertFalse(message, sameOriginalSender);
    }

    private void thenTheProvidedFinalRecipientIsTheSameAsTheOneContainedInsideTheUserMessage(String message) {
        Assert.assertTrue(message, sameFinalRecipient);
    }

    private void thenTheProvidedFinalRecipientIsNotTheSameAsTheOneContainedInsideTheUserMessage(String message) {
        Assert.assertFalse(message, sameFinalRecipient);
    }

    public static final class PropertyBuilder {

        protected String value;
        protected String name;
        protected String type;

        private PropertyBuilder() {
        }

        public static PropertyBuilder aProperty() {
            return new PropertyBuilder();
        }

        public PropertyBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public PropertyBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public PropertyBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public Property build() {
            Property property = new Property();
            property.setValue(value);
            property.setName(name);
            property.setType(type);
            return property;
        }
    }

    public static final class PartyIdBuilder {
        protected String value;
        protected String type;

        private PartyIdBuilder() {
        }

        public static PartyIdBuilder aPartyId() {
            return new PartyIdBuilder();
        }

        public PartyIdBuilder withValue(String value) {
            this.value = value;
            return this;
        }

        public PartyIdBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public PartyId build() {
            PartyId partyId = new PartyId();
            partyId.setValue(value);
            partyId.setType(type);
            return partyId;
        }
    }
}