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

import java.net.InetAddress;
import java.text.DecimalFormat;
import rabbitminer.ClusterNode.ClusterNode;
import rabbitminer.Core.Settings.SettingsManager;
import rabbitminer.Crypto.CryptoAlgorithms.CryptoAlgorithmsManager;
import rabbitminer.UI.Tools.UIHelper;

/**
 *
 * @author Nikos Siatras
 */
public class frmClusterNodeControl extends javax.swing.JFrame
{

    private int fUIUpdatesCount = 0;
    private ClusterNode fClusterNode;
    private final Thread fUpdateUIThread;
    private DecimalFormat fDoubleFormatter = new DecimalFormat("#0.00");
    private DecimalFormat fNonceFormater = new DecimalFormat("###,###.###");

    public frmClusterNodeControl(InetAddress clusterIP, int clusterPort, String clusterPassword)
    {
        initComponents();
        UIHelper.MoveFormInCenterOfScreen(this);

        this.setTitle("Rabbit Miner " + SettingsManager.getAppVersion() + " - Cluster Node");

        fClusterNode = new ClusterNode(clusterIP, clusterPort, clusterPassword);

        try
        {
            fClusterNode.StartNode();
        }
        catch (Exception ex)
        {

        }

        fUpdateUIThread = new Thread(() ->
        {
            while (true)
            {
                UpdateUI();

                try
                {
                    Thread.sleep(500);
                }
                catch (Exception ex)
                {

                }
            }
        });
        fUpdateUIThread.start();
    }

    private void UpdateUI()
    {
        jLabelConnectionStatus.setText(fClusterNode.getStatus());

        if (fClusterNode.getCurrentJob() != null)
        {
            jLabelRangeWorking.setText(fClusterNode.getCurrentJob().getNOnceRange());
            jLabelAlgorithm.setText(CryptoAlgorithmsManager.getCryptoAlgorithmNameFromEnum(fClusterNode.getCurrentJob().getCryptoAlgorithm()));
        }
        else
        {
            jLabelRangeWorking.setText("Waiting for job...");
            jLabelAlgorithm.setText("");
        }

        fUIUpdatesCount += 1;
        if (fUIUpdatesCount > 4)
        {
            jLabelHashesPerSec.setText(fDoubleFormatter.format(fClusterNode.getHashesPerSecond()));
            fUIUpdatesCount = 0;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelConnectionStatus = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabelAlgorithm = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelRangeWorking = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabelHashesPerSec = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Node Status", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18), new java.awt.Color(65, 130, 195))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel1.setText("Current Status:");

        jLabelConnectionStatus.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelConnectionStatus.setText("No");

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel2.setText("Algorith:");

        jLabelAlgorithm.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelAlgorithm.setText("Scrypt");

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel6.setText("NOnce Range:");

        jLabelRangeWorking.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelRangeWorking.setText("0-0");

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabel7.setText("Hashes / sec:");

        jLabelHashesPerSec.setFont(new java.awt.Font("Verdana", 0, 11)); // NOI18N
        jLabelHashesPerSec.setText("0-0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabelHashesPerSec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelRangeWorking, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelAlgorithm, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                    .addComponent(jLabelConnectionStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelConnectionStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabelAlgorithm))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabelRangeWorking))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabelHashesPerSec))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelAlgorithm;
    private javax.swing.JLabel jLabelConnectionStatus;
    private javax.swing.JLabel jLabelHashesPerSec;
    private javax.swing.JLabel jLabelRangeWorking;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
