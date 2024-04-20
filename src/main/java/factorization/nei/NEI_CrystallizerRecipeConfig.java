package factorization.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.forge.GuiContainerManager;
import codechicken.nei.recipe.FurnaceRecipeHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler.RecipeTransferRect;
import factorization.client.gui.GuiCrystallizer;
import factorization.client.gui.GuiSlag;
import factorization.common.Core;
import factorization.common.TileEntityCrystallizer;
import factorization.common.TileEntityCrystallizer.CrystalRecipe;
import factorization.common.TileEntitySlagFurnace;
import factorization.common.TileEntitySlagFurnace.SlagRecipes;
import factorization.common.TileEntitySlagFurnace.SmeltingResult;

public class NEI_CrystallizerRecipeConfig extends TemplateRecipeHandler implements IConfigureNEI {
    @Override
    public void loadConfig() {
        API.registerRecipeHandler(this);
        API.registerUsageHandler(this);
    }

    @Override
    public String getName() {
        return "factorization crystallizer recipes";
    }

    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        //XXX NOTE: This is probably a lame implementation of this function.
        for (CrystalRecipe cr : TileEntityCrystallizer.recipes) {
            if (result == null || result.isItemEqual(cr.output)) {
                arecipes.add(new CachedCrystallizerRecipe(cr));
            }
        }
    }
    
    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("crystallizing")) {
            loadCraftingRecipes(null);
            return;
        }
        super.loadCraftingRecipes(outputId, results);
    }
    
    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        //XXX NOTE: This is probably a lame implementation of this function.
        for (CrystalRecipe cr : TileEntityCrystallizer.recipes) {
            if (ingredient == null
                    || ingredient.isItemEqual(cr.input)
                    || (ingredient.getItem() == Core.registry.inverium && cr.inverium_count > 0)
                    || ingredient.isItemEqual(cr.solution)) {
                arecipes.add(new CachedCrystallizerRecipe(cr));
            }
        }
    }

    class CachedCrystallizerRecipe extends CachedRecipe {
        CrystalRecipe cr;

        public CachedCrystallizerRecipe(CrystalRecipe cr) {
            this.cr = cr;
        }

        @Override
        public PositionedStack getResult() {
            return new PositionedStack(cr.output, 75, 29 + 15);
        }

        @Override
        public PositionedStack getIngredient() {
            return null;
        }

        @Override
        public ArrayList<PositionedStack> getOtherStacks() {
            ArrayList<PositionedStack> ret = new ArrayList();
            ret.add(new PositionedStack(cr.input, 75, 2 + 15));
            if (cr.inverium_count > 0) {
                ItemStack inverium = new ItemStack(Core.registry.inverium, cr.inverium_count);
                ret.add(new PositionedStack(inverium, 103, 44 + 15));
                ret.add(new PositionedStack(cr.solution, 47, 44 + 15));
            } else {
                ret.add(new PositionedStack(cr.solution, 75, 58 + 15));
            }
            return ret;
        }

    }
    
    @Override
    public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List currenttip, int recipe) {
        if (stack == null) {
            return currenttip;
        }
        CrystalRecipe cr = ((CachedCrystallizerRecipe)arecipes.get(recipe)).cr;
        if (stack.isItemEqual(cr.output)) {
            float prob = cr.output_count*100;
            if (prob != 100 || true) {
                currenttip.add((int)prob + "%");
            }
        }
        if (stack.isItemEqual(cr.solution)) {
            currenttip.add("Does not get used up");
        }
        if (stack.isItemEqual(cr.input)) {
            currenttip.add("Extra copies of this can be added");
            currenttip.add("to the other slots to increase yield");
        }
        return currenttip;
    }

    @Override
    public void loadTransferRects() {
        // XXX TODO (if this is even actually necessary? What's it do?) (It might give you places to click on to bring up the recipes list.)
        transferRects.add(new RecipeTransferRect(new Rectangle(35 + 63, 78, 95 - 63, 16), "crystallizing"));
    }

    @Override
    public Class<? extends GuiContainer> getGuiClass() {
        return GuiCrystallizer.class;		
    }

    @Override
    public String getRecipeName() {
        return "Crystallizer";
    }

    @Override
    public String getGuiTexture() {
        return Core.texture_dir + "crystal.png";
    }

    @Override
    public String getOverlayIdentifier() {
        return "crystallizing";
    }
    
    public void drawBackground(GuiContainerManager gui, int recipe)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        gui.bindTextureByName(getGuiTexture());
        gui.drawTexturedModalRect(0, 15, 5, 11, 166, 95);
    }
    
    @Override
    public int recipiesPerPage() {
        return 1;
    }
}
