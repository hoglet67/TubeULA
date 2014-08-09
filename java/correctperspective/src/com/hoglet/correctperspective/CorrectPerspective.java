package com.hoglet.correctperspective;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.PerspectiveTransform;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpPerspective;

public class CorrectPerspective {

	private static Map<String, Double[]> xMap = new HashMap<String, Double[]>();
	private static Map<String, Double[]> yMap = new HashMap<String, Double[]>();
	
	static {
		
		xMap.put("00", new Double[] { 1862.0, 8256.0, 8216.0, 1821.0 });
		yMap.put("00", new Double[] { 1887.0, 1931.0, 8876.0, 8828.0 });
		
		xMap.put("10", new Double[] {  483.0, 6795.0, 6757.0,  442.0 });
		yMap.put("10", new Double[] { 1926.0, 1967.0, 8921.0, 8878.0 });

		xMap.put("20", new Double[] {  551.0, 6949.0, 6907.0,  511.0 });
		yMap.put("20", new Double[] { 1967.0, 2008.0, 8959.0, 8922.0 });

		xMap.put("01", new Double[] { 1820.0, 8215.0, 8177.0, 1783.0 });
		yMap.put("01", new Double[] {  296.0,  347.0, 7216.0, 7173.0 });

		xMap.put("11", new Double[] {  442.0, 6753.0, 6716.0,  405.0 });
		yMap.put("11", new Double[] {  348.0,  393.0, 7255.0, 7216.0 });

		xMap.put("21", new Double[] {  508.0, 6906.0, 6867.0,  470.0 });
		yMap.put("21", new Double[] {  391.0,  428.0, 7289.0, 7257.0 });

		xMap.put("02", new Double[] { 1783.0, 8176.0, 8138.0, 1745.0 });
		yMap.put("02", new Double[] {  264.0,  316.0, 7263.0, 7217.0 });

		xMap.put("12", new Double[] {  404.0, 6719.0, 6682.0,  366.0 });
		yMap.put("12", new Double[] {  316.0,  354.0, 7301.0, 7262.0 });

		xMap.put("22", new Double[] {  471.0, 6866.0, 6832.0,  434.0 });
		yMap.put("22", new Double[] {  354.0,  386.0, 7331.0, 7302.0 });

	}

	public static final void main(String args[]) {

		try {

			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
			do {
				ImageWriter jiioWriter = (ImageWriter) iter.next();
				System.out.println(jiioWriter.getClass().getName());
			} while (iter.hasNext());

			if (args.length != 2) {
				System.err.println("usage: java -jar ulamangling.jar <Src PNG> <Dst PNG> ");
				System.exit(1);
			}
			File srcFile = new File(args[0]);
			if (!srcFile.exists()) {
				System.err.println("Src File: " + srcFile + " does not exist");
				System.exit(1);
			}
			if (!srcFile.isFile()) {
				System.err.println("Src File: " + srcFile + " is not a file");
				System.exit(1);
			}
			File dstFile = new File(args[1]);
			
			String name = srcFile.getName();
			name = name.substring(name.indexOf('_') + 1, name.lastIndexOf('.'));
			System.out.println("# name = " + name);
			
			
			String src = srcFile.getAbsolutePath();
			String dst = dstFile.getAbsolutePath();

			System.out.println("Reading image");
			// BufferedImage image = ImageIO.read(src);

			// Load the input image.
			RenderedOp reader = JAI.create("fileload", src);
			RenderedImage image = reader.createInstance();

			Double x[] = xMap.get(name);
			Double y[] = yMap.get(name);

			int origin = 0;
			int cellsize = 40;
			int w = 10 * 15;
			int h = 11 * 15;
			if (name.charAt(0) == '1') {
				w += 11;
			} else {
				w += 13;
			}
			if (name.charAt(1) == '1') {
				h += 11;
			} else {
				h += 13;
			}
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
