package util;

import accounts.*;

import java.math.BigInteger;
import java.util.*;
import java.io.*;

public class AccountManager {
	private final String path = "/Db/accounts.txt";
	private HashMap<BigInteger, Person> accounts;
	private ArrayList<Division> divisions;

	public AccountManager() {
		accounts = new HashMap<BigInteger, Person>();
		divisions = new ArrayList<Division>();
		readFile();
	}

	public Person getPersonFromSerialNumber(BigInteger serial) {
		if (accounts.containsKey(serial))
			return accounts.get(serial);
		return null;
	}

	public Person getPersonFromId(String id) {
		for (Person p : accounts.values())
			if (p.getId().equals(id))
				return p;
		return null;
	}
	
	//might change
	public Set<Map.Entry<BigInteger, Person>> getPersons() {
		return accounts.entrySet();
	}

	public Division getDivisionFromId(String id) {
		for (Division d : divisions)if (d.getId().equals(id))return d;
		return null;
	}

	public void readFile() {
		try {
			BufferedReader read = new BufferedReader(new InputStreamReader(AccountManager.class.getResourceAsStream(path)));
			String line;
			while ((line = read.readLine()) != null && !line.equals("ENDOFDIVISION")) {
				String[] division = line.split(",");
				divisions.add(new Division(division[0], division[1]));
			}
			while ((line = read.readLine()) != null) {
				String[] p = line.split(",");
				BigInteger serialNumber = new BigInteger(p[0]);
				String type = p[1];
				Division division = getDivisionFromId(p[2]);
				String id = p[3];
				String name = p[4];
				Person person = null;
				if (type.equals("0")) {
					person = new Government(name, id);
				} else if (type.equals("1")) {
					person = new Doctor(name, id, division);
				} else if (type.equals("2")) {
					person = new Nurse(name, id, division);
				} else if(type.equals("3")) {
					person = new Patient(name, id, division, p[5]);
					division.addMember((Patient) person);
				}
				accounts.put(serialNumber, person);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
}
