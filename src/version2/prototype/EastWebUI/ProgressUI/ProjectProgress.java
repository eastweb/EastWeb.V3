package version2.prototype.EastWebUI.ProgressUI;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JList;

import java.util.Iterator;
import java.util.TreeMap;

import version2.prototype.Config;
import version2.prototype.EASTWebManager;
import version2.prototype.ErrorLog;
import version2.prototype.GUIUpdateHandler;
import version2.prototype.Scheduler.SchedulerStatus;

public class ProjectProgress {

    private JFrame frame;
    private JList<String> logList;
    private JProgressBar downloadProgressBar;
    private JProgressBar processProgressBar;
    private JProgressBar indiciesProgressBar;
    private JProgressBar summaryProgressBar;

    private DefaultListModel<String> itemLog;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ProjectProgress window = new ProjectProgress(null);


                } catch (Exception e) {
                    ErrorLog.add(Config.getInstance(), "ProjectProgress.main problem with running a ProjectProgress window.", e);
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ProjectProgress(String projectName) {
        initialize();

        EASTWebManager.RegisterGUIUpdateHandler(new GUIUpdateHandlerImplementation(projectName));
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 400, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setVisible(true);

        CreateProgressView();
        CreateLogView();
    }

    private void CreateProgressView() {
        JPanel panel = new JPanel();
        panel.setBounds(10, 11, 364, 125);
        panel.setBorder(new TitledBorder(null, "Progress Summary", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        JLabel lblDownloadProgress = new JLabel("Download Progress:");
        lblDownloadProgress.setBounds(10, 25, 205, 14);
        panel.add(lblDownloadProgress);
        downloadProgressBar = new JProgressBar();
        downloadProgressBar.setBounds(225, 25, 129, 14);
        panel.add(downloadProgressBar);

        JLabel lblProcessProgress = new JLabel("Process Progress: ");
        lblProcessProgress.setBounds(10, 50, 205, 14);
        panel.add(lblProcessProgress);
        processProgressBar = new JProgressBar();
        processProgressBar.setBounds(225, 50, 129, 14);
        panel.add(processProgressBar);

        JLabel lblIndiciesProgress = new JLabel("Indicies Progress:");
        lblIndiciesProgress.setBounds(10, 75, 205, 14);
        panel.add(lblIndiciesProgress);
        indiciesProgressBar = new JProgressBar();
        indiciesProgressBar.setBounds(225, 75, 129, 14);
        panel.add(indiciesProgressBar);

        JLabel lblSummaryProgress = new JLabel("Summary Progress:");
        lblSummaryProgress.setBounds(10, 100, 205, 14);
        panel.add(lblSummaryProgress);
        summaryProgressBar = new JProgressBar();
        summaryProgressBar.setBounds(225, 100, 129, 14);
        panel.add(summaryProgressBar);
    }

    private void CreateLogView() {
        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(null, "Log", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_1.setBounds(10, 148, 364, 302);
        frame.getContentPane().add(panel_1);
        panel_1.setLayout(null);

        itemLog = new DefaultListModel<String>();

        logList = new JList<String>(itemLog);
        logList.setBounds(10, 22, 344, 269);
        panel_1.add(logList);
    }

    class GUIUpdateHandlerImplementation implements GUIUpdateHandler{

        private String projectName;

        public GUIUpdateHandlerImplementation(String projectName){
            this.projectName = projectName;
        }

        @Override
        public void run() {
            SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);

            downloadProgressBar.setValue(GetAverage1(status.GetDownloadProgress()));
            processProgressBar.setValue(GetAverage1(status.GetProcessorProgress()));
            indiciesProgressBar.setValue(GetAverage1(status.GetIndicesProgress()));
            summaryProgressBar.setValue(GetAverage2(status.GetSummaryProgress()));

            for(String log : status.GetAndClearLog())
            {
                itemLog.addElement(log);
            }
        }

        private int GetAverage1(TreeMap<String, Double> TotalProgress){

            int total = 0;

            Iterator<String> pluginIt = TotalProgress.keySet().iterator();
            while(pluginIt.hasNext())
            {
                total += TotalProgress.get(pluginIt.next());
            }

            return total / TotalProgress.size();
        }

        private int GetAverage2(TreeMap<String, TreeMap<Integer, Double>> TotalProgress){

            int total = 0;
            int count = 0;

            Iterator<String> pluginIt = TotalProgress.keySet().iterator();
            Iterator<Integer> summaryIt;
            TreeMap<Integer, Double> pluginTemp;
            while(pluginIt.hasNext())
            {
                pluginTemp = TotalProgress.get(pluginIt.next());
                summaryIt = pluginTemp.keySet().iterator();
                while(summaryIt.hasNext())
                {
                    total += pluginTemp.get(summaryIt.next());
                    count++;
                }
            }

            return total / count;
        }

    }
}