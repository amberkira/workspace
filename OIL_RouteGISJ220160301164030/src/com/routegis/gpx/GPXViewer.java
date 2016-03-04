package com.routegis.gpx;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

import com.routegis.applications.datachoose.GPXloader;
import com.routegis.applications.datachoose.ListLoader;
import com.routegis.applications.datachoose.NewDateChooserJButton;

public class GPXViewer extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7305034414289457560L;
	private JComboBox<String> name;//人员下拉菜单 
	private NewDateChooserJButton dataS;//GPX起始时间 
	private NewDateChooserJButton dataE;//GPX终止时间
	private JList<String> GPXList;//GPX文件选择界面
	private JButton loadListBtn;//查询list按钮
	private JButton allLoadBtn;//针对复选的加载
	private JButton clearBtn;//清空当前已加载Gpx轨迹按钮
	private JPanel namePanel;//1
	private JPanel timePanel;//2
	private JPanel listPanel;//3
	private JPanel buttonPanel;//4
	public GPXloader gpxLoader;
	public ListLoader listLoader;
	public String uri;//数据库返回uri参数
	private String cachefile;//applet数据路径
	public List<String> selectedList;//记录选中的GPX条目
	
	public  GPXViewer(){
		//get
		//BoxLayout bx=new BoxLayout(this, BoxLayout.Y_AXIS);
		//setLayout(bx);		
		namePanel=namePanel();
		timePanel=timePanel();
		listPanel=listPanel();
		buttonPanel=buttonPanel();
		add(namePanel);
		add(timePanel);
		add(listPanel);
		add(buttonPanel);
	}
	private JPanel namePanel() {
		// TODO Auto-generated method stub
				return null;		
	}

	private JPanel timePanel() {
		// TODO Auto-generated method stub
		return null;
	}

	private JPanel listPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	private JPanel buttonPanel() {
		// TODO Auto-generated method stub
		return null;
	}

}
