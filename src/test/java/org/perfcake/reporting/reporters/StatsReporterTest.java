package org.perfcake.reporting.reporters;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.perfcake.RunInfo;
import org.perfcake.common.Period;
import org.perfcake.common.PeriodType;
import org.perfcake.reporting.Measurement;
import org.perfcake.reporting.MeasurementUnit;
import org.perfcake.reporting.ReportManager;
import org.perfcake.reporting.ReportingException;
import org.perfcake.reporting.destinations.DummyDestination;
import org.perfcake.util.ObjectFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Pavel Macík <pavel.macik@gmail.com>
 */
public class StatsReporterTest {

   private static final long ITERATION_COUNT = 10L;

   private DummyDestination dest;

   @BeforeClass
   public void prepare() throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
      dest = (DummyDestination) ObjectFactory.summonInstance(DummyDestination.class.getName(), new Properties());
   }

   @Test
   public void testDefaults() throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
      ThroughputStatsReporter tsr = (ThroughputStatsReporter) ObjectFactory.summonInstance(ThroughputStatsReporter.class.getName(), new Properties());
      Assert.assertTrue(tsr.isAverageEnabled());
      Assert.assertTrue(tsr.isMinimumEnabled());
      Assert.assertTrue(tsr.isMaximumEnabled());
      Assert.assertNull(tsr.getAccumulatedResult(StatsReporter.AVERAGE));
      Assert.assertNull(tsr.getAccumulatedResult(StatsReporter.MINIMUM));
      Assert.assertNull(tsr.getAccumulatedResult(StatsReporter.MAXIMUM));
      Assert.assertNull(tsr.getAccumulatedResult(Measurement.DEFAULT_RESULT));

      ResponseTimeStatsReporter rtsr = (ResponseTimeStatsReporter) ObjectFactory.summonInstance(ResponseTimeStatsReporter.class.getName(), new Properties());
      Assert.assertTrue(rtsr.isAverageEnabled());
      Assert.assertTrue(rtsr.isMinimumEnabled());
      Assert.assertTrue(rtsr.isMaximumEnabled());
      Assert.assertNull(rtsr.getAccumulatedResult(StatsReporter.AVERAGE));
      Assert.assertNull(rtsr.getAccumulatedResult(StatsReporter.MINIMUM));
      Assert.assertNull(rtsr.getAccumulatedResult(StatsReporter.MAXIMUM));
      Assert.assertNull(rtsr.getAccumulatedResult(Measurement.DEFAULT_RESULT));
   }

   @DataProvider(name = "reporterProperties")
   public Object[][] createDataForReporters() {
      final int rep = 2;
      final int comb = 8;

      final Object[][] retVal = new Object[comb * rep][];

      final StatsReporter[] reps = new StatsReporter[] { new ThroughputStatsReporter(), new ResponseTimeStatsReporter() };

      int c = 0;
      for (int i = 0; i < rep; i++) {
         // reporter, averageEnabled, minimumEnabled, maximumEnabled
         retVal[c++] = new Object[] { reps[i], true, true, true };
         retVal[c++] = new Object[] { reps[i], true, true, false };
         retVal[c++] = new Object[] { reps[i], true, false, true };
         retVal[c++] = new Object[] { reps[i], true, false, false };
         retVal[c++] = new Object[] { reps[i], false, true, true };
         retVal[c++] = new Object[] { reps[i], false, true, false };
         retVal[c++] = new Object[] { reps[i], false, false, true };
         retVal[c++] = new Object[] { reps[i], false, false, false };
      }
      return retVal;
   }

   @Test(dataProvider = "reporterProperties")
   public void testReporters(StatsReporter reporter, boolean averageEnabled, boolean minimumEnabled, boolean maximumEnabled) throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException {
      final String reporterName = reporter.getClass().getName();

      Properties reporterProperties = new Properties();
      reporterProperties.put("averageEnabled", String.valueOf(averageEnabled));
      reporterProperties.put("minimumEnabled", String.valueOf(minimumEnabled));
      reporterProperties.put("maximumEnabled", String.valueOf(maximumEnabled));

      ThroughputStatsReporter tsr = (ThroughputStatsReporter) ObjectFactory.summonInstance(ThroughputStatsReporter.class.getName(), reporterProperties);

      final List<Measurement> measurementList = new LinkedList<>();
      final ReportManager rm = new ReportManager();
      tsr.registerDestination(dest, new Period(PeriodType.ITERATION, 1));
      rm.registerReporter(tsr);
      final RunInfo ri = new RunInfo(new Period(PeriodType.ITERATION, ITERATION_COUNT));
      rm.setRunInfo(ri);
      rm.start();

      try {
         for (int i = 0; i < ITERATION_COUNT; i++) {
            final MeasurementUnit mu = rm.newMeasurementUnit();
            mu.startMeasure();
            Thread.sleep(1);
            mu.stopMeasure();
            rm.report(mu);
            if (!measurementList.contains(dest.getLastMeasurement())) {
               measurementList.add(dest.getLastMeasurement());
            }
            dest.getLastMeasurement().toString();
         }
      } catch (InterruptedException | ReportingException e) {
         e.printStackTrace();
         Assert.fail(reporterName + ": Exception should not be thrown.", e);
      }
      rm.stop();
      final int mls = measurementList.size();
      Assert.assertEquals(mls, ITERATION_COUNT, reporterName + ": Number of Measurement sent to destination");

      for (final Measurement m : measurementList) {
         if (averageEnabled) {
            Assert.assertNotNull(m.get(StatsReporter.AVERAGE), reporterName + ": Not null average result");
         } else {
            Assert.assertNull(m.get(StatsReporter.AVERAGE), reporterName + ": Null average result");
         }

         if (minimumEnabled) {
            Assert.assertNotNull(m.get(StatsReporter.MINIMUM), reporterName + ": Not null minimum result");
         } else {
            Assert.assertNull(m.get(StatsReporter.MINIMUM), reporterName + ": Null minimum result");
         }

         if (maximumEnabled) {
            Assert.assertNotNull(m.get(StatsReporter.MAXIMUM), reporterName + ": Not null maximum result");
         } else {
            Assert.assertNull(m.get(StatsReporter.MAXIMUM), reporterName + ": Null maximum result");
         }
      }
   }
}
