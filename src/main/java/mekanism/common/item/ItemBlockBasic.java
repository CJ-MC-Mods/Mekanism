package mekanism.common.item;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.InductionCellTier;
import mekanism.common.Tier.InductionProviderTier;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple metal block IDs.
 * 0:0: Osmium Block
 * 0:1: Bronze Block
 * 0:2: Refined Obsidian
 * 0:3: Charcoal Block
 * 0:4: Refined Glowstone
 * 0:5: Steel Block
 * 0:6: Bin
 * 0:7: Teleporter Frame
 * 0:8: Steel Casing
 * 0:9: Dynamic Tank
 * 0:10: Dynamic Glass
 * 0:11: Dynamic Valve
 * 0:12: Copper Block
 * 0:13: Tin Block
 * 0:14: Solar Evaporation Controller
 * 0:15: Solar Evaporation Valve
 * 1:0: Solar Evaporation Block
 * 1:1: Induction Casing
 * 1:2: Induction Port
 * 1:3: Induction Cell
 * 1:4: Induction Provider
 * @author AidanBrady
 *
 */
public class ItemBlockBasic extends ItemBlock
{
	public Block metaBlock;

	public ItemBlockBasic(Block block)
	{
		super(block);
		metaBlock = block;
		setHasSubtypes(true);
	}
	
	public BaseTier getTier(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return BaseTier.BASIC;
		}

		return BaseTier.values()[itemstack.stackTagCompound.getInteger("tier")];
	}

	public void setTier(ItemStack itemstack, BaseTier tier)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setInteger("tier", tier.ordinal());
	}

	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		if(Block.getBlockFromItem(this) == MekanismBlocks.BasicBlock)
		{
			if(stack.getItemDamage() == 6)
			{
				return 1;
			}
		}

		return 64;
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

	@Override
	public IIcon getIconFromDamage(int i)
	{
		return metaBlock.getIcon(2, i);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(Block.getBlockFromItem(this) == MekanismBlocks.BasicBlock && itemstack.getItemDamage() == 6)
		{
			InventoryBin inv = new InventoryBin(itemstack);

			if(inv.getItemCount() > 0)
			{
				list.add(EnumColor.BRIGHT_GREEN + inv.getItemType().getDisplayName());
				list.add(EnumColor.INDIGO + "Item amount: " + EnumColor.GREY + inv.getItemCount());
			}
			else {
				list.add(EnumColor.DARK_RED + "Empty");
			}
		}
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return stack.getItemDamage() == 6 && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("newCount");
	}

	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		if(Block.getBlockFromItem(this) == MekanismBlocks.BasicBlock)
		{
			if(stack.getItemDamage() != 6)
			{
				return true;
			}
		}

		if(stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("newCount"))
		{
			return true;
		}

		return false;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(Block.getBlockFromItem(this) == MekanismBlocks.BasicBlock)
		{
			if(stack.getItemDamage() != 6 || stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("newCount"))
			{
				return null;
			}
		}

		ItemStack ret = stack.copy();
		ret.stackTagCompound.setInteger("itemCount", stack.stackTagCompound.getInteger("newCount"));

		return ret;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);

		if(place)
		{
			if(Block.getBlockFromItem(this) == MekanismBlocks.BasicBlock)
			{
				if(stack.getItemDamage() == 6 && stack.stackTagCompound != null)
				{
					TileEntityBin tileEntity = (TileEntityBin)world.getTileEntity(x, y, z);
					InventoryBin inv = new InventoryBin(stack);

					if(inv.getItemType() != null)
					{
						tileEntity.setItemType(inv.getItemType());
					}

					tileEntity.setItemCount(inv.getItemCount());
				}
			}
			else if(Block.getBlockFromItem(this) == MekanismBlocks.BasicBlock2)
			{
				if(stack.getItemDamage() == 3)
				{
					TileEntityInductionCell tileEntity = (TileEntityInductionCell)world.getTileEntity(x, y, z);
					tileEntity.tier = InductionCellTier.values()[getTier(stack).ordinal()];
					
					if(!world.isRemote)
					{
						Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
					}
				}
				else if(stack.getItemDamage() == 4)
				{
					TileEntityInductionProvider tileEntity = (TileEntityInductionProvider)world.getTileEntity(x, y, z);
					tileEntity.tier = InductionProviderTier.values()[getTier(stack).ordinal()];
					
					if(!world.isRemote)
					{
						Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
					}
				}
			}
		}

		return place;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		String name = "";

		if(Block.getBlockFromItem(this) == MekanismBlocks.BasicBlock)
		{
			switch(itemstack.getItemDamage())
			{
				case 0:
					name = "OsmiumBlock";
					break;
				case 1:
					name = "BronzeBlock";
					break;
				case 2:
					name = "RefinedObsidian";
					break;
				case 3:
					name = "CharcoalBlock";
					break;
				case 4:
					name = "RefinedGlowstone";
					break;
				case 5:
					name = "SteelBlock";
					break;
				case 6:
					name = "Bin";
					break;
				case 7:
					name = "TeleporterFrame";
					break;
				case 8:
					name = "SteelCasing";
					break;
				case 9:
					name = "DynamicTank";
					break;
				case 10:
					name = "DynamicGlass";
					break;
				case 11:
					name = "DynamicValve";
					break;
				case 12:
					name = "CopperBlock";
					break;
				case 13:
					name = "TinBlock";
					break;
				case 14:
					name = "SolarEvaporationController";
					break;
				case 15:
					name = "SolarEvaporationValve";
					break;
				default:
					name = "Unknown";
					break;
			}
		}
		else if(Block.getBlockFromItem(this) == MekanismBlocks.BasicBlock2)
		{
			switch(itemstack.getItemDamage())
			{
				case 0:
					name = "SolarEvaporationBlock";
					break;
				case 1:
					name = "InductionCasing";
					break;
				case 2:
					name = "InductionPort";
					break;
				case 3:
					name = "InductionCell" + getTier(itemstack).getName();
					break;
				case 4:
					name = "InductionProvider" + getTier(itemstack).getName();
					break;
			}
		}

		return getUnlocalizedName() + "." + name;
	}
}
