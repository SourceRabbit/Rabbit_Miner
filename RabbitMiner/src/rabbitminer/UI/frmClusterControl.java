/**
 * MIT License
 *
 * Copyright (c) 2022 Nikolaos Siatras
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rabbitminer.UI;

import Extasys.Network.TCP.Server.Listener.TCPClientConnection;
import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import rabbitminer.Cluster.RabbitCluster;
import rabbitminer.Cluster.Server.ClusterServerSettings;
import rabbitminer.Cluster.Server.NodeTCPConnectionVariables;
import rabbitminer.Cluster.StratumClient.StratumPoolSettings;
import rabbitminer.Core.Settings.SettingsManager;
import rabbitminer.Crypto.CryptoAlgorithms.CryptoAlgorithmsManager;
import rabbitminer.Stratum.StratumJob_RandomX;
import rabbitminer.Stratum.StratumJob_SCrypt;
import rabbitminer.UI.Tools.UIHelper;

/**
 *
 * @author Nikos Siatras
 */
public class frmClusterControl extends javax.swing.JFrame
{

    public static frmClusterControl ACTIVE_INSTANCE;
    private final RabbitCluster fRabbitCluster;
    private final Thread fUpdateUIThread;
    private boolean fKeepUpdatingUI = true;
    private final DecimalFormat fDoubleFormatter = new DecimalFormat("#0.00");
    private final DecimalFormat fNonceFormater = new DecimalFormat("###,###.###");

    private JFileChooser fMainFileChooser;

    private final Object fNodeConnectDisconnectLock = new Object();

    public frmClusterControl(StratumPoolSettings stratumPoolSettings, ClusterServerSettings clusterServerSettings) throws Exception
    {
        super();
        initComponents();
        UIHelper.MoveFormInCenterOfScreen(this);

        ACTIVE_INSTANCE = this;

        fRabbitCluster = new RabbitCluster(stratumPoolSettings, clusterServerSettings);
        fRabbitCluster.StartCluster();

        this.setTitle("Rabbit Miner " + SettingsManager.getAppVersion() + " - Cluster Control");

        fUpdateUIThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (fKeepUpdatingUI)
                {
                    UpdateUI();

                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (Exception ex)
                    {

                    }
                }
            }
        });
        fUpdateUIThread.start();
    }

    private void UpdateUI()
    {
        try
        {
            jLabelMingPoolURL.setText(fRabbitCluster.getStratumPoolSettings().getIPAddress().toString() + ":" + String.valueOf(fRabbitCluster.getStratumPoolSettings().getPort()));
            jLabelAlgorithm.setText(CryptoAlgorithmsManager.getCryptoAlgorithmNameFromEnum(fRabbitCluster.getStratumPoolSettings().getCryptoAlgorithm()));
            jLabelStatus.setText(fRabbitCluster.getStratumClient().getStatus());
            jLabelJobs.setText(String.valueOf(fRabbitCluster.fJobsReceived) + "/" + String.valueOf(fRabbitCluster.fJobsSubmitted) + " Queue(" + String.valueOf(fRabbitCluster.getJobsInQueue()) + ")");

            jLabelClusterIP.setText(fRabbitCluster.getClusterServerSettings().getIPAddress().toString());
            jLabelClusterPort.setText(String.valueOf(fRabbitCluster.getClusterServerSettings().getPort()));
            jLabelActiveConnections.setText(String.valueOf(fRabbitCluster.getClusterServer().getConnectedClients().size()));

            jLabelNOnceIndex.setText(fNonceFormater.format(fRabbitCluster.getNOnceRangeIndex()) + "/" + fNonceFormater.format(Integer.MAX_VALUE));
            jLabelNOnceLeft.setText(fNonceFormater.format(Integer.MAX_VALUE - Math.abs(fRabbitCluster.getNOnceRangeIndex())));

            if (fRabbitCluster.getCurrentStratumJob() != null)
            {
                jTableCurrentJob.setVisible(true);

                // Δείξε τα Data του Job Μέσα στο jTableCurrentJob
                LinkedHashMap jobParams = new LinkedHashMap();
                switch (fRabbitCluster.getCurrentStratumJob().getCryptoAlgorithm())
                {
                    case SCrypt:
                        StratumJob_SCrypt job = (StratumJob_SCrypt) fRabbitCluster.getCurrentStratumJob();
                        jobParams.put("Job ID", job.getJobID());
                        jobParams.put("Difficulty", job.getDifficulty());
                        jobParams.put("Extranonce1", job.getExtranonce1());
                        jobParams.put("getExtraNonce2Size", job.getExtraNonce2Size());
                        break;

                    case RandomX:
                        StratumJob_RandomX jobX = (StratumJob_RandomX) fRabbitCluster.getCurrentStratumJob();
                        jobParams.put("Job ID", jobX.getJobID());
                        jobParams.put("Difficulty", jobX.getDifficulty());
                        jobParams.put("Target", jobX.getTarget());
                        jobParams.put("Height", jobX.getHeight());
                        jobParams.put("Blob", jobX.getBlob());
                        jobParams.put("SeedHash", jobX.getSeedHash());
                        break;
                }

                DefaultTableModel model = (DefaultTableModel) jTableCurrentJob.getModel();
                for (Object key : jobParams.keySet())
                {
                    String keyStr = key.toString();
                    String valueStr = String.valueOf(jobParams.get(key));

                    boolean found = false;
                    for (int i = 0; i < jTableCurrentJob.getRowCount(); i++)
                    {
                        if (jTableCurrentJob.getValueAt(i, 0).equals(keyStr))
                        {
                            jTableCurrentJob.setValueAt(valueStr, i, 1);
                            found = true;
                            break;
                        }
                    }

                    if (!found)
                    {
                        Object[] values = new Object[2];
                        values[0] = keyStr;
                        values[1] = valueStr;
                        model.addRow(values);
                    }
                }

            }
            else
            {
                // Ακομα δεν έχει ερθει Job
                jTableCurrentJob.setVisible(false);

            }

            // Κάνε Update τα συνδεδεμένα Nodes
            synchronized (fNodeConnectDisconnectLock)
            {
                double totalHashesPerSec = 0;

                DefaultTableModel model = (DefaultTableModel) jTableConnectedMiners.getModel();
                for (int row = 0; row < model.getRowCount(); row++)
                {
                    String nodeIp = model.getValueAt(row, 0).toString();
                    TCPClientConnection connection = fRabbitCluster.getClusterServer().getConnectedClients().get(nodeIp);
                    if (connection.getTag() != null)
                    {

                        String workRange = ((NodeTCPConnectionVariables) connection.getTag()).getWorkRange();
                        model.setValueAt(workRange, row, 1);

                        final double nodeHashesPerSecond = ((NodeTCPConnectionVariables) connection.getTag()).getHashesPerSecond();
                        String hashesPerSecond = fDoubleFormatter.format(nodeHashesPerSecond);
                        model.setValueAt(hashesPerSecond, row, 2);

                        totalHashesPerSec += nodeHashesPerSecond;
                    }
                }

                jLabelTotalHashesPerSec.setText(fNonceFormater.format(totalHashesPerSec));
            }
        }
        catch (Exception ex)
        {
            System.err.println("frmClusterControl.UpdateUI Error: " + ex.getMessage());
        }
    }

    public void NodeConnected(TCPClientConnection connection)
    {
        Thread th = new Thread(() ->
        {
            synchronized (fNodeConnectDisconnectLock)
            {
                DefaultTableModel model = (DefaultTableModel) jTableConnectedMiners.getModel();
                String[] data = new String[2];
                data[0] = connection.getIPAddress();
                data[1] = "Idle...";
                model.addRow(data);
            }
        });
        th.start();
    }

    public void NodeDisconnected(TCPClientConnection connection)
    {
        Thread th = new Thread(() ->
        {
            synchronized (fNodeConnectDisconnectLock)
            {
                DefaultTableModel model = (DefaultTableModel) jTableConnectedMiners.getModel();
                int rowToRemove = -1;
                for (int row = 0; row < model.getRowCount(); row++)
                {
                    if (model.getValueAt(row, 0).toString().equals(connection.getIPAddress()))
                    {
                        rowToRemove = row;
                        break;
                    }
                }

                if (rowToRemove > -1)
                {
                    model.removeRow(rowToRemove);
                }
            }
        });
        th.start();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelMingPoolURL = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelJobs = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelAlgorithm = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabelStatus = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableCurrentJob = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableConnectedMiners = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabelClusterIP = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabelClusterPort = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabelActiveConnections = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabelNOnceIndex = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabelNOnceLeft = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabelTotalHashesPerSec = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Rabbit Miner Cluster Control");
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mining Pool", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 18), new java.awt.Color(65, 130, 195))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel1.setText("URL:");

        jLabelMingPoolURL.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelMingPoolURL.setText("MINING POOL URL");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel2.setText("Jobs:");

        jLabelJobs.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelJobs.setText("0/0");

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel6.setText("Algorith:");

        jLabelAlgorithm.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelAlgorithm.setText("No Job yet...");

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel7.setText("Status:");

        jLabelStatus.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelStatus.setText("Disconnected");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelAlgorithm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelJobs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelMingPoolURL, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelMingPoolURL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelJobs))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabelAlgorithm))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabelStatus))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Job", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 18), new java.awt.Color(65, 130, 195))); // NOI18N

        jTableCurrentJob.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "ID", "Value"
            }
        ));
        jScrollPane1.setViewportView(jTableCurrentJob);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Connected Nodes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 18), new java.awt.Color(65, 130, 195))); // NOI18N

        jTableConnectedMiners.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jTableConnectedMiners.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "IP", "Nonce Range Scanning", "Hashes/sec"
            }
        )
        {
            boolean[] canEdit = new boolean []
            {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTableConnectedMiners);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cluster", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 1, 18), new java.awt.Color(65, 130, 195))); // NOI18N

        jLabelClusterIP.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelClusterIP.setText("127.0.0.1");

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel3.setText("IP:");

        jLabel10.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel10.setText("Port:");

        jLabelClusterPort.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelClusterPort.setText("8551");

        jLabel11.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel11.setText("Active Connections:");

        jLabelActiveConnections.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelActiveConnections.setText("0");

        jLabel12.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel12.setText("NOnce Scanned:");

        jLabelNOnceIndex.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelNOnceIndex.setText("0");

        jLabel13.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel13.setText("NOnce Left:");

        jLabelNOnceLeft.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelNOnceLeft.setText("0");

        jLabel14.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel14.setText("Total Hashes/sec:");

        jLabelTotalHashesPerSec.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelTotalHashesPerSec.setText("0");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabelClusterPort, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelClusterIP, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelNOnceIndex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelActiveConnections, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelNOnceLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelTotalHashesPerSec, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabelClusterIP))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabelClusterPort))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabelActiveConnections))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabelNOnceIndex))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabelNOnceLeft))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabelTotalHashesPerSec))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jMenu1.setText("File");

        jMenuItem1.setText("Save Cluster Settings");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem1ActionPerformed
        String lastPathUsedToSaveProject = SettingsManager.getLastFolderPathUsedToSaveSettings();

        fMainFileChooser = new JFileChooser(new File(lastPathUsedToSaveProject));
        fMainFileChooser.setDialogTitle("Specify where to save Cluster Settings");
        fMainFileChooser.removeChoosableFileFilter(fMainFileChooser.getAcceptAllFileFilter());
        FileNameExtensionFilter filter = new FileNameExtensionFilter("RabbitMiner Cluster (." + SettingsManager.fClusterSettingsFileExtension + ")", SettingsManager.fClusterSettingsFileExtension);
        fMainFileChooser.setFileFilter(filter);

        int returnVal = fMainFileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            String savePath = fMainFileChooser.getSelectedFile().getAbsolutePath();
            File fileToSave = new File(savePath + "." + SettingsManager.fClusterSettingsFileExtension);

            SettingsManager.setLastFolderPathUsedToSaveSettings(fileToSave.getParent());
            try
            {
                fRabbitCluster.SaveToFile(fileToSave);
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(null, "Unable to save file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_jMenuItem1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelActiveConnections;
    private javax.swing.JLabel jLabelAlgorithm;
    private javax.swing.JLabel jLabelClusterIP;
    private javax.swing.JLabel jLabelClusterPort;
    private javax.swing.JLabel jLabelJobs;
    private javax.swing.JLabel jLabelMingPoolURL;
    private javax.swing.JLabel jLabelNOnceIndex;
    private javax.swing.JLabel jLabelNOnceLeft;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelTotalHashesPerSec;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTableConnectedMiners;
    private javax.swing.JTable jTableCurrentJob;
    // End of variables declaration//GEN-END:variables
}
