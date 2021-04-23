package de.snx.fileIO;

/**
 * A simple class to pass and take two objects at the same time.
 * 
 * @author Sunnix
 *
 * @param <K> Object 1 (or Key)
 * @param <V> Object 2 (or Value)
 */
public class Pair<K, V> {

	public K key;
	public V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
}
