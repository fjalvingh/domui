/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.util;


public class GIS {
	/*--------------------------------------------------------------*/
	/*  CODING:  Projection conversions                             */
	/*--------------------------------------------------------------*/

	/** X and Y coordinates for the basepoint: Onze-Lieve-Vrouwe tower in Amersfoort */
	static private final double		RDX0		= 155000.00;

	static private final double		RDY0		= 463000.00;

	/** WGS84-coordinates for base-point: Onze-Lieve-Vrouwe tower in Amersfoort */
	static private final double		WGS84LA0	= 52.15517440;

	static private final double		WGS84LO0	= 5.38720621;

	static private final double		IGN			= Double.MAX_VALUE;


	static private final double[][]	K_FACTORS	= {{IGN, 3235.65389, -0.24750, -0.06550}, // k[0]
		{-0.00738, -0.00012}, // k[1]
		{-32.58297, -0.84978, -0.01709, -0.00039}, // k[2]
		{IGN}, // k[3]
		{0.00530, 0.00033}						};

	static private final double[][]	L_FACTORS	= {{IGN, 0.01199, 0.00022}, {5260.52916, 105.94684, 2.45656, 0.05594, 0.00128}, {-0.00022}, {-0.81885, -0.05607, -0.00256}, {IGN}, {0.00026}};

	private double					m_long;

	private double					m_lat;

	/**
	 * Convert RijksDriehoeksstel coordinates (epsg:28992) to WGS84 coordinates (epsg:4326) in decimal
	 * (longitude,latitude) notation.
	 *
	 * Based on "Benaderingsformules voor de transformatie tussen RD- en WGS84-kaartcoordinaten"
	 * Authors: - ing. F.H. Schreutelkamp, Stichting 'De Koepel', sterrenwacht 'Sonnenborgh', Utrecht, The Netherlands
	 * Examples:
	 * RD(x,y) --> WGS84(long,lat)
	 * 81 Amsterdam (Westertoren): (120700.723, 487525.501) --> (52.37453253, 4.88352559)
	 * 21 Groningen (Martinitoren): (233883.131, 582065.167)--> (53.21938317, 6.56820053)
	 *
	 * result[0] = lon, result[1] = lat.
	 */
	static public void convertRDToWGS84(double[] result, double rdx, double rdy) {
		double dX = (rdx - RDX0) / 100000.0;
		double dY = (rdy - RDY0) / 100000.0;

		//-- latitude: pass over K array.
		double wgslat = 0.0;
		for(int x = 0; x < K_FACTORS.length; x++) {
			for(int y = 0; y < K_FACTORS[x].length; y++) {
				if(K_FACTORS[x][y] != IGN) {
					wgslat += K_FACTORS[x][y] * Math.pow(dX, x) * Math.pow(dY, y);
					//                    double a = Math.pow(dX,x);
					//                    double b = Math.pow(dY,y);
					//                    WGS84Long += k[x][y] * a * b;
				}
			}
		}

		result[0] = WGS84LA0 + (wgslat / 3600.0);

		// longitude: more of the same.
		double wgslon = 0.0;
		for(int x = 0; x < L_FACTORS.length; x++) {
			for(int y = 0; y < L_FACTORS[x].length; y++) {
				if(L_FACTORS[x][y] != IGN) {
					wgslon += L_FACTORS[x][y] * Math.pow(dX, x) * Math.pow(dY, y);
				}
			}
		}
		result[1] = WGS84LO0 + (wgslon / 3600.0);
	}


	public void convertRD(double rdx, double rdy) {
		double dX = (rdx - RDX0) / 100000.0;
		double dY = (rdy - RDY0) / 100000.0;

		//-- latitude: pass over K array.
		double wgslat = 0.0;
		for(int x = 0; x < K_FACTORS.length; x++) {
			for(int y = 0; y < K_FACTORS[x].length; y++) {
				if(K_FACTORS[x][y] != IGN) {
					wgslat += K_FACTORS[x][y] * Math.pow(dX, x) * Math.pow(dY, y);
					//                    double a = Math.pow(dX,x);
					//                    double b = Math.pow(dY,y);
					//                    WGS84Long += k[x][y] * a * b;
				}
			}
		}

		m_lat = WGS84LA0 + (wgslat / 3600.0);

		// longitude: more of the same.
		double wgslon = 0.0;
		for(int x = 0; x < L_FACTORS.length; x++) {
			for(int y = 0; y < L_FACTORS[x].length; y++) {
				if(L_FACTORS[x][y] != IGN) {
					wgslon += L_FACTORS[x][y] * Math.pow(dX, x) * Math.pow(dY, y);
				}
			}
		}
		m_long = WGS84LO0 + (wgslon / 3600.0);
	}

	public double getLatitude() {
		return m_lat;
	}

	public double getLongitude() {
		return m_long;
	}

	static public double deg2rad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 * Calculate distance with lat/long in deg (90%).
	 * @param alat
	 * @param alng
	 * @param blat
	 * @param blng
	 * @return
	 */
	static public double distanceInDeg(double alat, double alng, double blat, double blng) {
		double ala = deg2rad(alat);
		double bla = deg2rad(blat);
		double alo = deg2rad(alng);
		double blo = deg2rad(blng);

		double r = 6367000.0; // The earth radius, in meters (approx)
		double dlat = ala - bla; // Delta latitude
		double dlong = alo - blo; // Delta longitude

		double tla = Math.sin(dlat / 2);
		double tlo = Math.sin(dlong / 2);
		double ta = tla * tla + Math.cos(ala) * Math.cos(bla) * tlo * tlo;
		double tc = 2 * Math.atan2(Math.sqrt(ta), Math.sqrt(1 - ta));
		return r * tc;
	}

	static public void main(String[] args) {
		try {
			double[] res = new double[2];

			convertRDToWGS84(res, 120700.723, 487525.501);
			System.out.println("Westertoren (" + res[0] + ", " + res[1] + ")");

			convertRDToWGS84(res, 233883.131, 582065.167);
			System.out.println("Martinitoren (" + res[0] + ", " + res[1] + ")");

			GIS g = new GIS();
			g.convertRD(162558, 498715);
			System.out.println("Pascallaan (" + g.getLatitude() + ", " + g.getLongitude() + ")");

			g.convertRD(144690, 487111);
			System.out.println("Peggy Ashcroftstr 11 (" + g.getLatitude() + ", " + g.getLongitude() + ")");

			g.convertRD(163705, 503159);
			System.out.println("Oostrandpark 81 (" + g.getLatitude() + ", " + g.getLongitude() + ")");

			double d = GIS.distanceInDeg(52.518971777678, 6.095555421998, 51.9163557064, 4.4868857542);
			System.out.println("D=" + d);

		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
