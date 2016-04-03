package EastWeb_UserInterface.PluginWindow.PluginExtension;

import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import com.toedter.calendar.JDateChooser;

import EastWeb_UserInterface.DocumentBuilderInstance;
import EastWeb_UserInterface.PluginWindow.BasePlugin;
import EastWeb_UserInterface.PluginWindow.IPlugin;

public class NldasForcingPluginUI extends BasePlugin {
    private JDateChooser freezingDateChooser;
    private JTextField coolingTextField;
    private JDateChooser heatingDateChooser;
    private JTextField heatingTextField;
    private JLabel lblNldasForcing;
    private JLabel lblFreezingStartDate;
    private JLabel lblHeatingStartDate;
    private JLabel lblCoolingDegreeThreshold;
    private JLabel lblHeatingDegreeThreshold;

    private String freezingDate;
    private String coolingDegree;
    private String heatingDate;
    private String heatingDegree;

    public NldasForcingPluginUI(String PluginName, String QCLevel, ArrayList<String> Indicies) {
        super(PluginName, QCLevel, Indicies);
    }

    public NldasForcingPluginUI() {
    }

    @Override
    public void Save() {
        SetFreezingDate(freezingDateChooser.getDate().toString());
        SetCoolingDegree(coolingTextField.getText());
        SetHeatingDate(heatingDateChooser.getDate().toString());
        SetHeatingDegree(heatingTextField.getText());
    }

    @Override
    public IPlugin GetParseObject(NodeList nodeList, int itemNumber) {
        NldasForcingPluginUI parsePlugin = super.GetParseObject(nodeList.item(itemNumber), NldasForcingPluginUI.class);
        parsePlugin.SetFreezingDate(GetNodeListValuesIgnoreIfEmpty(((Element)nodeList.item(itemNumber))
                .getElementsByTagName("FreezingDate")).get(0));
        parsePlugin.SetCoolingDegree(GetNodeListValuesIgnoreIfEmpty(((Element)nodeList.item(itemNumber))
                .getElementsByTagName("CoolingDegree")).get(0));
        parsePlugin.SetHeatingDate(GetNodeListValuesIgnoreIfEmpty(((Element)nodeList.item(itemNumber))
                .getElementsByTagName("HeatingDate")).get(0));
        parsePlugin.SetHeatingDegree(GetNodeListValuesIgnoreIfEmpty(((Element)nodeList.item(itemNumber))
                .getElementsByTagName("HeatingDegree")).get(0));

        return parsePlugin;
    }

    public void SetHeatingDegree(String string) {
        freezingDate = string;
    }

    public void SetHeatingDate(String string) {
        coolingDegree = string;
    }

    public void SetCoolingDegree(String string) {
        heatingDate = string;
    }

    public void SetFreezingDate(String string) {
        heatingDegree = string;
    }

    public String GetHeatingDegree() {
        return freezingDate;
    }

    public String GetHeatingDate() {
        return coolingDegree;
    }

    public String GetCoolingDegree() {
        return heatingDate;
    }

    public String GetFreezingDate() {
        return heatingDegree;
    }

    @Override
    public JPanel SetupUI(JPanel NldasForcingPanel, JFrame frame) {
        lblNldasForcing = new JLabel("Nldas Forcing");
        lblNldasForcing.setFont(new Font("Courier", Font.BOLD,15));
        lblNldasForcing.setBounds(400, 41, 150, 20);

        NldasForcingPanel.add(lblNldasForcing);
        NldasForcingPanel.setLayout(null);
        NldasForcingPanel.setBounds(359, 420, 275, 390);

        lblFreezingStartDate = new JLabel("Freezing Date: ");
        lblFreezingStartDate.setBounds(338, 107, 100, 14);
        NldasForcingPanel.add(lblFreezingStartDate);

        freezingDateChooser = new JDateChooser();
        freezingDateChooser.setDateFormatString("MMM d");
        freezingDateChooser.setBounds(430, 101, 120, 27);
        NldasForcingPanel.add(freezingDateChooser);

        lblHeatingStartDate = new JLabel("Heating Date:");
        lblHeatingStartDate.setBounds(338, 170, 100, 14);
        NldasForcingPanel.add(lblHeatingStartDate);

        heatingDateChooser = new JDateChooser();
        heatingDateChooser.setDateFormatString("MMM d");
        heatingDateChooser.setBounds(430, 165, 120, 27);
        NldasForcingPanel.add(heatingDateChooser);

        lblCoolingDegreeThreshold = new JLabel("Cooling degree:");
        lblCoolingDegreeThreshold.setToolTipText("Cooling degree threshold");
        lblCoolingDegreeThreshold.setBounds(338, 137, 100, 14);
        NldasForcingPanel.add(lblCoolingDegreeThreshold);

        coolingTextField = new JTextField();
        coolingTextField.setColumns(10);
        coolingTextField.setBounds(430, 132, 75, 25);
        NldasForcingPanel.add(coolingTextField);

        lblHeatingDegreeThreshold = new JLabel("Heating degree:");
        lblHeatingDegreeThreshold.setToolTipText("Heating degree threshold");
        lblHeatingDegreeThreshold.setBounds(338, 202, 100, 14);
        NldasForcingPanel.add(lblHeatingDegreeThreshold);

        heatingTextField = new JTextField();
        heatingTextField.setColumns(10);
        heatingTextField.setBounds(430, 197, 75, 25);
        NldasForcingPanel.add(heatingTextField);

        return NldasForcingPanel;
    }

    @Override
    public String GetUIDisplayPlugin() {
        String freezingstartDate = String.format("<br>FreezingDate: %s</span>",freezingDateChooser.getDate().toString());
        String coolingDegree = String.format("<br>CoolingDegree: %s</span>",coolingTextField.getText());
        String heatingstartDate = String.format("<br>HeatingDate: %s</span>",heatingDateChooser.getDate().toString());
        String heatingDegree = String.format("<br>HeatingDegree: %s</span>",heatingTextField.getText());

        return String.format("<html>%s%s%s%s%s</html>",super.GetUIDisplayPlugin(),
                freezingstartDate,
                coolingDegree,
                heatingstartDate,
                heatingDegree);
    }

    @Override
    public Element GetXMLObject() throws ParserConfigurationException {
        Element p  = super.GetXMLObject();
        Element nldasForcing = DocumentBuilderInstance.Instance().GetDocument().createElement("NldasForcing");

        // Freezing start Date
        Element freezingstartDate = DocumentBuilderInstance.Instance().GetDocument().createElement("FreezingDate");
        freezingstartDate.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(freezingDateChooser.getDate().toString()));
        nldasForcing.appendChild(freezingstartDate);

        // Cooling degree value
        Element coolingDegree = DocumentBuilderInstance.Instance().GetDocument().createElement("CoolingDegree");
        coolingDegree.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(coolingTextField.getText()));
        nldasForcing.appendChild(coolingDegree);

        // Heating start Date
        Element heatingstartDate = DocumentBuilderInstance.Instance().GetDocument().createElement("HeatingDate");
        heatingstartDate.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(heatingDateChooser.getDate().toString()));
        nldasForcing.appendChild(heatingstartDate);

        // heating degree value
        Element heatingDegree = DocumentBuilderInstance.Instance().GetDocument().createElement("HeatingDegree");
        heatingDegree.appendChild(DocumentBuilderInstance.Instance().GetDocument().createTextNode(heatingTextField.getText()));
        nldasForcing.appendChild(heatingDegree);

        p.appendChild(nldasForcing);

        return p;
    }

    @Override
    public void ClearUI(JPanel Panel) {
        Panel.remove(heatingTextField);
        Panel.remove(coolingTextField);
        Panel.remove(heatingDateChooser);
        Panel.remove(freezingDateChooser);
        Panel.remove(lblNldasForcing);
        Panel.remove(lblHeatingStartDate);
        Panel.remove(lblCoolingDegreeThreshold);
        Panel.remove(lblHeatingDegreeThreshold);
        Panel.remove(lblFreezingStartDate);
    }


}