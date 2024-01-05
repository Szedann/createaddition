package com.mrh0.createaddition.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class CAContinuousSound extends AbstractTickableSoundInstance {
	private float sharedPitch;
	private CASoundScape scape;
	private float relativeVolume;

	protected CAContinuousSound(SoundEvent event, CASoundScape scape, float sharedPitch, float relativeVolume) {
		super(event, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
		this.scape = scape;
		this.sharedPitch = sharedPitch;
		this.relativeVolume = relativeVolume;
		this.looping = true;
		this.delay = 0;
		this.relative = false;
	}

	public void remove() {
		stop();
	}

	@Override
	public float getVolume() {
		return scape.getVolume() * relativeVolume;
	}

	@Override
	public float getPitch() {
		return sharedPitch;
	}

	@Override
	public double getX() {
		return scape.getMeanPos().x;
	}

	@Override
	public double getY() {
		return scape.getMeanPos().y;
	}

	@Override
	public double getZ() {
		return scape.getMeanPos().z;
	}

	@Override
	public void tick() {}
}