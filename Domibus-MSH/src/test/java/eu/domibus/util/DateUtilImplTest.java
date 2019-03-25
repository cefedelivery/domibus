package eu.domibus.util;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class DateUtilImplTest {

    @Tested
    private DateUtilImpl dateUtilImpl;

    @Test
    public void convertsIso8601ValuesToDates() {
        // Given
        String value = "2020-02-29T11:53:37";

        // When
        Timestamp actual = dateUtilImpl.fromISO8601(value);

        // Then
        LocalDateTime expected = LocalDateTime.of(2020, Month.FEBRUARY, 29, 11, 53, 37);
        Assert.assertEquals("Should have converted correctly the ISO 8601 value to a timestamp", expected, actual.toLocalDateTime());
    }

    @Test
    public void convertsIso8601ValuesToDates_EpochZulu() {
        // Given
        String value = "1970-01-01T00:00:00Z";

        // When
        Timestamp actual = dateUtilImpl.fromISO8601(value);

        // Then
        Instant expected = LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0, 0).atOffset(ZoneOffset.UTC).toInstant();
        Assert.assertEquals("Should have converted correctly the epoch ISO 8601 value to a timestamp", expected, actual.toInstant());
    }

    @Test
    public void convertsIso8601ValuesToDates_ZoneOffset() {
        // Given
        String value = "2020-02-29T11:53:37+02:00";

        // When
        Timestamp actual = dateUtilImpl.fromISO8601(value);

        // Then
        Instant expected = LocalDateTime.of(2020, Month.FEBRUARY, 29, 11, 53, 37).atOffset(ZoneOffset.of("+02:00")).toInstant();
        Assert.assertEquals("Should have converted correctly the offset ISO 8601 value to a timestamp", expected, actual.toInstant());
    }

    @Test
    public void convertsNumberValuesToDates() {
        // Given
        Number value = new Long(912740921);

        // When
        Timestamp actual = dateUtilImpl.fromNumber(value);

        // Then
        Assert.assertEquals("Should have converted correctly the number value to a timestamp", new Timestamp(912740921), actual);
    }

    @Test
    public void convertsNumberValuesPassedInAsStringToDates() {
        // Given
        String value = "13231";

        // When
        Date actual = dateUtilImpl.fromString(value);

        // Then
        Assert.assertEquals("Should have converted correctly the string number value to a timestamp", new Timestamp(13231), actual);
    }

    @Test
    public void convertsIso8601ValuesPassedInAsStringToDates() {
        // Given
        String value = "1989-12-24T12:59:59Z";

        // When
        Date actual = dateUtilImpl.fromString(value);

        // Then
        long expected = LocalDateTime.of(1989, Month.DECEMBER, 24, 12, 59, 59).atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();
        Assert.assertEquals("Should have converted correctly the string ISO 8601 value to a timestamp", expected, actual.getTime());
    }

    @Test
    public void returnsNullWhenConvertingNullValuesPassedInAsStringToDates() {
        // When
        Date actual = dateUtilImpl.fromString(null);

        // Then
        Assert.assertNull("Should have returned null when converting null values to a timestamp", actual);
    }

    @Test
    public void returnsCorrectlyTheStartOfDayAsADate() {
        // When
        Date actual = dateUtilImpl.getStartOfDay();

        // Then
        Assert.assertEquals("Should have returned the correct start of day as a date",
                LocalDateTime.now().toLocalDate().atStartOfDay(ZoneOffset.systemDefault()).toInstant(), actual.toInstant());
    }

}