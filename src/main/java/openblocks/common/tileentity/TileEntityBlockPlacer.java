package openblocks.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import openblocks.client.gui.GuiBlockPlacer;
import openblocks.common.container.ContainerBlockPlacer;
import openmods.api.IHasGui;
import openmods.fakeplayer.FakePlayerPool;
import openmods.fakeplayer.UseItemAction;
import openmods.include.IncludeInterface;
import openmods.inventory.GenericInventory;
import openmods.inventory.IInventoryProvider;
import openmods.inventory.TileEntityInventory;

public class TileEntityBlockPlacer extends TileEntityBlockManipulator implements IHasGui, IInventoryProvider {

	static final int BUFFER_SIZE = 9;

	private final GenericInventory inventory = registerInventoryCallback(new TileEntityInventory(this, "blockPlacer", false, BUFFER_SIZE));

	@Override
	protected boolean canWork(IBlockState targetState, BlockPos target, EnumFacing direction) {
		if (inventory.isEmpty()) return false;

		final Block block = targetState.getBlock();
		return block.isAir(targetState, worldObj, target) || block.isReplaceable(worldObj, target);
	}

	@Override
	protected void doWork(IBlockState targetState, BlockPos target, EnumFacing direction) {
		ItemStack stack = null;
		int slotId = 0;

		for (slotId = 0; slotId < inventory.getSizeInventory(); slotId++) {
			stack = inventory.getStackInSlot(slotId);
			if (stack != null && stack.stackSize > 0) break;
		}

		if (stack == null) return;

		// this logic is tuned for vanilla blocks (like pistons), which places blocks with front facing player
		// so to place object pointing in the same direction as placer, we need configuration player-target-placer
		// * 2, since some blocks may take into account player height, so distance must be greater than that
		final BlockPos playerPos = target.offset(direction, 2);

		final ItemStack result = FakePlayerPool.instance.executeOnPlayer((WorldServer)worldObj, new UseItemAction(
				stack,
				new Vec3d(playerPos),
				new Vec3d(target),
				new Vec3d(target).addVector(0.5, 0.5, 0.5),
				direction.getOpposite(),
				EnumHand.MAIN_HAND));

		inventory.setInventorySlotContents(slotId, result);
	}

	@Override
	public Object getServerGui(EntityPlayer player) {
		return new ContainerBlockPlacer(player.inventory, this);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		return new GuiBlockPlacer(new ContainerBlockPlacer(player.inventory, this));
	}

	@Override
	public boolean canOpenGui(EntityPlayer player) {
		return true;
	}

	@Override
	@IncludeInterface
	public IInventory getInventory() {
		return inventory;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		inventory.writeToNBT(tag);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		inventory.readFromNBT(tag);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T)inventory.getHandler();

		return super.getCapability(capability, facing);
	}
}
