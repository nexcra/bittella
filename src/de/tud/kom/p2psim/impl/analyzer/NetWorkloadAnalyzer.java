package de.tud.kom.p2psim.impl.analyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.tud.kom.p2psim.api.analyzer.Analyzer.NetAnalyzer;
import de.tud.kom.p2psim.api.network.NetID;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.simengine.Simulator;

public class NetWorkloadAnalyzer implements NetAnalyzer {

	private Map<NetID, NodeWorkLoad> nodeWorkloads;

	private boolean isRunning;

	protected class NodeWorkLoad {
		public int outMsgCounter;

		public int inMsgCounter;

		public int totalBytesSend;
	}

	public NetWorkloadAnalyzer() {
		nodeWorkloads = new HashMap<NetID, NodeWorkLoad>(0);
		this.isRunning = false;
	}

	public void stop(Writer output) {
		try {
			this.isRunning = false;
			int sumOutMsg = 0;
			int sumInMsg = 0;
			int sumTotalBytes = 0;
			int total = this.nodeWorkloads.size();
			Iterator<NodeWorkLoad> list = this.nodeWorkloads.values().iterator();

			while (list.hasNext()) {
				NodeWorkLoad tmp = list.next();
				sumOutMsg += tmp.outMsgCounter;
				sumTotalBytes += tmp.totalBytesSend;
				sumInMsg += tmp.inMsgCounter;
			}

			double avg_outmsg = (double) sumOutMsg / (double) (total);
			double avg_totalbytes = (double) sumTotalBytes / (double) (total);
			double avg_inmsg = (double) sumInMsg / (double) (total);

			output.write("\n******** Network Workload Stats ***********\n");
			output.write("Total hosts: " + total + "\n");
			output.write("Total Msg out: " + sumOutMsg + "\n");
			output.write("Total Msg in: " + sumInMsg + "\n");
			output.write("TotalBytes sent: " + sumTotalBytes + "\n");
			output.write("Values are 'per Host'!\n");
			output.write("Average Message Out's: " + avg_outmsg + "\n");
			output.write("Average Message In's: " + avg_inmsg + "\n");
			output.write("Average Bytes Sent: " + avg_totalbytes + "\n");
			output.write("*******Network Workload Stats End *********\n");

			// ################ Network Workload FILE ################
			File f = new File("outputs/monitoring/networkload_seed_" + Simulator.getSeed() + ".dat");
			boolean exists = f.exists();

			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			if (!exists) {
				bw.write("#network size\t total_msg_flow\t avg_msg_out\t avg_msg_in\t");
				bw.newLine();
			} else
				bw.newLine();
			bw.write(total + "\t" + (sumOutMsg + sumInMsg) + "\t" + avg_outmsg + "\t" + avg_inmsg);
			bw.close();
			// ################# Network Workload FILE END #############
		} catch (IOException e) {
			throw new IllegalStateException("Problems in writing results" + e);
		}
	}

	public String toString() {
		return "Network workload analyzer";
	}

	public void netMsgDrop(NetMessage msg, NetID id) {
		// TODO Auto-generated method stub

	}

	public void netMsgReceive(NetMessage msg, NetID id) {
		if (this.isRunning) {
			NodeWorkLoad tmp = this.nodeWorkloads.get(id);
			if (tmp == null) {
				NodeWorkLoad nn = new NodeWorkLoad();
				nn.inMsgCounter++;
				this.nodeWorkloads.put(id, nn);
			} else {
				tmp.inMsgCounter++;
			}
		}
	}

	public void netMsgSend(NetMessage msg, NetID id) {
		if (this.isRunning) {
			NodeWorkLoad tmp = this.nodeWorkloads.get(id);
			if (tmp == null) {
				NodeWorkLoad nn = new NodeWorkLoad();
				nn.outMsgCounter++;
				nn.totalBytesSend += msg.getSize();
				this.nodeWorkloads.put(id, nn);
			} else {
				tmp.outMsgCounter++;
				tmp.totalBytesSend += msg.getSize();
			}
		}
	}

	public void start() {
		this.isRunning = true;
	}

}
