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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.coobird.thumbnailator.resizers.Resizers;
import net.coobird.thumbnailator.resizers.prl.ParallelResizer;

/**
 * GUI for exploratory testing/benching of Jimsizr.
 */
public class ThbTestGui {
	
	//--------------------------------------------------------------------------
	// CONFIGURATION
	//--------------------------------------------------------------------------
	
	/*
	 * Default resize configuration.
	 */
	
	private static final String IMAGE_DIR_PATH = "src/test/resources/Thumbnailator";
	private static final File IMAGE_DIR = new File(IMAGE_DIR_PATH);
	
	private static final TestImageTypeEnum DEFAULT_SRC_IMAGE_TYPE =
		TestImageTypeEnum.TYPE_INT_ARGB_PRE;
	private static final TestImageTypeEnum DEFAULT_DST_IMAGE_TYPE =
		TestImageTypeEnum.TYPE_INT_ARGB_PRE;
	
	private static final boolean DEFAULT_PARALLEL_ELSE_SEQUENTIAL = true;
	
	/**
	 * Both fast and qualitative.
	 */
	private static final int DEFAULT_SCALING_ALGO_INDEX =
		Resizers.PARALLEL_PROGRESSIVE_BILINEAR.ordinal();
	
	/*
	 * Resize configuration.
	 */
	
	private static final int MAX_PARALLELISM =
		Runtime.getRuntime().availableProcessors();
	
	/*
	 * Default GUI configuration.
	 */
	
	private static final MyResizeTrigger DEFAULT_UPDATE_TRIGGER =
		MyResizeTrigger.CHOICE_OR_DELAY_100_MS;
	
	private static final int FRAME_MIN_POSSIBLE_WIDTH = 120;
	
	private static final int INITIAL_DST_IMAGE_FRAME_WIDTH = FRAME_MIN_POSSIBLE_WIDTH + 500;
	private static final int INITIAL_DST_IMAGE_FRAME_HEIGHT = 500;
	
	private static final int INITIAL_CONTROL_FRAME_WIDTH = 420;
	private static final int INITIAL_CONTROL_FRAME_HEIGHT = 320;
	
	/*
	 * GUI configuration.
	 */
	
	private static final boolean MUST_RESIZE_IN_BG_THREAD = true;
	
	//--------------------------------------------------------------------------
	// PRIVATE CLASSES
	//--------------------------------------------------------------------------
	
	private enum MyResizeTrigger {
		CHOICE_OR_COMPONENT_RESIZED,
		CHOICE_OR_DELAY_1_S,
		CHOICE_OR_DELAY_100_MS
	}
	
	private enum MyScheduleType {
		ASAP(0),
		DELAY_100_MS(100),
		DELAY_1_S(1000);
		final long delayMs;
		private MyScheduleType(long delayMs) {
			this.delayMs = delayMs;
		}
	}
	
	/**
	 * Immutable.
	 * 
	 * To transfer choices done on GUI in EDT,
	 * to the BG thread that computes resized dst image
	 * and eventually reloads ref image and recompute src image
	 * before if needed.
	 * This allows to keep the GUI reactive.
	 */
	private static final class MyBgInput {
		final String refImageName;
		final TestImageTypeEnum srcImageType;
		final TestImageTypeEnum dstImageType;
		final int dstImageWidth;
		final int dstImageHeight;
		final int parallelism;
		final Object algo;
		public MyBgInput(
			final String refImageName,
			final TestImageTypeEnum srcImageType,
			final TestImageTypeEnum dstImageType,
			final int dstImageWidth,
			final int dstImageHeight,
			final int parallelism,
			final Object algo) {
			this.refImageName = refImageName;
			this.srcImageType = srcImageType;
			this.dstImageType = dstImageType;
			this.dstImageWidth = dstImageWidth;
			this.dstImageHeight = dstImageHeight;
			this.parallelism = parallelism;
			this.algo = algo;
		}
	}
	
	/**
	 * Mutable.
	 */
	private static final class MyBgState {
		String refImageName;
		BufferedImage refImage;
		TestImageTypeEnum srcImageType;
		BufferedImage srcImage;
		long resizeCount;
		public MyBgState() {
		}
	}
	
	/**
	 * Immutable, other than for dstImage state.
	 */
	private static final class MyBgOutput {
		final MyBgInput input;
		final int srcImageWidth;
		final int srcImageHeight;
		final long resizeTimeNs;
		final BufferedImage dstImage;
		final long resizeNum;
		public MyBgOutput(
			final MyBgInput input,
			final int srcImageWidth,
			final int srcImageHeight,
			final long resizeTimeNs,
			final BufferedImage dstImage,
			final long resizeNum) {
			this.input = input;
			this.srcImageWidth = srcImageWidth;
			this.srcImageHeight = srcImageHeight;
			this.resizeTimeNs = resizeTimeNs;
			this.dstImage = dstImage;
			this.resizeNum = resizeNum;
		}
	}
	
	/**
	 * Useful because BG scheduler typically swallows exceptions without a burp.
	 */
	private static class MyCatchyRunnable implements Runnable {
		private final Runnable runnable;
		public MyCatchyRunnable(Runnable runnable) {
			this.runnable = runnable;
		}
		@Override
		public void run() {
			try {
				this.runnable.run();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	//--------------------------------------------------------------------------
	// FIELDS
	//--------------------------------------------------------------------------
	
	private static final ParallelResizer COPY_RESIZER = Resizers.PARALLEL_NEAREST;
	
	/*
	 * 
	 */
	
	private final ScheduledExecutorService bgScheduler =
		Executors.newScheduledThreadPool(1);
	
	private final Map<Integer,Executor> prlExecByPrl =
		new TreeMap<>();
	
	private final List<String> algoIdByIndex = new ArrayList<>();
	/**
	 * Contains either a ScalingType or a InterfaceTestResizer.
	 */
	private final List<Object> algoByIndex = new ArrayList<>();
	
	private int refImageCount;
	
	/*
	 * 
	 */
	
	/**
	 * To avoid scheduling an ASAP resize
	 * if one is scheduled already.
	 */
	private boolean asapResizeScheduled = false;
	
	/**
	 * To avoid scheduling a delayed resize
	 * if one of same type is scheduled already,
	 * and executing one if one of a different type
	 * has been scheduled (which could cause multiple
	 * delays to be active at once).
	 * 
	 * -1 if no delayed resize scheduled.
	 */
	private long scheduledResizeDelayMs = -1L;
	
	private final MyBgState bgState = new MyBgState();
	
	private BufferedImage currentDstImage;
	
	/*
	 * GUI
	 */
	
	private JFrame controlFrame;
	private JPanel controlPanel;
	
	private JFrame dstImageFrame;
	private JPanel dstImagePanel;
	
	/*
	 * 
	 */
	
	private JLabel infoResizeTimeLabel;
	private JLabel infoSpansLabel;
	private JLabel infoSpansRatiosLabel;
	private JLabel infoResizeCountLabel;
	private JLabel infoResizedImageDrawTimeLabel;
	
	/*
	 * 
	 */
	
	private JComboBox<MyResizeTrigger> resizeTriggerComboBox;
	
	/*
	 * 
	 */
	
	private JComboBox<String> refImageNameComboBox;
	
	private JComboBox<TestImageTypeEnum> srcImageTypeComboBox;
	private JComboBox<TestImageTypeEnum> dstImageTypeComboBox;
	
	private JComboBox<Integer> prlComboBox;
	
	private JComboBox<String> algoIdComboBox;
	
	//--------------------------------------------------------------------------
	// PUBLIC METHODS
	//--------------------------------------------------------------------------
	
	public ThbTestGui() {
		for (Resizers resizer : Resizers.values()) {
			final String id = "Resizers." + resizer;
			this.algoIdByIndex.add(id);
			this.algoByIndex.add(resizer);
		}
	}
	
	public void init() {
		this.createControlFrame();
		this.createControlPanel();
		
		this.createDstImageFrame();
		this.createDstImagePanel();
		
		this.dstImagePanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				final MyResizeTrigger resizeTrigger =
					(MyResizeTrigger) resizeTriggerComboBox.getSelectedItem();
				if (resizeTrigger == MyResizeTrigger.CHOICE_OR_COMPONENT_RESIZED) {
					ensureResizeScheduled_edt(MyScheduleType.ASAP);
				}
			}
		});
	}
	
	public void show() {
		{
			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			final GraphicsDevice gd = ge.getDefaultScreenDevice();
			final GraphicsConfiguration gc = gd.getDefaultConfiguration();
			
			final Rectangle screenBounds = gc.getBounds();
			
			final int cx = screenBounds.x + screenBounds.width / 10;
			final int cy = screenBounds.y + screenBounds.height / 10;
			final int ix = cx + INITIAL_CONTROL_FRAME_WIDTH + 10;
			final int iy = cy;
			
			this.controlFrame.setBounds(
				cx,
				cy,
				this.controlFrame.getWidth(),
				this.controlFrame.getHeight());
			this.dstImageFrame.setBounds(
				ix,
				iy,
				this.dstImageFrame.getWidth(),
				this.dstImageFrame.getHeight());
		}
		this.controlFrame.setVisible(true);
		this.dstImageFrame.setVisible(true);
		
		this.applyChoices_edt();
	}
	
	public void dispose() {
		this.controlFrame.dispose();
		this.dstImageFrame.dispose();
		
		this.bgScheduler.shutdownNow();
		
		for (Executor prlExec : this.prlExecByPrl.values()) {
			ThbTestUtils.shutdownNow(prlExec);
		}
	}
	
	//--------------------------------------------------------------------------
	// PRIVATE METHODS
	//--------------------------------------------------------------------------
	
	/*
	 * Resize scheduling.
	 */
	
	/**
	 * Must be called in EDT.
	 * 
	 * @return Delayed schedule type to use, or null if none.
	 */
	private MyScheduleType getDelayedScheduleType_edt() {
		final MyResizeTrigger resizeTrigger =
			(MyResizeTrigger) resizeTriggerComboBox.getSelectedItem();
		
		final MyScheduleType ret;
		if (resizeTrigger == MyResizeTrigger.CHOICE_OR_DELAY_1_S) {
			ret = MyScheduleType.DELAY_1_S;
		} else if (resizeTrigger == MyResizeTrigger.CHOICE_OR_DELAY_100_MS) {
			ret = MyScheduleType.DELAY_100_MS;
		} else {
			ret = null;
		}
		return ret;
	}
	
	/**
	 * Must be called in EDT.
	 */
	private boolean isScheduled_edt(MyScheduleType scheduleType) {
		if (scheduleType == MyScheduleType.ASAP) {
			return this.asapResizeScheduled;
		} else {
			return (this.scheduledResizeDelayMs == scheduleType.delayMs);
		}
	}
	
	/**
	 * Must be called in EDT.
	 */
	private void setScheduled_edt(MyScheduleType scheduleType) {
		if (scheduleType == MyScheduleType.ASAP) {
			this.asapResizeScheduled = true;
		} else {
			this.scheduledResizeDelayMs = scheduleType.delayMs;
		}
	}
	
	/**
	 * Must be called in EDT.
	 */
	private void setNotScheduled_edt(MyScheduleType scheduleType) {
		if (scheduleType == MyScheduleType.ASAP) {
			this.asapResizeScheduled = false;
		} else {
			if (this.scheduledResizeDelayMs == scheduleType.delayMs) {
				this.scheduledResizeDelayMs = -1L;
			}
		}
	}
	
	/**
	 * Must be called in EDT.
	 */
	private void ensureResizeScheduled_edt(
		final MyScheduleType scheduleType) {
		
		if (this.isScheduled_edt(scheduleType)) {
			// Already one scheduled.
			return;
		}
		
		this.setScheduled_edt(scheduleType);
		
		if (MUST_RESIZE_IN_BG_THREAD) {
			final Runnable runnableForBgWork = new Runnable() {
				@Override
				public void run() {
					doResizeWork(scheduleType);
				}
			};
			if (scheduleType == MyScheduleType.ASAP) {
				this.bgScheduler.execute(
					new MyCatchyRunnable(runnableForBgWork));
			} else {
				this.bgScheduler.schedule(
					new MyCatchyRunnable(runnableForBgWork),
					scheduleType.delayMs,
					TimeUnit.MILLISECONDS);
			}
		} else {
			final Runnable runnableForEdtWork = new Runnable() {
				@Override
				public void run() {
					if (isScheduled_edt(scheduleType)) {
						doResizeWork(scheduleType);
					}
				}
			};
			if (scheduleType == MyScheduleType.ASAP) {
				// Async in case could avoid issues.
				SwingUtilities.invokeLater(runnableForEdtWork);
			} else {
				final Runnable runnableForBgDelay = new Runnable() {
					@Override
					public void run() {
						SwingUtilities.invokeLater(runnableForEdtWork);
					}
				};
				this.bgScheduler.schedule(
					new MyCatchyRunnable(runnableForBgDelay),
					scheduleType.delayMs,
					TimeUnit.MILLISECONDS);
			}
		}
	}
	
	/*
	 * Choices read, resize, GUI update.
	 */
	
	/**
	 * Must be called in EDT.
	 */
	private void applyChoices_edt() {
		
		{
			final int algoIndex = this.algoIdComboBox.getSelectedIndex();
			final Object algo = this.algoByIndex.get(algoIndex);
			
			final Resizers biScalingType;
			if (algo instanceof Resizers) {
				biScalingType = (Resizers) algo;
			} else {
				biScalingType = null;
			}
			
			final boolean isPrlCapable =
				(biScalingType.ordinal() >= Resizers.PARALLEL_NEAREST.ordinal());
			
			this.prlComboBox.setEnabled(isPrlCapable);
		}
		
		{
			final MyScheduleType delayedScheduleType =
				this.getDelayedScheduleType_edt();
			if (delayedScheduleType == null) {
				/*
				 * Cancelling any pending delayed schedule
				 * (i.e. making it do nothing when it executes).
				 */
				this.setNotScheduled_edt(MyScheduleType.DELAY_100_MS);
				this.setNotScheduled_edt(MyScheduleType.DELAY_1_S);
			}
		}
		
		this.ensureResizeScheduled_edt(MyScheduleType.ASAP);
	}
	
	private void doResizeWork(final MyScheduleType scheduleType) {
		
		final MyBgState state = this.bgState;
		
		state.resizeCount++;
		
		final MyBgInput input;
		if (MUST_RESIZE_IN_BG_THREAD) {
			final MyBgInput[] inputW = new MyBgInput[1];
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					
					@Override
					public void run() {
						inputW[0] = newInput_edt(scheduleType);
					}
				});
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			input = inputW[0];
		} else {
			input = newInput_edt(scheduleType);
		}
		if (input == null) {
			return;
		}
		
		boolean refImageChanged = false;
		if (!input.refImageName.equals(state.refImageName)) {
			final File newRefImageFile =
				new File(IMAGE_DIR, input.refImageName);
			final BufferedImage newRefImage =
				ThbTestUtils.loadImage(
					newRefImageFile);
			
			state.refImageName = input.refImageName;
			if (state.refImage != null) {
				state.refImage.flush();
				state.refImage = null;
			}
			state.refImage = newRefImage;
			
			refImageChanged = true;
		}
		if (refImageChanged
			|| (input.srcImageType != state.srcImageType)) {
			final BufferedImage newSrcImage =
				new BufferedImage(
					state.refImage.getWidth(),
					state.refImage.getHeight(),
					input.srcImageType.imageType());
			
			COPY_RESIZER.resize(
				state.refImage,
				newSrcImage,
				prlExecByPrl.get(MAX_PARALLELISM));
			
			state.srcImageType = input.srcImageType;
			if (state.srcImage != null) {
				state.srcImage.flush();
				state.srcImage = null;
			}
			state.srcImage = newSrcImage;
		}
		
		final BufferedImage dstImageForResize =
			new BufferedImage(
				input.dstImageWidth,
				input.dstImageHeight,
				input.dstImageType.imageType());
		
		final Executor parallelExecutor;
		{
			final int prl = input.parallelism;
			if (prl <= 1) {
				parallelExecutor = null;
			} else {
				Executor prlExec = this.prlExecByPrl.get(prl);
				if (prlExec == null) {
					prlExec = ThbTestUtils.newPrlExec(prl);
					this.prlExecByPrl.put(prl, prlExec);
				}
				parallelExecutor = prlExec;
			}
		}
		
		final long t1Ns = System.nanoTime();
		resizeWithAlgo(
			input.algo,
			state,
			dstImageForResize,
			parallelExecutor);
		final long t2Ns = System.nanoTime();
		final long resizeTimeNs = t2Ns - t1Ns;
		
		final BufferedImage dstImageToShow = dstImageForResize;
		
		final MyBgOutput output =
			new MyBgOutput(
				input,
				state.srcImage.getWidth(),
				state.srcImage.getHeight(),
				resizeTimeNs,
				dstImageToShow,
				state.resizeCount);
		
		this.updateGuiNowOrLater(output);
	}
	
	private static void resizeWithAlgo(
		Object algo,
		MyBgState state,
		BufferedImage dstImageForResize,
		Executor parallelExecutor) {
		if (algo instanceof Resizers) {
			final ParallelResizer resizer =
				(ParallelResizer) algo;
			resizer.resize(
				state.srcImage,
				dstImageForResize,
				parallelExecutor);
		} else {
			throw new IllegalArgumentException("" + algo);
		}
	}
	
	private void updateGuiNowOrLater(MyBgOutput output) {
		if (MUST_RESIZE_IN_BG_THREAD) {
			final Runnable runnableForEdt =
				new Runnable() {
				
				@Override
				public void run() {
					updateGuiNow_edt(output);
				}
			};
			SwingUtilities.invokeLater(runnableForEdt);
		} else {
			this.updateGuiNow_edt(output);
		}
	}
	
	/**
	 * Must be called in EDT.
	 */
	private void updateGuiNow_edt(MyBgOutput output) {
		
		/*
		 * Updating the image to be shown.
		 */
		
		if (this.currentDstImage != null) {
			this.currentDstImage.flush();
		}
		this.currentDstImage = output.dstImage;
		
		// To cause call to drawDstImageOn().
		this.dstImagePanel.repaint();
		
		/*
		 * Updating infos.
		 */
		
		final long dtNs = output.resizeTimeNs;
		final double dtS = dtNs / 1000 / 1e6;
		this.infoResizeTimeLabel.setText(dtS + " s");
		
		final int dw = output.input.dstImageWidth;
		final int dh = output.input.dstImageHeight;
		final int sw = output.srcImageWidth;
		final int sh = output.srcImageHeight;
		this.infoSpansLabel.setText("("
			+ sw
			+ ", "
			+ sh
			+ ")->("
			+ dw
			+ ", "
			+ dh
			+ ")");
		
		this.infoSpansRatiosLabel.setText("("
			+ (dw / (float) sw)
			+ ", "
			+ (dh / (float) sh)
			+ ")");
		
		this.infoResizeCountLabel.setText("" + output.resizeNum);
	}
	
	/**
	 * Must be called in EDT.
	 */
	private void drawDstImageOn_edt(Graphics g) {
		final BufferedImage dstImage = this.currentDstImage;
		if (dstImage == null) {
			return;
		}
		
		final long t1Ns = System.nanoTime();
		g.drawImage(dstImage, 0, 0, null);
		final long t2Ns = System.nanoTime();
		
		final double dtS = (t2Ns - t1Ns) / 1000 / 1e6;
		this.infoResizedImageDrawTimeLabel.setText(dtS + " s");
		
		final MyScheduleType delayedScheduleType =
			this.getDelayedScheduleType_edt();
		if (delayedScheduleType == null) {
			// No re-trigger after draw.
		} else {
			this.ensureResizeScheduled_edt(delayedScheduleType);
		}
	}
	
	/*
	 * 
	 */
	
	/**
	 * Must be called in EDT.
	 * 
	 * @return Input, or null if schedule cancelled
	 *         or could not compute a valid one.
	 */
	private MyBgInput newInput_edt(final MyScheduleType scheduleType) {
		
		if (!this.isScheduled_edt(scheduleType)) {
			// Has been cancelled.
			return null;
		}
		
		/**
		 * Schedule no longer counting (to prevent other schedules)
		 * once corresponding input read started.
		 */
		this.setNotScheduled_edt(scheduleType);
		
		final String refImageName =
			(String) this.refImageNameComboBox.getSelectedItem();
		
		final TestImageTypeEnum srcImageType =
			(TestImageTypeEnum) this.srcImageTypeComboBox.getSelectedItem(); 
		final TestImageTypeEnum dstImageType =
			(TestImageTypeEnum) this.dstImageTypeComboBox.getSelectedItem(); 
		
		final int dstImageWidth = Math.max(1, this.dstImagePanel.getWidth());
		final int dstImageHeight = Math.max(1, this.dstImagePanel.getHeight());
		
		final int parallelism = (Integer) this.prlComboBox.getSelectedItem();
		
		final int algoIndex = this.algoIdComboBox.getSelectedIndex();
		final Object algo = this.algoByIndex.get(algoIndex);
		
		return new MyBgInput(
			refImageName,
			srcImageType,
			dstImageType,
			dstImageWidth,
			dstImageHeight,
			parallelism,
			algo);
	}
	
	/*
	 * GUI (all in EDT)
	 */
	
	private void createControlFrame() {
		final JFrame frame = new JFrame(ThbTestGui.class.getSimpleName() + "-Control");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.controlFrame = frame;
		
		frame.setPreferredSize(new Dimension(
			INITIAL_CONTROL_FRAME_WIDTH,
			INITIAL_CONTROL_FRAME_HEIGHT));
		frame.pack();
	}
	
	private void createDstImageFrame() {
		final JFrame frame = new JFrame(ThbTestGui.class.getSimpleName() + "-Image");
		// To avoid accidental closing on resize.
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.dstImageFrame = frame;
		
		frame.setPreferredSize(new Dimension(
			INITIAL_DST_IMAGE_FRAME_WIDTH,
			INITIAL_DST_IMAGE_FRAME_HEIGHT));
		frame.pack();
	}
	
	private void addImageWheelChoiceListener(Component cpt) {
		final MouseWheelListener listener = new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				final int oldIndex = refImageNameComboBox.getSelectedIndex();
				final int rot = event.getWheelRotation();
				if (rot < 0) {
					if (oldIndex > 0) {
						refImageNameComboBox.setSelectedIndex(oldIndex - 1);
					}
				}
				if (rot > 0) {
					if (oldIndex < refImageCount - 1) {
						refImageNameComboBox.setSelectedIndex(oldIndex + 1);
					}
				}
			}
		};
		cpt.addMouseWheelListener(listener);
	}
	
	private void createControlPanel() {
		this.controlPanel = newAddedVerPanel(
			(JComponent) this.controlFrame.getContentPane());
		
		this.addImageWheelChoiceListener(this.controlPanel);
		
		final ActionListener choiceListener =
			new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				applyChoices_edt();
			}
		};
		
		final JPanel gridPanel = new JPanel(new GridBagLayout());
		this.controlPanel.add(gridPanel);
		int gx = 0;
		int gy = 0;
		/*
		 * Infos top-left, to be visible
		 * even when window is small.
		 */
		{
			this.infoResizeTimeLabel = newInfoLabel(
				"Resize Time: ",
				gridPanel,
				gx,
				gy);
		}
		gy++;
		{
			this.infoSpansLabel = newInfoLabel(
				"Src->Dst Spans: ",
				gridPanel,
				gx,
				gy);
		}
		gy++;
		{
			this.infoSpansRatiosLabel = newInfoLabel(
				"Dst/Src Ratio: ",
				gridPanel,
				gx,
				gy);
		}
		gy++;
		{
			this.infoResizeCountLabel = newInfoLabel(
				"Resize Count: ",
				gridPanel,
				gx,
				gy);
		}
		gy++;
		{
			this.infoResizedImageDrawTimeLabel = newInfoLabel(
				"Draw Time: ",
				gridPanel,
				gx,
				gy);
		}
		gy++;
		// Last row, not to hide infos when it pops up.
		{
			this.resizeTriggerComboBox = newComboBoxEntry(
				"Resize Trigger",
				MyResizeTrigger.values(),
				DEFAULT_UPDATE_TRIGGER.ordinal(),
				gridPanel,
				gx,
				gy,
				choiceListener);
		}
		/*
		 * Resize configuration.
		 */
		gy++;
		{
			final File dir = IMAGE_DIR;
			final String[] fileNameArr = dir.list();
			if ((fileNameArr == null)
				|| (fileNameArr.length == 0)) {
				throw new IllegalArgumentException(
					"no image in "
						+ dir.getAbsolutePath());
			}
			this.refImageCount = fileNameArr.length;
			this.refImageNameComboBox = newComboBoxEntry(
				"Ref Image:",
				fileNameArr,
				0,
				gridPanel,
				gx,
				gy,
				choiceListener);
		}
		gy++;
		{
			this.srcImageTypeComboBox = newComboBoxEntry(
				"Src Image Type:",
				TestImageTypeEnum.values(),
				DEFAULT_SRC_IMAGE_TYPE.ordinal(),
				gridPanel,
				gx,
				gy,
				choiceListener);
		}
		gy++;
		{
			this.dstImageTypeComboBox = newComboBoxEntry(
				"Dst Image Type:",
				TestImageTypeEnum.values(),
				DEFAULT_DST_IMAGE_TYPE.ordinal(),
				gridPanel,
				gx,
				gy,
				choiceListener);
		}
		gy++;
		{
			final List<Integer> prlList = new ArrayList<>();
			int prl = 1;
			for (; prl <= MAX_PARALLELISM; prl *= 2) {
				prlList.add(prl);
			}
			if (prlList.get(prlList.size()-1) != MAX_PARALLELISM) {
				prlList.add(MAX_PARALLELISM);
			}
			final Integer[] prlArr =
				prlList.toArray(new Integer[prlList.size()]);
			final int defaultSelectedIndex =
				(DEFAULT_PARALLEL_ELSE_SEQUENTIAL
					? prlArr.length - 1 : 0);
			this.prlComboBox = newComboBoxEntry(
				"Parallelism:",
				prlArr,
				defaultSelectedIndex,
				gridPanel,
				gx,
				gy,
				choiceListener);
		}
		gy++;
		{
			this.algoIdComboBox = newComboBoxEntry(
				"Resizing Algo:",
				this.algoIdByIndex.toArray(new String[this.algoIdByIndex.size()]),
				DEFAULT_SCALING_ALGO_INDEX,
				gridPanel,
				gx,
				gy,
				choiceListener);
		}
	}
	
	private void createDstImagePanel() {
		final JPanel gridPanel = new JPanel(new GridBagLayout());
		this.dstImageFrame.add(gridPanel);
		
		this.addImageWheelChoiceListener(gridPanel);
		
		int gx = 0;
		int gy = 0;
		{
			final JPanel paddingPanel = new JPanel();
			
			final GridBagConstraints c = newGbc(gx, gy);
			c.weightx = 1e-10;
			c.weighty = 1e-10;
			c.insets = new Insets(0, 60, 0, 60);
			gridPanel.add(paddingPanel, c);
		}
		gx++;
		{
			final JPanel dstImagePanel = new JPanel() {
				private static final long serialVersionUID = 1L;
				@Override
				protected void paintComponent(Graphics g) {
					// Need to call super else alpha images
					// blend on undefined background.
					super.paintComponent(g);
					drawDstImageOn_edt(g);
				}
			};
			
			final GridBagConstraints c = newGbc(gx, gy);
			c.weightx = 1.0;
			c.weighty = 1.0;
			gridPanel.add(dstImagePanel, c);
			this.dstImagePanel = dstImagePanel;
		}
	}
	
	/*
	 * 
	 */
	
	private static JPanel newAddedVerPanel(JComponent parent) {
		return newAddedBoxPanel(BoxLayout.Y_AXIS, parent);
	}
	
	private static JPanel newAddedBoxPanel(int axis, JComponent parent) {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(
			panel,
			axis));
		parent.add(panel);
		return panel;
	}
	
	/*
	 * 
	 */
	
	private static <T> JComboBox<T> newComboBoxEntry(
		String text,
		T[] arr,
		int defaultSelectedIndex,
		JComponent parent,
		int gx,
		int gy,
		ActionListener listener) {
		
		final JLabel label = new JLabel(text);
		parent.add(label, newGbc(gx, gy));
		//
		final JComboBox<T> comboBox = new JComboBox<>(arr);
		// All choices visible on pop.
		comboBox.setMaximumRowCount(arr.length);
		comboBox.setSelectedIndex(defaultSelectedIndex);
		if (listener != null) {
			comboBox.addActionListener(listener);
		}
		parent.add(comboBox, newGbc(gx + 1, gy));
		//
		return comboBox;
	}
	
	private static JLabel newInfoLabel(
		String text,
		JComponent parent,
		int gx,
		int gy) {
		
		final JLabel label = new JLabel(text);
		parent.add(label, newGbc(gx, gy));
		//
		final JLabel infoLabel = new JLabel();
		parent.add(infoLabel, newGbc(gx + 1, gy));
		//
		return infoLabel;
	}
	
	/*
	 * 
	 */
	
	private static GridBagConstraints newGbc(int gx, int gy) {
		return new GridBagConstraints(
			gx, // gridx
			gy, // gridy
			1, // gridwidth
			1, // gridheight
			0.0, // weightx
			0.0, // weighty
			GridBagConstraints.BASELINE, // anchor
			GridBagConstraints.BOTH, // fill
			new Insets(0, 0, 0, 0), // insets
			0, // ipadx
			0); // ipady
	}
}
