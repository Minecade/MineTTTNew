package src.main.java.de.orion304.ttt.main;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class TempBlock {

	private static ConcurrentHashMap<Block, TempBlock> blocks = new ConcurrentHashMap<>();

	/**
	 * Reverts all blocks which have been changed. NOTE: Any TempBlock objects
	 * in memory from elsewhere will no longer function.
	 */
	public static void revertAll() {
		for (TempBlock block : blocks.values()) {
			block.revert();
		}
	}

	private Block block;

	private final BlockState oldState;

	/**
	 * Changes the block to this type, but keeps the previous state in memory
	 * for reversion whenever called.
	 * 
	 * @param block
	 *            The block to change.
	 * @param type
	 *            The Material to change it to.
	 */
	public TempBlock(Block block, Material type) {
		this.oldState = block.getState();
		block.setType(type);

		blocks.put(block, this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TempBlock other = (TempBlock) obj;
		if (this.block == null) {
			if (other.block != null) {
				return false;
			}
		} else if (!this.block.equals(other.block)) {
			return false;
		}
		if (this.oldState == null) {
			if (other.oldState != null) {
				return false;
			}
		} else if (!this.oldState.equals(other.oldState)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.block == null) ? 0 : this.block.hashCode());
		result = prime * result
				+ ((this.oldState == null) ? 0 : this.oldState.hashCode());
		return result;
	}

	/**
	 * Reverts the block to its previous state. NOTE: Do not call any methods of
	 * this object after using this.
	 */
	public void revert() {
		this.oldState.update(true, true);
		blocks.remove(this);
	}

}
