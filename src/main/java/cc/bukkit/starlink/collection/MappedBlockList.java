package cc.bukkit.starlink.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import net.minecraft.server.BlockPosition;

/**
 * A simple wrapper of <code>BlockPosition</code> list and capable
 * of using it as a <code>Block</code> list with all operations accepted.
 * 
 * Since <code>BlockPosition</code> do not contains the world, however
 * <code>Block</code> does, this list also holding a <code>World</code> instance.
 * 
 * Also note that this list do not accept <code>null</code> elements, and
 * due to block operations, this list was considered as not thread-safe.
 * 
 * @see BlockPosition
 * @see Block
 * @see World
 * @author Sotr (cakoyo)
 */
@NotThreadSafe
public class MappedBlockList implements List<Block> {
  private final World world;
  private final List<BlockPosition> blocks;

  public MappedBlockList(List<BlockPosition> src, World bukkitWorld) {
    blocks = src;
    world = bukkitWorld;
  }

  public Block toBukkit(@Nullable BlockPosition position) {
    return world.getBlockAt(position.getX(), position.getY(), position.getZ());
  }

  private static BlockPosition toNMS(@Nullable Block block) {
    return new BlockPosition(block.getX(), block.getY(), block.getZ());
  }

  public static MappedBlockList of(List<BlockPosition> src, World bukkitWorld) {
    return new MappedBlockList(src, bukkitWorld);
  }

  @Override
  public int size() {
    return blocks.size();
  }

  @Override
  public boolean isEmpty() {
    return blocks.isEmpty();
  }

  @Override
  public boolean contains(@Nullable Object o) {
    return o instanceof Block ? blocks.contains(toNMS((Block) o)) : false;
  }

  /**
   * An optimized version of ArrayList.Itr
   */
  private class BlockIterator implements Iterator<Block> {
    private final Iterator<BlockPosition> iterator = blocks.iterator();

    public boolean hasNext() {
      return iterator.hasNext();
    }

    public Block next() {
      return toBukkit(iterator.next());
    }

    public void remove() {
      iterator.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super Block> consumer) {
      iterator.forEachRemaining(nms -> consumer.accept(toBukkit(nms)));
    }
  }

  private BlockIterator iterator;

  @Override
  public Iterator<Block> iterator() {
    return iterator == null ? (iterator = new BlockIterator()) : iterator;
  }

  @Override
  public Object[] toArray() {
    return Lists.transform(blocks, nms -> toBukkit(nms)).toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return Lists.transform(blocks, nms -> toBukkit(nms)).toArray(a);
  }

  @Override
  public boolean add(@Nonnull Block e) {
    return blocks.add(toNMS(e));
  }

  @Override
  public boolean remove(@Nullable Object o) {
    return o instanceof Block ? blocks.remove(toNMS((Block) o)) : false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c)
      if (!contains(o))
        return false;
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends Block> c) {
    int before = size();
    for (Block b : c)
      add(b);
    return size() != before;
  }

  @Override
  public boolean addAll(int index, Collection<? extends Block> c) {
    int before = size();
    for (Block b : c)
      add(index++, b);
    return size() != before;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    int before = size();
    for (Object b : c)
      remove(b);
    return size() != before;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return blocks
        .retainAll(Collections2.transform(c, b -> b instanceof Block ? toNMS((Block) b) : b));
  }

  @Override
  public void clear() {
    blocks.clear();
  }

  @Override
  public Block get(int index) {
    return toBukkit(blocks.get(index));
  }

  @Override
  public Block set(int index, @Nonnull Block element) {
    return toBukkit(blocks.set(index, toNMS(element)));
  }

  @Override
  public void add(int index, @Nonnull Block element) {
    blocks.add(index, toNMS(element));
  }

  @Override
  public Block remove(int index) {
    return toBukkit(blocks.remove(index));
  }

  @Override
  public int indexOf(@Nullable Object o) {
    return o instanceof Block ? blocks.indexOf(toNMS((Block) o)) : -1;
  }

  @Override
  public int lastIndexOf(@Nullable Object o) {
    return o instanceof Block ? blocks.lastIndexOf(toNMS((Block) o)) : -1;
  }

  @Override
  public ListIterator<Block> listIterator() {
    return Lists.transform(blocks, nms -> toBukkit(nms)).listIterator(); // Can be optimized
  }

  @Override
  public ListIterator<Block> listIterator(int index) {
    return Lists.transform(blocks, nms -> toBukkit(nms)).listIterator(index); // Can be optimized
  }

  @Override
  public List<Block> subList(int fromIndex, int toIndex) {
    return Lists.transform(blocks.subList(fromIndex, toIndex), nms -> toBukkit(nms));
  }
}
