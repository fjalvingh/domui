package to.etc.util;

import org.apache.commons.lang3.time.FastDateFormat;
import org.eclipse.jdt.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Handy util to do profiling on duration spent in certain code blocks.
 * Call logInit to initialize in memory logging that is stored in thread locale.
 * Call logStart with given string key-name on start of code block to measure. Call logStop with given same key-name at end of code block, to stop measuring.
 * It would lof start time plus duration kept in precise nano seconds. It also allows for easy measuring in nested code paths as loops, it automatically
 * keeps records on each separate run and also produces an overall record for given key-name.
 *
 * Example (with given 'test overall duration' key around loop, and 'test iteration duration' key inside loop:
 * test overall duration started at 13:29:26.743=157
 * test iteration duration (total count 3) started at 13:29:26.759=41
 * test iteration duration_#1 started at 13:29:26.759=11
 * test iteration duration_#2 started at 13:29:26.774=15
 * test iteration duration_#3 started at 13:29:26.790=15
 *
 * To get the Map with durations report, call report method at the end, with specified time unit for reporting. That also unregister logging from thread locale
 * automatically.
 *
 * If initLog is not called, called log methods are ignored, and report returns null result.
 */
public final class DiagnosticUtil {

	private static ThreadLocal<Map<String, Object>> diagnostic = new ThreadLocal<>();
	private static ThreadLocal<Boolean> diagnosticEnabled = new ThreadLocal<>();

	private static final String SUB_MAP = "_subMap";

	@NonNull
	private static final FastDateFormat DF = FastDateFormat.getInstance("HH:mm:ss.SSS");

	private DiagnosticUtil() {}

	private static Map<String, Object> getMap() {
		Map<String, Object> map = diagnostic.get();
		if (null == map) {
			map = new HashMap<>();
			diagnostic.set(map);
		}
		return map;
	}

	public static void logInit() {
		diagnosticEnabled.set(Boolean.TRUE);
		clear();
	}

	public static void logStart(String key) {
		if (null == diagnosticEnabled.get()) {
			return;
		}

		Map<String, Object> map = getMap();
		Pair<Long, Long> previousLoggedSingleItem = (Pair<Long, Long>) map.get(key);
		if (null != previousLoggedSingleItem) {
			Map<String, Pair<Long, Long>> submap = (Map<String, Pair<Long, Long>>) map.computeIfAbsent(key + SUB_MAP, akey -> new LinkedHashMap<>());
			if(! submap.isEmpty()) {
				throw new IllegalStateException("seems that for previous logStart for same key '" + key + "' logStop was never called. Check if your code block can throw exceptions and wrap logStop into finally block!");
			}
			submap.put(key + "_#" + (submap.size() + 1), previousLoggedSingleItem);
		}
		map.put(key, new Pair(new Date().getTime(), System.nanoTime()));
	}

	public static void logStop(String key) {
		if (null == diagnosticEnabled.get()) {
			return;
		}

		Map<String, Object> map = getMap();
		Pair<Long, Long> item = (Pair<Long, Long>) map.get(key);
		if (item == null) {
			//should not be possible?
			throw new IllegalStateException("called logStop on key '" + key + "' that was never started with logStart!?");
		}
		long duration = System.nanoTime() - item.get2();
		if(item.get2() == 0) {
			System.out.println("HOW 0???");
		}
		Pair<Long, Long> itemEnded = new Pair(item.get1(), duration);
		Map<String, Pair<Long, Long>> submap = (Map<String, Pair<Long, Long>>) map.get(key + SUB_MAP);
		if (null != submap) {
			submap.put(key + "_#" + (submap.size() + 1), itemEnded);
			map.remove(key);
		} else {
			map.put(key, itemEnded);
		}
	}

	private static void clear() {
		getMap().clear();
	}

	public static Map<String, Long> report(TimeUnit unit, boolean detailed) {
		if (null == diagnosticEnabled.get()) {
			//has to initLog beforehand
			return null;
		}

		Map<String, Object> map = getMap();
		Map<String, Long> report = new LinkedHashMap<>();
		map.entrySet().stream().sorted((entry1, entry2) -> {
			Date date1 = new Date(getStartDateFrom(entry1));
			Date date2 = new Date(getStartDateFrom(entry2));
			return date1.compareTo(date2);
		}).forEach(entry -> {
			Object value = entry.getValue();
			String key = entry.getKey();
			if (value instanceof Pair) {
				Pair<Long, Long> durationPair = (Pair<Long, Long>)value;
				addToReport(report, unit, key, durationPair, false);
			} else {
				Map<String, Pair<Long, Long>> subMap = (Map<String, Pair<Long, Long>>)value;
				String mainKeyPrefix = key.substring(0, key.indexOf(SUB_MAP));
				long overallDuration = subMap.values().stream().mapToLong(pair -> pair.get2()).sum();
				addToReport(report, unit, mainKeyPrefix + " (total count " + subMap.size() + ")", new Pair(subMap.values().iterator().next().get1(), overallDuration), false);
				if (detailed) {
					subMap.entrySet().stream().forEach(subEntry -> addToReport(report, unit, subEntry.getKey(), subEntry.getValue(), true));
				}
			}
		});

		diagnosticEnabled.set(null);
		return report;
	}

	private static void addToReport(Map<String, Long> report, TimeUnit unit, String key, Pair<Long, Long> startDateAndNanoDurationPair, boolean skipZeroDurations) {
		Long nanoDuration = startDateAndNanoDurationPair.get2();
		long convertedDuration = unit.convert(nanoDuration, TimeUnit.NANOSECONDS);
		if (!skipZeroDurations || convertedDuration > 0) {
			report.put(key + " started at " + DF.format(startDateAndNanoDurationPair.get1()), convertedDuration);
		}
	}

	private static Long getStartDateFrom(Map.Entry<String, Object> entry1) {
		return entry1.getValue() instanceof Pair
			? ((Pair<Long, Long>) entry1.getValue()).get1()
			: ((Map<String, Pair<Long, Long>>) entry1.getValue()).values().iterator().next().get1();
	}
}
