package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import accounts.*;

public class RecordsManager {
	private final String path = "Resources/Db/records.txt";
	private HashMap<String,ArrayList<Records>> allRecords;
	private AccountManager accMan;
	
	public RecordsManager(AccountManager accMan) {
		allRecords = new HashMap<String,ArrayList<Records>>();
		this.accMan = accMan;
		readRecords();
	}
	
	public void saveRecords() {
		PrintWriter pw = null;
		File recordsSave = new File(path);
		if(!recordsSave.exists()) {
			try {
				recordsSave.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			pw = new PrintWriter(recordsSave);
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for(Entry<String,ArrayList<Records>> e : allRecords.entrySet()) {
			pw.println("Patient="+e.getKey());
			for(Records r : e.getValue())pw.println(r.toString());
			pw.println("ENDOFRECORD");
		}
		pw.close();
	}
	
	public void readRecords() {
        FileReader fileReader;
        try {
            fileReader = new FileReader(path);
            BufferedReader read = new BufferedReader(fileReader);
            String line = null;
            while ((line = read.readLine()) != null) {
                ArrayList<Records> temp = new ArrayList<Records>();
                String patientId = line.substring(8);
                while (!(line = read.readLine()).equals("ENDOFRECORD")) {
                	if (line.substring(0, 6).equals("Doctor")) {
                        String[] lines = line.split(",");
                        String doctorId = lines[0].substring(7);
                        String nurseId = lines[1].substring(6);
                        temp.add(new Records(patientId, doctorId, nurseId));
                    } else {
                        temp.get(temp.size() - 1).addRecord(line.substring(20),line.substring(0, 19));
                    }
                }
                allRecords.put(patientId, temp);
            }
            read.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }
	}
	
	public ArrayList<Records> getRecordThird(String patientId, String doctorOrNurseId) {
		ArrayList<Records> rec = new ArrayList<Records>();
		if(allRecords.containsKey(patientId))for(Records r : allRecords.get(patientId))if((r.getDoctorId().equals(doctorOrNurseId) || r.getNurseId().equals(doctorOrNurseId)) || (accMan.getPersonFromId(patientId).getDiv().equals(accMan.getPersonFromId(doctorOrNurseId).getDiv())))rec.add(r);
		return rec;
	}
	
	public Records getRecord(String patientId, String doctorOrNurseId) {
		if(allRecords.containsKey(patientId))for(Records r : allRecords.get(patientId))
			if(r.getDoctorId().equals(doctorOrNurseId) || r.getNurseId().equals(doctorOrNurseId))return r;
		return null;
	}	
	public ArrayList<Patient> getPatientsForDivision(Person doctorOrNurse){
		ArrayList<Patient> patients = doctorOrNurse.getDiv().getMembers();
		for(int i = 0; i < patients.size();i++)if(!allRecords.containsKey(patients.get(i).getId()))patients.remove(i);
		return patients;
	}
	
	public ArrayList<Records> getAllPatientRecords(String patientId){
		if(allRecords.containsKey(patientId))return allRecords.get(patientId);
		return new ArrayList<Records>();
	}
	
	public ArrayList<Patient> getPatientsForPerson(Person person){
		ArrayList<Patient> patients = new ArrayList<Patient>();
		for(Entry<String, ArrayList<Records>> e : allRecords.entrySet())for(Records r : e.getValue())if(r.isNurseOrDoctor(person))patients.add((Patient) accMan.getPersonFromId(e.getKey()));
		return patients;
	}
	
	public boolean deleteRecords(String patientId) {
		return allRecords.remove(patientId) != null;
	}
	
	public boolean addRecords(String patientId, Doctor doctor, String nurseId) {
		Records newRecord = new Records(patientId,doctor.getId(),nurseId);
		Person patient = accMan.getPersonFromId(patientId);
		Person nurse	= accMan.getPersonFromId(nurseId);
		if(!(patient instanceof Patient && nurse instanceof Nurse) || !(patient.getDiv().equals(doctor.getDiv()) && nurse.getDiv().equals(doctor.getDiv())))return false;
		if(!allRecords.containsKey(patientId)) {
			ArrayList<Records> patientRecords = new ArrayList<Records>();
			patientRecords.add(newRecord);
			allRecords.put(patientId, patientRecords);
			return true;
		}else if (getRecord(patientId, doctor.getId()) == null){
			allRecords.get(patientId).add(newRecord);
			return true;
		}
		return false;
	}
}
