package com.routegis.applications.datachoose;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

public class GPXutils extends JPanel{
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
	
	
	/**
	 * 在构造该JPanel时就访问数据库获取人员下拉菜单的树形表
	 */
	public  GPXutils(){
		getTree();
		BoxLayout bx=new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(bx);		
		namePanel=namePanel();
		timePanel=timePanel();
		listPanel=listPanel();
		buttonPanel=buttonPanel();
		add(namePanel);
		add(timePanel);
		add(listPanel);
		add(buttonPanel);
		
	}



	private JPanel buttonPanel() {
		// TODO Auto-generated method stub
		buttonPanel=new JPanel();
		loadListBtn=new JButton("获取轨迹");
		allLoadBtn=new JButton("加载");
		clearBtn=new JButton("清空");
		loadListBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				
				if (!isListOn()) {
					LoadList();
					
				}else{
					
				}
			}
				
				

		});
		
	    allLoadBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedList=GPXList.getSelectedValuesList();
				//该方法可以获取到List上所有选中的item返回的是一个list，需要解析
				//需要得知一个与服务器对接的方法？get？数据库访问需要重写gis.php 
				if(uri!=null&&cachefile!=null)
				gpxLoader.analysedata(uri, cachefile);
				
			}

			
		});
	    
	    clearBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				gpxLoader.cleardata();
				
			}
		});
		return buttonPanel;
	}


/**
 * 判断当前list列表是否为空 false直接加载  true重新填Jlist
 * @return
 */
	protected boolean isListOn() {
		// TODO Auto-generated method stub
		return false;
	}



	private JPanel listPanel() {
		getlist();// TODO Auto-generated method stub
		DefaultListModel<String> model=new DefaultListModel<>();
		addList(model);
		GPXList=new JList<>(model);
		GPXList.setSelectionMode
		            (ListSelectionModel.SINGLE_SELECTION);//default模式是支持多选这里单选是用作测试；
		selectedList=GPXList.getSelectedValuesList();//该方法可以获取到List上所有选中的item返回的是一个list，需要解析
		listPanel=new JPanel();
		listPanel.setLayout(null);
		listPanel.add(GPXList);
		return listPanel;
	}






	private JPanel timePanel() {
		timePanel=new JPanel();
		dataS=new NewDateChooserJButton();
		dataE=new NewDateChooserJButton();
		timePanel.setLayout(null);
		timePanel.add(dataS);
		timePanel.add(dataE);
		//TODO 写入ischanged()函数判定是否提交至服务器
		
		return timePanel;
	}


	
/**
 * namePanel设置selectListener
 * 回调访问服务器获取相关的GPX记录
 * @return
 */
	private JPanel namePanel() {
		namePanel=new JPanel();
		name=new JComboBox<>();
		addNameTree(name);
		name.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				String s=name.getSelectedItem().toString();//获取选中人员；
				//TODO 可能会需要加入判断函数
			}
		});
		namePanel.setLayout(null);
		namePanel.add(name);
		return namePanel;
	}
	
	
/**
 * 访问服务器获取成员树
 * @return
 */
	private Object getTree() {			
	   	return null;			
		}
	
/**
 * 获取GPXlist
 * @return
 */
	private Object getlist() {
		// TODO Auto-generated method stub
		return null;			
	}

/**
 * 添加成员树至JComBox内（之后变为两参数，还有一个参数是从服务器返回的成员树结果目前类型还不清楚）
 * @param JComboBox
 */
    private void addNameTree(JComboBox<String> JComboBox) {
	// TODO 添加成员树至JComBox内
}
	

/**
 * 将GPXlist加入model并显示（之后变为两参数，还有一个参数是从服务器返回的成员树结果目前类型还不清楚）
 * @param model
 */

   private void addList(DefaultListModel<String> model){
	listLoader.getModel(model);
	listLoader.analysedata(uri, cachefile);//通过uri获取符合要求的GPx id;
	
}
/**
 * 动态加载List
 */
   private void LoadList(){
	
}

   
}

