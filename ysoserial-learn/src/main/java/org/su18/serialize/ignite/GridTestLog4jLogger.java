package org.su18.serialize.ignite;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.internal.util.GridConcurrentHashSet;
import org.apache.ignite.internal.util.tostring.GridToStringExclude;
import org.apache.ignite.internal.util.typedef.C1;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.internal.A;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.logger.LoggerNodeIdAware;
import org.apache.log4j.*;
import org.apache.log4j.varia.LevelRangeFilter;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.UUID;

import static org.apache.ignite.IgniteSystemProperties.IGNITE_CONSOLE_APPENDER;
import static org.apache.ignite.IgniteSystemProperties.IGNITE_QUIET;

/**
 * @author su18
 */
public class GridTestLog4jLogger implements IgniteLogger, LoggerNodeIdAware {
	/** Appenders. */
	private static Collection<FileAppender> fileAppenders = new GridConcurrentHashSet<>();

	/** */
	private static volatile boolean inited;

	/** */
	private static volatile boolean quiet0;

	/** */
	private static final Object mux = new Object();

	/** Logger implementation. */
	@GridToStringExclude
	@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
	private Logger impl;

	/** Path to configuration file. */
	@GridToStringExclude
	private final String cfg;

	/** Quiet flag. */
	private final boolean quiet;

	/** Node ID. */
	@GridToStringExclude
	private UUID nodeId;

	/**
	 * Creates new logger and automatically detects if root logger already
	 * has appenders configured. If it does not, the root logger will be
	 * configured with default appender (analogous to calling
	 * {@link #GridTestLog4jLogger(boolean) Log4jLogger(boolean)}
	 * with parameter {@code true}, otherwise, existing appenders will be used (analogous
	 * to calling {@link #GridTestLog4jLogger(boolean) Log4jLogger(boolean)}
	 * with parameter {@code false}).
	 */
	public GridTestLog4jLogger() {
		this(!isConfigured());
	}

	/**
	 * Creates new logger. If initialize parameter is {@code true} the Log4j
	 * logger will be initialized with default console appender and {@code INFO}
	 * log level.
	 *
	 * @param init If {@code true}, then a default console appender with
	 *      following pattern layout will be created: {@code %d{ISO8601} %-5p [%c{1}] %m%n}.
	 *      If {@code false}, then no implicit initialization will take place,
	 *      and {@code Log4j} should be configured prior to calling this
	 *      constructor.
	 */
	public GridTestLog4jLogger(boolean init) {
		impl = Logger.getRootLogger();

		if (init) {
			// Implementation has already been inited, passing NULL.
			addConsoleAppenderIfNeeded(Level.INFO, null);

			quiet = quiet0;
		}
		else
			quiet = true;

		cfg = null;
	}

	/**
	 * Creates new logger with given implementation.
	 *
	 * @param impl Log4j implementation to use.
	 */
	protected GridTestLog4jLogger(final Logger impl) {
		assert impl != null;

		addConsoleAppenderIfNeeded(null, new C1<Boolean, Logger>() {
			@Override public Logger apply(Boolean init) {
				return impl;
			}
		});

		quiet = quiet0;
		cfg = null;
	}

	/**
	 * Creates new logger with given configuration {@code path}.
	 *
	 * @param path Path to log4j configuration XML file.
	 * @throws IgniteCheckedException Thrown in case logger can't be created.
	 */
	public GridTestLog4jLogger(String path) throws IgniteCheckedException {
		if (path == null)
			throw new IgniteCheckedException("Configuration XML file for Log4j must be specified.");

		this.cfg = path;

		final URL cfgUrl = U.resolveIgniteUrl(path);

		if (cfgUrl == null)
			throw new IgniteCheckedException("Log4j configuration path was not found: " + path);

		addConsoleAppenderIfNeeded(null, new C1<Boolean, Logger>() {
			@Override public Logger apply(Boolean init) {
				if (init)
					DOMConfigurator.configure(cfgUrl);

				return Logger.getRootLogger();
			}
		});

		quiet = quiet0;
	}

	/**
	 * Creates new logger with given configuration {@code cfgFile}.
	 *
	 * @param cfgFile Log4j configuration XML file.
	 * @throws IgniteCheckedException Thrown in case logger can't be created.
	 */
	public GridTestLog4jLogger(File cfgFile) throws IgniteCheckedException {
		if (cfgFile == null)
			throw new IgniteCheckedException("Configuration XML file for Log4j must be specified.");

		if (!cfgFile.exists() || cfgFile.isDirectory())
			throw new IgniteCheckedException("Log4j configuration path was not found or is a directory: " + cfgFile);

		cfg = cfgFile.getAbsolutePath();

		addConsoleAppenderIfNeeded(null, new C1<Boolean, Logger>() {
			@Override public Logger apply(Boolean init) {
				if (init)
					DOMConfigurator.configure(cfg);

				return Logger.getRootLogger();
			}
		});

		quiet = quiet0;
	}

	/**
	 * Creates new logger with given configuration {@code cfgUrl}.
	 *
	 * @param cfgUrl URL for Log4j configuration XML file.
	 * @throws IgniteCheckedException Thrown in case logger can't be created.
	 */
	public GridTestLog4jLogger(final URL cfgUrl) throws IgniteCheckedException {
		if (cfgUrl == null)
			throw new IgniteCheckedException("Configuration XML file for Log4j must be specified.");

		cfg = cfgUrl.getPath();

		addConsoleAppenderIfNeeded(null, new C1<Boolean, Logger>() {
			@Override public Logger apply(Boolean init) {
				if (init)
					DOMConfigurator.configure(cfgUrl);

				return Logger.getRootLogger();
			}
		});

		quiet = quiet0;
	}

	/**
	 * Checks if Log4j is already configured within this VM or not.
	 *
	 * @return {@code True} if log4j was already configured, {@code false} otherwise.
	 */
	public static boolean isConfigured() {
		return Logger.getRootLogger().getAllAppenders().hasMoreElements();
	}

	/**
	 * Sets level for internal log4j implementation.
	 *
	 * @param level Log level to set.
	 */
	public void setLevel(Level level) {
		impl.setLevel(level);
	}

	/** {@inheritDoc} */
	@Override public String fileName() {
		FileAppender fapp = F.first(fileAppenders);

		return fapp != null ? fapp.getFile() : null;
	}

	/**
	 * Adds console appender when needed with some default logging settings.
	 *
	 * @param logLevel Optional log level.
	 * @param implInitC Optional log implementation init closure.
	 */
	private void addConsoleAppenderIfNeeded(Level logLevel,
	                                        IgniteClosure<Boolean, Logger> implInitC) {
		if (inited) {
			if (implInitC != null)
				// Do not init.
				impl = implInitC.apply(false);

			return;
		}

		synchronized (mux) {
			if (inited) {
				if (implInitC != null)
					// Do not init.
					impl = implInitC.apply(false);

				return;
			}

			if (implInitC != null)
				// Init logger impl.
				impl = implInitC.apply(true);

			boolean quiet = Boolean.valueOf(System.getProperty(IGNITE_QUIET, "true"));

			boolean consoleAppenderFound = false;
			Category        rootCategory = null;
			ConsoleAppender errAppender  = null;

			for (Category l = impl; l != null; ) {
				if (!consoleAppenderFound) {
					for (Enumeration appenders = l.getAllAppenders(); appenders.hasMoreElements(); ) {
						Appender appender = (Appender)appenders.nextElement();

						if (appender instanceof ConsoleAppender) {
							if ("CONSOLE_ERR".equals(appender.getName())) {
								// Treat CONSOLE_ERR appender as a system one and don't count it.
								errAppender = (ConsoleAppender)appender;

								continue;
							}

							consoleAppenderFound = true;

							break;
						}
					}
				}

				if (l.getParent() == null) {
					rootCategory = l;

					break;
				}
				else
					l = l.getParent();
			}

			if (consoleAppenderFound && quiet)
				// User configured console appender, but log is quiet.
				quiet = false;

			if (!consoleAppenderFound && !quiet && Boolean.valueOf(System.getProperty(IGNITE_CONSOLE_APPENDER, "true"))) {
				// Console appender not found => we've looked through all categories up to root.
				assert rootCategory != null;

				// User launched ignite in verbose mode and did not add console appender with INFO level
				// to configuration and did not set IGNITE_CONSOLE_APPENDER to false.
				if (errAppender != null) {
					rootCategory.addAppender(createConsoleAppender(Level.INFO));

					if (errAppender.getThreshold() == Level.ERROR)
						errAppender.setThreshold(Level.WARN);
				}
				else
					// No error console appender => create console appender with no level limit.
					rootCategory.addAppender(createConsoleAppender(Level.OFF));

				if (logLevel != null)
					impl.setLevel(logLevel);
			}

			quiet0 = quiet;
			inited = true;
		}
	}

	/**
	 * Creates console appender with some reasonable default logging settings.
	 *
	 * @param maxLevel Max logging level.
	 * @return New console appender.
	 */
	private Appender createConsoleAppender(Level maxLevel) {
		String fmt = "[%d{ISO8601}][%-5p][%t][%c{1}] %m%n";

		// Configure output that should go to System.out
		Appender app = new ConsoleAppender(new PatternLayout(fmt), ConsoleAppender.SYSTEM_OUT);

		LevelRangeFilter lvlFilter = new LevelRangeFilter();

		lvlFilter.setLevelMin(Level.TRACE);
		lvlFilter.setLevelMax(maxLevel);

		app.addFilter(lvlFilter);

		return app;
	}

	/**
	 * Adds file appender.
	 *
	 * @param a Appender.
	 */
	public static void addAppender(FileAppender a) {
		A.notNull(a, "a");

		fileAppenders.add(a);
	}

	/**
	 * Removes file appender.
	 *
	 * @param a Appender.
	 */
	public static void removeAppender(FileAppender a) {
		A.notNull(a, "a");

		fileAppenders.remove(a);
	}

	/** {@inheritDoc} */
	@Override public void setNodeId(UUID nodeId) {
		A.notNull(nodeId, "nodeId");

		this.nodeId = nodeId;

		for (FileAppender a : fileAppenders) {
			if (a instanceof LoggerNodeIdAware) {
				((LoggerNodeIdAware)a).setNodeId(nodeId);

				a.activateOptions();
			}
		}
	}

	/** {@inheritDoc} */
	@Override public UUID getNodeId() {
		return nodeId;
	}

	/**
	 * Gets files for all registered file appenders.
	 *
	 * @return List of files.
	 */
	public static Collection<String> logFiles() {
		Collection<String> res = new ArrayList<>(fileAppenders.size());

		for (FileAppender a : fileAppenders)
			res.add(a.getFile());

		return res;
	}

	/**
	 * Gets {@link org.apache.ignite.IgniteLogger} wrapper around log4j logger for the given
	 * category. If category is {@code null}, then root logger is returned. If
	 * category is an instance of {@link Class} then {@code (Class)ctgr).getName()}
	 * is used as category name.
	 *
	 * @param ctgr {@inheritDoc}
	 * @return {@link org.apache.ignite.IgniteLogger} wrapper around log4j logger.
	 */
	@Override public GridTestLog4jLogger getLogger(Object ctgr) {
		return new GridTestLog4jLogger(ctgr == null ? Logger.getRootLogger() :
				ctgr instanceof Class ? Logger.getLogger(((Class<?>)ctgr).getName()) :
						Logger.getLogger(ctgr.toString()));
	}

	/** {@inheritDoc} */
	@Override public void trace(String msg) {
		if (!impl.isTraceEnabled())
			warning("Logging at TRACE level without checking if TRACE level is enabled: " + msg);

		assert impl.isTraceEnabled() : "Logging at TRACE level without checking if TRACE level is enabled: " + msg;

		impl.trace(msg);
	}

	/** {@inheritDoc} */
	@Override public void debug(String msg) {
		if (!impl.isDebugEnabled())
			warning("Logging at DEBUG level without checking if DEBUG level is enabled: " + msg);

		assert impl.isDebugEnabled() : "Logging at DEBUG level without checking if DEBUG level is enabled: " + msg;

		impl.debug(msg);
	}

	/** {@inheritDoc} */
	@Override public void info(String msg) {
		if (!impl.isInfoEnabled())
			warning("Logging at INFO level without checking if INFO level is enabled: " + msg);

		assert impl.isInfoEnabled() : "Logging at INFO level without checking if INFO level is enabled: " + msg;

		impl.info(msg);
	}

	/** {@inheritDoc} */
	@Override public void warning(String msg) {
		impl.warn(msg);
	}

	/** {@inheritDoc} */
	@Override public void warning(String msg, Throwable e) {
		impl.warn(msg, e);
	}

	/** {@inheritDoc} */
	@Override public void error(String msg) {
		impl.error(msg);
	}

	/** {@inheritDoc} */
	@Override public void error(String msg, Throwable e) {
		impl.error(msg, e);
	}

	/** {@inheritDoc} */
	@Override public boolean isTraceEnabled() {
		return impl.isTraceEnabled();
	}

	/** {@inheritDoc} */
	@Override public boolean isDebugEnabled() {
		return impl.isDebugEnabled();
	}

	/** {@inheritDoc} */
	@Override public boolean isInfoEnabled() {
		return impl.isInfoEnabled();
	}

	/** {@inheritDoc} */
	@Override public boolean isQuiet() {
		return quiet;
	}

	/** {@inheritDoc} */
	@Override public String toString() {
		return S.toString(GridTestLog4jLogger.class, this, "config", cfg);
	}
}
