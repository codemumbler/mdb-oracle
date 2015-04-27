package io.github.codemumbler.datatype;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class DateTimeTest {

	@Test
	public void wrapDateObject() {
		DateTime dateTime = new DateTime();
		Date date = new Date(1234567890000L);
		Assert.assertEquals("TO_TIMESTAMP('02/13/2009 18:31:30', 'MM/DD/YYYY HH24:MI:SS')", dateTime.writeValue(date));
	}
}