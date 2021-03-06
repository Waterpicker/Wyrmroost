package WolfShotz.Wyrmroost.util.compat;

import WolfShotz.Wyrmroost.Wyrmroost;
import WolfShotz.Wyrmroost.registry.WRItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * @deprecated Tarrgaon Tome OR JEI?
 */
@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    @Override
    public ResourceLocation getPluginUid()
    {
        return Wyrmroost.rl("info");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry)
    {
        // Soul Crystal
        registry.addIngredientInfo(new ItemStack(WRItems.SOUL_CRYSTAL.get(), 1), VanillaTypes.ITEM,
                new TranslationTextComponent(
                        "item.wyrmroost.soul_crystal.jeidesc",
                        new StringTextComponent("dsabgi")
                                .applyTextStyle(TextFormatting.OBFUSCATED)
                                .getFormattedText())
                        .getFormattedText());

        registry.addIngredientInfo(new ItemStack(WRItems.SOUL_CRYSTAL.get(), 1), VanillaTypes.ITEM, new StringTextComponent("item.wyrmroost.dragon_egg.jeidesc").getFormattedText());
    }
}
