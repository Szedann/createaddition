package com.mrh0.createaddition.compat.jei;

import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.mrh0.createaddition.util.ClientMinecraftWrapper;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class LiquidBurningCategory extends CARecipeCategory<LiquidBurningRecipe> {

	private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

	public LiquidBurningCategory(Info<LiquidBurningRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, LiquidBurningRecipe recipe, IFocusGroup focuses) {
		List<ItemStack> buckets = recipe.getFluidIngredient().getMatchingFluidStacks().stream()
				.filter(e -> e != null)
				.map((e) -> new ItemStack(e.getFluid().getBucket()))
				.toList();
		builder
			.addSlot(RecipeIngredientRole.INPUT, getBackground().getWidth() / 2 -56, 3)
			.setBackground(getRenderedSlot(), -1, -1)
			.addItemStack(new ItemStack(CAItems.STRAW.get()));
		builder
			.addSlot(RecipeIngredientRole.INPUT, getBackground().getWidth() / 2 -36, 3)
			.setBackground(getRenderedSlot(), -1, -1)
			.addItemStacks(buckets);
		builder
			.addSlot(RecipeIngredientRole.INPUT, getBackground().getWidth() / 2 -16, 3)
			.setBackground(getRenderedSlot(), -1, -1)
			.addIngredients(FabricTypes.FLUID_STACK, toJei(withImprovedVisibility(recipe.getFluidIngredient().getMatchingFluidStacks())))
			.addTooltipCallback(addFluidTooltip(recipe.getFluidIngredient().getRequiredAmount()));
	}

	@Override
	public void draw(LiquidBurningRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics stack, double mouseX,
					 double mouseY) {

		stack.drawString(ClientMinecraftWrapper.getFont(), formatTime(recipe.getBurnTime()), getBackground().getWidth() / 2 + 48, 86 - 50, 4210752);


		HeatCondition requiredHeat = recipe.isSuperheated() ? HeatCondition.SUPERHEATED : HeatCondition.HEATED;

		AllGuiTextures.JEI_LIGHT.render(stack, 81, 58 + 30 - 50);

		AllGuiTextures.JEI_HEAT_BAR.render(stack, 4, 80 - 50);
		stack.drawString(ClientMinecraftWrapper.getFont(), Lang.translateDirect(requiredHeat.getTranslationKey()), 9,
				86 - 50, requiredHeat.getColor());

		heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
			.draw(stack, getBackground().getWidth() / 2 + 3, 55 - 50);

		AllGuiTextures.JEI_DOWN_ARROW.render(stack, getBackground().getWidth() / 2 + 3, 8);
	}

	public static String formatTime(int ticks) {
		if (ticks > 20*60) return (ticks/(20*60)) + " min";
		if (ticks > 20) return (ticks/20) + " sec";
		return (ticks) + " ticks";
	}
}
