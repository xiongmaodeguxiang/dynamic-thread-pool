package com.zl.learn.threads.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ReChangeBlockingQueue<T> implements BlockingQueue<T>, ChangeQueue<T> {
    private BlockingQueue<T> queue;
    public BlockingQueue<T> getDelegate(){
        return queue;
    }
    public ReChangeBlockingQueue(BlockingQueue<T> queue){
        this.queue  = queue;
    }
    @Override
    public boolean supportResize() {
        return ResizeQueue.class.isAssignableFrom(queue.getClass());
    }
    @Override
    public void changeType(BlockingQueue<T> newQueue) {
        synchronized (this){
            BlockingQueue<T> oldQueue = this.queue;
            this.queue = newQueue;
            for(T t : oldQueue){
                try {
                    this.queue.put(t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
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
