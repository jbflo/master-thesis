package org.micromanager.rapp.MultiFOV;

import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.rapp.RappGui;
import org.micromanager.rapp.SequenceAcquisitions.SeqAcqGui;
import org.micromanager.rapp.utils.AboutGui;
import org.micromanager.rapp.utils.FileDialog;
import org.micromanager.utils.ReportingUtils;

import javax.swing.*;
import java.awt.*;        // Using AWT's Graphics and Color
import java.awt.event.*;  // Using AWT's event classes and listener interfaces
import java.util.ArrayList;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class FOV_GUI extends JInternalFrame {

    protected JPanel main_well_panel;
    FOV_Controller FOV_control;

    private stageControl myFrame_;

    private JLabel title_lbl;
    private JRadioButton rdbtnSelectWells_;
    private JRadioButton rdbtnMoveStage_;

    private final JLabel txt_pos_Label;
    private final JLabel txt_plate_Label;
    private final JLabel txt_calirate_Label;
    private JButton calibrateXY_btn ;
    private JButton putStageInFirstWell;
    public static JTextField rootField_xmlWellFile;
    private  final JButton browseRootButton_plate;
    private JComboBox plateIDCombo_;

    private static RappGui parent_ = RappGui.getInstance();
    protected AboutGui  dlgAbout;
    private wellPanel well_panel;

    protected static int well_plate_type;
    private  CMMCore core_ ;
    private  ScriptInterface app_;


    // Constructor to setup the UI components and event handlers
    public FOV_GUI(RappGui parent, CMMCore core, ScriptInterface app) {
        parent_ = parent;
        core_ = core;
        app_ = app;
       // getContentPane().setLayout(null);

        // Set JFrame Form and Location
        BasicInternalFrameUI bi = (BasicInternalFrameUI)this.getUI();
        bi.setNorthPane(null);
        setLocation(0,0);

        FOV_control = new FOV_Controller(core_, app_);

        FOVTableModel FOVTableModel_ = new FOVTableModel(FOV_control);

        main_well_panel = new JPanel();
        main_well_panel.setLayout(null);
        main_well_panel.setBackground(Color.decode("#34495e"));

        txt_plate_Label = new JLabel();
        txt_plate_Label.setFont(new Font("Arial", Font.PLAIN, 12));
        txt_plate_Label.setForeground(Color.white);
        txt_plate_Label.setText("Plate Format :");
        txt_plate_Label.setBounds(220, 2, 150, 26);
        main_well_panel.add(txt_plate_Label);

        plateIDCombo_ = new JComboBox();
        plateIDCombo_.setBounds(220, 30, 150, 24);
        main_well_panel.add(plateIDCombo_);

        // here we add all the plate type from the XML fILE ,  to the JComboBox
//        ArrayList plateListe = FOV_Controller.wellTypes;
//        if(plateListe.size() != 0) {
//            boolean valide = FOV_Controller.valideXml( FOV_Controller.readXmlFile(file, well_plate_type));
//            plateIDCombo_.addItem(plateListe.iterator().next());
//        }

//        plateIDCombo_.addItem(FOV_Controller.MATRI_12_WELL);
//        plateIDCombo_.addItem(FOV_Controller.MATRI_24_WELL);
//        plateIDCombo_.addItem(FOV_Controller.MATRI_24_WELL);
//        plateIDCombo_.addItem(FOV_Controller.MATRI_48_WELL);
//        plateIDCombo_.addItem(FOV_Controller.MATRI_96_WELL);
//        plateIDCombo_.addItem(FOV_Controller.MATRI_384_WELL);
//        plateIDCombo_.addItem(FOV_Controller.SLIDE_HOLDER);


        plateIDCombo_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                String file = rootField_xmlWellFile.getText();

                String plateChoosed = (String) plateIDCombo_.getSelectedItem();

                if (file.equals("")) {
                    ReportingUtils.showMessage(" Please Choose well map configuration file before");
                }
                else {
                    boolean valide = FOV_Controller.valideXml( FOV_Controller.readXmlFile(file, plateChoosed));
                    if (valide){
                        well_panel = new wellPanel(FOV_GUI.this, core_);
                        well_panel.setBounds(0, 70,600, 410);
                        well_panel.setBackground(Color.decode("#edf3f3"));
                        main_well_panel.add(well_panel);
                        well_panel.repaint();
                    }else {
                        ReportingUtils.showMessage(" Please Choose a correct xml configuration file to load Well Map");
                    }
                }
            }
        });

        txt_pos_Label = new JLabel();
        txt_pos_Label.setFont(new Font("Arial", Font.PLAIN, 12));
        txt_pos_Label.setForeground(Color.white);
        txt_pos_Label.setText("Load plate configuration file :");
        txt_pos_Label.setBounds(10, 2, 160, 26);
        main_well_panel.add(txt_pos_Label);

        rootField_xmlWellFile = new JTextField();
        rootField_xmlWellFile.setFont(new Font("Arial", Font.PLAIN, 10));
        rootField_xmlWellFile.setBounds(60, 30, 140, 24);
        rootField_xmlWellFile.setEditable(false);
        main_well_panel.add(rootField_xmlWellFile);

        browseRootButton_plate = new JButton();
        browseRootButton_plate.addActionListener(e->{
            String path = FileDialog.xmlFileChooserDialog("Load plate configuration file :");
            rootField_xmlWellFile.setText(path);
            ArrayList plateListe = FOV_Controller.wellTypes;
            plateIDCombo_.removeAllItems();
                boolean valide = FOV_Controller.valideXml( FOV_Controller.readXmlFileOnce(path));
                if (valide) {
                    for(int i = 0; i< plateListe.size(); i ++) {
                        plateIDCombo_.addItem(plateListe.get(i));
                    }
                }
        });

        browseRootButton_plate.setMargin(new Insets(2, 5, 2, 5));
        browseRootButton_plate.setFont(new Font("Dialog", Font.PLAIN, 10));
        browseRootButton_plate.setText("...");
        browseRootButton_plate.setBounds(10, 30, 45, 24);
        main_well_panel.add(browseRootButton_plate);
        browseRootButton_plate.setToolTipText("Load well map configuration");

        //////////////////////////////////////////////////////////////////////////////////////////////////

        txt_calirate_Label = new JLabel();
        txt_calirate_Label.setFont(new Font("Arial", Font.PLAIN, 12));
        txt_calirate_Label.setForeground(Color.white);
        txt_calirate_Label.setText("XY Stage Calibbration Process :");
        txt_calirate_Label.setBounds(410, 2, 160, 26);
        main_well_panel.add(txt_calirate_Label);

        calibrateXY_btn = new JButton();
        calibrateXY_btn.setText("Calibrate XY...");
        calibrateXY_btn.setBounds(430, 30, 120, 24);
        main_well_panel.add(calibrateXY_btn);
        calibrateXY_btn.addActionListener(evt->{
            FOV_control.calibrateXY();
        });



        posPanel xyPos_panel = new posPanel(this, core_);
        xyPos_panel.setBackground(Color.decode("#edf3f3"));

        if (myFrame_ == null) {
            myFrame_ = new stageControl(app_);
            // myFrame_.setBackground(app_.getBackgroundColor());
        }
        myFrame_.initialize();
        myFrame_.setVisible(true);
        myFrame_.setBounds(1,260, 400, 349 );
        xyPos_panel.add(myFrame_);

        putStageInFirstWell = new JButton();
        putStageInFirstWell.setText("Move Stage to Well A1...");
        putStageInFirstWell.setBounds(270,484 , 160, 24);
        main_well_panel.add(putStageInFirstWell);
        putStageInFirstWell.addActionListener(evt->{
            FOV_control.moveStageToA1();
        });

        ButtonGroup toolButtonGroup = new ButtonGroup();

        rdbtnSelectWells_ = new JRadioButton("Select Wells");
        rdbtnSelectWells_.setSelected(true);
        rdbtnSelectWells_.setBounds(10, 484, 120, 24);
        toolButtonGroup.add(rdbtnSelectWells_);
        main_well_panel.add(rdbtnSelectWells_);
        rdbtnSelectWells_.addActionListener(arg0->{
            if (rdbtnSelectWells_.isSelected()) {
                wellPanel.stageAction_move.set(false);
                System.out.println("Select");
            }
        });

        rdbtnMoveStage_ = new JRadioButton("Move Stage");
        toolButtonGroup.add(rdbtnMoveStage_);
        rdbtnMoveStage_.setBounds(140, 483, 120, 24);
        toolButtonGroup.add(rdbtnMoveStage_);
        main_well_panel.add(rdbtnMoveStage_);
        rdbtnMoveStage_.addActionListener(e->{
            if (rdbtnMoveStage_.isSelected()) {
                wellPanel.stageAction_move.set(true);
                System.out.println("Move");
            }
        });

        JSplitPane splitPaneBody = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, main_well_panel, xyPos_panel);
        splitPaneBody.setDividerLocation(600);
        this.add(splitPaneBody,BorderLayout.CENTER);

        //setDefaultCloseOperation(0);
        setTitle("Set Multi");
       // setSize(900, 445);
       // setResizable(false);
        //  setLocationRelativeTo(null);  // center the application window
        setVisible(true);
    }



}

