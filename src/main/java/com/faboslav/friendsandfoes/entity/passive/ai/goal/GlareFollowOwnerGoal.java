package com.faboslav.friendsandfoes.entity.passive.ai.goal;


import com.faboslav.friendsandfoes.entity.passive.GlareEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.EnumSet;

public class GlareFollowOwnerGoal extends Goal
{
	private final GlareEntity glare;
	private LivingEntity owner;
	private final WorldView world;
	private final double speed;
	private final EntityNavigation navigation;
	private int updateCountdownTicks;
	private final float maxDistance;
	private final float minDistance;
	private float oldWaterPathfindingPenalty;
	private final boolean leavesAllowed;

	public GlareFollowOwnerGoal(
		GlareEntity glare,
		double speed,
		float minDistance,
		float maxDistance,
		boolean leavesAllowed
	) {
		this.glare = glare;
		this.world = glare.world;
		this.speed = speed;
		this.navigation = glare.getNavigation();
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.leavesAllowed = leavesAllowed;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	public boolean canStart() {
		if (this.glare.isLeashed()) {
			return false;
		}

		LivingEntity livingEntity = this.glare.getOwner();

		if (livingEntity == null) {
			return false;
		} else if (livingEntity.isSpectator()) {
			return false;
		} else if (this.glare.squaredDistanceTo(livingEntity) < (double) (this.minDistance * this.minDistance)) {
			return false;
		} else {
			this.owner = livingEntity;
			return true;
		}
	}

	public boolean shouldContinue() {
		if (this.navigation.isIdle()) {
			return false;
		} else {
			return !(this.glare.squaredDistanceTo(this.owner) <= (double) (this.maxDistance * this.maxDistance));
		}
	}

	public void start() {
		this.updateCountdownTicks = 0;
		this.oldWaterPathfindingPenalty = this.glare.getPathfindingPenalty(PathNodeType.WATER);
		this.glare.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
	}

	public void stop() {
		this.owner = null;
		this.navigation.stop();
		this.glare.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
	}

	public void tick() {
		this.glare.getLookControl().lookAt(this.owner, 10.0F, (float) this.glare.getMaxLookPitchChange());
		if (--this.updateCountdownTicks <= 0) {
			this.updateCountdownTicks = this.getTickCount(10);
			if (!this.glare.isLeashed() && !this.glare.hasVehicle()) {
				if (this.glare.squaredDistanceTo(this.owner) >= 144.0D) {
					this.tryTeleport();
				} else {
					this.navigation.startMovingTo(this.owner, this.speed);
				}

			}
		}
	}

	private void tryTeleport() {
		BlockPos blockPos = this.owner.getBlockPos();

		for (int i = 0; i < 10; ++i) {
			int j = this.getRandomInt(-3, 3);
			int k = this.getRandomInt(-1, 1);
			int l = this.getRandomInt(-3, 3);
			boolean bl = this.tryTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l);
			if (bl) {
				return;
			}
		}

	}

	private boolean tryTeleportTo(int x, int y, int z) {
		if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
			return false;
		} else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
			return false;
		} else {
			this.glare.refreshPositionAndAngles((double) x + 0.5D, y, (double) z + 0.5D, this.glare.getYaw(), this.glare.getPitch());
			this.navigation.stop();
			return true;
		}
	}

	private boolean canTeleportTo(BlockPos pos) {
		PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(this.world, pos.mutableCopy());
		if (pathNodeType != PathNodeType.WALKABLE) {
			return false;
		} else {
			BlockState blockState = this.world.getBlockState(pos.down());
			if (!this.leavesAllowed && blockState.getBlock() instanceof LeavesBlock) {
				return false;
			} else {
				BlockPos blockPos = pos.subtract(this.glare.getBlockPos());
				return this.world.isSpaceEmpty(this.glare, this.glare.getBoundingBox().offset(blockPos));
			}
		}
	}

	private int getRandomInt(int min, int max) {
		return this.glare.getRandom().nextInt(max - min + 1) + min;
	}
}