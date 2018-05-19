package to.etc.json;

import java.util.*;

public class JsonData1 {
	private int m_number1;

	private int m_number2;

	private String m_string1;

	private String m_string2;

	private JsonData1 m_next;

	private JsonEnum1 m_enum;

	private JsonEnum1 m_enum2;

	private Date m_date;

	private boolean m_onoff;

	private Long m_long1;

	private Long m_long2;

	private List<JsonData1> m_list1 = new ArrayList<JsonData1>();

	private List<Long> m_list2 = new ArrayList<Long>();

	public JsonData1(int number1, int number2, String string1, String string2, JsonData1 next) {
		m_number1 = number1;
		m_number2 = number2;
		m_string1 = string1;
		m_string2 = string2;
		m_next = next;
	}

	public JsonData1() {}

	public int getNumber1() {
		return m_number1;
	}

	public void setNumber1(int number1) {
		m_number1 = number1;
	}

	public int getNumber2() {
		return m_number2;
	}

	public void setNumber2(int number2) {
		m_number2 = number2;
	}

	public String getString1() {
		return m_string1;
	}

	public void setString1(String string1) {
		m_string1 = string1;
	}

	public String getString2() {
		return m_string2;
	}

	public void setString2(String string2) {
		m_string2 = string2;
	}

	public JsonData1 getNext() {
		return m_next;
	}

	public void setNext(JsonData1 next) {
		m_next = next;
	}

	public JsonEnum1 getEnum() {
		return m_enum;
	}

	public void setEnum(JsonEnum1 enum1) {
		m_enum = enum1;
	}

	public JsonEnum1 getEnum2() {
		return m_enum2;
	}

	public void setEnum2(JsonEnum1 enum2) {
		m_enum2 = enum2;
	}

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date date) {
		m_date = date;
	}

	public boolean isOnoff() {
		return m_onoff;
	}

	public void setOnoff(boolean onoff) {
		m_onoff = onoff;
	}

	public Long getLong1() {
		return m_long1;
	}

	public void setLong1(Long long1) {
		m_long1 = long1;
	}

	public Long getLong2() {
		return m_long2;
	}

	public void setLong2(Long long2) {
		m_long2 = long2;
	}

	public List<JsonData1> getList1() {
		return m_list1;
	}

	public void setList1(List<JsonData1> list1) {
		m_list1 = list1;
	}

	public List<Long> getList2() {
		return m_list2;
	}

	public void setList2(List<Long> list2) {
		m_list2 = list2;
	}
}
