package edu.tju.powersaving.utils;

public class Appliance {

	public String Hostname;
	public String IP_Addr;
	public String ApplianceName;

	
	public Appliance(String host, String IP, String applicance_name){
		Hostname=host;
		IP_Addr=IP;
		ApplianceName=applicance_name;
	}
	
	@Override
	public String toString(){
		//String s=Hostname.split(".")[0];
		return Hostname;
	}

}