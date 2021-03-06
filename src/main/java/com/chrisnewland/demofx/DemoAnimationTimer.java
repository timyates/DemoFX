/*
 * Copyright (c) 2015-2016 Chris Newland.
 * Licensed under https://github.com/chriswhocodes/demofx/blob/master/LICENSE-BSD
 */
package com.chrisnewland.demofx;

import java.util.List;

import com.chrisnewland.demofx.effect.IEffect;
import com.chrisnewland.demofx.measurement.Measurements;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;

public class DemoAnimationTimer extends AnimationTimer
{
	private long startTime = 0;

	private GraphicsContext onScreenGC;

	private final Measurements measurements = new Measurements();

	private Label statsLabel;

	private List<IEffect> effects;

	private long lastSecond;
	private int frameCount = 0;
	private int framesPerSecond = 0;

	private long lastNanos = 0;
	private long lastRenderNanos = 0;
	private long averageRenderNanos = 0;

	private long count = 0;
	private StringBuilder builder = new StringBuilder();

	private static long scriptStartTimeMillis;

	private DemoFX parent;
	private DemoConfig config;

	public DemoAnimationTimer(DemoFX parent, DemoConfig config, Label statsLabel, List<IEffect> effects)
	{
		super();

		this.parent = parent;
		this.config = config;
		this.onScreenGC = config.getOnScreenCanvasGC();
		this.statsLabel = statsLabel;
		this.effects = effects;
	}

	@Override
	public void start()
	{		
		super.start();
		startTime = System.currentTimeMillis();
		scriptStartTimeMillis = startTime;

		System.out.println("DemoAnimationTimer start " + startTime);
	}

	@Override
	public void handle(long nanos)
	{
		IEffect currentEffect = null;

		final int effectCount = effects.size();

		final long now = System.currentTimeMillis();

		final long elapsed = now - startTime;

		int effectsUsed = 0;

		for (int i = 0; i < effectCount; i++)
		{
			currentEffect = effects.get(i);

			if (currentEffect.isVisible(elapsed))
			{
				plotEffect(currentEffect);

				effectsUsed++;				
			}
		}
		
		updateStatistics(now, nanos);

		if (effectsUsed == 0)
		{
			stop();
			parent.timerCompleted(measurements);
		}
	}

	private final void plotEffect(IEffect effect)
	{
		onScreenGC.save();

		effect.renderForeground();

		onScreenGC.restore();
	}

	public void updateStatistics(long now, long nanoStamp)
	{
		frameCount++;

		lastRenderNanos = nanoStamp - lastNanos;
		lastNanos = nanoStamp;

		if (now - lastSecond > 1000L)
		{
			framesPerSecond = frameCount;
			frameCount = 0;
			lastSecond = now;

			if (averageRenderNanos == 0)
			{
				averageRenderNanos = lastRenderNanos;
			}
			else
			{
				averageRenderNanos += (lastRenderNanos - averageRenderNanos) / ++count;
			}

			if (!config.isFullScreen())
			{
				statsLabel.setText(getStatsString(now));
			}

			measurements.measure(now - startTime, framesPerSecond);
		}
	}

	public String getStatsString(long now)
	{
		builder.setLength(0);

		builder.append(config.getWidth()).append("x").append(config.getHeight()).append(" | ");

		builder.append(framesPerSecond).append(" fps | ");

		if (config.isUseScriptedDemoConfig())
		{
			long elapsedSeconds = (now - scriptStartTimeMillis) / 1000L;
			builder.append("Demo mode: ").append(elapsedSeconds).append("s | ");
		}
		else
		{

			if (config.getCount() > -1)
			{
				builder.append(config.getCount()).append(" | ");
			}

			builder.append(config.getEffect()).append(" | ");
		}

		builder.append("render ");

		formatNanos(builder, lastRenderNanos);

		builder.append(" | avg render ");

		formatNanos(builder, averageRenderNanos);

		return builder.toString();
	}

	private void formatNanos(StringBuilder builder, long nanos)
	{
		if (nanos > 5_000_000)
		{
			builder.append(nanos / 1_000_000).append("ms");
		}
		else if (nanos > 5_000)
		{
			builder.append(nanos / 1_000).append("us");
		}
		else
		{
			builder.append(nanos).append("ns");
		}
	}
}
