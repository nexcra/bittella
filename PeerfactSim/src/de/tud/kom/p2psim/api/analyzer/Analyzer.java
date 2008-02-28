package de.tud.kom.p2psim.api.analyzer;

import java.io.Writer;

import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.api.common.Monitor;
import de.tud.kom.p2psim.api.common.Operation;
import de.tud.kom.p2psim.api.network.NetID;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;

/**
 * In general, analyzers are used to receive notifications about actions that
 * took place on specific components, for instance the sending or receiving of
 * messages. In particular, analyzers are able to collect data during a
 * simulation run and prepare the results at the end of a simulation.
 * 
 * Note that analyzers must be registered by an implementation of the
 * {@link Monitor} interfaceby using the xml configuration file before the
 * simulation starts.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 12/03/2007
 * 
 */
public interface Analyzer {

	/**
	 * TransAnalyzers receive notifications when a network message is sent, or
	 * received at the transport layer.
	 * 
	 */
	public interface TransAnalyzer extends Analyzer {

		/**
		 * Invoking this method denotes that the given message is sent at the
		 * transport layer (from the application towards the network layer).
		 * 
		 * @param msg
		 *            the AbstractTransMessage which is sent out.
		 */
		public void transMsgSent(AbstractTransMessage msg);

		/**
		 * Invoking this method denotes that the given message is received at
		 * the transport layer (from the network layer towards the application
		 * layer).
		 * 
		 * @param msg
		 *            the received AbstractTransMessage.
		 */
		public void transMsgReceived(AbstractTransMessage msg);

	}

	/**
	 * NetAnalyzers receive notifications when a network message is send,
	 * received or dropped at the network layer.
	 * 
	 */
	public interface NetAnalyzer extends Analyzer {
		/**
		 * Invoking this method denotes that the given network message is sent
		 * at the network layer with the given NetID
		 * 
		 * @param msg
		 *            the message which is send out
		 * @param id
		 *            the NetID of the sender of the given message
		 */
		public void netMsgSend(NetMessage msg, NetID id);

		/**
		 * Invoking this method denotes that the given network message is
		 * received at the network layer with the given NetID
		 * 
		 * @param msg
		 *            the received message
		 * @param id
		 *            the NetID of the receiver of the given message
		 */
		public void netMsgReceive(NetMessage msg, NetID id);

		/**
		 * Invoking this method denotes that the given network message is
		 * dropped at the network layer with the given NetID (due to packet loss
		 * or the receiving network layer has no physical connectivity).
		 * 
		 * @param msg
		 *            the dropped message
		 * @param id
		 *            the NetID of the receiver at which the message is droped
		 */
		public void netMsgDrop(NetMessage msg, NetID id);
	}

	/**
	 * OperationAnalyzers receive notifications when a operation is finished
	 * either with or without success.
	 * 
	 */
	public interface OperationAnalyzer extends Analyzer {

		/**
		 * This method is called whenever an operation has been triggered.
		 * 
		 * @param op
		 *            the Operation that has been triggered.
		 */
		public void operationInitiated(Operation<?> op);

		/**
		 * This method is called whenever an operation has completed.
		 * 
		 * @param op
		 *            the Operation that has completed.
		 */
		public void operationFinished(Operation<?> op);

	}

	/**
	 * OperationAnalyzers receive notifications when the network connectivity
	 * has been changed of churn affected hosts
	 * 
	 */
	public interface ChurnAnalyzer extends Analyzer {

		/**
		 * Invoking this method denotes that the given host does not have
		 * network connectivity
		 * 
		 * @param host
		 *            the churn affected host
		 */
		public void offlineEvent(Host host);

		/**
		 * Invoking this method denotes that the given host does have network
		 * connectivity
		 * 
		 * @param host
		 *            the churn affected host
		 */
		public void onlineEvent(Host host);
	}

	/**
	 * Invoking this method denotes start running analyzer
	 * 
	 */
	public void start();

	/**
	 * Invoking this method denotes stop running analyzer. Furthermore, all
	 * results have to be prepared and printed out using the given writer
	 * 
	 * @param output
	 *            the given output writer
	 */
	public void stop(Writer output);

}
