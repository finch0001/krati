package krati.sos;

import java.io.IOException;

import krati.store.DataStore;

/**
 * A key-value store for serializable objects. The store requires that both key and value be serializable objects.
 * 
 * This class is not thread-safe by design. It is expected that the conditions below hold within one JVM.
 * <pre>
 *    1. There is one and only one instance of SerializableObjectStore for a given data store.
 *    2. There is one and only one thread is calling put and delete methods at any given time. 
 * </pre>
 * 
 * It is expected that this class is used in the case of multiple readers and single writer.
 * 
 * @author jwu
 *
 * @param <K> Key (serializable object)
 * @param <V> Value (serializable object)
 */
public class SerializableObjectStore<K, V> implements ObjectStore<K, V>
{
    protected final DataStore<byte[], byte[]> _store;
    protected final ObjectSerializer<K> _keySerializer;
    protected final ObjectSerializer<V> _valSerializer;
    
    /**
     * Constructs a key-value store for serializable objects.
     * 
     * @param store          the underlying data store for serializable objects.
     * @param keySerializer  the object serializer to serialize/de-serialize keys.
     * @param valSerializer  the object serializer to serialize/de-serialize values.
     */
    public SerializableObjectStore(DataStore<byte[], byte[]> store,
                                   ObjectSerializer<K> keySerializer,
                                   ObjectSerializer<V> valSerializer)
    {
        this._store = store;
        this._keySerializer = keySerializer;
        this._valSerializer = valSerializer;
    }
    
    /**
     * @return the underlying content data store.
     */
    protected DataStore<byte[], byte[]> getContentStore()
    {
        return _store;
    }
    
    /**
     * @return the key serializer.
     */
    public ObjectSerializer<K> getKeySerializer()
    {
        return _keySerializer;
    }
    
    /**
     * @return the value serializer.
     */
    public ObjectSerializer<V> getValueSerializer()
    {
        return _valSerializer;
    }
    
    /**
     * Gets an object based on its key from the store.
     * 
     * @param key  the retrieving key for a serializable object in the store. 
     * @return the retrieved object.
     */
    @Override
    public V get(K key)
    {
        return getValueSerializer().construct(_store.get(getKeySerializer().serialize(key)));
    }
    
    /**
     * Gets an object in the form of byte array from the store.
     * 
     * @param key  the retrieving key. 
     * @return the retrieved object in raw bytes.
     */
    @Override
    public byte[] getBytes(K key)
    {
        return _store.get(getKeySerializer().serialize(key));
    }
    
    /**
     * Gets an object in the form of byte array from the store.
     * 
     * @param key  the retrieving key in raw bytes. 
     * @return the retrieved object in raw bytes.
     */
    @Override
    public byte[] getBytes(byte[] keyBytes)
    {
        return _store.get(keyBytes);
    }
    
    /**
     * Puts an serializable object into the store.
     * 
     * @param key    the object key.
     * @param value  the serializable object.
     * @return true if the put operation is succeeds.  
     * @throws Exception
     */
    @Override
    public boolean put(K key, V value) throws Exception
    {
        return _store.put(getKeySerializer().serialize(key), getValueSerializer().serialize(value));
    }
    
    /**
     * Deletes an object from the store based on its key.
     * 
     * @param key   the object key.
     * @return true if the delete operation succeeds.
     * @throws Exception
     */
    @Override
    public boolean delete(K key) throws Exception
    {
        return _store.delete(getKeySerializer().serialize(key));
    }
    
    @Override
    public void sync() throws IOException
    {
        synchronized(_store)
        {
            _store.sync();
        }
    }
    
    /**
     * Persists this object store.
     * 
     * @throws IOException
     */
    @Override
    public void persist() throws IOException
    {
        synchronized(_store)
        {
            _store.persist();
        }
    }
    
    /**
     * Clears this object store by removing all the persisted data permanently.
     * 
     * @throws IOException
     */
    public void clear() throws IOException
    {
        synchronized(_store)
        {
            _store.clear();
        }
    }
}
