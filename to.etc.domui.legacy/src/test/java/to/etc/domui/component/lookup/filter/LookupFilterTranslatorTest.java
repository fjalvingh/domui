package to.etc.domui.component.lookup.filter;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.util.DateUtil;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.testsupport.TestDataContextStub;
import to.etc.xml.XmlWriter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since 2/9/16.
 */
public class LookupFilterTranslatorTest {

	private static QDataContext m_dc;

	@BeforeClass
	public static void prepareTest() {
		m_dc = new TestDataContextStub();
	}

	@Test
	public void testSerializeString() throws Exception {
		Map<String, Object> filterValues = new HashMap<>();
		String title = "Ticket 1234";
		filterValues.put("title", title);
		final String serialize = LookupFilterTranslator.serialize(filterValues);

		String serializedFilter = getXMLString(StringTranslator.class.getCanonicalName(), "title", title);
		Assert.assertEquals(serializedFilter, serialize);
	}

	@Test
	public void testDeserializeString() throws Exception {
		final Map<String, ?> keyValues = LookupFilterTranslator.deserialize(m_dc, "<Filter><item>"
			+ "<value>Ticket 1234</value>"
			+ "<metaData key=\"title\" type=\"" + StringTranslator.class.getCanonicalName() + "\"></metaData>"
			+ "</item></Filter>");
		Assert.assertEquals(1, keyValues.size());
		for(Entry<String, ?> entry : keyValues.entrySet()) {
			Assert.assertEquals("title", entry.getKey());
			Assert.assertEquals("Ticket 1234", entry.getValue());
		}
	}

	@Test
	public void testSerializeBoolean() throws Exception {
		Map<String, Object> filterValues = new HashMap<>();
		Boolean priority = Boolean.TRUE;
		filterValues.put("priority", priority);
		final String serialize = LookupFilterTranslator.serialize(filterValues);

		String serializedFilter = getXMLString(BooleanTranslator.class.getCanonicalName(), "priority", priority.toString());
		Assert.assertEquals(serializedFilter, serialize);
	}

	@Test
	public void testDeserializeBoolean() throws Exception {
		final Map<String, ?> keyValues = LookupFilterTranslator.deserialize(m_dc, "<Filter><item>"
			+ "<value>true</value>"
			+ "<metaData key=\"priority\" type=\"" + BooleanTranslator.class.getCanonicalName() + "\"></metaData>"
			+ "</item></Filter>");
		Assert.assertEquals(1, keyValues.size());
		for(Entry<String, ?> entry : keyValues.entrySet()) {
			Assert.assertEquals("priority", entry.getKey());
			Assert.assertEquals(Boolean.TRUE, entry.getValue());
		}
	}

	@Test
	public void testSerializeInteger() throws Exception {
		Map<String, Object> filterValues = new HashMap<>();
		Integer count = new Integer("1234");
		filterValues.put("count", count);
		final String serialize = LookupFilterTranslator.serialize(filterValues);

		String serializedFilter = getXMLString(IntegerTranslator.class.getCanonicalName(), "count", count.toString());
		Assert.assertEquals(serializedFilter, serialize);
	}

	@Test
	public void testDeserializeInteger() throws Exception {
		final Map<String, ?> keyValues = LookupFilterTranslator.deserialize(m_dc, "<Filter><item>"
			+ "<value>1234</value>"
			+ "<metaData key=\"count\" type=\"" + IntegerTranslator.class.getCanonicalName() + "\"></metaData>"
			+ "</item></Filter>");
		Assert.assertTrue(keyValues.size() == 1);
		for(Entry<String, ?> entry : keyValues.entrySet()) {
			Assert.assertEquals("count", entry.getKey());
			Assert.assertEquals(Integer.valueOf("1234"), entry.getValue());
		}
	}

	@Test
	public void testSerializeBigInteger() throws Exception {
		Map<String, Object> filterValues = new HashMap<>();
		BigInteger count = new BigInteger("1234");
		filterValues.put("count", count);
		final String serialize = LookupFilterTranslator.serialize(filterValues);

		String serializedFilter = getXMLString(BigIntegerTranslator.class.getCanonicalName(), "count", count.toString());
		Assert.assertEquals(serializedFilter, serialize);
	}

	@Test
	public void testDeserializeBigInteger() throws Exception {
		final Map<String, ?> keyValues = LookupFilterTranslator.deserialize(m_dc, "<Filter><item>"
			+ "<value>1234</value>"
			+ "<metaData key=\"count\" type=\"" + BigIntegerTranslator.class.getCanonicalName() + "\" />"
			+ "</item></Filter>");
		Assert.assertTrue(keyValues.size() == 1);
		for(Entry<String, ?> entry : keyValues.entrySet()) {
			Assert.assertEquals("count", entry.getKey());
			Assert.assertEquals(new BigInteger("1234"), entry.getValue());
		}
	}

	@Test
	public void testSerializeDateFromTo() throws Exception {
		Map<String, Object> filterValues = new HashMap<>();
		DateFromTo dateFromTo = new DateFromTo(DateUtil.dateFor(2016, 2, 1), DateUtil.dateFor(2016, 4, 20));
		filterValues.put("dateBetween", dateFromTo);
		final String serialize = LookupFilterTranslator.serialize(filterValues);

		String serializedFilter =
			"<Filter>\n"
			+ "  <item>\n"
			+ "    <value>\n"
			+ "      <dateFrom>01-03-2016</dateFrom>\n"
			+ "      <dateTo>20-05-2016</dateTo>\n"
			+ "    </value>\n"
			+ "    <metaData key=\"dateBetween\" type=\"" + DateFromToTranslator.class.getCanonicalName() + "\" />\n"
			+ "  </item>\n"
			+ "</Filter>\n";

		Assert.assertEquals(serializedFilter, serialize);
	}

	@Test
	public void testDeserializeDateFromTo() throws Exception {
		final Map<String, ?> keyValues = LookupFilterTranslator.deserialize(m_dc, "<Filter><item>"
			+ "<value><dateFrom>01-03-2016</dateFrom><dateTo>20-05-2016</dateTo></value>"
			+ "<metaData key=\"dateBetween\" type=\"" + DateFromToTranslator.class.getCanonicalName() + "\" />"
			+ "</item></Filter>");
		Assert.assertTrue(keyValues.size() == 1);
		for(Entry<String, ?> entry : keyValues.entrySet()) {
			Assert.assertEquals("dateBetween", entry.getKey());
			DateFromTo dateFromToActual = (DateFromTo) entry.getValue();
			DateFromTo dateFromToExpected = new DateFromTo(DateUtil.dateFor(2016, 2, 1), DateUtil.dateFor(2016, 4, 20));
			Assert.assertEquals(dateFromToExpected.getDateFrom(), dateFromToActual.getDateFrom());
			Assert.assertEquals(dateFromToExpected.getDateTo(), dateFromToActual.getDateTo());
		}
	}

	@Test
	public void testSerializeSetEnum() throws Exception {
		Map<String, Object> filterValues = new HashMap<>();

		Set<PropertyRelationType> testSet = new HashSet<>();
		testSet.add(PropertyRelationType.UP);
		testSet.add(PropertyRelationType.DOWN);

		filterValues.put("testEnums", testSet);
		final String serialize = LookupFilterTranslator.serialize(filterValues);

		Assert.assertTrue(serialize.contains("<value class=\"" + PropertyRelationType.class.getCanonicalName() + "\">UP</value>"));
		Assert.assertTrue(serialize.contains("<value class=\"" + PropertyRelationType.class.getCanonicalName() + "\">DOWN</value>"));
	}

	@Test
	public void testDeserializeSetEnum() throws Exception {
		final Map<String, ?> keyValues = LookupFilterTranslator.deserialize(m_dc, "<Filter>\n"
			+ "  <item>\n"
			+ "    <value>\n"
			+ "      <item>\n"
			+ "        <value class=\"" + PropertyRelationType.class.getCanonicalName() + "\">UP</value>\n"
			+ "        <metaData type=\"" + EnumTranslator.class.getCanonicalName() + "\" />\n"
			+ "      </item>\n"
			+ "      <item>\n"
			+ "        <value class=\"" + PropertyRelationType.class.getCanonicalName() + "\">DOWN</value>\n"
			+ "        <metaData type=\"" + EnumTranslator.class.getCanonicalName() + "\" />\n"
			+ "      </item>\n"
			+ "    </value>\n"
			+ "    <metaData key=\"testEnums\" type=\"" + SetTranslator.class.getCanonicalName() + "\" />\n"
			+ "  </item>\n"
			+ "</Filter>\n");
		Assert.assertTrue(keyValues.size() == 1);
		for(Entry<String, ?> entry : keyValues.entrySet()) {
			Assert.assertEquals("testEnums", entry.getKey());
			Assert.assertTrue(entry.getValue() instanceof Set<?>);
			Set<PropertyRelationType> testEnumSet = (Set<PropertyRelationType>) entry.getValue();
			Assert.assertEquals(2, testEnumSet.size());
			Assert.assertTrue(testEnumSet.contains(PropertyRelationType.DOWN));
			Assert.assertTrue(testEnumSet.contains(PropertyRelationType.UP));
		}
	}

	@Nonnull
	private String getXMLString(String... keyValues) throws IOException {
		StringWriter serializedFilter = new StringWriter();
		XmlWriter w = new XmlWriter(serializedFilter);
		w.tag("Filter");
		for(int i = 0; i < keyValues.length; i += 3) {
			w.tag("item");
			w.tag("value");
			w.write(keyValues[i + 2]);
			w.tagendnl();
			w.tagonly("metaData", "key", keyValues[i+1], "type", keyValues[i]);
		}
		w.tagendnl();
		w.close();

		return serializedFilter.toString();
	}
}
