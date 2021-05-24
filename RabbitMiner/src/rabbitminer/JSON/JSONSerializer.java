package rabbitminer.JSON;

import java.util.HashMap;
import net.sf.sojo.interchange.Serializer;
import net.sf.sojo.interchange.json.JsonSerializer;

/**
 *
 * @author Nikos Siatras
 */
public class JSONSerializer
{

    private static Serializer fSerializer;

    static
    {
        fSerializer = new JsonSerializer();
    }

    public static String SerializeObject(Object obj)
    {
        return (String) fSerializer.serialize(obj);
    }

    public static Object DeserializeObject(String str)
    {
        return fSerializer.deserialize(str);
    }

    public static HashMap DeserializeObjectToHash(String str)
    {
        return (HashMap) fSerializer.deserialize(str, HashMap.class);
    }
}
