package tv.athena.live.barrage.utils.pack;

/**
 *
 * @author Vincent 2013-4-16
 */
public class Uint64
{

	private long v;

	public Uint64(int i)
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

	public Uint64(long l)
	{
		v = l;
	}

	public Uint64(String l)
	{
		v = Long.valueOf(l);
	}

	public static Uint64 toUInt(int i)
	{
		return new Uint64(i);
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
		Uint64 other = (Uint64) obj;
		if (v != other.v)
			return false;
		return true;
	}
}
