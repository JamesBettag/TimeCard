/*
 * Author: James Bettag
 * Completed: 7 June 2019
 * Filename: TimeCard.java
 */

package com.company;

import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class Main
{
    private static String DBUser;
    private static String pass;
    private static String url;
    private static ArrayList<Pair<String, Integer>> employees;

    private static ActionListener listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Boolean insert = false;
            Integer employeeID;
            if(e.getSource() instanceof JButton) {
                int n = JOptionPane.showConfirmDialog(null, "Would you like to Clock In/Out?", ((JButton) e.getSource()).getText(), JOptionPane.YES_NO_OPTION);
                if(n == 0) {
                    for(Pair<String, Integer> i : employees) {
                        if(i.getKey().equals(((JButton) e.getSource()).getText())) {
                            employeeID = i.getValue();
                            insert = AddTime(employeeID);
                        }
                    }
                    if(insert) {
                        JOptionPane.showMessageDialog(null, "Success!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Could Not Update Time!", ((JButton) e.getSource()).getText(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    };

    private static class MyLayout extends JPanel
    {
        private MyLayout(ArrayList<Pair<String, Integer>> employees) {
            JButton myButton;
            setLayout(new GridLayout(employees.size(), 1));
            for(Pair<String, Integer> i : employees) {
                myButton = new JButton(i.getKey());
                myButton.setPreferredSize(new Dimension(250, 50));
                myButton.setFont(new Font("Arial", Font.BOLD, 14));
                myButton.addActionListener(listener);
                add(myButton);
            }
        }
    }

    private static ArrayList<Pair<String, Integer>> GetEmployees()
    {
        ArrayList<Pair<String, Integer>> myEmployees = new ArrayList<>();
        Pair<String, Integer> singleEmployee;
        ResultSet rs;
        Statement s1;
        String query = "SELECT Fname, Lname, ID FROM Employees WHERE active = 'true'";
        String employeeName = "";
        Integer employeeID;

        try{
            Connection con = null;
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(url, DBUser, pass);
            s1 = con.createStatement();
            rs = s1.executeQuery(query);


            if(rs != null) {
                if(rs.isBeforeFirst()) {
                    while(rs.next()) {
                        employeeName = rs.getString(1) + " " + rs.getString(2);
                        employeeID = Integer.parseInt(rs.getString(3));
                        singleEmployee = new Pair<>(employeeName, employeeID);
                        myEmployees.add(singleEmployee);
                    }
                }
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Invalid Database Handshake, please check credentials.");
        }
        return myEmployees;
    }

    private static Boolean AddTime(int employeeID)
    {
        Boolean added = true;
        PreparedStatement ps;
        LocalDateTime now = LocalDateTime.now();
        Date epoch, current = new Date();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String insertTimeToDatabase = "";
        int payPeriod;

        try{
            epoch = sdf.parse("12/31/2017");
            payPeriod = (int) (((((current.getTime() - epoch.getTime()) / 1000) / 60) / 60) / 24) / 14;
            insertTimeToDatabase = "INSERT INTO TCLog (EmployeeID, LogTime, LogType, PayPeriod, active) VALUES ('" + employeeID + "', '" + timeFormatter.format(now) + "', 'P', " + payPeriod + ", 'TRUE')";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection con = DriverManager.getConnection(url, DBUser, pass);
            ps = con.prepareStatement(insertTimeToDatabase);
            ps.executeUpdate();
            con.close();
        } catch (Exception e) {
            added = false;
        }
        return added;
    }

    private static void getDatabase()
    {
        String filename = "myDatabase.txt";
        try{
            BufferedReader br = new BufferedReader(new FileReader(filename));
            DBUser = br.readLine();
            pass = br.readLine();
            url = br.readLine();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        getDatabase();
        employees = new ArrayList<>();
        MyLayout mainPanel;

        employees = GetEmployees();
        mainPanel = new MyLayout(employees);

        JFrame frame = new JFrame("Time Card");
        frame.setUndecorated(true);
        frame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(mainPanel);
        frame.pack();
        frame.setLocation(dim.width/2 - frame.getSize().width/2, dim.height/2 - frame.getSize().height/2);
        frame.setVisible(true);
    }
}
