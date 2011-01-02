package net.coobird.thumbnailator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.builders.BufferedImageBuilder;
import net.coobird.thumbnailator.name.Rename;
import net.coobird.thumbnailator.resizers.Resizer;
import net.coobird.thumbnailator.resizers.Resizers;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;

import org.junit.Test;


/**
 * A class which tests the behavior of the builder interface of the
 * {@link Thumbnails} class.
 * 
 * @author coobird
 *
 */
public class ThumbnailsBuilderTest
{
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The size method is called.</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The thumbnail is successfully created.</li>
	 * <li>The thumbnail dimensions are that which is specified by the 
	 * size method.</li>
	 * </ol>
	 */
	@Test
	public void sizeOnly() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		BufferedImage thumbnail = Thumbnails.of(img)
			.size(50, 50)
			.asBufferedImage();
		
		assertEquals(50, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The size method is called.</li>
	 * <li>The keepAspectRatio method is true.</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The thumbnail is successfully created.</li>
	 * <li>The thumbnail dimensions are not the same as what is specified
	 * by the size method, but one that keeps the aspect ratio of the
	 * original.</li>
	 * </ol>
	 */
	@Test
	public void sizeWithAspectRatioTrue() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		BufferedImage thumbnail = Thumbnails.of(img)
			.size(120, 50)
			.keepAspectRatio(true)
			.asBufferedImage();
		
		assertEquals(50, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The size method is called.</li>
	 * <li>The keepAspectRatio method is false.</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The thumbnail is successfully created.</li>
	 * <li>The thumbnail dimensions are that which is specified by the 
	 * size method.</li>
	 * </ol>
	 */
	@Test
	public void sizeWithAspectRatioFalse() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		BufferedImage thumbnail = Thumbnails.of(img)
			.size(120, 50)
			.keepAspectRatio(false)
			.asBufferedImage();
		
		assertEquals(120, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}

	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The scale method is called.</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The thumbnail is successfully created.</li>
	 * <li>The thumbnail dimensions are determined by the value passed into the
	 * scale method.</li>
	 * </ol>
	 */
	@Test
	public void scaleOnly() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		BufferedImage thumbnail = Thumbnails.of(img)
			.scale(0.25f)
			.asBufferedImage();
		
		assertEquals(50, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The scale method is called.</li>
	 * <li>The keepAspectRatio method is true.</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An {@link IllegalStateException} is thrown</li>
	 * </ol>
	 */	
	@Test
	public void scaleWithAspectRatioTrue() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		try
		{
			Thumbnails.of(img)
				.scale(0.5f)
				.keepAspectRatio(true)
				.asBufferedImage();
			
			fail();
		}
		catch (IllegalStateException e)
		{
			assertTrue(e.getMessage().contains("scaling factor has already been specified"));
		}
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The scale method is called.</li>
	 * <li>The keepAspectRatio method is false.</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An {@link IllegalStateException} is thrown</li>
	 * </ol>
	 */	
	@Test
	public void scaleWithAspectRatioFalse() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		try
		{
			Thumbnails.of(img)
				.scale(0.5f)
				.keepAspectRatio(false)
				.asBufferedImage();
			
			fail();
		}
		catch (IllegalStateException e)
		{
			assertTrue(e.getMessage().contains("scaling factor has already been specified"));
		}
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The keepAspectRatio method is called before the size method.</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An {@link IllegalStateException} is thrown</li>
	 * </ol>
	 */	
	@Test
	public void keepAspectRatioBeforeSize() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		try
		{
			Thumbnails.of(img)
				.keepAspectRatio(false);
			
			fail();
		}
		catch (IllegalStateException e)
		{
			assertTrue(e.getMessage().contains("unless the size parameter has already been specified."));
		}
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The keepAspectRatio method is called after the scale method.</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An {@link IllegalStateException} is thrown</li>
	 * </ol>
	 */	
	@Test
	public void keepAspectRatioAfterScale() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		try
		{
			Thumbnails.of(img)
				.scale(0.5f)
				.keepAspectRatio(false)
				.asBufferedImage();
			
			fail();
		}
		catch (IllegalStateException e)
		{
			assertTrue(e.getMessage().contains("scaling factor has already been specified"));
		}
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The resizer method is called</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The specified Resizer is called for resizing.</li>
	 * </ol>
	 */	
	@Test
	public void resizerOnly() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		Resizer resizer = mock(Resizer.class);
	
		BufferedImage thumbnail = Thumbnails.of(img)
			.size(200, 200)
			.resizer(resizer)
			.asBufferedImage();
		
		verify(resizer).resize(img, thumbnail);
		verifyNoMoreInteractions(resizer);
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The resizer method is called twice</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An {@link IllegalStateException} is thrown</li>
	 * </ol>
	 */	
	@Test(expected=IllegalStateException.class)
	public void resizerTwice() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		Thumbnails.of(img)
			.size(200, 200)
			.resizer(Resizers.PROGRESSIVE)
			.resizer(Resizers.PROGRESSIVE)
			.asBufferedImage();
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The scalingMode method is called</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The thumbnail is successfully created.</li>
	 * </ol>
	 */	
	@Test
	public void scalingModeOnly() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		Thumbnails.of(img)
			.size(200, 200)
			.scalingMode(ScalingMode.PROGRESSIVE_BILINEAR)
			.asBufferedImage();
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The scalingMode method is called twice</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An {@link IllegalStateException} is thrown</li>
	 * </ol>
	 */	
	@Test(expected=IllegalStateException.class)
	public void scalingModeTwice() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		Thumbnails.of(img)
			.size(200, 200)
			.scalingMode(ScalingMode.PROGRESSIVE_BILINEAR)
			.scalingMode(ScalingMode.PROGRESSIVE_BILINEAR)
			.asBufferedImage();
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The resizer method is called, then</li>
	 * <li>The scalingMode method is called</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An {@link IllegalStateException} is thrown</li>
	 * </ol>
	 */	
	@Test(expected=IllegalStateException.class)
	public void resizerThenScalingMode() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		Thumbnails.of(img)
			.size(200, 200)
			.resizer(Resizers.PROGRESSIVE)
			.scalingMode(ScalingMode.PROGRESSIVE_BILINEAR)
			.asBufferedImage();
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The scalingMode method is called, then</li>
	 * <li>The resizer method is called</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An {@link IllegalStateException} is thrown</li>
	 * </ol>
	 */	
	@Test(expected=IllegalStateException.class)
	public void scalingModeThenResizer() throws IOException
	{
		BufferedImage img = new BufferedImageBuilder(200, 200).build();
		
		Thumbnails.of(img)
			.size(200, 200)
			.scalingMode(ScalingMode.PROGRESSIVE_BILINEAR)
			.resizer(Resizers.PROGRESSIVE)
			.asBufferedImage();
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The imageType method is not called</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The image type of the resulting image is the same as the original
	 * image</li>
	 * </ol>
	 */	
	@Test
	public void imageTypeNotCalled() throws IOException
	{
		BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_BYTE_INDEXED);
		
		BufferedImage thumbnail = Thumbnails.of(img)
			.size(200, 200)
			.asBufferedImage();
		
		assertEquals(BufferedImage.TYPE_BYTE_INDEXED, thumbnail.getType());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The imageType method is called with the same type as the original
	 * image</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The image type of the resulting image is the same as the original
	 * image</li>
	 * </ol>
	 */	
	@Test
	public void imageTypeCalledSameType() throws IOException
	{
		BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_BYTE_GRAY);
		
		BufferedImage thumbnail = Thumbnails.of(img)
			.size(200, 200)
			.imageType(BufferedImage.TYPE_BYTE_GRAY)
			.asBufferedImage();
		
		assertEquals(BufferedImage.TYPE_BYTE_GRAY, thumbnail.getType());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The imageType method is called with the different type as the
	 * original image</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The image type of the resulting image is the the specified by the
	 * imageType method</li>
	 * </ol>
	 */	
	@Test
	public void imageTypeCalledDifferentType() throws IOException
	{
		BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_BYTE_GRAY);
		
		BufferedImage thumbnail = Thumbnails.of(img)
			.size(200, 200)
			.imageType(BufferedImage.TYPE_BYTE_GRAY)
			.asBufferedImage();
		
		assertEquals(BufferedImage.TYPE_BYTE_GRAY, thumbnail.getType());
	}

	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The imageType method is called twice</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An IllegalStateException is thrown.</li>
	 * </ol>
	 */	
	@Test(expected=IllegalStateException.class)
	public void imageTypeCalledTwice() throws IOException
	{
		BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_BYTE_GRAY);
		
		Thumbnails.of(img)
			.size(200, 200)
			.imageType(BufferedImage.TYPE_BYTE_GRAY)
			.imageType(BufferedImage.TYPE_BYTE_GRAY)
			.asBufferedImage();
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(float) is 0.0f</li>
	 * <li>toFile(File)</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The outputQuality is allowed</li>
	 * <li>The thumbnail is successfully produced</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputQuality_float_ValidArg_ZeroZero() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(0.0f)
			.toFile(outFile);
		
		BufferedImage thumbnail = ImageIO.read(outFile);
		
		assertEquals(50, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(float) is 0.5f</li>
	 * <li>toFile(File)</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The outputQuality is allowed</li>
	 * <li>The thumbnail is successfully produced</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputQuality_float_ValidArg_ZeroFive() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(0.5f)
			.toFile(outFile);
		
		BufferedImage thumbnail = ImageIO.read(outFile);
		
		assertEquals(50, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(float) is 1.0f</li>
	 * <li>toFile(File)</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The outputQuality is allowed</li>
	 * <li>The thumbnail is successfully produced</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputQuality_float_ValidArg_OneZero() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(1.0f)
			.toFile(outFile);
		
		BufferedImage thumbnail = ImageIO.read(outFile);
		
		assertEquals(50, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(float) is negative</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An IllegalArgumentException is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test(expected=IllegalArgumentException.class)
	public void outputQuality_float_InvalidArg_Negative() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(-0.01f);
	}

	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(float) is greater than 1.0d</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An IllegalArgumentException is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test(expected=IllegalArgumentException.class)
	public void outputQuality_float_InvalidArg_OverOne() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(1.01f);
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(double) is 0.0d</li>
	 * <li>toFile(File)</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The outputQuality is allowed</li>
	 * <li>The thumbnail is successfully produced</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputQuality_double_ValidArg_ZeroZero() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(0.0d)
			.toFile(outFile);
		
		BufferedImage thumbnail = ImageIO.read(outFile);
		
		assertEquals(50, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(double) is 0.5d</li>
	 * <li>toFile(File)</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The outputQuality is allowed</li>
	 * <li>The thumbnail is successfully produced</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputQuality_double_ValidArg_ZeroFive() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(0.5d)
			.toFile(outFile);
		
		BufferedImage thumbnail = ImageIO.read(outFile);
		
		assertEquals(50, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(double) is 1.0d</li>
	 * <li>toFile(File)</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The outputQuality is allowed</li>
	 * <li>The thumbnail is successfully produced</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputQuality_double_ValidArg_OneZero() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(1.0d)
			.toFile(outFile);
		
		BufferedImage thumbnail = ImageIO.read(outFile);
		
		assertEquals(50, thumbnail.getWidth());
		assertEquals(50, thumbnail.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(double) is negative</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An IllegalArgumentException is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test(expected=IllegalArgumentException.class)
	public void outputQuality_double_InvalidArg_Negative() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(-0.01d);
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputQuality(double) is greater than 1.0d</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An IllegalArgumentException is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test(expected=IllegalArgumentException.class)
	public void outputQuality_double_InvalidArg_OverOne() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.jpg");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.outputQuality(1.01d);
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>format name matches the file extension</li>
	 * <li>format is same as the original format</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_SameAsOriginal_SameAsFileExtension() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.png");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.png");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("png")
			.toFile(outFile);
		
		BufferedImage fromFileImage = ImageIO.read(outFile);
		
		String formatName = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile)).next().getFormatName();
		assertEquals("png", formatName);
		
		assertEquals(50, fromFileImage.getWidth());
		assertEquals(50, fromFileImage.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>format name matches the file extension</li>
	 * <li>format is different from the original format</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_DiffersFromOriginal_SameAsFileExtension() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.png");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.toFile(outFile);
		
		BufferedImage fromFileImage = ImageIO.read(outFile);
		
		String formatName = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile)).next().getFormatName();
		assertEquals("JPEG", formatName);
		
		assertEquals(50, fromFileImage.getWidth());
		assertEquals(50, fromFileImage.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>format name matches the file extension</li>
	 * <li>format is different from the original format</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_DiffersFromOriginal_SameAsFileExtension_Jpeg() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.png");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		outFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpeg")
			.toFile(outFile);
		
		BufferedImage fromFileImage = ImageIO.read(outFile);
		
		String formatName = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile)).next().getFormatName();
		assertEquals("JPEG", formatName);
		
		assertEquals(50, fromFileImage.getWidth());
		assertEquals(50, fromFileImage.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>format name differs from the file extension</li>
	 * <li>format is same as the original format</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * <li>The format extension is appended to the output filename.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_SameAsOriginal_DiffersFromFileExtension() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.png");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.jpg");
		
		File actualOutFile = new File("test-resources/Thumbnailator/grid.tmp.jpg.png");
		actualOutFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("png")
			.toFile(outFile);
		
		BufferedImage fromFileImage = ImageIO.read(actualOutFile);
		
		String formatName = ImageIO.getImageReaders(ImageIO.createImageInputStream(actualOutFile)).next().getFormatName();
		assertEquals("png", formatName);
		
		assertEquals(50, fromFileImage.getWidth());
		assertEquals(50, fromFileImage.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>format name differs from the file extension</li>
	 * <li>format differs from the original format</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * <li>The format extension is appended to the output filename.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_DiffersFromOriginal_DiffersFromFileExtension() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.png");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.png");

		File actualOutFile = new File("test-resources/Thumbnailator/grid.tmp.png.jpg");
		actualOutFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpg")
			.toFile(outFile);
		
		BufferedImage fromFileImage = ImageIO.read(actualOutFile);
		
		String formatName = ImageIO.getImageReaders(ImageIO.createImageInputStream(actualOutFile)).next().getFormatName();
		assertEquals("JPEG", formatName);
		
		assertEquals(50, fromFileImage.getWidth());
		assertEquals(50, fromFileImage.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>format name differs from the file extension</li>
	 * <li>format differs from the original format</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * <li>The format extension is appended to the output filename.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_DiffersFromOriginal_DiffersFromFileExtension_Jpeg() throws IOException
	{
		File f = new File("test-resources/Thumbnailator/grid.png");
		File outFile = new File("test-resources/Thumbnailator/grid.tmp.png");
		
		File actualOutFile = new File("test-resources/Thumbnailator/grid.tmp.png.jpeg");
		actualOutFile.deleteOnExit();
		
		Thumbnails.of(f)
			.size(50, 50)
			.outputFormat("jpeg")
			.toFile(outFile);
		
		BufferedImage fromFileImage = ImageIO.read(actualOutFile);
		
		String formatName = ImageIO.getImageReaders(ImageIO.createImageInputStream(actualOutFile)).next().getFormatName();
		assertEquals("JPEG", formatName);
		
		assertEquals(50, fromFileImage.getWidth());
		assertEquals(50, fromFileImage.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>multiple files</li>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>format name is same as file extension</li>
	 * <li>format is same as original format</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_Multiple_SameAsOriginal_SameAsExtension_Both() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.png");
		File f2 = new File("test-resources/Thumbnailator/igrid.png");
		
		// when
		Thumbnails.of(f1, f2)
			.size(50, 50)
			.outputFormat("png")
			.toFiles(Rename.PREFIX_DOT_THUMBNAIL);
		
		// then
		File outFile1 = new File("test-resources/Thumbnailator/thumbnail.grid.png");
		File outFile2 = new File("test-resources/Thumbnailator/thumbnail.igrid.png");
		outFile1.deleteOnExit();
		outFile2.deleteOnExit();
		
		BufferedImage fromFileImage1 = ImageIO.read(outFile1);
		BufferedImage fromFileImage2 = ImageIO.read(outFile2);
		
		String formatName1 = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile1)).next().getFormatName();
		assertEquals("png", formatName1);
		String formatName2 = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile2)).next().getFormatName();
		assertEquals("png", formatName2);
		
		assertEquals(50, fromFileImage1.getWidth());
		assertEquals(50, fromFileImage1.getHeight());
		assertEquals(50, fromFileImage2.getWidth());
		assertEquals(50, fromFileImage2.getHeight());
	}
	
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>multiple files</li>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>1st file: file extension same as format specified</li>
	 * <li>2nd file: file extension differs from format specified</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * <li>For the file with the different format and extension, the extension
	 * of the specified format will be added to the file name.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_Multiple_FirstExtensionSame_SecondExtensionDifferent() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.png");
		File f2 = new File("test-resources/Thumbnailator/grid.jpg");
		
		// when
		Thumbnails.of(f1, f2)
		.size(50, 50)
		.outputFormat("png")
		.toFiles(Rename.PREFIX_DOT_THUMBNAIL);
		
		// then
		File outFile1 = new File("test-resources/Thumbnailator/thumbnail.grid.png");
		File outFile2 = new File("test-resources/Thumbnailator/thumbnail.grid.jpg.png");
		outFile1.deleteOnExit();
		outFile2.deleteOnExit();
		
		BufferedImage fromFileImage1 = ImageIO.read(outFile1);
		BufferedImage fromFileImage2 = ImageIO.read(outFile2);
		
		String formatName1 = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile1)).next().getFormatName();
		assertEquals("png", formatName1);
		String formatName2 = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile2)).next().getFormatName();
		assertEquals("png", formatName2);
		
		assertEquals(50, fromFileImage1.getWidth());
		assertEquals(50, fromFileImage1.getHeight());
		assertEquals(50, fromFileImage2.getWidth());
		assertEquals(50, fromFileImage2.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>multiple files</li>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>1st file: file extension differs from format specified</li>
	 * <li>2nd file: file extension same as format specified</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * <li>For the file with the different format and extension, the extension
	 * of the specified format will be added to the file name.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_Multiple_FirstExtensionDifferent_SecondExtensionSame() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		File f2 = new File("test-resources/Thumbnailator/grid.png");
		
		// when
		Thumbnails.of(f1, f2)
			.size(50, 50)
			.outputFormat("png")
			.toFiles(Rename.PREFIX_DOT_THUMBNAIL);
		
		// then
		File outFile1 = new File("test-resources/Thumbnailator/thumbnail.grid.jpg.png");
		File outFile2 = new File("test-resources/Thumbnailator/thumbnail.grid.png");
		outFile1.deleteOnExit();
		outFile2.deleteOnExit();
		
		BufferedImage fromFileImage1 = ImageIO.read(outFile1);
		BufferedImage fromFileImage2 = ImageIO.read(outFile2);
		
		String formatName1 = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile1)).next().getFormatName();
		assertEquals("png", formatName1);
		String formatName2 = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile2)).next().getFormatName();
		assertEquals("png", formatName2);
		
		assertEquals(50, fromFileImage1.getWidth());
		assertEquals(50, fromFileImage1.getHeight());
		assertEquals(50, fromFileImage2.getWidth());
		assertEquals(50, fromFileImage2.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>multiple files</li>
	 * <li>outputFormat</li>
	 * <li>toFile(File)</li>
	 * <li>1st file: file extension differs from format specified</li>
	 * <li>2nd file: file extension differs from format specified</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>The output format of the image is one that is specified.</li>
	 * <li>For the file with the different format and extension, the extension
	 * of the specified format will be added to the file name.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_Multiple_FirstExtensionDifferent_SecondExtensionDifferent() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		File f2 = new File("test-resources/Thumbnailator/grid.bmp");
		
		// when
		Thumbnails.of(f1, f2)
			.size(50, 50)
			.outputFormat("png")
			.toFiles(Rename.PREFIX_DOT_THUMBNAIL);
		
		// then
		File outFile1 = new File("test-resources/Thumbnailator/thumbnail.grid.jpg.png");
		File outFile2 = new File("test-resources/Thumbnailator/thumbnail.grid.bmp.png");
		outFile1.deleteOnExit();
		outFile2.deleteOnExit();
		
		BufferedImage fromFileImage1 = ImageIO.read(outFile1);
		BufferedImage fromFileImage2 = ImageIO.read(outFile2);
		
		String formatName1 = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile1)).next().getFormatName();
		assertEquals("png", formatName1);
		String formatName2 = ImageIO.getImageReaders(ImageIO.createImageInputStream(outFile2)).next().getFormatName();
		assertEquals("png", formatName2);
		
		assertEquals(50, fromFileImage1.getWidth());
		assertEquals(50, fromFileImage1.getHeight());
		assertEquals(50, fromFileImage2.getWidth());
		assertEquals(50, fromFileImage2.getHeight());
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat with a supported format</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>No exception is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_SupportedFormat() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		
		// when
		Thumbnails.of(f1)
			.size(50, 50)
			.outputFormat("png");
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat with a supported format</li>
	 * <li>toFile(File)</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An IllegalArgumentException is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test(expected=IllegalArgumentException.class)
	public void outputFormat_UnsupportedFormat() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		
		// when
		Thumbnails.of(f1)
			.size(50, 50)
			.outputFormat("unsupported");
		
		// then
		// expect an IllagelArgumentException.
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat specified</li>
	 * <li>outputFormatType is supported</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>No exception is thrown</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_Checks_FormatSpecified_TypeSupported() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		
		// when
		Thumbnails.of(f1)
			.size(50, 50)
			.outputFormat("JPEG")
			.outputFormatType("JPEG");
		
		// then
		// no exception occurs
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat specified</li>
	 * <li>outputFormatType is specified, but the outputFormat does not
	 * support compression.</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An IllegalArgumentException is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test(expected=IllegalArgumentException.class)
	public void outputFormat_Checks_FormatSpecified_FormatDoesNotSupportCompression() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		
		// when
		Thumbnails.of(f1)
			.size(50, 50)
			.outputFormat("PNG")
			.outputFormatType("JPEG");
		
		// then
		// expect an IllagelArgumentException.
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat specified</li>
	 * <li>outputFormatType is unsupported</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An IllegalArgumentException is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test(expected=IllegalArgumentException.class)
	public void outputFormat_Checks_FormatSpecified_TypeUnsupported() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		
		// when
		Thumbnails.of(f1)
		.size(50, 50)
		.outputFormat("JPEG")
		.outputFormatType("foo");
		
		// then
		// expect an IllagelArgumentException.
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat specified</li>
	 * <li>outputFormatType is default for the output format</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>No exception is thrown</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_Checks_FormatSpecified_TypeDefault() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		
		// when
		Thumbnails.of(f1)
			.size(50, 50)
			.outputFormat("JPEG")
			.outputFormatType(ThumbnailParameter.DEFAULT_FORMAT_TYPE);
		
		// then
		// no exception occurs
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat is the format of the original image</li>
	 * <li>outputFormatType is specified</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>An IllegalArgumentException is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test(expected=IllegalArgumentException.class)
	public void outputFormat_Checks_FormatSpecifiedAsOriginal_TypeSpecified() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		
		// when
		Thumbnails.of(f1)
			.size(50, 50)
			.outputFormat(ThumbnailParameter.ORIGINAL_FORMAT)
			.outputFormatType("JPEG");
		
		// then
		// expect an IllagelArgumentException.
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>outputFormat is the format of the original image</li>
	 * <li>outputFormatType is specified</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>No exception is thrown.</li>
	 * </ol>
	 * @throws IOException 
	 */	
	@Test
	public void outputFormat_Checks_FormatSpecifiedAsOriginal_TypeIsDefaultForFormat() throws IOException
	{
		// given
		File f1 = new File("test-resources/Thumbnailator/grid.jpg");
		
		// when
		Thumbnails.of(f1)
			.size(50, 50)
			.outputFormat(ThumbnailParameter.ORIGINAL_FORMAT)
			.outputFormatType(ThumbnailParameter.DEFAULT_FORMAT_TYPE);
		
		// then
		// no exception is thrown.
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The resizer method is called with null</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>A NullPointerException is thrown</li>
	 * </ol>
	 */	
	@Test(expected=NullPointerException.class)
	public void resizer_Null()
	{
		try
		{
			// given
			// when
			Thumbnails.of("non-existent-file")
				.size(200, 200)
				.resizer(null);
		}
		catch (NullPointerException e)
		{
			// then
			assertEquals("Resizer is null.", e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The alphaInterpolation method is called with null</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>A NullPointerException is thrown</li>
	 * </ol>
	 */	
	@Test(expected=NullPointerException.class)
	public void alphaInterpolation_Null()
	{
		try
		{
			// given
			// when
			Thumbnails.of("non-existent-file")
				.size(200, 200)
				.alphaInterpolation(null);
		}
		catch (NullPointerException e)
		{
			// then
			assertEquals("Alpha interpolation is null.", e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The dithering method is called with null</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>A NullPointerException is thrown</li>
	 * </ol>
	 */	
	@Test(expected=NullPointerException.class)
	public void dithering_Null()
	{
		try
		{
			// given
			// when
			Thumbnails.of("non-existent-file")
				.size(200, 200)
				.dithering(null);
		}
		catch (NullPointerException e)
		{
			// then
			assertEquals("Dithering is null.", e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The antialiasing method is called with null</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>A NullPointerException is thrown</li>
	 * </ol>
	 */	
	@Test(expected=NullPointerException.class)
	public void antialiasing_Null()
	{
		try
		{
			// given
			// when
			Thumbnails.of("non-existent-file")
				.size(200, 200)
				.antialiasing(null);
		}
		catch (NullPointerException e)
		{
			// then
			assertEquals("Antialiasing is null.", e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The rendering method is called with null</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>A NullPointerException is thrown</li>
	 * </ol>
	 */	
	@Test(expected=NullPointerException.class)
	public void rendering_Null()
	{
		try
		{
			// given
			// when
			Thumbnails.of("non-existent-file")
				.size(200, 200)
				.rendering(null);
		}
		catch (NullPointerException e)
		{
			// then
			assertEquals("Rendering is null.", e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Test for the {@link Thumbnails.Builder} class where,
	 * <ol>
	 * <li>The scalingMode method is called with null</li>
	 * </ol>
	 * and the expected outcome is,
	 * <ol>
	 * <li>A NullPointerException is thrown</li>
	 * </ol>
	 */	
	@Test(expected=NullPointerException.class)
	public void scalingMode_Null()
	{
		try
		{
			// given
			// when
			Thumbnails.of("non-existent-file")
				.size(200, 200)
				.scalingMode(null);
		}
		catch (NullPointerException e)
		{
			// then
			assertEquals("Scaling mode is null.", e.getMessage());
			throw e;
		}
	}
}
