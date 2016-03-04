package com.routegis.typerender;

import java.util.ArrayList;
import java.util.List;

import com.sun.xml.internal.ws.policy.jaxws.SafePolicyReader;

import core.routegis.engine.geom.Angle;
import core.routegis.engine.geom.LatLon;

public class Observation {
	private LatLon latlon;//观测点坐标
	private int station;//观测点编号
	private String type;//反演数据类型
	private int loc;//网格位置定位
	private int length;//数据长度
	private ArrayList<Double> datalist;//反演数据
	
	public Observation(){
		latlon=null;
		station=0;
		type="";
		loc=0;
		length=0;
		datalist=new ArrayList<>();		
	}
	
	public Observation(LatLon latlon,int station,String type,ArrayList<Double> datalist){
		this.latlon=latlon;
		this.station=station;
		this.type=type;
		this.datalist=datalist;
		loc=0;
		length=datalist.size();
	}
	
	public Observation(LatLon latlon,String type,ArrayList<Double> datalist){
		this.latlon=latlon;
		this.station=0;
		this.type=type;
		this.datalist=datalist;
		loc=0;
		length=datalist.size();
	}
	
	public void setType(String type){
		this.type=type;
	}
	
	public void setStation(int station){
		this.station=station;
	}
	
	public void setData(double data){
		this.datalist.add(data);
	}
	
	public void setDataList(ArrayList<Double> datalist){
		this.datalist=datalist;
	}
	
	public void setLatLon(LatLon latlon){
		this.latlon=latlon;
	}

	public void setLatLon(double lat,double lon){
		this.latlon=new LatLon(Angle.fromDegrees(lat), Angle.fromDegrees(lon));
	}
	
	public void setLoc(int gridnum){
		this.loc=gridnum;
	}
	
	public int getLoc(){
		return loc;
	}
	
	public LatLon getLatlon(){
		return latlon;		
	}
	
	public Angle getLat(){
		return latlon.getLatitude();
	}
	
	public Angle getLon(){
		return latlon.getLongitude();
	}
	
	public double getData(int index){
		return datalist.get(index);
	}
	
	public ArrayList<Double> getDataList(){
		return datalist;
	}
	
	public void popUsedData(){
		this.datalist.remove(0);
	}
	
	public int getDataLength(){
		return this.datalist.size();
	}
	
	public String getType(){
		return type;
	}
	
	public int getStation(){
		return station;
	}
	
	public static void swap(Observation a,Observation b){
		Observation temp=new Observation();
		temp.setDataList(b.getDataList());
		temp.setType(b.getType());
		temp.setLatLon(b.getLatlon());
		temp.setStation(b.getStation());
		temp.setLoc(b.getLoc());
		b.setDataList(a.getDataList());
		b.setType(a.getType());
		b.setLatLon(a.getLatlon());
		b.setStation(a.getStation());
		b.setLoc(a.getLoc());
		a.setDataList(temp.getDataList());
		a.setType(temp.getType());
		a.setLatLon(temp.getLatlon());
		a.setStation(temp.getStation());
		a.setLoc(temp.getLoc());		
	}
	



	public static void main(String[] args) {	
		/**for(Thread t:getThreads()){
		t.start();
		}
		}

		public static Thread[] getThreads(){
		Thread[] thread = new Thread[10];
		for(int i=0;i<10;i++){
		final Integer num = new Integer(i);
		thread[i] = new Thread(new Runnable(){
		public void run() {
		int j=5;
		while(j-->0){	
		System.out.println("this is thread"+num);	
		}	
		}
		});
		}
		return thread;
		}**/
		LatLon latlon1=new LatLon(Angle.fromDegrees(50), Angle.fromDegrees(50));
		ArrayList<Double> list1=new ArrayList<>() ;
		list1.add(2.3);
		list1.add(3.3);
        Observation ob1=new Observation(latlon1, "wave", list1);
        
        LatLon latlon2=new LatLon(Angle.fromDegrees(27), Angle.fromDegrees(35));
		ArrayList<Double> list2=new ArrayList<>() ;
		list2.add(1.3);
		list2.add(2.7);
        Observation ob2=new Observation(latlon2, "height", list2);
        
       ArrayList<Observation> oblist=new ArrayList<>();
       oblist.add(ob1);
       oblist.add(ob2);
       SectorDivision sd=new SectorDivision();
       sd.SectorConstructor(oblist, 20, 20);
       sd.LocateOfOb(oblist);
       sd.SpreadValue(oblist);


        
        
   }
}
