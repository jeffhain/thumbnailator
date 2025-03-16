/*
 * Thumbnailator - a thumbnail generation library
 *
 * Copyright (c) 2008-2020 Chris Kroells
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.coobird.thumbnailator.resizers;

import net.coobird.thumbnailator.builders.BufferedImageBuilder;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;

/**
 * A {@link Resizer} which performs resizing operations by using
 * progressive bilinear scaling.
 * <p>
 * The resizing technique used in this class is based on the technique
 * discussed in <em>Chapter 4: Images</em> of
 * <a href="http://filthyrichclients.org">Filthy Rich Clients</a>
 * by Chet Haase and Romain Guy.
 * <p>
 * The actual implemenation of the technique is independent of the code which
 * is provided in the book.
 * 
 * @author coobird
 *
 */
public class ProgressiveBilinearResizer extends AbstractResizer {
	/**
	 * Instantiates a {@link ProgressiveBilinearResizer} with default
	 * rendering hints.
	 */
	public ProgressiveBilinearResizer() {
		this(Collections.<RenderingHints.Key, Object>emptyMap());
	}
	
	/**
	 * Instantiates a {@link ProgressiveBilinearResizer} with the specified
	 * rendering hints.
	 * 
	 * @param hints		Additional rendering hints to apply.
	 */
	public ProgressiveBilinearResizer(Map<RenderingHints.Key, Object> hints) {
		super(RenderingHints.VALUE_INTERPOLATION_BILINEAR, hints);
	}
	
	/**
	 * Resizes an image using the progressive bilinear scaling technique.
	 * <p>
	 * If the source and/or destination image is {@code null}, then a
	 * {@link NullPointerException} will be thrown.
	 * 
	 * @param srcImage		The source image.
	 * @param destImage		The destination image.
	 * 
	 * @throws NullPointerException		When the source and/or the destination
	 * 									image is {@code null}.
	 */	
	@Override
	public void resize(BufferedImage srcImage, BufferedImage destImage)
			throws NullPointerException {
		super.performChecks(srcImage, destImage);
		
		final int targetWidth = destImage.getWidth();
		final int targetHeight = destImage.getHeight();
		
		BufferedImage currentSrcImage = srcImage;
		int currentSrcWidth = currentSrcImage.getWidth();
		int currentSrcHeight = currentSrcImage.getHeight();
		
		// Temporary image used for in-place resizing of image.
		BufferedImage tempImage = null;
		Graphics2D g = null;
		while (true) {
			// +1 to avoid downscaling by more than 2 when span is odd.
			final int tempDstWidth = Math.max(targetWidth, ((currentSrcWidth + 1) / 2));
			final int tempDstHeight = Math.max(targetHeight, ((currentSrcHeight + 1) / 2));
			if ((tempDstWidth <= targetWidth)
				&& (tempDstHeight <= targetHeight)) {
				/*
				 * Each current span is either close enough from, or lower than, target span:
				 * will finish by drawing with target spans on destination image.
				 */
				break;
			}
			
			if (tempImage == null) {
				tempImage = new BufferedImageBuilder(
					tempDstWidth,
					tempDstHeight,
					// Fastest type for bilinear (and bicubic).
					BufferedImage.TYPE_INT_ARGB_PRE
					).build();
				g = createGraphics(tempImage);
				g.setComposite(AlphaComposite.Src);
			}
			
			g.drawImage(
				currentSrcImage,
				0, 0, tempDstWidth, tempDstHeight,
				0, 0, currentSrcWidth, currentSrcHeight,
				null
				);
			
			currentSrcImage = tempImage;
			currentSrcWidth = tempDstWidth;
			currentSrcHeight = tempDstHeight;
		}
		if (g != null) {
			g.dispose();
		}
		
		// Last resizing: target spans onto the destination image.
		Graphics2D destg = createGraphics(destImage);
		destg.drawImage(currentSrcImage, 0, 0, targetWidth, targetHeight, 0, 0, currentSrcWidth, currentSrcHeight, null);
		destg.dispose();
	}
}
