package de.tud.kom.p2psim.impl.scenario;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.Scenario;
import de.tud.kom.p2psim.api.scenario.ScenarioFactory;
import de.tud.kom.p2psim.impl.simengine.SimulatorTest;
/**
 * This test case is used to test ALL configuration files for consistency.
 * The files should be located in the configuration directory (@see constant {@link #configFile})
 * if they satisfy the filter criterium (@see the constant {@link #CONFIG_FILE_ENDING}).
 * The will be one test run for EACH configuration file.
 * Note, that the config files will be parsed and a simulation scenario will be created,
 * but no simulation will be started. 
 * @author Konstantin Pussep
 * @version 0.1, 12.07.2007
 *
 */
@RunWith(value=Parameterized.class)// used to run the test with different parameters
public class ConfigurationFileTest extends SimulatorTest{
	private static final String CONFIG_FILE_ENDING = ".xml";
	/**
	 * The file which is being tested in this test run.
	 */
	File configFile;
	/**
	 * Directory containing configuration files, which should be tested.
	 */
	final static String configDir = "config";
	
	/**
	 * Test given configuration file for consistency.
	 * @param configFile
	 */
	public ConfigurationFileTest(File configFile) {
		super();
		this.configFile = configFile;
	}

	/**
	 * This method selects the collection of parameters for each run of this test (this is the way
	 * it works with JUnit 4.0).
	 * @return collection containing all found configuration files.
	 */
	@Parameters
	public static Collection data() {
		File[] files = new File(configDir).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(CONFIG_FILE_ENDING);
			}		
		});
		List<File[]> data = new ArrayList<File[]>(files.length);
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			data.add(new File[]{file});
			log.info(i+". config file is: "+Arrays.asList(file));
		}
		
        return data;
    }
	
	/**
	 * Test one configuration file for consistency and format correctness.
	 * @throws Exception 
	 *
	 */
	@Test
	public void testConfigureAll() throws Exception{
		log.info("Test config file: "+configFile);
		DefaultConfigurator conf = new DefaultConfigurator(configFile.getAbsolutePath());
		int count;
		try {
			count = conf.configureAll().size();
		} catch (Exception e) {
			log.error("Failed to configure from file "+configFile, e);
			throw e;
		}
		assertTrue(count>0);
		ScenarioFactory scenarioBuilder = (ScenarioFactory) conf.getConfigurable(Configurator.SCENARIO_TAG);
		if(scenarioBuilder!=null){
			Scenario scenario = scenarioBuilder.createScenario();
			assertNotNull(scenario);
		}
	}
//	private int expected;
//    private int value;
//
//    @Parameters
//    public static Collection data() {
//        return Arrays.asList( new Object[] []{
//                             { 1 },   // expected, value
//                             { 1 }, 
//                             { 2 },
//                             { 4 },
//                             { 7 },
//                             });
//    }
//
//    public ConfigurationFileTest(int expected) {
//        this.expected = expected;
//    }
//
//    @Test
//    public void factorial() {
//        assertEquals(expected, expected);
//    }

	@Override
	public void setUp() {
		// TODO Auto-generated method stub
//		super.setUp();
	}

}
