package com.routegis.applications.datachoose;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.text.ParseException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DebugGraphics;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;

import com.routegis.users.applet.CommonApplet;

public class DateChooserJPanel extends JPanel {
      private static DateChooserJPanel DCJ;
	  private NewDateChooserJButton bt1;
      private NewDateChooserJButton bt2;
      private JButton ConfirmButton;
      private JComboBox<String> TypeJPanelBox;
      private JComboBox<String> TimedetlaBox;
      public  CommonApplet app;
      private DateChooserJPanel(CommonApplet app){
    	  this.app=app;
    	  BoxLayout bx=new BoxLayout(this, BoxLayout.Y_AXIS);

    	  setLayout(bx);
    	  setPreferredSize(new Dimension(260,300));
    	  setMaximumSize(new Dimension(260, 300));
          JPanel TimeStartJPanel=TimeStartJPanel();
          add(TimeStartJPanel);
          JPanel TimeEndJPanel=TimeEndJPanel();
          add(TimeEndJPanel);
          JPanel  Timedetla=Timedetla();
          add(Timedetla);
          JPanel TypeJPanel=TypeJPanel();
          add(TypeJPanel);
          JPanel SubmitJPanel=SubmitJPanel();
          add(SubmitJPanel);
          JPanel jb1=new JPanel();
          add(jb1);
          JPanel jb2=new JPanel();
          add(jb1);
          JPanel jb3=new JPanel();
          add(jb1);

   
      }
     public static DateChooserJPanel getInstance(CommonApplet app){
    	    if(DCJ==null){
    	    	synchronized (DateChooserJPanel.class) {
					if(DCJ==null)
						DCJ=new DateChooserJPanel(app);
					
				}
    	    }
    	       return DCJ;
      }
      
      
     
      
      
	public JPanel SubmitJPanel() {
		JPanel jp=new JPanel();
		jp.setBounds(300, 300, 300, 30);
		ConfirmButton=new JButton("确认");
		ConfirmButton.setPreferredSize(new Dimension(300, 30));
		ConfirmButton.setMaximumSize(new Dimension(300, 30));
		
		ConfirmButton.addActionListener(new ActionListener() {
			
			
			public void actionPerformed(ActionEvent e) {
				String str_time_start=bt1.getText();
				String str_time_end=bt2.getText();
				String str_time_detla= (String) TimedetlaBox.getSelectedItem();
				String str_type=(String) TypeJPanelBox.getSelectedItem();
				String[] result=new String[4];
				result[0]=str_time_start;
				result[1]=str_time_end;
				result[2]=str_time_detla;
				result[3]=str_type;
				/*
                try {
					app.getFeedback(result);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				*/
			}
		});
		
		jp.setLayout(null);
		jp.add(ConfirmButton);
		ConfirmButton.setBounds(20, 0, 220, 30);
		return jp;
	}
	
	
	public JPanel TypeJPanel() {
		JPanel jp=new JPanel();
		JLabel TypeJPanelLabel=new JLabel("反演类型");	
		TypeJPanelBox=new JComboBox<>();
		TypeJPanelBox.setPreferredSize(new Dimension(100, 30));
		TypeJPanelBox.setMaximumSize(new Dimension(100, 30));
		TypeJPanelBox.addItem("海风");
		TypeJPanelBox.addItem("海浪");
		TypeJPanelBox.addItem("海冰");
		TypeJPanelBox.addItem("溢油");
		TypeJPanelBox.addItem("人员");
		TypeJPanelBox.setSelectedItem("海风");

		jp.setLayout(null);
		jp.add(TypeJPanelLabel);
        TypeJPanelLabel.setBounds(20, 10, 70, 30);
		jp.add(TypeJPanelBox);
		TypeJPanelBox.setBounds(150, 10, 100, 30);
		return jp;
	}
	
	public JPanel Timedetla() {
		JPanel jp=new JPanel();
		JLabel TimedetlaLabel=new JLabel("反演间隔");
		TimedetlaBox=new JComboBox<>();
		TimedetlaBox.setPreferredSize(new Dimension(100, 30));
		TimedetlaBox.setMaximumSize(new Dimension(100, 30));
		TimedetlaBox.addItem("分");
		TimedetlaBox.addItem("时");
		TimedetlaBox.addItem("天");
		TimedetlaBox.setSelectedItem("分");
		jp.setLayout(null);
		jp.add(TimedetlaLabel);
		TimedetlaLabel.setBounds(20, 10, 70, 30);
		jp.add(TimedetlaBox);
		TimedetlaBox.setBounds(150, 10, 100, 30);
		return jp;
	}
	
	
	public JPanel TimeEndJPanel() {
		JPanel jp=new JPanel();
		JLabel TimeEndJLabel=new JLabel("结束时间");
		bt2=new NewDateChooserJButton();
		jp.setLayout(null);
		jp.add(TimeEndJLabel);
		TimeEndJLabel.setBounds(21, 10, 70, 30);
		jp.add(bt2);
		bt2.setBounds(120, 10, 130, 30);
		return jp;
	}
	
	
	public JPanel TimeStartJPanel() {
		JPanel jp=new JPanel();
		JLabel TimeStartJLabel=new JLabel("开始时间");
		bt1=new NewDateChooserJButton();
		jp.setLayout(null);
		jp.add(TimeStartJLabel);
		TimeStartJLabel.setBounds(21, 10, 70, 30);
		jp.add(bt1);
		bt1.setBounds(120, 10, 130, 30);
		return jp;
	}
	
/*	public String[] getresult(){			 
		return result;		
	}*/

/*
 * ���Դ���
 */
/*public static void main(String[] args) {
    JFrame mainFrame = new JFrame("����");
    JFrame mainFrame2 = new JFrame();
    mainFrame2.setLayout(null);
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame2.setSize(300, 400);
    DateChooserJPanel desktop =   DateChooserJPanel.getInstance();
    mainFrame2.getContentPane().add(desktop);
    desktop.setBounds(0, 0, 300, 400);
    //mainFrame.getContentPane().add(mainFrame2);
    mainFrame2.setVisible(true);
}*/
}
