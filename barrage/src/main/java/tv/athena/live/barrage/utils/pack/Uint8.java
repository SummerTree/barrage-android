package tv.athena.live.barrage.utils.pack;


/**
 *
 * @author Vincent 2013-4-16
 */
public class Uint8
{

	private long v;

	public Uint8(int i)
	{
		if (i < 0)
		{
            v = i & 0xFF;
		}
		else
		{
			v = i;
		}
	}

	public Uint8(String l)
	{
		v = Long.valueOf(l);
	}

	public static Uint8 toUInt(int i)
	{
		return new Uint8(i);
	}
	
	public int toInt()
	{
		return (int) v;
	}

	public byte toByte()
	{
		return (byte)v;
	}

	@Override
	public String toString()
	{
		return Long.toString(v);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (v ^ (v >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Uint8 other = (Uint8) obj;
		if (v != other.v)
			return false;
		return true;
	}
}
