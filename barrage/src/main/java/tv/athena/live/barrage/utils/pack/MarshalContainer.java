package tv.athena.live.barrage.utils.pack;

import java.util.Collection;
import java.util.Map;


/**
 * marshal collection or map util
 * 
 * @author Vincent 2013-4-16
 */
public class MarshalContainer
{
	public static void marshalColUint8(Pack pack, Collection<Uint8> col)
	{
		pack.push(new Uint32(col.size()));
		for (Uint8 o : col)
		{
			pack.push(o);
		}
	}

	public static void marshalColUint16(Pack pack, Collection<Uint16> col)
	{
		pack.push(new Uint32(col.size()));
		for (Uint16 o : col)
		{
			pack.push(o);
		}
	}

	public static void marshalColUint32(Pack pack, Collection<Uint32> col)
	{
		pack.push(new Uint32(col.size()));
		for (Uint32 o : col)
		{
			pack.push(o);
		}
	}

	public static void marshalColUint64(Pack pack, Collection<Uint64> col)
	{
		pack.push(new Uint32(col.size()));
		for (Uint64 o : col)
		{
			pack.push(o);
		}
	}

	public static void marshalColString(Pack pack, Collection<String> col)
	{
		pack.push(new Uint32(col.size()));
		for (String o : col)
		{
			pack.push(o);
		}
	}

	public static void marshalColBytes(Pack pack, Collection<byte[]> col)
	{
		pack.push(new Uint32(col.size()));
		for (byte[] o : col)
		{
			pack.push(o);
		}
	}

	public static void marshalColMarshallable(Pack pack, Collection<? extends Marshallable> col)
	{
		pack.push(new Uint32(col.size()));
		for (Marshallable m : col)
		{
			m.marshall(pack);
		}
	}

	public static void marshalColMapStringString(Pack pack, Collection<Map<String, String>> col)
	{
		pack.push(new Uint32(col.size()));
		for (Map<String, String> m : col)
		{
			marshalMapStringString(pack, m);
		}
	}

	public static void marshalColMapUint32String(Pack pack, Collection<Map<Uint32, String>> col)
	{
		pack.push(new Uint32(col.size()));
		for (Map<Uint32, String> m : col)
		{
			marshalMapUint32String(pack, m);
		}
	}

	public static void marshalMapUint8Uint32(Pack pack, Map<Uint8, Uint32> map)
	{
		pack.push(new Uint32(map.size()));
		for (Uint8 key : map.keySet())
		{
			pack.push(key);
			pack.push(map.get(key));
		}
	}
	
	public static void marshalMapUint16Uint32(Pack pack, Map<Uint16, Uint32> map)
	{
		pack.push(new Uint32(map.size()));
		for (Uint16 key : map.keySet())
		{
			pack.push(key);
			pack.push(map.get(key));
		}
	}
	
	public static void marshalMapUint32Uint32(Pack pack, Map<Uint32, Uint32> map)
	{
		pack.push(new Uint32(map.size()));
		for (Uint32 key : map.keySet())
		{
			pack.push(key);
			pack.push(map.get(key));
		}
	}

    public static void marshalMapUint32String(Pack pack, Map<Uint32, String> map)
    {
        pack.push(new Uint32(map.size()));
        for (Uint32 key : map.keySet())
        {
            pack.push(key);
            pack.push(map.get(key));
        }
    }

    public static void marshalMapUint16String(Pack pack, Map<Uint16, String> map)
    {
        pack.push(new Uint32(map.size()));
        for (Uint16 key : map.keySet())
        {
            pack.push(key);
            pack.push(map.get(key));
        }
    }
	
	public static void marshalMapUint32Bytes(Pack pack, Map<Uint32, byte[]> map)
	{
		pack.push(new Uint32(map.size()));
		for (Uint32 key : map.keySet())
		{
			pack.push(key);
			pack.push(map.get(key));
		}
	}
	
	public static void marshalMapStringString(Pack pack, Map<String, String> map)
	{
		pack.push(new Uint32(map.size()));
		for (String key : map.keySet())
		{
			pack.push(key);
			pack.push(map.get(key));
		}
	}
	
	public static void marshalMapBytesBytes(Pack pack, Map<byte[], byte[]> map)
	{
		pack.push(new Uint32(map.size()));
		for (byte[] key : map.keySet())
		{
			pack.push(key);
			pack.push(map.get(key));
		}
	}
	
	public static void marshalMapStringUint32(Pack pack, Map<String, Uint32> map)
	{
		pack.push(new Uint32(map.size()));
		for (String key : map.keySet())
		{
			pack.push(key);
			pack.push(map.get(key));
		}
	}
	
	public static void marshalMapBytesUint32(Pack pack, Map<byte[], Uint32> map)
	{
		pack.push(new Uint32(map.size()));
		for (byte[] key : map.keySet())
		{
			pack.push(key);
			pack.push(map.get(key));
		}
	}

}
