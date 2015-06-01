package version2.prototype.EastWebUI.ProgressUI;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JList;

import java.util.Timer;
import java.util.TimerTask;

import version2.prototype.Scheduler.Scheduler;

public class ProjectProgress {

    private JFrame frame;
    private JList<String> logList;
    private JProgressBar downloadProgressBar;
    private JProgressBar processProgressBar;
    private JProgressBar indiciesProgressBar;
    private JProgressBar summaryProgressBar;
    private Scheduler scheduler;

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
                    window.frame.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ProjectProgress(Scheduler scheduler) {
        initialize();
        this.scheduler = scheduler;

        // And From your main() method or any other method
        Timer timer = new Timer();
        timer.schedule(new UpdateUI(), 0, 1000);
        frame.setVisible(true);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 400, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().setLayout(null);

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

    class UpdateUI extends TimerTask {
        @Override
        public void run() {
            downloadProgressBar.setValue(scheduler.DownloadProgress);
            processProgressBar.setValue(scheduler.ProcessProgress);
            indiciesProgressBar.setValue(scheduler.IndiciesProgress);
            summaryProgressBar.setValue(scheduler.SummaryProgress);

            for(String log : scheduler.Log)
            {
                itemLog.addElement(log);
            }
            scheduler.Log.clear();
        }
    }
}
