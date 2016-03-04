package com.routegis.typerender;

import java.util.ArrayList;

import core.routegis.engine.geom.Angle;
import core.routegis.engine.geom.Sector;

public class SectorDivision {
	private double minlatitude;
	private double maxlatitude;
	private double minlongitude;
	private double maxlongitude;
	public static double A=2*Math.PI/45;
	public static double B=2*Math.PI/30;
	public static double C=2*Math.PI/25;
	private int width;
	private int height;
    private double[] values;
	
	public  Sector SectorConstructor(ArrayList<Observation> oblist,int width,int height){
		this.width=width;
		this.height=height;
		values=new double[width*height];
	    Angle minlat=Angle.fromDegrees(90);
	    Angle maxlat=Angle.fromDegrees(-90);
        Angle minlon=Angle.fromDegrees(180);
        Angle maxlon=Angle.fromDegrees(-180);
		if(!oblist.isEmpty()){			
			for (Observation obser : oblist) {
				minlat=Angle.min(obser.getLat(), minlat);
				maxlat=Angle.max(obser.getLat(), maxlat);
				minlon=Angle.min(obser.getLon(), minlon);
				maxlon=Angle.max(obser.getLon(), maxlon);
			}
		}
		minlatitude=minlat.degrees;
		maxlatitude=maxlat.degrees;
		minlongitude=minlon.degrees;
		maxlongitude=maxlon.degrees;
		Sector s=new Sector(minlat, maxlat, minlon, maxlon);
		return s;		
	}
	/**
	 * 观测点组位置规则化--定位以及排序
	 * @param oblist
	 */
	public ArrayList<Observation> LocateOfOb(ArrayList<Observation> oblist){
		double Xstride=Math.abs((maxlatitude-minlatitude)/(width-1));//获取网格横向步进
		double Ystride=Math.abs((maxlongitude-minlongitude)/(height-1));//纵向步进
		for (Observation ob : oblist) {	
		    int x=(int) ((ob.getLat().degrees-minlatitude)/Xstride);
			int y=(int) ((ob.getLon().degrees-minlongitude)/Ystride);
			ob.setLoc(x+y*width);
		    }
		//按照观测点在网格中的位置大小排列新的观测点表
		for (int i=oblist.size()-1;i>0;i--) {
			for(int j=0;j<i;j++){
				if(oblist.get(j+1).getLoc()<oblist.get(j).getLoc()){
					Observation.swap(oblist.get(j+1), oblist.get(j));			
				}
			}
		}
		return oblist;
	}
	/**
	 * 为了规避部分数据缺失造成的各数据点实际观测数据量不一致，这里规整统一使用所有观测点中数据量最少的为标准(有待商榷)
	 * （不过存在新增观测点与老观测点同时查询时会出现各自持有数据量差异巨大的情况）这里会定义了报错机制。
	 * @param oblist
	 */
	public void UniformOriginalDataLength(ArrayList<Observation> oblist){
		
	}
	/**
	 * 短期内线程不多的演示用这个 之后加入线程池保证效率
	 * @param oblist
	 * @param values
	 */
	public double[] SpreadValue(ArrayList<Observation> oblist){
		double sum=0;
		for (Observation observation : oblist) {
			sum+=observation.getData(0);
		}
		double mean=sum/oblist.size();

		for(int i=0;i<oblist.size()-1;i++){
		 int spota=oblist.get(i).getLoc();
		 values[spota]=oblist.get(i).getData(0);
		 oblist.get(i).popUsedData();
		 int spotb=oblist.get(i+1).getLoc();
		 values[spotb]=oblist.get(i+1).getData(0);
		 oblist.get(i+1).popUsedData();
		 Spreading(spota,spotb,values);
		}
		CompleteGrid(mean,values);
		return values;
	}

/**
 * 
 * @param sptA较小位置点
 * @param sptB较大位置点
 * @param values数据保存
 */
	private void Spreading(int sptA,int sptB,double[] values) {		
		int y1=(int)sptA/width;
		int x1=sptA-y1*width;
		int y2=(int)sptB/width;
		int x2=sptB-y2*width;
		int minx=Math.min(x1, x2);
		int miny=Math.min(y2, y1);
		int dx=Math.abs(x2-x1);
		int dy=Math.abs(y2-y1);
		double distance=Math.sqrt(Math.pow(dy,2)+Math.pow(dx,2 ));
		double offset=(Math.abs(values[sptA]-values[sptB]))/distance;
		for(int j=0;j<=dy;j++){
			for(int i=0;i<=dx;i++){
				
			    double disV=Math.sqrt(Math.pow(Math.abs(minx+i-x1),2)
			    		             +Math.pow(Math.abs(miny+j-y1),2))*offset;
			    int num=minx+i+(miny+j)*width;
			    values[num]=Math.min(values[sptA], values[sptB])+disV;
			}
		}	
	}
	
	private void CompleteGrid(double mean,double[] values) {
		for (double d : values) {
			if(d==0)
				d=mean;
		}
	}
}
