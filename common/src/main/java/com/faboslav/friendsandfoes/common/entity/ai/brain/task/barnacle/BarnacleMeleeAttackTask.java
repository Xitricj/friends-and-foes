package com.faboslav.friendsandfoes.common.entity.ai.brain.task.barnacle;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

public class BarnacleMeleeAttackTask extends Task<MobEntity>
{
	private final int interval;

	public BarnacleMeleeAttackTask(int interval) {
		super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleState.VALUE_ABSENT));
		this.interval = interval;
	}

	protected boolean shouldRun(ServerWorld serverWorld, MobEntity mobEntity) {
		LivingEntity livingEntity = this.getAttackTarget(mobEntity);
		return !this.isHoldingUsableRangedWeapon(mobEntity) && LookTargetUtil.isVisibleInMemory(mobEntity, livingEntity) && mobEntity.isInAttackRange(livingEntity);
	}

	private boolean isHoldingUsableRangedWeapon(MobEntity entity) {
		return entity.isHolding((stack) -> {
			Item item = stack.getItem();
			return item instanceof RangedWeaponItem && entity.canUseRangedWeapon((RangedWeaponItem)item);
		});
	}

	protected void run(ServerWorld serverWorld, MobEntity mobEntity, long l) {
		LivingEntity livingEntity = this.getAttackTarget(mobEntity);
		double squaredDistance = mobEntity.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
		double d0 = this.getSquaredMaxAttackDistance(livingEntity);

		if (squaredDistance <= d0) {
			LookTargetUtil.lookAt(mobEntity, livingEntity);
			mobEntity.swingHand(Hand.MAIN_HAND);
			mobEntity.tryAttack(livingEntity);
			mobEntity.getBrain().remember(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long) this.interval);
		}
	}

	protected double getSquaredMaxAttackDistance(LivingEntity entity) {
		float f = entity.getWidth() - 0.1F;
		return (f * 2.0F * f * 2.0F + entity.getWidth());
	}

	private LivingEntity getAttackTarget(MobEntity entity) {
		return entity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).get();
	}
}
