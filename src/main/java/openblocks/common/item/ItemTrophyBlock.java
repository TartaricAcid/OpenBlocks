package openblocks.common.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import openblocks.common.TrophyHandler.Trophy;
import openmods.item.ItemOpenBlock;
import openmods.utils.ItemUtils;
import openmods.utils.TranslationUtils;

public class ItemTrophyBlock extends ItemOpenBlock {

	private static final String TAG_ENTITY = "entity";

	public ItemTrophyBlock(Block block) {
		super(block);
	}

	public static Trophy getTrophy(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag.hasKey(TAG_ENTITY)) {
				String entityKey = tag.getString(TAG_ENTITY);
				return Trophy.TYPES.get(entityKey);
			}
		}

		return null;
	}

	@Override
	public int getMetadata(ItemStack stack) {
		// for item rendering purposes
		Trophy trophy = getTrophy(stack);
		return trophy != null? trophy.ordinal() : 0;
	}

	public static ItemStack putMetadata(ItemStack stack, Trophy trophy) {
		NBTTagCompound tag = ItemUtils.getItemTag(stack);
		tag.setString(TAG_ENTITY, trophy.name());
		return stack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		Trophy trophyType = getTrophy(stack);
		if (trophyType != null) {
			final String name = TranslationUtils.translateToLocal("entity." + trophyType.name() + ".name");
			return TranslationUtils.translateToLocalFormatted(super.getUnlocalizedName() + ".entity.name", name);
		}

		return super.getItemStackDisplayName(stack);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> result) {
		for (Trophy trophy : Trophy.VALUES) {
			result.add(putMetadata(new ItemStack(this), trophy));
		}
	}
}
