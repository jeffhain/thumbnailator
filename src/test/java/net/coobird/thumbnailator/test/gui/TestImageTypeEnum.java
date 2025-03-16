/*
 * Copyright 2025 Jeff Hain
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.coobird.thumbnailator.test.gui;

import java.awt.image.BufferedImage;

/**
 * BufferedImage image types for test,
 * as an enum.
 */
public enum TestImageTypeEnum {
	TYPE_INT_RGB(BufferedImage.TYPE_INT_RGB),
	TYPE_INT_ARGB(BufferedImage.TYPE_INT_ARGB),
	TYPE_INT_ARGB_PRE(BufferedImage.TYPE_INT_ARGB_PRE),
	//
	TYPE_INT_BGR(BufferedImage.TYPE_INT_BGR),
	//
	TYPE_3BYTE_BGR(BufferedImage.TYPE_3BYTE_BGR),
	TYPE_4BYTE_ABGR(BufferedImage.TYPE_4BYTE_ABGR),
	TYPE_4BYTE_ABGR_PRE(BufferedImage.TYPE_4BYTE_ABGR_PRE),
	//
	TYPE_USHORT_565_RGB(BufferedImage.TYPE_USHORT_565_RGB),
	TYPE_USHORT_555_RGB(BufferedImage.TYPE_USHORT_555_RGB),
	//
	TYPE_BYTE_GRAY(BufferedImage.TYPE_BYTE_GRAY),
	TYPE_USHORT_GRAY(BufferedImage.TYPE_USHORT_GRAY),
	//
	TYPE_BYTE_BINARY(BufferedImage.TYPE_BYTE_BINARY),
	TYPE_BYTE_INDEXED(BufferedImage.TYPE_BYTE_INDEXED);
	/*
	 * 
	 */
	private final int imageType;
	TestImageTypeEnum(int imageType) {
		this.imageType = imageType;
	}
	public int imageType() {
		return this.imageType;
	}
}
