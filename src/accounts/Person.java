package accounts;

import util.Division;

public class Person {
	private String id;
	private String name;
	private Division div;
	
	public Person(String name, String id, Division div) {
		this.id = id;
		this.name = name;
		this.div = div;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Division getDiv() {
		return div;
	}
	
	public String toString() { 
		return "Name=" + name + ", ID=" + id;
	}
	
}
