package org.su18.serialize.ignite;

import org.apache.ignite.*;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.binary.BinaryCachingMetadataHandler;
import org.apache.ignite.internal.binary.BinaryContext;
import org.apache.ignite.internal.binary.BinaryMarshaller;
import org.apache.ignite.internal.binary.builder.BinaryObjectBuilderImpl;
import org.apache.ignite.internal.processors.cacheobject.NoOpBinary;
import org.apache.ignite.internal.processors.tracing.configuration.NoopTracingConfigurationManager;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteProductVersion;
import org.apache.ignite.logger.NullLogger;
import org.apache.ignite.marshaller.Marshaller;
import org.apache.ignite.plugin.IgnitePlugin;
import org.apache.ignite.plugin.PluginNotFoundException;
import org.apache.ignite.spi.tracing.TracingConfigurationManager;

import javax.management.MBeanServer;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

/**
 * @author su18
 */
public class IgniteMock implements Ignite {

	/**
	 * Ignite name
	 */
	private final String name;

	/**
	 * Local host.
	 */
	private final String locHost;

	/**
	 *
	 */
	private final UUID nodeId;

	/**
	 *
	 */
	private Marshaller marshaller;

	/**
	 *
	 */
	private final MBeanServer jmx;

	/**
	 *
	 */
	private final String home;

	/**
	 *
	 */
	private IgniteConfiguration staticCfg;

	/**
	 *
	 */
	private IgniteBinary binaryMock;

	/**
	 *
	 */
	private BinaryContext ctx;

	/**
	 * Mock values
	 *
	 * @param name       Name.
	 * @param locHost    Local host.
	 * @param nodeId     Node ID.
	 * @param marshaller Marshaller.
	 * @param jmx        Jmx Bean Server.
	 * @param home       Ignite home.
	 */
	public IgniteMock(
			String name, String locHost, UUID nodeId, Marshaller marshaller, MBeanServer jmx, String home, IgniteConfiguration staticCfg) {
		this.locHost = locHost;
		this.nodeId = nodeId;
		this.marshaller = marshaller;
		this.jmx = jmx;
		this.home = home;
		this.name = name;
		this.staticCfg = staticCfg;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteLogger log() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteConfiguration configuration() {
		if (staticCfg != null)
			return staticCfg;

		IgniteConfiguration cfg = new IgniteConfiguration();

		cfg.setMarshaller(marshaller);
		cfg.setNodeId(nodeId);
		cfg.setMBeanServer(jmx);
		cfg.setIgniteHome(home);
		cfg.setLocalHost(locHost);

		try {
			cfg.setWorkDirectory(U.defaultWorkDirectory());
		} catch (IgniteCheckedException e) {
			throw new IgniteException("Failed to get default work directory.", e);
		}

		return cfg;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteCluster cluster() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteCompute compute() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteCompute compute(ClusterGroup grp) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteMessaging message() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteMessaging message(ClusterGroup prj) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteEvents events() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteEvents events(ClusterGroup grp) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteServices services() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteServices services(ClusterGroup grp) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutorService executorService() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutorService executorService(ClusterGroup grp) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteProductVersion version() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteScheduler scheduler() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteCache<K, V> cache(String name) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> cacheNames() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteCache<K, V> createCache(CacheConfiguration<K, V> cacheCfg) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<IgniteCache> createCaches(Collection<CacheConfiguration> cacheCfgs) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteCache<K, V> getOrCreateCache(CacheConfiguration<K, V> cacheCfg) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteCache<K, V> createCache(CacheConfiguration<K, V> cacheCfg,
	                                            NearCacheConfiguration<K, V> nearCfg) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteCache<K, V> createNearCache(String cacheName, NearCacheConfiguration<K, V> nearCfg) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteCache<K, V> getOrCreateCache(CacheConfiguration<K, V> cacheCfg, NearCacheConfiguration<K, V> nearCfg) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteCache<K, V> getOrCreateNearCache(String cacheName, NearCacheConfiguration<K, V> nearCfg) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteCache<K, V> getOrCreateCache(String cacheName) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<IgniteCache> getOrCreateCaches(Collection<CacheConfiguration> cacheCfgs) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteCache<K, V> createCache(String cacheName) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> void addCacheConfiguration(CacheConfiguration<K, V> cacheCfg) {
		// No-op.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroyCache(String cacheName) {
		// No-op.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroyCaches(Collection<String> cacheNames) {
		// No-op.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteTransactions transactions() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K, V> IgniteDataStreamer<K, V> dataStreamer(String cacheName) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends IgnitePlugin> T plugin(String name) throws PluginNotFoundException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteBinary binary() {
		if (binaryMock != null)
			return binaryMock;

		if (ctx == null) {
			/** {@inheritDoc} */
			ctx = new BinaryContext(BinaryCachingMetadataHandler.create(), configuration(), new NullLogger()) {
				@Override
				public int typeId(String typeName) {
					return typeName.hashCode();
				}
			};

			if (marshaller instanceof BinaryMarshaller)
				ctx.configure((BinaryMarshaller) marshaller, configuration().getBinaryConfiguration());
		}

		binaryMock = new NoOpBinary() {
			/** {@inheritDoc} */
			@Override
			public int typeId(String typeName) {
				return typeName.hashCode();
			}

			/** {@inheritDoc} */
			@Override
			public BinaryObjectBuilder builder(String typeName) throws BinaryObjectException {
				return new BinaryObjectBuilderImpl(ctx, typeName);
			}
		};

		return binaryMock;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteAtomicSequence atomicSequence(String name, long initVal, boolean create) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteAtomicSequence atomicSequence(String name, AtomicConfiguration cfg, long initVal,
	                                           boolean create) throws IgniteException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteAtomicLong atomicLong(String name, long initVal, boolean create) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteAtomicLong atomicLong(String name, AtomicConfiguration cfg, long initVal,
	                                   boolean create) throws IgniteException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> IgniteAtomicReference<T> atomicReference(String name,
	                                                    T initVal,
	                                                    boolean create) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> IgniteAtomicReference<T> atomicReference(String name, AtomicConfiguration cfg,
	                                                    T initVal, boolean create) throws IgniteException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T, S> IgniteAtomicStamped<T, S> atomicStamped(String name,
	                                                      T initVal,
	                                                      S initStamp,
	                                                      boolean create) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T, S> IgniteAtomicStamped<T, S> atomicStamped(String name, AtomicConfiguration cfg,
	                                                      T initVal, S initStamp, boolean create) throws IgniteException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteCountDownLatch countDownLatch(String name,
	                                           int cnt,
	                                           boolean autoDel,
	                                           boolean create) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteSemaphore semaphore(String name,
	                                 int cnt,
	                                 boolean failoverSafe,
	                                 boolean create) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteLock reentrantLock(String name,
	                                boolean failoverSafe,
	                                boolean fair,
	                                boolean create) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> IgniteQueue<T> queue(String name,
	                                int cap,
	                                CollectionConfiguration cfg) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> IgniteSet<T> set(String name,
	                            CollectionConfiguration cfg) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <K> Affinity<K> affinity(String cacheName) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean active() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void active(boolean active) {
		// No-op.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resetLostPartitions(Collection<String> cacheNames) {
		// No-op.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<DataRegionMetrics> dataRegionMetrics() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRegionMetrics dataRegionMetrics(String memPlcName) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataStorageMetrics dataStorageMetrics() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteEncryption encryption() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IgniteSnapshot snapshot() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TracingConfigurationManager tracingConfiguration() {
		return NoopTracingConfigurationManager.INSTANCE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<MemoryMetrics> memoryMetrics() {
		return DataRegionMetricsAdapter.collectionOf(dataRegionMetrics());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MemoryMetrics memoryMetrics(String memPlcName) {
		return DataRegionMetricsAdapter.valueOf(dataRegionMetrics(memPlcName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistenceMetrics persistentStoreMetrics() {
		return DataStorageMetricsAdapter.valueOf(dataStorageMetrics());
	}

	/**
	 * @param staticCfg Configuration.
	 */
	public void setStaticCfg(IgniteConfiguration staticCfg) {
		this.staticCfg = staticCfg;
	}
}

