package com.faboslav.friendsandfoes.mixin;

import com.faboslav.friendsandfoes.FriendsAndFoes;
import com.faboslav.friendsandfoes.init.FriendsAndFoesEntityTypes;
import com.faboslav.friendsandfoes.util.CustomRaidMember;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.village.raid.Raid;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(Raid.Member.class)
@SuppressWarnings({"ShadowTarget", "InvokerTarget", "UnresolvedMixinReference"})
public final class RaidMemberMixin
{
	@Invoker("<init>")
	private static Raid.Member newRaidMember(
		String internalName,
		int internalId,
		EntityType<? extends RaiderEntity> entityType,
		int[] countInWave
	) {
		throw new AssertionError();
	}

	@Shadow
	private static @Final
	@Mutable
	Raid.Member[] field_16632;

	@Inject(
		method = "<clinit>",
		at = @At(
			value = "FIELD",
			opcode = Opcodes.PUTSTATIC,
			target = "Lnet/minecraft/village/raid/Raid$Member;field_16632:[net/minecraft/village/raid/Raid$Member;",
			shift = At.Shift.AFTER
		)
	)
	private static void addCustomRaidMembers(CallbackInfo ci) {
		var raidMembers = new ArrayList<>(Arrays.asList(field_16632));
		var lastRaidMember = raidMembers.get(raidMembers.size() - 1);

		var iceologerRaidMember = newRaidMember(
			CustomRaidMember.ICEOLOGER_INTERNAL_NAME,
			lastRaidMember.ordinal() + 1,
			FriendsAndFoesEntityTypes.ICEOLOGER.get(),
			CustomRaidMember.ICEOLOGER_COUNT_IN_WAVE
		);
		CustomRaidMember.ICEOLOGER = iceologerRaidMember;
		raidMembers.add(iceologerRaidMember);

		var illusionerRaidMember = newRaidMember(
			CustomRaidMember.ILLUSIONER_INTERNAL_NAME,
			lastRaidMember.ordinal() + 1,
			EntityType.ILLUSIONER,
			CustomRaidMember.ILLUSIONER_COUNT_IN_WAVE
		);
		CustomRaidMember.ILLUSIONER = illusionerRaidMember;
		raidMembers.add(illusionerRaidMember);

		field_16632 = raidMembers.toArray(new Raid.Member[0]);
	}

	@ModifyArg(
		method = "<clinit>",
		slice = @Slice(
			from = @At(
				value = "CONSTANT",
				args = "stringValue=EVOKER"
			)
		),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/village/raid/Raid$Member;<init>(Ljava/lang/String;ILnet/minecraft/entity/EntityType;[I)V",
			ordinal = 0
		)
	)
	private static int[] updateCountInWave(
		int[] countInWave
	) {
		if (
			FriendsAndFoes.getConfig().enableIllusioner
			|| FriendsAndFoes.getConfig().enableIllusionerInRaids
			|| FriendsAndFoes.getConfig().enableIceologer
			|| FriendsAndFoes.getConfig().enableIceologerInRaids
		) {
			return new int[]{0, 0, 0, 0, 0, 1, 1, 1};
		}

		return new int[]{0, 0, 0, 0, 0, 1, 1, 2};
	}
}
