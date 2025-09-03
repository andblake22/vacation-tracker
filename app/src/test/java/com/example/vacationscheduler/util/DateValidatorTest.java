package com.example.vacationscheduler.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class DateValidatorTest {

    @Test
    public void validDate_returnsTrue() {
        assertTrue(DateValidator.isValidDate("01/15/2025"));
    }

    @Test
    public void invalidDateFormat_returnsFalse() {
        assertFalse(DateValidator.isValidDate("2025-01-15"));
    }

    @Test
    public void invalidMonth_returnsFalse() {
        assertFalse(DateValidator.isValidDate("13/10/2025"));
    }

    @Test
    public void endAfterStart_returnsTrue() {
        assertTrue(DateValidator.isEndDateAfterStartDate("01/01/2025", "01/10/2025"));
    }

    @Test
    public void endSameOrBeforeStart_returnsFalse() {
        assertFalse(DateValidator.isEndDateAfterStartDate("01/10/2025", "01/10/2025"));
        assertFalse(DateValidator.isEndDateAfterStartDate("01/10/2025", "01/05/2025"));
    }

    @Test
    public void nullInputs_returnFalse() {
        assertFalse(DateValidator.isValidDate(null));
        assertFalse(DateValidator.isEndDateAfterStartDate(null, "01/05/2025"));
        assertFalse(DateValidator.isEndDateAfterStartDate("01/01/2025", null));
    }
}

