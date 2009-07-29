package to.etc.domui.converter;

import java.text.*;
import java.util.*;

import to.etc.domui.trouble.*;

/**
 * This converts a Double containing a monetary amount to a full representation
 * including thousands separator and valuta indicator.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 29, 2009
 */
public class MoneyConverter implements IConverter<Double> {
	public String convertObjectToString(Locale loc, Double in) throws UIException {
		if(in == null)
			return null;







		// TODO Auto-generated method stub
		return null;
	}

	public Double convertStringToObject(Locale loc, String in) throws UIException {
		// TODO Auto-generated method stub
		return null;
	}


	public static void main(String[] args) {
		try {
			NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("nl", "NL"));
			Double v = Double.valueOf(1234.56d);
			System.out.println("Amount: " + nf.format(v));

			String s = "€ 88,55";

			Number n = nf.parse(s);
			System.out.println("res = " + n.getClass() + ", " + n);

		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
