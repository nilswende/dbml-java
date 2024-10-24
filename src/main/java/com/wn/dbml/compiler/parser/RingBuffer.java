package com.wn.dbml.compiler.parser;

import java.util.ArrayDeque;
import java.util.Deque;

class RingBuffer<E> {
	private final Deque<E> queue;
	private final int size;
	
	public RingBuffer(int size) {
		if (size < 1) throw new IllegalArgumentException("Illegal Size: " + size);
		queue = new ArrayDeque<>(size);
		this.size = size;
	}
	
	public void add(E e) {
		if (queue.size() == size) {
			poll();
		}
		queue.add(e);
	}
	
	public E poll() {
		return queue.poll();
	}
	
	@Override
	public String toString() {
		return queue.toString();
	}
}
