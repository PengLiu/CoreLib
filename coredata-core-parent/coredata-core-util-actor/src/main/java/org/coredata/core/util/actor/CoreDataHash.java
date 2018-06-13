package org.coredata.core.util.actor;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;

public class CoreDataHash<K, N extends Comparable<? super N>> {

	/**
	 * A hashing function from guava, ie Hashing.murmur3_128()
	 */
	private final HashFunction hasher;

	/**
	 * A funnel to describe how to take the key and add it to a hash.
	 * 
	 * @see com.google.common.hash.Funnel
	 */
	private final Funnel<K> keyFunnel;

	/**
	 * Funnel describing how to take the type of the node and add it to a hash
	 */
	private final Funnel<N> nodeFunnel;

	/**
	 * All the current nodes in the pool
	 */
	private final ConcurrentSkipListSet<N> ordered;

	/**
	 * Creates a new RendezvousHash with a starting set of nodes provided by
	 * init. The funnels will be used when generating the hash that combines the
	 * nodes and keys. The hasher specifies the hashing algorithm to use.
	 */
	public CoreDataHash(HashFunction hasher, Funnel<K> keyFunnel, Funnel<N> nodeFunnel, Collection<N> init) {
		if (hasher == null)
			throw new NullPointerException("hasher");
		if (keyFunnel == null)
			throw new NullPointerException("keyFunnel");
		if (nodeFunnel == null)
			throw new NullPointerException("nodeFunnel");
		if (init == null)
			throw new NullPointerException("init");
		this.hasher = hasher;
		this.keyFunnel = keyFunnel;
		this.nodeFunnel = nodeFunnel;
		this.ordered = new ConcurrentSkipListSet<N>(init);
	}

	/**
	 * Removes a node from the pool. Keys that referenced it should after this
	 * be evenly distributed amongst the other nodes
	 * 
	 * @return true if the node was in the pool
	 */
	public boolean remove(N node) {
		return ordered.remove(node);
	}

	/**
	 * Add a new node to pool and take an even distribution of the load off
	 * existing nodes
	 * 
	 * @return true if node did not previously exist in pool
	 */
	public boolean add(N node) {
		return ordered.add(node);
	}

	/**
	 * return a node for a given key
	 */
	public N get(K key) {
		long maxValue = Long.MIN_VALUE;
		N max = null;
		for (N node : ordered) {
			long nodesHash = hasher.newHasher().putObject(key, keyFunnel).putObject(node, nodeFunnel).hash().asLong();
			if (nodesHash > maxValue) {
				max = node;
				maxValue = nodesHash;
			}
		}
		return max;
	}
}