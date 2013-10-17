package to.etc.webapp.json;

public class JsonData1 {
	private int m_number1;

	private int m_number2;

	private String m_string1;

	private String m_string2;

	private JsonData1 m_next;

	public JsonData1(int number1, int number2, String string1, String string2, JsonData1 next) {
		m_number1 = number1;
		m_number2 = number2;
		m_string1 = string1;
		m_string2 = string2;
		m_next = next;
	}

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
}
