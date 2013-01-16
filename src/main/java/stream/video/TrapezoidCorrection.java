/**
 * 
 */
package stream.video;

import java.awt.Point;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.laser.Trapez;

/**
 * @author chris
 * 
 */
public class TrapezoidCorrection {

	static Logger log = LoggerFactory.getLogger(TrapezoidCorrection.class);

	RealMatrix A;

	public TrapezoidCorrection() {
		A = null;
	}

	public TrapezoidCorrection(RealMatrix a) {
		this.A = a;
		log.info("A : {}", A);
	}

	public void setMatrix(RealMatrix a) throws Exception {
		double[][] data = a.getData();
		if (data.length != 2 || data[0].length != 2)
			throw new Exception(
					"Invalid Matrix-dimensions! Matrix needs to be in 2x2 format!");
		A = a;
	}

	public Point map(Point x) {

		if (A == null) {
			log.info("No matrix defined, returning point AS IS.");
			return new Point(x.x, x.y);
		}

		log.info("Point {},{} is", x.x, x.y);
		RealMatrix b = new RealMatrixImpl(new double[][] { { x.x }, { x.y } });
		RealMatrix p = A.multiply(b);
		Double nx = p.getData()[0][0];
		Double ny = p.getData()[1][0];
		log.info("   mapped to {},{}", nx, ny);
		return new Point(nx.intValue(), ny.intValue());
	}

	public static void main(String[] args) {

		RealMatrix X = new RealMatrixImpl(new double[][] {
				{ 305.0, 1503.0, 115.0, 1797.0 },
				{ 175.0, 255.0, 902.0, 981.0 } });

		RealMatrix G = new RealMatrixImpl(new double[][] { { 8.0, 1912.0 },
				{ 8.0, 1912.0 }, { 8.0, 8.0 }, { 1134.0, 1134.0 } });

		X = new RealMatrixImpl(new double[][] { { 128, 10 }, { 92, 312 },
				{ 517, 8 }, { 432, 303 } });

		G = new RealMatrixImpl(new double[][] { { 0, 0 }, { 0, 768 },
				{ 1024, 0 }, { 1024, 768 } });

		RealMatrix XtX = X.transpose().multiply(X);

		RealMatrix XtX_inv_Xt = XtX.inverse().multiply(X.transpose());
		RealMatrix A = XtX_inv_Xt.multiply(G);

		TrapezoidCorrection tc = new TrapezoidCorrection(A);
		Point p = new Point(128, 10);

		Point q = tc.map(p);
		log.info("{}  ~>  {}", p, q);

		p = new Point(93, 312);
		q = tc.map(p);
		log.info("{}  ->  {}", p, q);

		//
		// RealMatrix XXT = X.multiply(X.transpose());
		// RealMatrix XXT_1 = XXT.inverse();
		//
		// RealMatrix A = XXT_1.multiply(X).multiply(G);
	}

	public static RealMatrix matrix(Trapez from) {
		double[][] tf = new double[][] {
				{ from.tl.x, from.bl.x, from.tr.x, from.br.getX() },
				{ from.tl.y, from.bl.y, from.tr.y, from.br.y } };

		return new RealMatrixImpl(tf);
	}

	public static RealMatrix trapez2matrixT(Trapez from) {
		double[][] tf = new double[][] { { from.tl.x, from.tl.y },
				{ from.bl.x, from.bl.y }, { from.tr.x, from.tr.y },
				{ from.br.x, from.br.y } };

		return new RealMatrixImpl(tf);
	}

	public static RealMatrix compute(Trapez from, Trapez to) {

		RealMatrix x = trapez2matrixT(from);
		log.info("X = {}", x);

		RealMatrix x_inv = x.inverse();
		RealMatrix xxt = x.multiply(x.transpose());

		RealMatrix xxt_inv = xxt.inverse();

		RealMatrix xxt_invx = xxt_inv.multiply(x);

		RealMatrix g = trapez2matrixT(to);
		log.info("G = {}", g);

		RealMatrix a = xxt_invx.multiply(g);
		log.info("A = {}", a);
		return a;
	}
}
