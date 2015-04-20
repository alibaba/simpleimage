package com.alibaba.simpleimage.analyze.testbed;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.alibaba.simpleimage.analyze.sift.SIFT;
import com.alibaba.simpleimage.analyze.sift.render.RenderImage;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 * 
	 * @throws IOException
	 * @throws
	 */
	public void testApp() throws IOException {
		String url = "https://cloud.githubusercontent.com/assets/8112710/7214699/be4689ca-e5e9-11e4-8502-48a92ff7827c.jpg";
		HttpURLConnection conn = (HttpURLConnection) new URL(url)
				.openConnection();
		InputStream in = conn.getInputStream();
		BufferedImage src = ImageIO.read(in);
		in.close();
		conn.disconnect();
		RenderImage ri = new RenderImage(src);
		SIFT sift = new SIFT();
		sift.detectFeatures(ri.toPixelFloatArray(null));
		System.out.println("detect points:" + sift.getGlobalKDFeaturePoints());
	}
}
