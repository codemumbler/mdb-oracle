package io.github.codemumbler.datatype;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

public class DateTimeTest {

  @Test
  public void wrapDateObject() {
    DateTime dateTime = new DateTime();
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.MONTH, 0);
    calendar.set(Calendar.DAY_OF_MONTH, 2);
    calendar.set(Calendar.YEAR, 2015);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 16);
    calendar.set(Calendar.SECOND, 7);
    Assert.assertEquals("TO_TIMESTAMP('01/02/2015 23:16:07', 'MM/DD/YYYY HH24:MI:SS')", dateTime.writeValue(calendar.getTime()));
  }
}
