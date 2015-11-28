package version2.prototype.EastWebUI_V2.ProgressUI_v2;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.JList;

import java.util.Iterator;
import java.util.TreeMap;

import version2.prototype.Config;
import version2.prototype.EASTWebManager;
import version2.prototype.ErrorLog;
import version2.prototype.GUIUpdateHandler;
import version2.prototype.Scheduler.ProcessName;
import version2.prototype.Scheduler.SchedulerStatus;

public class ProjectProgress {
    private JFrame frame;
    private JList<String> logList;
    private JProgressBar downloadProgressBar;
    private JProgressBar processProgressBar;
    private JProgressBar indiciesProgressBar;
    private JProgressBar summaryProgressBar;

    private DefaultListModel<String> itemLog;
    private GUIUpdateHandlerImplementation updateHandler;
    private String projectName;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new ProjectProgress(null);
                } catch (Exception e) {
                    ErrorLog.add(Config.getInstance(), "ProjectProgress.main problem with running a ProjectProgress window.", e);
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ProjectProgress(String projectName) throws Exception{
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

        frame = new JFrame();
        frame.setBounds(100, 100, 400, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setVisible(true);

        initialize(projectName);

        updateHandler = new GUIUpdateHandlerImplementation(projectName);
        updateHandler.run();
        EASTWebManager.RegisterGUIUpdateHandler(updateHandler);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize(String projectName) {
        this.projectName = projectName;

        CreateProgressView();
        CreateLogView();
    }

    private void CreateProgressView() {
        JPanel panel = new JPanel();
        panel.setBounds(10, 11, 364, 125);
        panel.setBorder(new TitledBorder(null, String.format("%1$s Progress Summary", projectName), TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Log", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setBounds(10, 148, 364, 302);
        panel.setLayout(null);
        frame.getContentPane().add(panel);

        itemLog = new DefaultListModel<String>();
        logList = new JList<String>(itemLog);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(logList);
        scrollPane.setBounds(10, 22, 344, 269);
        panel.add(scrollPane);
    }

    class GUIUpdateHandlerImplementation implements GUIUpdateHandler{
        private String projectName;

        public GUIUpdateHandlerImplementation(String projectName){
            this.projectName = projectName;
        }

        @Override
        public void run() {
            synchronized(frame){
                if(frame != null){
                    SchedulerStatus status = EASTWebManager.GetSchedulerStatus(projectName);

                    if(status == null) {
                        downloadProgressBar.setValue(0);
                        processProgressBar.setValue(0);
                        indiciesProgressBar.setValue(0);
                        summaryProgressBar.setValue(0);
                        itemLog.clear();
                    } else {
                        downloadProgressBar.setValue(GetAverageDownload(status.GetDownloadProgressesByData()).intValue());
                        processProgressBar.setValue(GetAverage(status.GetProcessorProgresses()).intValue());
                        indiciesProgressBar.setValue(GetAverage(status.GetIndicesProgresses()).intValue());
                        summaryProgressBar.setValue(GetAverageSummary(status.GetSummaryProgresses()).intValue());
                        itemLog.clear();

                        for(String log : status.ReadAllRemainingLogEntries()){
                            itemLog.addElement(log);
                        }

                        StringBuilder processWorkerInfo = new StringBuilder();
                        Iterator<ProcessName> it = status.GetWorkersInQueuePerProcess().keySet().iterator();
                        ProcessName tempKey;

                        processWorkerInfo.append("Workers Queued For Processes:\n");
                        while(it.hasNext()){
                            tempKey = it.next();
                            processWorkerInfo.append("\t" + tempKey.toString() + ":\t" + status.GetWorkersInQueuePerProcess().get(tempKey) + "\n");
                        }

                        it = status.GetActiveWorkersPerProcess().keySet().iterator();
                        processWorkerInfo.append("Active Workers For Processes:\n");
                        while(it.hasNext()){
                            tempKey = it.next();
                            processWorkerInfo.append("\t" + tempKey.toString() + ":\t" + status.GetActiveWorkersPerProcess().get(tempKey) + "\n");
                        }

                        System.out.print(processWorkerInfo);
                    }
                } else {
                    EASTWebManager.RemoveGUIUpdateHandler(updateHandler);
                }
            }
        }

        private Double GetAverage(TreeMap<String, Double> TotalProgress){
            double total = 0;
            Iterator<String> pluginIt = TotalProgress.keySet().iterator();

            while(pluginIt.hasNext()){
                total += TotalProgress.get(pluginIt.next());
            }

            return total / TotalProgress.size();
        }

        private Double GetAverageSummary(TreeMap<String, TreeMap<Integer, Double>> TotalProgress){
            int count = 0;
            double total = 0;
            Iterator<String> pluginIt = TotalProgress.keySet().iterator();
            Iterator<Integer> summaryIt;
            TreeMap<Integer, Double> pluginTemp;

            while(pluginIt.hasNext()){
                pluginTemp = TotalProgress.get(pluginIt.next());
                summaryIt = pluginTemp.keySet().iterator();

                while(summaryIt.hasNext()){
                    total += pluginTemp.get(summaryIt.next());
                    count++;
                }
            }
            return total / count;
        }

        private Double GetAverageDownload(TreeMap<String, TreeMap<String, Double>> TotalProgress){
            int count = 0;
            double total = 0;
            Iterator<String> pluginIt = TotalProgress.keySet().iterator();
            TreeMap<String, Double> dataMap;

            while(pluginIt.hasNext()){
                dataMap = TotalProgress.get(pluginIt.next());

                for(Double value : dataMap.values()){
                    total += value;
                    count++;
                }
            }
            return total / count;
        }
    }
}