package factorization.shared;

import static factorization.shared.BlockHelper.BlockStyle.*;

import java.util.ArrayList;

import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockHelper
{
    public static enum BlockStyle {
        // No value has been cached. This is the default value.
        UNDECIDED,
        // Do not return an ItemStack.
        NOTHING,
        // Call Blocks.getDamageValue to get the ItemStack
        USE_GET_DAMAGE_VALUE,
        // Call Blocks.idDropped to get the ItemStack
        USE_ID_DROPPED,
        // Return the first thing from Blocks.getBlockDropped.
        USE_GET_BLOCK_DROPPED,
        // Return the block with its metadata
        CLONE_MD,

        // These blocks do weird things and need to be special-cased
        STEM,
        SLAB,
        CAKE,
        CROP,
        DOOR,
        REDSTONE_ORE,
        PISTON_EXTENSION,
        BED
    }

    static BlockStyle getBlockStyle(Block block)
    {
        return classifyBlock(block);
    }

    private static BlockStyle classifyBlock(Block block)
    {
        // These blocks don't have a good way of extracting the item,
        // so no instanceof.
        if (block == Blocks.cake)
        {
            return CAKE;
        }
        if (block == Blocks.oreRedstone || block == Blocks.oreRedstoneGlowing)
        {
            return REDSTONE_ORE;
        }
        if (block == Blocks.pistonExtension)
        {
            return PISTON_EXTENSION;
        }
        if (block == Blocks.melonStem || block == Blocks.pumpkinStem)
        {
            return STEM;
        }
        if (block instanceof BlockSign || block instanceof BlockFlowerPot || block instanceof BlockRedstoneWire || block instanceof BlockBrewingStand
                || block instanceof BlockReed || block instanceof BlockTripWire || block instanceof BlockCauldron || block instanceof BlockRedstoneRepeater
                || block instanceof BlockComparator || block instanceof BlockRedstoneTorch || block instanceof BlockFarmland || block instanceof BlockFurnace
                || block instanceof BlockMushroomCap || block instanceof BlockRedstoneLight)
        {
            return USE_ID_DROPPED;
        }
        if (block instanceof BlockCocoa || block instanceof BlockNetherStalk || block instanceof BlockSkull)
        {
            return USE_GET_BLOCK_DROPPED;
        }
        if (block instanceof BlockPistonMoving || block instanceof BlockPortal || block instanceof BlockEndPortal || block instanceof BlockSilverfish
                || block instanceof BlockMobSpawner)
        {
            return NOTHING;
        }
        if (block instanceof BlockOre)
        {
            return CLONE_MD;
        }
        // Special blocks
        if (block instanceof BlockHalfSlab)
        {
            return SLAB;
        }
        if (block instanceof BlockCrops)
        {
            return CROP;
        }
        if (block instanceof BlockBed)
        {
            return BED;
        }
        if (block instanceof BlockDoor)
        {
            return DOOR;
        }
        return USE_GET_DAMAGE_VALUE;
    }

    private static ItemStack makeItemStack(int itemId, int stackSize, int damage)
    {
        if (itemId == 0)
        {
            return null;
        }
        return new ItemStack(itemId, stackSize, damage);
    }

    public static ItemStack getPlacingItem(Block block, MovingObjectPosition target, World world, int x, int y, int z)
    {
        int md;
        switch (classifyBlock(block))
        {
            default:
            case UNDECIDED:
            case NOTHING:
            case PISTON_EXTENSION:
                return null;
            case USE_GET_DAMAGE_VALUE:
                return new ItemStack(block, 1, block.getDamageValue(world, x, y, z));
            case USE_ID_DROPPED:
            case BED:
                md = world.getBlockMetadata(x, y, z);
                return makeItemStack(block.idDropped(md, world.rand, 0), 1, 0);
            case USE_GET_BLOCK_DROPPED:
                md = world.getBlockMetadata(x, y, z);
                ArrayList<ItemStack> drops = block.getBlockDropped(world, x, y, z, md, 0);
                if (drops.isEmpty())
                {
                    return null;
                }
                return drops.get(0);
            case CLONE_MD:
                md = world.getBlockMetadata(x, y, z);
                return new ItemStack(block, 1, md);
            case STEM:
                if (block == Blocks.pumpkinStem)
                {
                    return new ItemStack(Item.pumpkinSeeds);
                }
                else if (block == Blocks.melonStem)
                {
                    return new ItemStack(Item.melonSeeds);
                }
                else
                {
                    return null;
                }
            case SLAB:
                md = world.getBlockMetadata(x, y, z);
                int slabId = block.idDropped(md, world.rand, 0);
                int dropped = block.quantityDropped(world.rand);
                return makeItemStack(slabId, dropped, block.damageDropped(md));
            case CAKE:
                md = world.getBlockMetadata(x, y, z);
                return md == 0 ? new ItemStack(Item.cake) : null;
            case CROP:
                return new ItemStack(block.idDropped(0, world.rand, 0), 1, block.getDamageValue(world, x, y, z));
            case DOOR:
                md = world.getBlockMetadata(x, y, z);
                int doorId = block.idDropped(md, world.rand, 0);
                if (doorId == 0)
                {
                    return null;
                }
                return new ItemStack(doorId, 1, 0);
            case REDSTONE_ORE:
                return new ItemStack(Blocks.oreRedstone);
        }
    }
}
