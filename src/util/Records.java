package util;
import java.util.ArrayList;
import accounts.Person;
public class Records {
	private ArrayList<Record> records;
    private String patientId;
    private String doctorId;
    private String nurseId;
    
	public Records(String patientId, String doctorId, String nurseId) {
		 records = new ArrayList<Record>();
	        this.patientId = patientId;
	        this.doctorId = doctorId;
	        this.nurseId = nurseId;
	}
	
    public String getDoctorId(){
        return doctorId;
    }

    public String getNurseId(){
        return nurseId;
    }

    public String getPatientId() { 
    	return patientId; 
	}
	public void addRecord(String entry, String date) {
		records.add(new Record(entry,date));
	}
	
	public boolean isNurseOrDoctor(Person person) {
		return person.getId().equals(doctorId) || person.getId().equals(nurseId);
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Doctor="+ doctorId + ",Nurse="+nurseId);
		for(Record r : records)s.append("\n"+r.toString());
		return s.toString();
	}
	
	private class Record{
		private String entry;
		private String date;
		
		private Record(String entry, String date) {
			this.entry = entry;
			this.date = date;
		}
		public String toString() {
			return date + "-" +entry;
		}
	}
}
