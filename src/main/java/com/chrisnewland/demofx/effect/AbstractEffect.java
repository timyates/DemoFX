package com.chrisnewland.demofx.effect;

import java.util.Random;

import javafx.scene.canvas.GraphicsContext;

public abstract class AbstractEffect implements IEffect
{
	protected final GraphicsContext gc;

	protected int itemCount;

	protected final int width;
	protected final int height;

	protected final int halfWidth;
	protected final int halfHeight;

	protected long lastSecond;
	protected int frameCount = 0;
	protected int showFPS = 0;
	protected String itemName = null;

	private long lastRenderNanos = 0;
	private long averageRenderNanos = 0;
	private long count = 0;

	private long now;
	private StringBuilder builder = new StringBuilder();

	protected final double getRandomDouble(double min, double max)
	{
		Random random = new Random();
		return min + (max - min) * random.nextDouble();
	}

	protected final int getRandomIntInclusive(int min, int max)
	{
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}

	public AbstractEffect(GraphicsContext gc, int count, int width, int height)
	{
		this.gc = gc;

		this.itemCount = count;

		this.width = width;
		this.height = height;

		this.halfWidth = width / 2;
		this.halfHeight = height / 2;

		initialise();
	}

	protected abstract void initialise();

	protected void updateFPS(long renderNanos)
	{
		frameCount++;
		lastRenderNanos = renderNanos;

		now = System.currentTimeMillis();

		if (now - lastSecond > 1000)
		{
			showFPS = frameCount;
			frameCount = 0;
			lastSecond = now;

			averageRenderNanos += (renderNanos - averageRenderNanos) / ++count;
		}
	}

	@Override
	public String getStatistics()
	{
		builder.setLength(0);

		builder.append(showFPS).append(" fps / ");

		if (itemCount > -1)
		{
			builder.append(itemCount).append(' ').append(itemName).append(" / ");
		}

		builder.append("render ");

		formatNanos(builder, lastRenderNanos);

		builder.append(" / avg render ");

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