package openblocks.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import openblocks.OpenBlocks;
import openblocks.common.LiquidXpUtils;
import openblocks.common.block.BlockXPShower;
import openblocks.common.entity.EntityXPOrbNoFly;
import openmods.OpenMods;
import openmods.liquids.GenericTank;
import openmods.sync.SyncableBoolean;
import openmods.tileentity.SyncedTileEntity;

public class TileEntityXPShower extends SyncedTileEntity implements ITickable {

	private static final FluidStack XP_FLUID = new FluidStack(OpenBlocks.Fluids.xpJuice, 1);

	private static final int DRAIN_PER_CYCLE = 100;

	private static final int ORB_SPAWN_FREQUENCY = 3;

	private GenericTank bufferTank = new GenericTank(Fluid.BUCKET_VOLUME, OpenBlocks.Fluids.xpJuice);

	private SyncableBoolean particleSpawnerActive;
	private int particleSpawnTimer = 0;

	@Override
	protected void createSyncedFields() {
		particleSpawnerActive = new SyncableBoolean();
	}

	@Override
	public void update() {
		if (!worldObj.isRemote) {
			trySpawnXpOrbs();
		} else {
			trySpawnParticles();
		}
	}

	private void trySpawnXpOrbs() {
		boolean hasSpawnedParticle = false;
		if (OpenMods.proxy.getTicks(worldObj) % ORB_SPAWN_FREQUENCY == 0 && isPowered()) {
			bufferTank.fillFromSide(DRAIN_PER_CYCLE, worldObj, pos, getBack());

			final int amountInTank = bufferTank.getFluidAmount();

			if (amountInTank > 0) {
				final int xpInTank = LiquidXpUtils.liquidToXpRatio(amountInTank);
				final int xpInOrb = EntityXPOrb.getXPSplit(xpInTank);
				final int toDrain = LiquidXpUtils.xpToLiquidRatio(xpInOrb);

				if (toDrain > 0) {
					bufferTank.drain(toDrain, true);
					hasSpawnedParticle = true;

					final BlockPos p = getPos();
					worldObj.spawnEntityInWorld(new EntityXPOrbNoFly(worldObj, p.getX() + 0.5, p.getY() + 0.1, p.getZ() + 0.5, xpInOrb));
				}
			}
		}

		particleSpawnerActive.set(hasSpawnedParticle);
		sync();
	}

	private boolean isPowered() {
		final IBlockState state = worldObj.getBlockState(pos);
		return state.getBlock() instanceof BlockXPShower && state.getValue(BlockXPShower.POWERED);
	}

	private void trySpawnParticles() {
		final int particleLevel = OpenBlocks.proxy.getParticleSettings();
		if (particleLevel == 0 || (particleLevel == 1 && worldObj.rand.nextInt(3) == 0)) {
			particleSpawnTimer = particleSpawnerActive.get()? 10 : particleSpawnTimer - 1;

			if (particleSpawnTimer > 0) {
				final BlockPos p = getPos();
				Vec3d vec = new Vec3d(
						(worldObj.rand.nextDouble() - 0.5) * 0.05,
						0,
						(worldObj.rand.nextDouble() - 0.5) * 0.05);
				OpenBlocks.proxy.spawnLiquidSpray(worldObj, XP_FLUID, p.getX() + 0.5d, p.getY() + 0.4d, p.getZ() + 0.5d, 0.4f, 0.7f, vec);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		bufferTank.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		bufferTank.writeToNBT(tag);
		return tag;
	}
}
