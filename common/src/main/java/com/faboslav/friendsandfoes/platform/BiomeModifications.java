package com.faboslav.friendsandfoes.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.world.biome.Biome;

public final class BiomeModifications
{
	@ExpectPlatform
	public static void addMobSpawn(
		TagKey<Biome> tag,
		EntityType<?> entityType,
		SpawnGroup spawnGroup,
		int weight,
		int minGroupSize,
		int maxGroupSize
	) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void addButtercupFeature() {
		throw new AssertionError();
	}

	private BiomeModifications() {
	}
}
