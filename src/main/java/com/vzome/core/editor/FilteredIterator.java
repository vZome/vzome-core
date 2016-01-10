package com.vzome.core.editor;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * @author David Hall
 * Based on http://stackoverflow.com/questions/5474893/how-to-implement-this-filteringiterator
 */
public abstract class FilteredIterator<T, R> implements Iterator<R>, Iterable<R> {

    @Override
    public Iterator<R> iterator() {
        return this; // careful, don't return wrappedIterator
    }

    private final Iterator<T> wrappedIterator;
    private final Predicate<T> preFilter;
    private final Predicate<R> postFilter;
    private R nextElement = null;
    private Boolean hasNext = null; // determined on first use
    
    /**
     * A convenience function that may be passed as a preFilter parameter
     * @param <T>
     * @param element
     * @return {@code true} if element is not null
     */
    protected static <T> boolean ElementNotNull(T element) {
        return element != null;
    }

    /**
     * A convenience function that may be passed as a postFilter parameter
     * @param <R>
     * @param result
     * @return {@code true} if result is not null
     */
    protected static <R> boolean ResultNotNull(R result) {
        return result != null;
    }

    /**
     * Elements must match this filter before conversion
     * @param element
     */
    protected boolean preFilter(T element) {
        return preFilter == null 
                ? true
                : preFilter.test(element);
    }

    /**
     * Elements must match this filter after conversion
     * @param element
     */
    protected boolean postFilter(R element) {
        return postFilter == null 
                ? true
                : postFilter.test(element);
    }
    
    /**
     * Elements are converted from T to R. 
     * T and R may be identical, related (e.g. sub-classed) or completely unrelated.
     * @param element
     */
    protected abstract R convert(T element);

    protected FilteredIterator(Iterable<T> iterable) {
        this((Predicate<T>) null, iterable, (Predicate<R>) null);    
    }

    protected FilteredIterator(Predicate<T> preTest, Iterable<T> iterable) {
        this(preTest, iterable, (Predicate<R>) null);    
    }

    protected FilteredIterator(Iterable<T> iterable, Predicate<R> postTest) {
        this((Predicate<T>) null, iterable, postTest);    
    }

    /**
     * Creates a new FilteredIterator by wrapping the iterator 
     *  and returning only converted elements matching the filters.
     * 
     * @param preTest may be null to skip preTest;
     * @param iterable
     * @param postTest may be null to skip postTest;
     */
    protected FilteredIterator(Predicate<T> preTest, Iterable<T> iterable, Predicate<R> postTest) {
        this.wrappedIterator = iterable.iterator();
        preFilter = preTest;
        postFilter = postTest;
        // can't call nextMatch in any c'tor, because derived classes 
        // may not be fully initialized yet and their filters may not work.this(preTest, iterable.iterator(), postTest);    
    }

    @Override
    public boolean hasNext() {
        if(hasNext == null) {
            // first call after c'tor
            nextMatch();
        }
        return hasNext;
    }

    @Override
    public R next() {
        if(hasNext == null) {
            // first call after c'tor
            nextMatch();
        }
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        return nextMatch();
    }
    
    private R nextMatch() {
        R lastMatch = nextElement;
        while (wrappedIterator.hasNext()) {
            T next = wrappedIterator.next();
            if (preFilter(next)) {
                R converted = convert(next);
                if(postFilter(converted)) {
                    nextElement = converted;
                    hasNext = true;
                    return lastMatch;
                }
            }
        }
        hasNext = false;
        return lastMatch;
    }
    
    @Override
    public void remove() {
        wrappedIterator.remove();
    }
}
