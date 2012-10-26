package to.etc.log;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import org.slf4j.*;
import org.slf4j.spi.*;

import to.etc.log.MyLogger.Level;

public class MyLoggerFactory implements ILoggerFactory {

	static class Config {
		/**
		 * Root dir for cofig.
		 */
		private File	m_rootDir;

		/**
		 * Root dir for all 'per logger' log files created.
		 */
		private File	m_logDir;

		/**
		 * Default logger level.
		 */
		private Level	m_level		= DEFAULT_LOG_LEVEL;

		/**
		 * Questionable if needed? Disables all logging!?.
		 */
		private boolean	m_disabled	= false;

		File getRootDir() {
			return m_rootDir;
		}

		void setRootDir(File rootDir) {
			m_rootDir = rootDir;
		}

		File getLogDir() {
			return m_logDir;
		}

		void setLogDir(File logDir) {
			m_logDir = logDir;
		}

		Level getLevel() {
			return m_level;
		}

		void setLevel(Level level) {
			m_level = level;
		}

		boolean isDisabled() {
			return m_disabled;
		}

		void setDisabled(boolean disabled) {
			m_disabled = disabled;
		}
	}

	/**
	 * Contains Logger instances.
	 */
	private static final Map<String, MyLogger>				LOGGERS					= new HashMap<String, MyLogger>();

	/**
	 * Contains definitions of output files per logger, only for non default settings.
	 */
	private static final Map<String, String>				OUTS					= new HashMap<String, String>();

	/**
	 * Contains set of disabled loggers.
	 */
	private static final Set<String>						DISABLED				= new HashSet<String>();

	/**
	 * Contains enabled markers definitions.
	 */
	private static final Map<String, Map<Marker, Level>>	MARKERS					= new HashMap<String, Map<Marker, Level>>();

	/**
	 * Contains definitions of log levels per logger, only for non default settings.
	 */
	private static final Map<String, Level>					LEVELS					= new HashMap<String, Level>();

	private static final String								DEFAULT_LOG_DIR			= "log";

	private static final Level								DEFAULT_LOG_LEVEL		= Level.WARN;

	private static final String								CONF_OUTS_FILENAME		= "logconf.outs";

	private static final String								CONF_DISABLED_FILENAME	= "logconf.disabled";

	private static final String								CONF_LEVELS_FILENAME	= "logconf.levels";

	private static final String								CONF_MARKERS_FILENAME	= "logconf.markers";

	private static final String								CONF_ROOT_FILENAME		= "logconf.rootconf";

	private static final Config								m_rootConfig			= new Config();

	private static final MyMarkerFactory					m_markerFactory			= new MyMarkerFactory();

	@Override
	public Logger getLogger(@Nonnull String key) {
		return get(key);
	}

	private static MyLogger get(@Nonnull String key) {
		MyLogger logger = null;
		synchronized(LOGGERS) {
			logger = LOGGERS.get(key);
			if(logger == null) {
				logger = MyLogger.create(key, getOut(key), m_rootConfig);
				LOGGERS.put(key, logger);
			}
		}
		synchronized(DISABLED) {
			if(DISABLED.contains(key)) {
				logger.setDisabled(true);
			}
		}
		synchronized(LEVELS) {
			if(LEVELS.containsKey(key)) {
				logger.setLevel(LEVELS.get(key));
			}
		}
		synchronized(MARKERS) {
			if(MARKERS.containsKey(key)) {
				Map<Marker, Level> map = MARKERS.get(key);
				for(Marker marker : map.keySet()) {
					logger.addMarker(marker, map.get(marker));
				}
			}
		}
		return logger;
	}

	private static String getOut(@Nonnull String key) {
		synchronized(OUTS) {
			return OUTS.get(key);
		}
	}

	static boolean isDisabled(@Nonnull String key) {
		synchronized(DISABLED) {
			return DISABLED.contains(key);
		}
	}

	public static void setDisabled(@Nonnull String key, boolean disabled) {
		synchronized(DISABLED) {
			if(!disabled) {
				DISABLED.remove(key);
			} else {
				DISABLED.add(key);
			}
		}
		synchronized(LOGGERS) {
			MyLogger logger = LOGGERS.get(key);
			if(logger != null) {
				logger.setDisabled(disabled);
			}
		}
	}

	private static void loadRootConfig(@Nonnull File file) throws Exception {
		synchronized(m_rootConfig) {
			Properties prop = new Properties();
			InputStream is = null;
			m_rootConfig.setRootDir(file);
			try {
				is = createInputStreamIfExists(new File(file, CONF_ROOT_FILENAME));
				if(is != null) {
					prop.load(is);
					m_rootConfig.setLogDir(new File(file, DEFAULT_LOG_DIR));
					if(prop.containsKey("dir")) {
						String location = prop.getProperty("dir");
						if(location.startsWith("/")) {
							m_rootConfig.setLogDir(new File(file, location.substring(1)));
						} else {
							m_rootConfig.setLogDir(new File(location));
						}
					} else {
						m_rootConfig.setLogDir(new File(file, DEFAULT_LOG_DIR));
					}
					if(prop.containsKey("disabled")) {
						m_rootConfig.setDisabled("true".equalsIgnoreCase(prop.getProperty("disabled", m_rootConfig.isDisabled() ? "true" : "false")));
					}
					if(prop.containsKey("level")) {
						String level = prop.getProperty("level", m_rootConfig.getLevel().name());
						m_rootConfig.setLevel(Level.valueOf(level));
					}
				} else {
					m_rootConfig.setLogDir(new File(file, DEFAULT_LOG_DIR));
				}
			} finally {
				try {
					if(is != null) {
						is.close();
					}
				} catch(Exception x) {}
			}
		}
	}

	private static @Nullable
	InputStream createInputStreamIfExists(File file) throws FileNotFoundException {
		if(file.exists()) {
			return new FileInputStream(file);
		}
		return null;
	}

	private static @Nullable
	BufferedReader createBufferedReaderIfExists(File file) throws FileNotFoundException {
		if(file.exists()) {
			return new BufferedReader(new FileReader(file));
		}
		return null;
	}

	private static @Nonnull
	FileOutputStream createFileOutputStream(@Nonnull File file) throws IOException {
		file.getParentFile().mkdirs();
		file.createNewFile();
		return new FileOutputStream(file);
	}

	private static @Nonnull
	BufferedWriter createBufferedWritter(@Nonnull File file) throws IOException {
		file.getParentFile().mkdirs();
		file.createNewFile();
		return new BufferedWriter(new FileWriter(file));
	}

	private static void saveRootConfig() throws Exception {
		synchronized(m_rootConfig) {
			OutputStream os = null;
			try {
				os = createFileOutputStream(new File(m_rootConfig.getRootDir(), CONF_ROOT_FILENAME));
				Properties prop = new Properties();
				prop.setProperty("dir", m_rootConfig.getLogDir().getAbsolutePath());
				if(m_rootConfig.isDisabled()) {
					prop.setProperty("disabled", "true");
				}
				if(DEFAULT_LOG_LEVEL != m_rootConfig.getLevel()) {
					prop.setProperty("level", m_rootConfig.getLevel().name());
				}
				prop.store(os, null);
			} finally {
				try {
					if(os != null) {
						os.close();
					}
				} catch(Exception x) {}
			}
		}
	}

	private static void saveOutsConfig() throws Exception {
		synchronized(OUTS) {
			BufferedWriter w = createBufferedWritter(new File(m_rootConfig.getRootDir(), CONF_OUTS_FILENAME));
			try {
				for(String key : OUTS.keySet()) {
					String outLogFile = OUTS.get(key);
					if(null != outLogFile && outLogFile.length() > 0) {
						w.write(key + "|" + outLogFile);
						w.newLine();
					}
				}
			} finally {
				try {
					w.close();
				} catch(Exception x) {}
			}
		}
	}

	private static void loadOutsConfig(@Nonnull File file) throws Exception {
		synchronized(OUTS) {
			BufferedReader r = createBufferedReaderIfExists(new File(file, CONF_OUTS_FILENAME));
			if(r == null) {
				return;
			}
			try {
				String line = null;
				while((line = r.readLine()) != null) {
					String[] parts = line.split("\\|");
					if(parts.length == 2) {
						OUTS.put(parts[0], parts[1]);
					}
				}
			} finally {
				try {
					r.close();
				} catch(Exception x) {}
			}
		}
	}

	private static void saveDisabledConfig() throws Exception {
		synchronized(DISABLED) {
			BufferedWriter w = createBufferedWritter(new File(m_rootConfig.getRootDir(), CONF_DISABLED_FILENAME));
			try {
				Iterator<String> i = DISABLED.iterator();
				while(i.hasNext()) {
					w.write(i.next());
					w.newLine();
				}
			} finally {
				try {
					w.close();
				} catch(Exception x) {}
			}
		}
	}

	private static void loadDisabledConfig(@Nonnull File file) throws Exception {
		synchronized(DISABLED) {
			BufferedReader r = createBufferedReaderIfExists(new File(file, CONF_DISABLED_FILENAME));
			if(r == null) {
				return;
			}
			try {
				String line = null;
				while((line = r.readLine()) != null) {
					DISABLED.add(line);
				}
			} finally {
				try {
					r.close();
				} catch(Exception x) {}
			}
		}
	}

	private static void saveLevelsConfig() throws Exception {
		synchronized(LEVELS) {
			BufferedWriter w = createBufferedWritter(new File(m_rootConfig.getRootDir(), CONF_LEVELS_FILENAME));
			try {
				for(String key : LEVELS.keySet()) {
					w.write(key + "|" + LEVELS.get(key).name());
					w.newLine();
				}
			} finally {
				try {
					w.close();
				} catch(Exception x) {}
			}
		}
	}

	private static void loadLevelsConfig(@Nonnull File file) throws Exception {
		synchronized(LEVELS) {
			BufferedReader r = createBufferedReaderIfExists(new File(file, CONF_LEVELS_FILENAME));
			if(r == null) {
				return;
			}
			try {
				String line = null;
				while((line = r.readLine()) != null) {
					String[] parts = line.split("\\|");
					if(parts.length == 2) {
						String key = parts[0];
						Level level = Level.valueOf(parts[1]);
						if(level != m_rootConfig.getLevel()) {
							LEVELS.put(key, level);
						}
					}
				}
			} finally {
				try {
					r.close();
				} catch(Exception x) {}
			}
		}
	}

	private static void saveMarkersConfig() throws Exception {
		synchronized(MARKERS) {
			BufferedWriter w = createBufferedWritter(new File(m_rootConfig.getRootDir(), CONF_MARKERS_FILENAME));
			try {
				for(String key : MARKERS.keySet()) {
					StringBuilder sb = new StringBuilder();
					sb.append(key).append("|");
					Map<Marker, Level> map = MARKERS.get(key);
					boolean first = true;
					for(Marker marker : map.keySet()) {
						if(first) {
							first = false;
						} else {
							sb.append(",");
						}
						sb.append(marker.getName()).append("-").append(map.get(marker).name());
					}
					w.write(sb.toString());
					w.newLine();
				}
			} finally {
				try {
					w.close();
				} catch(Exception x) {}
			}
		}
	}

	private static void loadMarkersConfig(@Nonnull File file) throws Exception {
		synchronized(MARKERS) {
			BufferedReader r = createBufferedReaderIfExists(new File(file, CONF_MARKERS_FILENAME));
			if(r == null) {
				return;
			}
			try {
				String line = null;
				while((line = r.readLine()) != null) {
					String[] parts = line.split("\\|");
					if(parts.length == 2) {
						String key = parts[0];
						Map<Marker, Level> map = Collections.EMPTY_MAP;
						String[] subparts = parts[1].split(",");
						for(String sub : subparts) {
							String[] subsub = sub.split("-");
							if(subsub.length == 2) {
								Marker marker = m_markerFactory.getMarker(subsub[0]);
								Level level = Level.valueOf(subsub[1]);
								if(marker != null) {
									if(map == Collections.EMPTY_MAP) {
										map = new HashMap<Marker, Level>();
									}
									map.put(marker, level);
								}
							}
						}
						if(!map.isEmpty()) {
							MARKERS.put(key, map);
						}
					}
				}
			} finally {
				try {
					r.close();
				} catch(Exception x) {}
			}
		}
	}

	public static void setOut(@Nonnull String key, @Nullable String outLogFile) {
		synchronized(OUTS) {
			if(outLogFile == null) {
				OUTS.remove(key);
			} else {
				OUTS.put(key, outLogFile);
			}
		}
		synchronized(LOGGERS) {
			MyLogger logger = LOGGERS.get(key);
			if(logger != null) {
				logger.setOut(outLogFile);
			}
		}
	}

	/**
	 * Loads logging configuration from specified destination - should be executed early as possible, and only once. 
	 * @param rootConfDir
	 * @throws Exception
	 */
	public static void loadConfig(File rootConfDir) throws Exception {
		MarkerFactoryBinder binder = new MarkerFactoryBinder() {
			IMarkerFactory	m_mfactory;

			@Override
			public IMarkerFactory getMarkerFactory() {
				final Map<String, Marker> markers = new HashMap<String, Marker>();
				final Map<String, Marker> deatached = new HashMap<String, Marker>();
				if(m_mfactory == null) {
					m_mfactory = new IMarkerFactory() {

						@Override
						public boolean detachMarker(String arg0) {
							synchronized(markers) {
								Marker m = markers.remove(arg0);
								if(m != null) {
									deatached.put(arg0, m);
								}
								return true;
							}
						}

						@Override
						public boolean exists(String arg0) {
							synchronized(markers) {
								return markers.containsKey(arg0);
							}
						}

						@Override
						public Marker getDetachedMarker(String arg0) {
							synchronized(deatached) {
								Marker deatachedm = deatached.get(arg0);
								return deatachedm;
							}
						}

						@Override
						public Marker getMarker(final String arg0) {
							synchronized(markers) {
								Marker m = markers.get(arg0);
								if(m != null) {
									return m;
								}
							}
							Marker dm = null;
							synchronized(deatached) {
								dm = deatached.get(arg0);
								if(dm != null) {
									deatached.remove(dm);
								}
							}

							if(dm != null) {
								synchronized(markers) {
									markers.put(arg0, dm);
									return dm;
								}
							} else {
								Marker nm = new Marker() {

									@Override
									public void add(Marker arg0) {
										// TODO Auto-generated method stub

									}

									@Override
									public boolean contains(Marker arg0) {
										// TODO Auto-generated method stub
										return false;
									}

									@Override
									public boolean contains(String arg0) {
										// TODO Auto-generated method stub
										return false;
									}

									@Override
									public String getName() {
										return arg0;
									}

									@Override
									public boolean hasChildren() {
										// TODO Auto-generated method stub
										return false;
									}

									@Override
									public boolean hasReferences() {
										// TODO Auto-generated method stub
										return false;
									}

									@Override
									public Iterator iterator() {
										// TODO Auto-generated method stub
										return null;
									}

									@Override
									public boolean remove(Marker arg0) {
										// TODO Auto-generated method stub
										return false;
									}
								};
								synchronized(markers) {
									markers.put(arg0, nm);
									return nm;
								}
							}
						}
					};
				}
				return m_mfactory;
			}

			@Override
			public String getMarkerFactoryClassStr() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		rootConfDir.mkdirs();
		loadRootConfig(rootConfDir);
		loadOutsConfig(rootConfDir);
		loadDisabledConfig(rootConfDir);
		loadLevelsConfig(rootConfDir);
		loadMarkersConfig(rootConfDir);
	}

	/**
	 * Saves current logging configuration. 
	 * @throws Exception
	 */
	public static void save() throws Exception {
		saveRootConfig();
		saveOutsConfig();
		saveDisabledConfig();
		saveLevelsConfig();
		saveMarkersConfig();
	}

	public static void addMarker(@Nonnull String key, @Nonnull Marker marker, @Nonnull Level level) {
		synchronized(MARKERS) {
			Map<Marker, Level> map = MARKERS.get(key);
			if(map == null) {
				map = new HashMap<Marker, Level>();
				MARKERS.put(key, map);
			}
			map.put(marker, level);
		}
		synchronized(LOGGERS) {
			MyLogger logger = LOGGERS.get(key);
			if(logger != null) {
				logger.addMarker(marker, level);
			}
		}
	}

	public static void removeMarker(@Nonnull String key, @Nonnull Marker marker) {
		synchronized(MARKERS) {
			Map<Marker, Level> map = MARKERS.get(key);
			if(map != null) {
				map.remove(marker);
			}
			if(map.isEmpty()) {
				MARKERS.remove(key);
			}
		}
		synchronized(LOGGERS) {
			MyLogger logger = LOGGERS.get(key);
			if(logger != null) {
				logger.removeMarker(marker);
			}
		}
	}

	public static void setLevel(@Nonnull String key, @Nonnull Level level) {
		synchronized(LEVELS) {
			Level storedlevel = LEVELS.get(key);
			if(storedlevel != level) {
				if(level == m_rootConfig.getLevel()) {
					LEVELS.remove(key);
				} else {
					LEVELS.put(key, level);
				}
			}
		}
		synchronized(LOGGERS) {
			MyLogger logger = LOGGERS.get(key);
			if(logger != null) {
				logger.setLevel(level);
			}
		}
	}

	/**
	 * Returns copy of map that defines specific output files per logger name. Logger name is key, output file is value.
	 * @return
	 */
	public static @Nonnull
	Map<String, String> getLoggerOutsDef() {
		synchronized(OUTS) {
			return new HashMap<String, String>(OUTS);
		}
	}
}
