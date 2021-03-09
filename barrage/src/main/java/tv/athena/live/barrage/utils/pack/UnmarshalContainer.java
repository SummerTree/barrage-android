package tv.athena.live.barrage.utils.pack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * unmarshal collection or map util
 * 
 * @author Vincent 2013-4-16
 */
public class UnmarshalContainer
{

	public static void unmarshalColUint8(Unpack up, Collection<Uint8> col)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			col.add(up.popUint8());
		}
	}
	public static void unmarshalColUint16(Unpack up, Collection<Uint16> col)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			col.add(up.popUint16());
		}
	}
	
	public static void unmarshalColUint32(Unpack up, Collection<Uint32> col)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			col.add(up.popUint32());
		}
	}
	
	public static void unmarshalColUint64(Unpack up, Collection<Uint64> col)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			col.add(up.popUint64());
		}
	}
	
	public static void unmarshalColString(Unpack up, Collection<String> col)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			col.add(up.popString());
		}
	}
	
	public static void unmarshalColBytes(Unpack up, Collection<byte[]> col)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			col.add(up.popBytes());
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void unmarshalColMarshallable(Unpack up, Collection col, Class<? extends Marshallable> type)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			try
			{
				Marshallable m = type.newInstance();
				m.unmarshall(up);
				col.add(m);
			} catch (IllegalAccessException e)
			{
				throw new UnpackException(e);
			} catch (InstantiationException e)
			{
				throw new UnpackException(e);
			}
		}
	}
	
	public static void unmarshalColMapStringString(Unpack up, Collection<Map<String, String>> col)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			Map<String, String> map = new HashMap<String, String>();
			unmarshalMapStringString(up, map);
			col.add(map);
		}
	}
	
	public static void unmarshalColMapUint32String(Unpack up, Collection<Map<Uint32, String>> col)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			Map<Uint32, String> map = new HashMap<Uint32, String>();
			unmarshalMapUint32String(up, map);
			col.add(map);
		}
	}
	
	public static void unmarshalMapUint8Uint32(Unpack up, Map<Uint8, Uint32> map)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			map.put(up.popUint8(), up.popUint32());
		}
	}
	
	public static void unmarshalMapUint16Uint32(Unpack up, Map<Uint16, Uint32> map)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			map.put(up.popUint16(), up.popUint32());
		}
	}
	
	public static void unmarshalMapUint32Uint32(Unpack up, Map<Uint32, Uint32> map)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			map.put(up.popUint32(), up.popUint32());
		}
	}
	
	public static void unmarshalMapUint32String(Unpack up, Map<Uint32, String> map)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			map.put(up.popUint32(), up.popString());
		}
	}

    public static void unmarshalMapUint16String(Unpack up, Map<Uint16, String> map)
    {
        Uint32 size = up.popUint32();
        for (int i = 0; i < size.toInt(); i++)
        {
            map.put(up.popUint16(), up.popString());
        }
    }
	
	public static void unmarshalMapUint32Bytes(Unpack up, Map<Uint32, byte[]> map)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			map.put(up.popUint32(), up.popBytes());
		}
	}
	
	public static void unmarshalMapStringString(Unpack up, Map<String, String> map)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			map.put(up.popString(), up.popString());
		}
	}
	
	public static void unmarshalMapBytesBytes(Unpack up, Map<byte[], byte[]> map)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			map.put(up.popBytes(), up.popBytes());
		}
	}
	
	public static void unmarshalMapStringUint32(Unpack up, Map<String, Uint32> map)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			map.put(up.popString(), up.popUint32());
		}
	}
	
	public static void unmarshalMapBytesUint32(Unpack up, Map<byte[], Uint32> map)
	{
		Uint32 size = up.popUint32();
		for (int i = 0; i < size.toInt(); i++)
		{
			map.put(up.popBytes(), up.popUint32());
		}
	}
}
