package util;

import java.util.ArrayList;
import accounts.Patient;

public class Division {
	private String id;
	private String name;
	private ArrayList<Patient> patients;
	
	public Division(String id, String name) {
		this.id = id;
		this.name = name;
		patients = new ArrayList<Patient>();
	}
	
	public void addMember (Patient patient) {
		patients.add(patient);
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Patient> getMembers(){
		return (ArrayList<Patient>)patients.clone();
	}
	
	public String getId() {
		return id;
	}
	
	public String toString() {
		return name;
	}
}
