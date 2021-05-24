package rabbitminer.Cluster.StratumClient;

import java.util.ArrayList;
import java.util.HashMap;
import rabbitminer.JSON.JSONSerializer;

/**
 *
 * @author Nikos Siatras
 */
public class StratumToken
{

    private final String fStringData;
    private final HashMap fHash;

    /**
     * Create a new Stratum token
     *
     * @param str
     */
    public StratumToken(String str)
    {
        fStringData = str;
        fHash = JSONSerializer.DeserializeObjectToHash(str);
    }

    public Long getID()
    {
        return (fHash.containsKey("id") && fHash.get("id") != null) ? (Long) fHash.get("id") : null;
    }

    public Object getError()
    {
        return fHash.containsKey("error") ? fHash.get("error") : null;
    }

    public Object getResult()
    {
        return fHash.containsKey("result") ? fHash.get("result") : null;
    }

    public String getMethod()
    {
        return fHash.containsKey("method") ? (String) fHash.get("method") : null;
    }

    public ArrayList getParams()
    {
        return (fHash.containsKey("params") && fHash.get("params") != null) ? (ArrayList) fHash.get("params") : null;
    }

    public String getRawData()
    {
        return fStringData;
    }

}
