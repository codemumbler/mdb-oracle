package io.github.codemumbler.datatype;

import org.junit.Assert;
import org.junit.Test;

public class TextTest {

	@Test
	public void simpleText() {
		Text text = new Text();
		Assert.assertEquals("'Simple Text'", text.writeValue("Simple Text"));
	}

	@Test
	public void quotesInText() {
		Text text = new Text();
		Assert.assertEquals("'don''t fail'", text.writeValue("don't fail"));
	}
}