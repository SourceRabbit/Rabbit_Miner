package rabbitminer.Cluster;

import Extasys.Network.TCP.Server.Listener.TCPClientConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import rabbitminer.Cluster.Server.ClusterServer;
import rabbitminer.Cluster.Server.ClusterServerSettings;
import rabbitminer.Cluster.Server.NodeTCPConnectionVariables;
import rabbitminer.Cluster.StratumClient.StratumClient;
import rabbitminer.Cluster.StratumClient.StratumPoolSettings;
import rabbitminer.Stratum.StratumJob;

/**
 *
 * @author Nikos Siatras
 *
 * Περίληψη: Ο Rabbit Cluster έχει έναν Cluster Server, στον οποίο συνδέονται τα
 * Nodes, ένα StratumClient με το οποίο συνδέεται στο Stratum Pool και ένα
 * StratumJob το οποίο είναι το Job που μας έχει δώσει το Pool
 *
 * Ο Rabbit Cluster δινει τα Jobs στα Nodes σπάζωντας σε κομμάτια το Nonce
 * ανάλογα με την τιμή του fNOnceRangeStepPerNodeThread, η όνομα διαμορφώνεται
 * απο το difficulty του Job που έχει έρθει απο το Pool.
 */
public class RabbitCluster
{

    private final Queue<StratumJob> fLastJobs = new LinkedList<>();

    // Statistics
    public int fJobsReceived = 0;
    public int fJobsSubmitted = 0;

    private final ClusterServer fClusterServer;
    private final ClusterServerSettings fClusterServerSettings;

    // Stratum Client - Για το Mining Pool
    private final StratumClient fStratumClient;
    private final StratumPoolSettings fStratumPoolSettings;

    // Τρέχουσα εργασία απο το Mining Pool
    private StratumJob fCurrentStratumJob;
    private final Object fJobLock = new Object();

    private int fNOnceRangeStepPerNodeThread = 6000;
    private int fNOnceRangeIndex = -1;

    public RabbitCluster(StratumPoolSettings stratumPoolSettings, ClusterServerSettings clusterServerSettings)
    {
        fClusterServerSettings = clusterServerSettings;
        fClusterServer = new ClusterServer(this, fClusterServerSettings);

        fStratumPoolSettings = stratumPoolSettings;
        fStratumClient = new StratumClient(this, fStratumPoolSettings);
    }

    /**
     * Κάνει set το τρέχων Job που έχει έρθει από το Pool
     *
     * @param job
     * @param cleanJobs
     */
    public void setCurrentStratumJob(StratumJob job, boolean cleanJobs)
    {
        synchronized (fJobLock)
        {
            // Αν έχει ερθει Clean Job καθάρισε το Queue
            if (job == null || cleanJobs)
            {
                if (cleanJobs)
                {
                    fLastJobs.clear();
                }

                fCurrentStratumJob = null;

                // Πές στον Cluster Server να ζήτήσει απο τους clients-nodes
                // να κάνουν Clean Jobs
                fClusterServer.InformClientsToCleanJobs();

                // Ζητα απο τον Cluster Server να κάνει 0 τα  ranges που δουλέυει ο κάθε client
                // για να φανεί στο UI ότι τα Nodes είναι ανενεργά
                fClusterServer.ClearRangesFromClients();
            }

            if (job != null)
            {
                fJobsReceived += 1;

                switch (job.getCryptoAlgorithm())
                {
                    case RandomX:
                        fNOnceRangeStepPerNodeThread = 250;
                        break;

                    case SCrypt:
                        fNOnceRangeStepPerNodeThread = 20000;
                        break;

                    default:
                        System.err.println("RabbitCluster.setCurrentStratumJob: Δέν έχει οριστεί κάτι για το συγκεκριμένο ECryptoAlgorithm");
                        break;
                }

                fLastJobs.add(job);
            }

            /*// Κάνε set το Job μόνο αν το Clean Jobs ειναι True
            // ή άν αυτη τη στιγμή ΔΕΝ ΕΧΟΥΜΕ ΔΟΥΛΕΙΑ και η δουλειά που έχει έρθει δέν ειναι Null
            if (cleanJobs || fCurrentStratumJob == null)
            {
                fLastJobs.clear();
                fCurrentStratumJob = job;
                fNOnceRangeIndex = -1;

                // Πές στον Cluster Server να ζήτήσει απο τους clients-nodes
                // να κάνουν Clean Jobs
                fClusterServer.InformClientsToCleanJobs();

                // Ζητα απο τον Cluster Server να κάνει 0 τα  ranges που δουλέυει ο κάθε client
                // για να φανεί στο UI ότι τα Nodes είναι ανενεργά
                fClusterServer.ClearRangesFromClients();

                if (job != null)
                {
                    fJobsReceived += 1;

                    switch (job.getCryptoAlgorithm())
                    {
                        case RandomX:
                            fNOnceRangeStepPerNodeThread = 250;
                            break;

                        case SCrypt:
                            fNOnceRangeStepPerNodeThread = 8000;
                            break;

                        default:
                            System.err.println("RabbitCluster.setCurrentStratumJob: Δέν έχει οριστεί κάτι για το συγκεκριμένο ECryptoAlgorithm");
                            break;
                    }
                }
            }
            else
            {
                fJobsReceived += 1;
                fLastJobs.add(job);
            }*/
        }
    }

    /**
     * Δώσε ένα Job στο Node
     *
     * @param sender
     * @return
     */
    public StratumJob GiveNodeAJobToDo(TCPClientConnection sender)
    {
        synchronized (fJobLock)
        {
            // Αν δεν υπάρχει Job δές αν υπάρχει κάποιο στο Queue
            if (fCurrentStratumJob == null && fLastJobs.size() > 0)
            {
                // Πές στον Cluster Server να ζήτήσει απο τους clients-nodes
                // να κάνουν Clean Jobs
                fClusterServer.InformClientsToCleanJobs();

                // Ζητα απο τον Cluster Server να κάνει 0 τα  ranges που δουλέυει ο κάθε client
                // για να φανεί στο UI ότι τα Nodes είναι ανενεργά
                fClusterServer.ClearRangesFromClients();

                fNOnceRangeIndex = -1;
                fCurrentStratumJob = fLastJobs.poll();
            }

            if (fCurrentStratumJob != null)
            {
                try
                {
                    // Step 1. Βρές πόσα Threads έχει το Node που ζητέι δουλειά
                    NodeTCPConnectionVariables nodeVar = (NodeTCPConnectionVariables) sender.getTag();
                    int nodeThreadsCount = nodeVar.getThreadsCount();

                    if (fNOnceRangeIndex < Integer.MAX_VALUE)
                    {
                        // Step 2. Για κάθε thread του Node, του δίνουμε να σκανάρει
                        // ευρος ίσο με fNOnceRangeStepPerNodeThread * nodeThreadsCount 
                        // ΣΗΜΕΙΩΣΗ: To Μέγιστο range του Nonce είναι απο 0 εώς Integer.MAX_VALUE,
                        // εμείς το σπάμε σε κομμάτα των fNOnceRangeStepPerNodeThread
                        final int from = fNOnceRangeIndex + 1;
                        final int to = (from + (fNOnceRangeStepPerNodeThread * nodeThreadsCount)) - 1;
                        fCurrentStratumJob.setNOnceRange(from, to);

                        // Κάνε σετ το Range που δουλέυει το Node στο tag (NodeTCPConnectionVariables)
                        ((NodeTCPConnectionVariables) sender.getTag()).setWorkRange(from, to);

                        fNOnceRangeIndex = to;

                        return fCurrentStratumJob;
                    }
                }
                catch (Exception ex)
                {
                    // Αυτο το exception δέν έχει "σκασει" μέχρι τώρα.
                    // Υπάρχει απλά γιατί έτσι πρέπει.
                    return null;
                }
            }

            return null;
        }
    }

    public void StartCluster() throws Exception
    {
        fClusterServer.Start();
        fStratumClient.Start();
    }

    public void StopCluster()
    {
        fClusterServer.Stop();
        fStratumClient.Stop();
    }

    /**
     * Επιστρέφει τον Cluster Server
     *
     * @return
     */
    public ClusterServer getClusterServer()
    {
        return fClusterServer;
    }

    /**
     * Επιστρέφει τα settings του Cluster Server
     *
     * @return
     */
    public ClusterServerSettings getClusterServerSettings()
    {
        return fClusterServerSettings;
    }

    /**
     * Επιστρέφει το Stratum Client
     *
     * @return
     */
    public StratumClient getStratumClient()
    {
        return fStratumClient;
    }

    /**
     * Επιστρέφει τα settings του StratumPool
     *
     * @return
     */
    public StratumPoolSettings getStratumPoolSettings()
    {
        return fStratumPoolSettings;
    }

    public StratumJob getCurrentStratumJob()
    {
        return fCurrentStratumJob;
    }

    /**
     * Επιστρέφει το μέγιστο Nonce Που έχει δωθεί στα nodes για σκανάρισμα
     *
     * @return
     */
    public int getNOnceRangeIndex()
    {
        return fNOnceRangeIndex;
    }

    /**
     * Κάνε serialize τα πράγματα που χρειάζονται και σώσε το στο σκληρό δίσκο
     *
     * @param fileToSave
     * @param pathToSave
     * @throws java.io.FileNotFoundException
     */
    public void SaveToFile(File fileToSave) throws FileNotFoundException, IOException
    {
        HashMap<String, Object> map = new HashMap<>();
        ObjectOutputStream out;

        map.put("ClusterServerSettings", fClusterServerSettings);
        map.put("StratumPoolSettings", fStratumPoolSettings);

        try (FileOutputStream fileOut = new FileOutputStream(fileToSave))
        {
            out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
        }
    }

    /**
     * Διάβασε τα Settings απο το δίσκο
     *
     * @param file
     * @param fileToLoad
     * @return
     */
    public static RabbitCluster LoadFromFile(File file) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        HashMap<String, Object> settingsData;

        try (FileInputStream fileIn = new FileInputStream(file))
        {
            try (ObjectInputStream in = new ObjectInputStream(fileIn))
            {
                settingsData = (HashMap<String, Object>) in.readObject();
            }

            ClusterServerSettings clusterSettings = (ClusterServerSettings) settingsData.get("ClusterServerSettings");
            StratumPoolSettings stratumPoolSettings = (StratumPoolSettings) settingsData.get("StratumPoolSettings");

            RabbitCluster rabbitCluster = new RabbitCluster(stratumPoolSettings, clusterSettings);
            return rabbitCluster;
        }

    }

    public int getJobsInQueue()
    {
        return fLastJobs.size();
    }
}
