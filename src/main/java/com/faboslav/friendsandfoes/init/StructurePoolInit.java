package com.faboslav.friendsandfoes.init;

import com.faboslav.friendsandfoes.FriendsAndFoes;
import com.faboslav.friendsandfoes.config.Settings;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.Identifier;

public class StructurePoolInit
{
	public static void init() {
		if (FriendsAndFoes.CONFIG.generateBeekeeperAreaStructure) {
			StructurePools.register(
				new StructurePool(
					Settings.makeID("village/common/bee"),
					new Identifier("empty"),
					ImmutableList.of(
						Pair.of(StructurePoolElement.ofSingle(
							Settings.makeStringID("village/common/animals/bee")
						), 1)
					),
					StructurePool.Projection.RIGID
				)
			);
		}
	}

}