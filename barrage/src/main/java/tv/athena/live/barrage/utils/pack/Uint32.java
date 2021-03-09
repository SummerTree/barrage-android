package tv.athena.live.barrage.utils.pack;

/**
 *
 * @author Vincent 2013-4-16
 */
public class Uint32
{

	private long v;

	public Uint32(int i)
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

	public Uint32(long l)
	{
		v = l;
	}

	public Uint32(String l)
	{
		v = Long.valueOf(l);
	}

	public static Uint32 toUInt(int i)
	{
		return new Uint32(i);
	}

	public int toInt()
	{
		return (int) v;
	}

	public long toLong()
	{
		return v;
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
		Uint32 other = (Uint32) obj;
		if (v != other.v)
			return false;
		return true;
	}
}
