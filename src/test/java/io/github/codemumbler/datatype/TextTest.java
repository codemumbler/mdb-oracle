package io.github.codemumbler.datatype;

import org.junit.Assert;
import org.junit.Test;

public class TextTest {

  private Text text = new Text();

  @Test
  public void simpleText() {
    Assert.assertEquals("'Simple Text'", text.writeValue("Simple Text"));
  }

  @Test
  public void quotesInText() {
    Assert.assertEquals("'don''t fail'", text.writeValue("don't fail"));
  }

  @Test
  public void lineBreaks() {
    Assert.assertEquals("'line' || chr(13) || chr(10) ||'breaks'", text.writeValue("line\r\nbreaks"));
  }

  @Test
  public void nullValue() {
    Assert.assertEquals("NULL", text.writeValue(null));
  }
}
