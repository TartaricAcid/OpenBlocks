package openblocks.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import openmods.container.ContainerBase;

public class ContainerDevNull extends ContainerBase<Void> {

	private InventoryPlayer playerInventory;

	private final int protectedSlotIndex;

	private final int protectedSlotNumber;

	public ContainerDevNull(IInventory playerInventory, IInventory ownerInventory, int selectedIndex) {
		super(playerInventory, ownerInventory, null);
		this.playerInventory = (InventoryPlayer)playerInventory;
		addInventoryGrid(80, 22, 1);
		addPlayerInventorySlots(55);

		this.protectedSlotIndex = selectedIndex;
		this.protectedSlotNumber = findSlotForIndex(selectedIndex);
	}

	private int findSlotForIndex(int selectedIndex) {
		for (Slot slot : inventorySlots)
			if (slot.inventory == this.playerInventory && slot.getSlotIndex() == selectedIndex)
				return slot.slotNumber;

		return -1;
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
		if (slotId == protectedSlotNumber) return null;
		if (clickType == ClickType.SWAP && dragType == protectedSlotIndex) return null;
		return super.slotClick(slotId, dragType, clickType, player);
	}

	@Override
	protected void addPlayerInventorySlots(int offsetX, int offsetY) {
		for (int row = 0; row < 3; row++)
			for (int column = 0; column < 9; column++)
				addSlotToContainer(new Slot(playerInventory,
						column + row * 9 + 9,
						offsetX + column * 18,
						offsetY + row * 18));

		for (int slot = 0; slot < 9; slot++) {
			addSlotToContainer(new Slot(playerInventory, slot, offsetX + slot * 18, offsetY + 58) {
				@Override
				public boolean canTakeStack(EntityPlayer par1EntityPlayer) {
					return slotNumber != protectedSlotNumber;
				}

				@Override
				public boolean canBeHovered() {
					return slotNumber != protectedSlotNumber;
				}
			});

		}
	}

}
