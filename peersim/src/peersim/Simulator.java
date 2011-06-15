/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package peersim;

import java.io.PrintStream;

import peersim.cdsim.CDSimulator;
import peersim.config.Configuration;
import peersim.config.IllegalParameterException;
import peersim.config.MissingParameterException;
import peersim.config.ParsedProperties;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.edsim.EDSimulator;

/**
 * This is the main entry point to peersim. This class loads configuration and
 * detects the simulation type. According to this, it invokes the appropriate
 * simulator. The known simulators at this moment, along with the way to detect
 * them are the following:
 * <ul>
 * <li>{@link CDSimulator}: if {@link CDSimulator#isConfigurationCycleDriven}
 * returns true</li>
 * <li>{@link EDSimulator}: if {@link EDSimulator#isConfigurationEventDriven}
 * returns true</li>
 * </ul>
 * This list represents the order in which these alternatives are checked. That
 * is, if more than one return true, then the first will be taken. Note that
 * this class checks only for these clues and does not check if the
 * configuration is consistent or valid.
 * 
 * @see #main
 */
public class Simulator {
	
	private static Simulator sim = null;
	
	public static void setSimulator(Simulator s){
		sim = s;
	}
	
	public static Simulator getSimulator(){ 
		if(sim == null){
			sim = new Simulator();
		}
		return sim; 
	} 

	// ========================== singleton constants ==========================
	// ======================================================================

	/** {@link CDSimulator} */
	public final int CDSIM = 0;

	/** {@link EDSimulator} */
	public final int EDSIM = 1;

	/** Unknown simulator */
	public final int UNKNOWN = -1;

	/** the class names of simulators used */
	protected String[] simName = { "peersim.cdsim.CDSimulator",
			"peersim.edsim.EDSimulator", };

	/**
	 * Parameter representing the number of times the experiment is run.
	 * Defaults to 1.
	 * 
	 * @config
	 */
	public final String PAR_EXPS = "simulation.experiments";

	/**
	 * If present, this parameter activates the redirection of the standard
	 * output to a given PrintStream. This comes useful for processing the
	 * output of the simulation from within the simulator.
	 * 
	 * @config
	 */
	public final String PAR_REDIRECT = "simulation.stdout";

	// ==================== static fields ===================================
	// ======================================================================

	/** */
	private int simID = UNKNOWN;

	// ========================== methods ===================================
	// ======================================================================

	/**
	 * Returns the numeric id of the simulator to invoke. At the moment this can
	 * be {@link #CDSIM}, {@link #EDSIM} or {@link #UNKNOWN}.
	 */
	public int getSimID() {

		if (simID == UNKNOWN) {
			if (CDSimulator.isConfigurationCycleDriven()) {
				simID = CDSIM;
			} else if (EDSimulator.isConfigurationEventDriven()) {
				simID = EDSIM;
			}
		}
		return simID;
	}

	// ----------------------------------------------------------------------

	/**
	 * Loads the configuration and executes the experiments. The number of
	 * independent experiments is given by config parameter {@value #PAR_EXPS}.
	 * In all experiments the configuration is the same, only the random seed is
	 * not re-initialized between experiments.
	 * <p>
	 * Loading the configuration is currently done with the help of constructing
	 * an instance of {@link ParsedProperties} using the constructor
	 * {@link ParsedProperties#ParsedProperties(String[])}. The parameter
	 * <code>args</code> is simply passed to this class. This class is then used
	 * to initialize the configuration.
	 * <p>
	 * After loading the configuration, the experiments are run by invoking the
	 * appropriate engine, which is identified as follows:
	 * <ul>
	 * <li>{@link CDSimulator}: if
	 * {@link CDSimulator#isConfigurationCycleDriven} returns true</li>
	 * <li>{@link EDSimulator}: if
	 * {@link EDSimulator#isConfigurationEventDriven} returns true</li>
	 * </ul>
	 * <p>
	 * This list represents the order in which these alternatives are checked.
	 * That is, if more than one return true, then the first will be taken. Note
	 * that this class checks only for these clues and does not check if the
	 * configuration is consistent or valid.
	 * 
	 * @param args
	 *            passed on to
	 *            {@link ParsedProperties#ParsedProperties(String[])}
	 * @see ParsedProperties
	 * @see Configuration
	 * @see CDSimulator
	 * @see EDSimulator
	 */
	public static void main(String[] args) {
		getSimulator().parse_configuration(args);
		getSimulator().load_simulation();

	}

	public void load_simulation() {
		long time = System.currentTimeMillis();

		

		int exps = Configuration.getInt(PAR_EXPS, 1);

		final int SIMID = getSimID();
		if (SIMID == UNKNOWN) {
			System.err
					.println("Simulator: unable to determine simulation engine type");
			return;
		}

		try {

			for (int k = 0; k < exps; ++k) {
				if (k > 0) {
					long seed = CommonState.r.nextLong();
					CommonState.initializeRandom(seed);
				}
				System.err.print("Simulator: starting experiment " + k);
				System.err.println(" invoking " + simName[SIMID]);
				System.err.println("Random seed: "
						+ CommonState.r.getLastSeed());
				System.out.println("\n\n");

				runExperiment(SIMID);
			}

		} catch (MissingParameterException e) {
			System.err.println(e + "");
			System.exit(1);
		} catch (IllegalParameterException e) {
			System.err.println(e + "");
			System.exit(1);
		}

		// undocumented testing capabilities
		if (Configuration.contains("__t"))
			System.out.println(System.currentTimeMillis() - time);
		if (Configuration.contains("__x"))
			Network.test();
	}

	public void parse_configuration(String[] args) {
		System.err.println("Simulator: loading configuration");

		Configuration.setConfig(new ParsedProperties(args));

		PrintStream newout = (PrintStream) Configuration.getInstance(
				PAR_REDIRECT, System.out);
		if (newout != System.out)
			System.setOut(newout);
	}

	public void runExperiment(final int SIMID) {
		// XXX could be done through reflection, but
		// this is easier to read.
		switch (SIMID) {
		case CDSIM:
			CDSimulator.nextExperiment();
			break;
		case EDSIM:
			EDSimulator.nextExperiment();
			break;
		}
	}



}
