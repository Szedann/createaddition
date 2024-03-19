package com.mrh0.createaddition.blocks.liquid_blaze_burner;

import java.util.Random;

import javax.annotation.Nullable;

import com.mrh0.createaddition.index.CABlockEntities;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.IBE;

import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlazeBurnerBlock extends HorizontalDirectionalBlock implements IBE<LiquidBlazeBurnerBlockEntity>, IWrenchable {

	public static final EnumProperty<BlazeBurnerBlock.HeatLevel> HEAT_LEVEL = EnumProperty.create("blaze", BlazeBurnerBlock.HeatLevel.class);

	public LiquidBlazeBurnerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HEAT_LEVEL, FACING);
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		if (world.isClientSide)
			return;
		BlockEntity tileEntity = world.getBlockEntity(pos.above());
		if (!(tileEntity instanceof BasinBlockEntity))
			return;
		BasinBlockEntity basin = (BasinBlockEntity) tileEntity;
		basin.notifyChangeOfContents();
	}

	@Override
	public Class<LiquidBlazeBurnerBlockEntity> getBlockEntityClass() {
		return LiquidBlazeBurnerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends LiquidBlazeBurnerBlockEntity> getBlockEntityType() {
		return CABlockEntities.LIQUID_BLAZE_BURNER.get();
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return IBE.super.newBlockEntity(pos, state);
	}

	@Override
	public Item asItem() {
		return AllBlocks.BLAZE_BURNER.get().asItem();
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {

		ItemStack heldItem = player.getItemInHand(hand);
		BlazeBurnerBlock.HeatLevel heat = state.getValue(HEAT_LEVEL);

		if (AllItems.GOGGLES.isIn(heldItem) && heat != BlazeBurnerBlock.HeatLevel.NONE)
			return onBlockEntityUse(level, pos, be -> {
				if (be.goggles)
					return InteractionResult.PASS;
				be.goggles = true;
				be.notifyUpdate();
				return InteractionResult.SUCCESS;
			});

		if (heldItem.isEmpty() && heat != BlazeBurnerBlock.HeatLevel.NONE)
			return onBlockEntityUse(level, pos, be -> {
				if (!be.goggles)
					return InteractionResult.PASS;
				be.goggles = false;
				be.notifyUpdate();
				return InteractionResult.SUCCESS;
			});

		ContainerItemContext context = player.isCreative() ? ContainerItemContext.forCreativeInteraction(player, heldItem) : ContainerItemContext.ofPlayerHand(player, hand);
		boolean forceOverflow = !(player instanceof FakePlayer);

		InteractionResult result;
		try(Transaction t = Transaction.openOuter()) {
			result = tryInsert(state, level, pos, heldItem, context, t, forceOverflow);
			t.commit();
		}
		/*ItemStack leftover = res.getObject();
		if (!level.isClientSide && !doNotConsume && !leftover.isEmpty()) {
			if (heldItem.isEmpty()) {
				player.setItemInHand(hand, leftover);
			} else if (!player.getInventory()
				.add(leftover)) {
				player.drop(leftover, false);
			}
		}*/

		return result == InteractionResult.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	public static InteractionResult tryInsert(BlockState state, Level level, BlockPos pos,
				ItemStack stack, ContainerItemContext context, TransactionContext t, boolean forceOverflow) {
		if (!state.hasBlockEntity())
			return InteractionResult.FAIL;

		BlockEntity te = level.getBlockEntity(pos);
		if (!(te instanceof LiquidBlazeBurnerBlockEntity))
			return InteractionResult.FAIL;
		LiquidBlazeBurnerBlockEntity burnerTE = (LiquidBlazeBurnerBlockEntity) te;

		if (burnerTE.isCreativeFuel(stack)) {
			TransactionCallback.onSuccess(t, () -> burnerTE.applyCreativeFuel());
			return InteractionResult.SUCCESS;
		}
		if (!burnerTE.tryUpdateFuel(stack, context, t, forceOverflow))
			return InteractionResult.FAIL;

		/*if (!doNotConsume) {
			ItemStack container = stack.getRecipeRemainder();
			if (!level.isClientSide) {
				stack.shrink(1);
			}
			return InteractionResult.SUCCESS;
		}*/
		return InteractionResult.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
		return AllShapes.HEATER_BLOCK_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos,
		CollisionContext context) {
		if (context == CollisionContext.empty())
			return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;
		return getShape(state, getter, pos, context);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return Math.max(0, state.getValue(HEAT_LEVEL)
			.ordinal() - 1);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter getter, BlockPos pos, PathComputationType type) {
		return false;
	}



	@Environment(EnvType.CLIENT)
	public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
		if (random.nextInt(10) != 0)
			return;
		if (!state.getValue(HEAT_LEVEL)
			.isAtLeast(BlazeBurnerBlock.HeatLevel.SMOULDERING))
			return;
		world.playLocalSound((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F),
			(double) ((float) pos.getZ() + 0.5F), SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
			0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
	}
}
