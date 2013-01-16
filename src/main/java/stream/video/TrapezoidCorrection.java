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
import stream.laser.TrapezCorrection;

/**
 * @author chris
 * 
 */
public class TrapezoidCorrection implements TrapezCorrection {

	static Logger log = LoggerFactory.getLogger(TrapezoidCorrection.class);

	RealMatrix A;

	public TrapezoidCorrection() {
		A = null;
	}

	public TrapezoidCorrection(RealMatrix a) {
		this.A = a;
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

		RealMatrix b = new RealMatrixImpl(new double[][] { { x.x }, { x.y } });
		log.info("Point b = {}", b);
		RealMatrix p = A.multiply(b);
		log.info("   A b = {}", p);
		Long nx = Math.round(p.getData()[0][0]);
		Long ny = Math.round(p.getData()[1][0]);
		log.info("   mapped to {},{}", nx, ny);
		return new Point(nx.intValue(), ny.intValue());
	}

	public Point unmap(Point p) {

		if (A == null) {
			log.info("No matrix defined, returning point AS IS.");
			return new Point(p.x, p.y);
		}

		log.info("Point {},{} is", p.x, p.y);
		RealMatrix b = new RealMatrixImpl(new double[][] { { p.x }, { p.y } });
		RealMatrix q = A.inverse().multiply(b);
		Long nx = Math.round(q.getData()[0][0]);
		Long ny = Math.round(q.getData()[1][0]);
		log.info("   mapped to {},{}", nx, ny);
		return new Point(nx.intValue(), ny.intValue());

	}

	public static RealMatrix subSetTrapez2matrix(Trapez from) {
		double[][] tf = new double[][] {
				{ from.tl.x, from.bl.x, from.tr.x, from.br.x },
				{ from.tl.y, from.bl.y, from.tr.y, from.br.y } };
		return new RealMatrixImpl(tf);
	}

	public static void main(String[] args) {

		RealMatrix X;
		RealMatrix G;

		Trapez g = new Trapez(new Point(0, 0), new Point(0, 1),
				new Point(1, 0), new Point(1, 1));
		Trapez x = new Trapez(new Point(128, 10), new Point(92, 312),
				new Point(517, 8), new Point(432, 303));

		X = new RealMatrixImpl(new double[][] { { 128, 10 }, { 92, 312 },
				{ 517, 8 }, { 432, 303 } });

		G = new RealMatrixImpl(new double[][] { { 0, 0 }, { 0, 768 },
				{ 1024, 0 }, { 1024, 768 } });

		X = new RealMatrixImpl(new double[][] { { 128, 92, 517, 432 },
				{ 10, 312, 8, 303 } });
		// X = new RealMatrixImpl(new double[][] { { 128, 432 }, { 10, 303 } });
		// X = X.transpose();
		G = new RealMatrixImpl(new double[][] { { 0, 1024 }, { 0, 768 } });
		G = new RealMatrixImpl(
				new double[][] { { 0, 0, 1, 1 }, { 0, 1, 0, 1 } });
		G = new RealMatrixImpl(new double[][] { { 0, 0, 1024, 1024 },
				{ 0, 768, 0, 768 } });
		// G = new RealMatrixImpl(new double[][] { { 0, 1 }, { 0, 1 } });
		// G = G.transpose();

		G = subSetTrapez2matrix(g);
		X = subSetTrapez2matrix(x);

		RealMatrix At;
		RealMatrix XXt_inv = X.multiply(X.transpose()).inverse();
		// log.info("XX^T = {}", XXt);
		//

		At = XXt_inv.multiply(X).multiply(G.transpose()).transpose();
		// A = G.multiply(X.transpose()).multiply(XXt_inv);

		// A = X.multiply(X.transpose()).inverse().multiply(X).multiply(G);
		// RealMatrix XXt = X.multiply(X.transpose());
		// log.info("XXt = {}", XXt);
		// RealMatrix XXt_inv_X = XXt.inverse().multiply(X);
		// A = XXt_inv_X.multiply(G).transpose();

		log.info("At = {}", At);
		TrapezoidCorrection tc = new TrapezoidCorrection(At);
		Point p = new Point(128, 10);
		Point q;

		q = tc.map(p);
		log.info("{}  ~>  {}", p, q);

		p = new Point(92, 312);
		q = tc.map(p);
		log.info("{}  ->  {}", p, q);

		p = new Point(432, 303);
		q = tc.map(p);
		log.info("{}  ->  {}", p, q);

		//
		// p = tc.unmap(q);
		// log.info("{} -> {}", q, p);

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
