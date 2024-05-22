package com.zl.learn.threads.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 定义可变更的阻塞队列
 * @param <T>
 */
public class ResizeBlockingQueue<T> implements BlockingQueue<T>, ResizeQueue {
    private int capacity;
    private LinkedBlockingQueue<T> queue;
    public ResizeBlockingQueue(){
        this(100);
    }

    public ResizeBlockingQueue(int capacity){
        this.capacity = capacity;
        this.queue  = new LinkedBlockingQueue<>(capacity);
    }

    //进行容量扩容
    @Override
    public boolean resize(int size){
        synchronized (this){
            this.capacity = size;
            BlockingQueue<T> oldQueue = queue;
            this.queue = new LinkedBlockingQueue<>(size);
            for(T t : oldQueue){
                try {
                    this.queue.put(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public boolean add(T t) {
        return queue.add(t);
    }

    @Override
    public boolean offer(T t) {
        return queue.offer(t);
    }

    @Override
    public T remove() {
        return queue.remove();
    }

    @Override
    public T poll() {
        return queue.poll();
    }

    @Override
    public T element() {
        return queue.element();
    }

    @Override
    public T peek() {
        return queue.peek();
    }

    @Override
    public void put(T t) throws InterruptedException {
        queue.put(t);
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        return queue.offer(t, timeout, unit);
    }

    @Override
    public T take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll();
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public boolean equals(Object o) {
        return queue.equals(o);
    }

    @Override
    public int hashCode() {
        return queue.hashCode();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return queue.contains(0);
    }

    @Override
    public Iterator<T> iterator() {
        return queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return queue.toArray(a);
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        return queue.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        return queue.drainTo(c,maxElements);
    }
}
