package tv.athena.live.barrage.utils.pack;


/**
 *
 * @author Vincent 2013-4-16
 */
public class Uint16
{

	private long v;

	public Uint16(int i)
	{
		if (i < 0)
		{
			String s = Integer.toBinaryString(i);
			v = Long.valueOf(s, 2);
		}
		else
		{
			v = i;
		}
	}

	public Uint16(long l)
	{
		v = l;
	}

	public Uint16(String l)
	{
		v = Long.valueOf(l);
	}

	public static Uint16 toUInt(int i)
	{
		return new Uint16(i);
	}

	public int toInt()
	{
		return (int) v;
	}

	public long toLong()
	{
		return v;
	}
	
	public short toShort()
	{
		return (short)v;
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
		Uint16 other = (Uint16) obj;
		if (v != other.v)
			return false;
		return true;
	}
}
