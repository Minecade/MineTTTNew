package src.main.java.de.orion304.ttt.main;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class TempBlock {

	private static ConcurrentHashMap<Block, TempBlock> blocks = new ConcurrentHashMap<>();

	private Block block;
	private BlockState oldState;

	public TempBlock(Block block, Material type) {
		oldState = block.getState();
		block.setType(type);

		blocks.put(block, this);
	}

	public void revert() {
		oldState.update(true, true);
		blocks.remove(this);
	}

	public static void revertAll() {
		for (TempBlock block : blocks.values())
			block.revert();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((block == null) ? 0 : block.hashCode());
		result = prime * result
				+ ((oldState == null) ? 0 : oldState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TempBlock other = (TempBlock) obj;
		if (block == null) {
			if (other.block != null)
				return false;
		} else if (!block.equals(other.block))
			return false;
		if (oldState == null) {
			if (other.oldState != null)
				return false;
		} else if (!oldState.equals(other.oldState))
			return false;
		return true;
	}

}
