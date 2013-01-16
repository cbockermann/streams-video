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

	public final static Trapez EINHEITS_TRAPEZ = new Trapez(new Point(0, 0),
			new Point(0, 1), new Point(1, 0), new Point(1, 1));

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

		RealMatrix b = new RealMatrixImpl(new double[][] { { 1.0 }, { x.x },
				{ x.y }, { x.x * x.y } });
		log.info("Point b = {}", b);
		RealMatrix p = A.multiply(b);
		log.info("   A b = {}", p);
		Long nx = Math.round(p.getData()[1][0]);
		Long ny = Math.round(p.getData()[2][0]);
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
				{ 1.0, 1.0, 1.0, 1.0 },
				{ from.tl.x, from.bl.x, from.tr.x, from.br.x },
				{ from.tl.y, from.bl.y, from.tr.y, from.br.y },
				{ from.tl.x * from.tl.y, from.bl.x * from.bl.y,
						from.tr.x * from.tr.y, from.br.x * from.br.y } };
		return new RealMatrixImpl(tf);
	}

	public static void main(String[] args) {

		Trapez g = new Trapez(new Point(0, 0), new Point(0, 1024), new Point(
				768, 0), new Point(1024, 768));

		Trapez x = new Trapez(new Point(128, 10), new Point(92, 312),
				new Point(517, 8), new Point(432, 303));

		RealMatrix At = compute(x, g);

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
	}

	public static RealMatrix matrix4x4(Trapez from) {
		double[][] tf = new double[][] {
				{ 1.0, 1.0, 1.0, 1.0 },
				{ from.tl.x, from.bl.x, from.tr.x, from.br.x },
				{ from.tl.y, from.bl.y, from.tr.y, from.br.y },
				{ from.tl.x * from.tl.y, from.bl.x * from.bl.y,
						from.tr.x * from.tr.y, from.br.x * from.br.y } };

		return new RealMatrixImpl(tf);
	}

	public static RealMatrix trapez2matrixT(Trapez from) {
		double[][] tf = new double[][] { { from.tl.x, from.tl.y },
				{ from.bl.x, from.bl.y }, { from.tr.x, from.tr.y },
				{ from.br.x, from.br.y } };

		return new RealMatrixImpl(tf);
	}

	public static RealMatrix compute(Trapez from, Trapez to) {

		RealMatrix X = matrix4x4(from);
		RealMatrix G = matrix4x4(to);

		RealMatrix At;
		RealMatrix XXt_inv = X.multiply(X.transpose()).inverse();
		// log.info("XX^T = {}", XXt);
		//

		At = XXt_inv.multiply(X).multiply(G.transpose()).transpose();
		log.info("At = {}", At);

		return At;
	}
}
