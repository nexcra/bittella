package uc3m.netcom.peerfactsim.test;

import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTInternStatistic;

public class BTCompressedStatistic {
	
	/**
	 * The upload rate in packets per time period(this.itsPeriod).
	 * It only counts the packet numbers. This is false for the last block only.
	 * In a 100 MB file, the last block is one of 6400 packets.
	 * This is not very often. ;-) Therefore we ignore this very small problem.
	 */
	public int[] itsUploadRate;
	public int[] itsDownloadRate;
	
	public int itsUploadSum;
	public int itsDownloadSum;
	
	/**
	 * The period in simulator ticks for one entry in the statistic.
	 * If the period is 2000 for example, this means that all data is
	 * divided into 2000 tick long windows and summed up to one entry.
	 */
	public int itsPeriod;
	
	/**
	 * Time offset in number of skipped entries at the beginning of the data. (assuming the real start was at 0)
	 * If a peers is started after two days, we don't need to save the 0-entries for these two days.
	 */
	public long itsUploadTimeOffset;
	public long itsDownloadTimeOffset;
	
	private long itsDownloadStart;
	private long itsDownloadStop;
	private long itsUploadStart;
	private long itsUploadStop;
	
	public BTCompressedStatistic(BTInternStatistic theStatistic, int thePeriod) {
		this.itsDownloadStart = theStatistic.getDownloadStart();
		this.itsDownloadStop = theStatistic.getDownloadStop();
		this.itsUploadStart = theStatistic.getUploadStart();
		this.itsUploadStop = theStatistic.getUploadStop();
		this.itsPeriod = thePeriod;
		
		//Once for the upload:
		this.itsUploadTimeOffset = Long.MAX_VALUE;
		Map<BTContact, List<Long>> rawUploadData = theStatistic.getUploadStatistic();
		
		//to calc the time offset, we search for the first entry in the lists.
		for (BTContact anOtherPeer : rawUploadData.keySet()) {
			if (rawUploadData.get(anOtherPeer).isEmpty())
				continue;
			if ((rawUploadData.get(anOtherPeer).get(0) / this.itsPeriod) < this.itsUploadTimeOffset)
				this.itsUploadTimeOffset = (rawUploadData.get(anOtherPeer).get(0) / this.itsPeriod);
		}
		if (this.itsUploadTimeOffset == Long.MAX_VALUE) {
			this.itsUploadTimeOffset = 0;
		}
		
		//We calculate the size of the array. To do this, we search for the last entry.
		int uploadSize = 0;
		for (BTContact anOtherPeer : rawUploadData.keySet()) {
			if (rawUploadData.get(anOtherPeer).isEmpty())
				continue;
			if ((1 + (rawUploadData.get(anOtherPeer).get(rawUploadData.get(anOtherPeer).size() - 1) / this.itsPeriod) - this.itsUploadTimeOffset) > uploadSize)
				uploadSize = (int)(1 + (rawUploadData.get(anOtherPeer).get(rawUploadData.get(anOtherPeer).size() - 1) / this.itsPeriod) - this.itsUploadTimeOffset);
		}
		
		//The time offset is set. Now we just need to fill the data array:
		this.itsUploadRate = new int[uploadSize];
		this.itsUploadSum = 0;
		List<Long> currentUploadData;
		for (BTContact anOtherPeer : rawUploadData.keySet()) {
			currentUploadData = rawUploadData.get(anOtherPeer);
			for (long anEntry : currentUploadData) {
				this.itsUploadRate[(int)((anEntry / this.itsPeriod) - this.itsUploadTimeOffset)] += 1;
				this.itsUploadSum += 1;
			}
		}
		
		//and once for the download:
		this.itsDownloadTimeOffset = Long.MAX_VALUE;
		Map<BTContact, List<Long>> rawDownloadData = theStatistic.getDownloadStatistic();
		
		//to calc the time offset, we search for the first entry in the lists.
		for (BTContact anOtherPeer : rawDownloadData.keySet()) {
			if (rawDownloadData.get(anOtherPeer).isEmpty())
				continue;
			if ((rawDownloadData.get(anOtherPeer).get(0) / this.itsPeriod) < this.itsDownloadTimeOffset)
				this.itsDownloadTimeOffset = (rawDownloadData.get(anOtherPeer).get(0) / this.itsPeriod);
		}
		if (this.itsDownloadTimeOffset == Long.MAX_VALUE) {
			this.itsDownloadTimeOffset = 0;
		}
		
		//We calculate the size of the array. To do this, we search for the last entry.
		int downloadSize = 0;
		for (BTContact anOtherPeer : rawDownloadData.keySet()) {
			if (rawDownloadData.get(anOtherPeer).isEmpty())
				continue;
			if ((1 + (rawDownloadData.get(anOtherPeer).get(rawDownloadData.get(anOtherPeer).size() - 1) / this.itsPeriod) - this.itsDownloadTimeOffset) > downloadSize)
				downloadSize = (int)(1 + (rawDownloadData.get(anOtherPeer).get(rawDownloadData.get(anOtherPeer).size() - 1) / this.itsPeriod) - this.itsDownloadTimeOffset);
		}
		
		//The time offset is set. Now we just need to fill the data array:
		this.itsDownloadRate = new int[downloadSize];
		this.itsDownloadSum = 0;
		List<Long> currentDownloadData;
		for (BTContact anOtherPeer : rawDownloadData.keySet()) {
			currentDownloadData = rawDownloadData.get(anOtherPeer);
			for (long anEntry : currentDownloadData) {
				this.itsDownloadRate[(int)((anEntry / this.itsPeriod) - this.itsDownloadTimeOffset)] += 1;
				this.itsDownloadSum += 1;
			}
		}
	}
	
	@Override
	public String toString() {
		String result = "Download: [Start: " + (this.itsDownloadTimeOffset * this.itsPeriod) + "; Stop: " + ((this.itsDownloadTimeOffset + this.itsDownloadRate.length - 1) * this.itsPeriod) + "; Period: " + this.itsPeriod + "; Sum: " + this.itsDownloadSum + "; Data: ";
		for (int i : this.itsDownloadRate)
			result += ("" + i + ":");
		result += "]\n";
		
		result += ("Upload: [Start: " + (this.itsUploadTimeOffset * this.itsPeriod) + "; Stop: " + ((this.itsUploadTimeOffset * this.itsUploadRate.length - 1) * this.itsPeriod) + "; Period: " + this.itsPeriod + "; Sum: " + this.itsUploadSum + "; Data: ");
		for (int i : this.itsUploadRate)
			result += ("" + i + ":");
		result += "]";
		return result;
	}
	
	public String toString(boolean longFormat) {
		if (! longFormat)
			return this.toString();
		String result = "Download: [Start: " + this.itsDownloadStart + "; Stop: " + this.itsDownloadStop + "; Period: " + this.itsPeriod + "; Sum: " + this.itsDownloadSum + "; Data: ";
		for (int i = 0; i < this.itsDownloadTimeOffset; i++)
			result += ("\t0:");
		for (int i : this.itsDownloadRate)
			result += ("\t" + i + ":");
		result += "]\n";
		
		result += ("Upload: [Start: " + this.itsUploadStart + "; Stop: " + this.itsUploadStop + "; Period: " + this.itsPeriod + "; Sum: " + this.itsUploadSum + "; Data: ");
		for (int i = 0; i < this.itsUploadTimeOffset; i++)
			result += ("\t0:");
		for (int i : this.itsUploadRate)
			result += ("\t" + i + ":");
		result += "]";
		return result;
	}
	
	public String toCSV(boolean startAtZero) {
		//Constants for the CSV-Format:
		String CSV_COLUMN_SEPARATOR = ", ";
		String CSV_ROW_SEPARATOR = ";\r\n";
		
		//Some single data with headline:
		String result = "";
		//Ohne diese beiden Zeilen kann man es leichter verarbeiten.
//		result += ("Period" + CSV_COLUMN_SEPARATOR + "Download-Start" + CSV_COLUMN_SEPARATOR + "Download-Stop" + CSV_COLUMN_SEPARATOR + "Upload-Start" + CSV_COLUMN_SEPARATOR + "Upload-Stop" + CSV_ROW_SEPARATOR);
//		result += ("" + this.itsPeriod + CSV_COLUMN_SEPARATOR + (this.itsDownloadTimeOffset * this.itsPeriod) + CSV_COLUMN_SEPARATOR + ((this.itsDownloadTimeOffset + this.itsDownloadRate.length - 1) * this.itsPeriod) + CSV_COLUMN_SEPARATOR + (this.itsUploadTimeOffset * this.itsPeriod) + CSV_COLUMN_SEPARATOR + ((this.itsUploadTimeOffset + this.itsUploadRate.length - 1) * this.itsPeriod) + CSV_ROW_SEPARATOR);
		
		long firstEntry;
		if (startAtZero)
			firstEntry = 0;
		else
			firstEntry = Math.min(this.itsDownloadTimeOffset, this.itsUploadTimeOffset);
		long lastEntry = Math.max(this.itsDownloadTimeOffset + this.itsDownloadRate.length - 1, this.itsUploadTimeOffset + this.itsUploadRate.length - 1);
		
		//The data streams with headline:
		result += ("Download-Data" + CSV_COLUMN_SEPARATOR + "Upload-Data" + CSV_ROW_SEPARATOR);
		int downloadIndex = (int)(firstEntry - this.itsDownloadTimeOffset); //The current index. If it is negativ, which may happen, we just don't access the array.
		int uploadIndex = (int)(firstEntry - this.itsUploadTimeOffset);
		boolean downloadData; //Are there data for this time?
		boolean uploadData;
		for (int i = 0; i <= (lastEntry - firstEntry); i++) {
			
			downloadData = ((downloadIndex >= 0) && (downloadIndex < this.itsDownloadRate.length)); //Calculate, if we are in the array bounds.
			uploadData = ((uploadIndex >= 0) && (uploadIndex < this.itsUploadRate.length));
			
			result += ("" + (downloadData ? this.itsDownloadRate[downloadIndex] : "0") + CSV_COLUMN_SEPARATOR + (uploadData ? this.itsUploadRate[uploadIndex] : "0") + CSV_ROW_SEPARATOR);
			
			downloadIndex += 1;
			uploadIndex += 1;
		}
		return result;
	}
	
}
