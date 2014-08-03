package com.hoglet.correctperspective;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.FileOutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.PerspectiveTransform;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpPerspective;

public class CorrectPerspective {

	// /**
	// * this gets rid of exception for not using native acceleration
	// */
	// static {
	// System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	// }

	public static final void main(String args[]) {

		try {

			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
			do {
				ImageWriter jiioWriter = (ImageWriter) iter.next();
				System.out.println(jiioWriter.getClass().getName());
			} while (iter.hasNext());

			String src = "../../attempt6/xxx_00.png";
			String dst = "../../attempt6/yyy_00.png";

			System.out.println("Reading image");
			// BufferedImage image = ImageIO.read(src);

			// Load the input image.
			RenderedOp reader = JAI.create("fileload", src);
			RenderedImage image = reader.createInstance();


			// double xp[] = new double[] { 2090, 8021, 8021, 2090 };
			// double yp[] = new double[] { 2111, 2111, 8884, 8884 };

			// values used for attempt3
			// double x[] = new double[] { 2090, 8060, 8021, 2051 };
			// double y[] = new double[] { 2111, 2151, 8884, 8838 };
			// double xp[] = new double[] { 2090, 8021, 8021, 2090 };
			// double yp[] = new double[] { 2151, 2151, 8838, 8838};

			double x[] = new double[] { 1862, 8256, 8216, 1821 };
			double y[] = new double[] { 1887, 1931, 8876, 8828 };

			int origin = 0;
			int cellsize = 40;
			int w = 8 + 10 * 15 + 5;
			int h = 8 + 11 * 15 + 5;
			double xp[] = new double[] { origin, origin + w * cellsize, origin + w * cellsize, origin };
			double yp[] = new double[] { origin, origin, origin + h * cellsize, origin + h * cellsize };

			// Quality related hints when scaling the image
			RenderingHints rh = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			rh.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			rh.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

			System.out.println("Creating transform");
			PerspectiveTransform transform = PerspectiveTransform.getQuadToQuad(x[0], y[0], x[1], y[1], x[2], y[2], x[3], y[3],
					xp[0], yp[0], xp[1], yp[1], xp[2], yp[2], xp[3], yp[3]);
			ParameterBlock transformPb = new ParameterBlock();
			transformPb.addSource(image);
			transformPb.add(new WarpPerspective(transform.createInverse()));
			
			// Create the transform operation
			RenderedOp transformOp = JAI.create("warp", transformPb, rh);
			System.out.println("transform op bounds = " + transformOp.getBounds());

			ParameterBlock cropPb = new ParameterBlock();
			cropPb.addSource(transformOp);
			cropPb.add((float) origin);
			cropPb.add((float) origin);
			cropPb.add((float) cellsize * w);
			cropPb.add((float) cellsize * h);

			// Create the crop operation
			RenderedOp cropOp = JAI.create("crop", cropPb);
			System.out.println("crop op bounds = " + cropOp.getBounds());

			// Execute the chain of operations
			System.out.println("Transforming image");
			RenderedImage rendered = cropOp.createInstance();

			// Write the result
			System.out.println("Writing image: size " + rendered.getMinX() + ", " + rendered.getMinY() + "; " + rendered.getWidth() + "x" + rendered.getHeight());
			FileOutputStream stream = new FileOutputStream(dst);
			JAI.create("encode", rendered, stream, "PNG");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
