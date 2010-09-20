package pt.inevo.encontra.query;

import java.io.Closeable;

/**
 * Interface that allows lazy/custom instantiation of input objects.
 * {@link ResultList} objects do not necessarily load in data all
 * at once. Instead, they may lazily load objects as necessary. So,
 * the lifespan of a {@link ResultObjectProvider} instance is
 * related to how the application deals with processing the
 * {@link ResultList} created with a given
 * {@link ResultObjectProvider} instance.
 *
 * @author Marc Prud'hommeaux
 * @author Patrick Linskey
 * @author Abe White
 */
public interface ResultObjectProvider extends Closeable {

    /**
     * Return true if this provider supports random access.
     */
    public boolean supportsRandomAccess();

    /**
     * Open the result. This will be called before
     * {@link #next}, {@link #absolute}, or {@link #size}.
     */
    public void open() throws Exception;

    /**
     * Instantiate the current result object. This method will only be
     * called after {@link #next} or {@link #absolute}.
     */
    public Object getResultObject() throws Exception;

    /**
     * Advance the input to the next position. Return <code>true</code> if
     * there is more data; otherwise <code>false</code>.
     */
    public boolean next() throws Exception;

    /**
     * Move to the given 0-based position. This method is
     * only called for providers that support random access.
     * Return <code>true</code> if there is data at this position;
     * otherwise <code>false</code>. This may be invoked in place of
     * {@link #next}.
     */
    public boolean absolute(int pos) throws Exception;

    /**
     * Return the number of items in the input, or {@link Integer#MAX_VALUE}
     * if the size in unknown.
     */
    public int size() throws Exception;

    /**
     * Reset this provider. This is an optional operation. If supported,
     * it should move the position of the provider to before the first
     * element. Non-random-access providers may be able to support this
     * method by re-acquiring all resources as if the result were just opened.
     */
    public void reset() throws Exception;

    /**
     * Any checked exceptions that are thrown will be passed to this method.
     * The provider should re-throw the exception as an appropriate unchecked
     * exception.
     */
    public void handleCheckedException(Exception e);
}
